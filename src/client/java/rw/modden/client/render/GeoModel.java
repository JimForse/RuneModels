package rw.modden.client.render;

import net.minecraft.util.Identifier;
import rw.modden.profile.Names;
import rw.modden.profile.Profile;

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
}