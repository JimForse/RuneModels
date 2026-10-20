package rw.modden.client.render;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import rw.modden.profile.AnimationSets;
import rw.modden.profile.Profile;
import software.bernie.geckolib.animatable.GeoReplacedEntity;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class Animatable implements GeoReplacedEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final GeoModel model;

    public Animatable(GeoModel model) {
        this.model = model;
    }

    @Override
    public EntityType<?> getReplacingEntityType() {
        // The GeoReplacedEntity interface requires a type to be returned, but our replacement is based on a profile rather than a type.
        // Since GeckoLib does not call this method, the value is meaningless.
        return EntityType.PIG;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 5, state -> {
            Entity entity = state.getData(DataTickets.ENTITY);
            if (entity == null) return PlayState.STOP;
            int id = entity.getId();

            PlayRequest req = ClientProfiles.getPlay(id);
            if (req != null) {
                AnimationController<Animatable> c = state.getController();
                if (!req.started) {
                    req.started = true;
                    c.forceAnimationReset();
                } else if (++req.frames > 3 && c.hasAnimationFinished()) {
                    ClientProfiles.clearPlay(id);
                    req = null;
                }
            }
            if (req != null) {
                RawAnimation raw = RawAnimation.begin();
                switch (req.mode) {
                    case LOOP -> raw.thenLoop(req.name);
                    case HOLD -> raw.thenPlayAndHold(req.name);
                    case ONCE -> raw.then(req.name, Animation.LoopType.PLAY_ONCE);
                    case FILE -> raw.thenPlay(req.name);
                }
                return state.setAndContinue(raw);
            }

            Profile profile = ClientProfiles.getUsable(id);
            String set = profile == null ? null : profile.animations();
            List<String> situations = !state.isMoving()?List.of("idle")
                    :(entity.isSprinting()?List.of("run", "walk")
                    :List.of("walk"));

            String anim = pick(set, situations, id);
            if (anim == null) return PlayState.STOP;
            return state.setAndContinue(RawAnimation.begin().thenPlay(anim));
        }));
    }

    private String pick(String set, List<String> situations, int seed) {
        for (String situation: situations) {
            List<String> names = AnimationSets.get(set, situation);
            for (int i=0;i<names.size();i++) {
                String name = names.get(Math.floorMod(seed + i, names.size()));
                if (exists(name)) return name;
            }
        }
        return null;
    }
    private boolean exists(String name) {
        try {
            return model.getAnimation(this, name) != null;
        } catch (RuntimeException e) {
            return false;
        }
    }
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}