package rw.modden.client.render;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import rw.modden.client.api.ClientApi;
import rw.modden.profile.AnimationSets;
import rw.modden.profile.Profile;
import software.bernie.geckolib.animatable.GeoReplacedEntity;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.keyframe.BoneAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
            List<String> situations = !state.isMoving()?List.of("idle")
                    :(entity.isSprinting()?List.of("run", "walk")
                    :List.of("walk"));

            String anim = pick(profile, situations, id);
            if (anim == null) return PlayState.STOP;
            return state.setAndContinue(RawAnimation.begin().thenPlay(anim));
        }));
    }

    private String pick(Profile profile, List<String> situations, int seed) {
        if (profile == null) return null;
        boolean byName = profile.animations() == null && auto(profile);
        for (String situation: situations) {
            List<String> names = byName?byName(profile.model(), situation):AnimationSets.get(profile.animations(), situation);
            for (int i=0;i<names.size();i++) {
                String name = names.get(Math.floorMod(seed + i, names.size()));
                if (exists(name)) return name;
            }
        }
        return null;
    }

    /** runemodels.<name>: the model came from the name, so its own animation file is used even without a set */
    private static boolean auto(Profile profile) {
        return profile.name() != null && profile.name().equals(profile.model());
    }

    private static List<String> byName(String model, String situation) {
        List<String> names = new ArrayList<>();
        for (String name: ClientApi.listAnimations(model))
            if (name.equals(situation) || name.startsWith(situation + "_")) names.add(name);
        return names;
    }
    private boolean exists(String name) {
        try {
            return model.getAnimation(this, name) != null;
        } catch (RuntimeException e) {
            return false;
        }
    }
    /** Names of the bones moved by the custom animation playing on this entity right now (empty if none) */
    public Set<String> animatedBones(long instanceId) {
        AnimationController<?> controller = cache.getManagerForId(instanceId).getAnimationControllers().get("main");
        if (controller == null || controller.getAnimationState() == AnimationController.State.STOPPED
                || controller.getCurrentAnimation() == null) return Set.of();
        Set<String> names = new HashSet<>();
        for (BoneAnimation animation: controller.getCurrentAnimation().animation().boneAnimations()) names.add(animation.boneName());
        return names;
    }
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}