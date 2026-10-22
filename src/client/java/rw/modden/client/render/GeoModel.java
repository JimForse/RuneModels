package rw.modden.client.render;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import rw.modden.profile.Names;
import rw.modden.profile.Profile;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animation.AnimationState;

public class GeoModel extends software.bernie.geckolib.model.GeoModel<Animatable> {
    private Profile current = Profile.EMPTY;

    public void setCurrent(Profile profile) { this.current = profile; }
    @Override
    public Identifier getModelResource(Animatable a) {
        return Names.model(current.model());
    }
    @Override
    public Identifier getTextureResource(Animatable a) {
        return Names.texture(current.texture());
    }
    @Override
    public Identifier getAnimationResource(Animatable a) {
        return Names.animation(current.model());
    }

    @Override
    public void setCustomAnimations(Animatable animatable, long instanceId, AnimationState<Animatable> state) {
        if (!(state.getData(DataTickets.ENTITY) instanceof LivingEntity entity)) return;
        VanillaPose.apply(entity, state.getPartialTick(), getBakedModel(getModelResource(animatable)), animatable.animatedBones(instanceId));
    }
}