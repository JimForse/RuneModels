package rw.modden.api;

import net.minecraft.entity.Entity;
import rw.modden.ProfileSync;
import rw.modden.profile.ProfileTags;
import rw.modden.profile.Names;
import rw.modden.profile.Profile;

/** Public server-side API. Each method returns false if it performed no action */
public final class RuneModelsApi {
    private RuneModelsApi() {}

    /** Model + mandatory texture + optional set of animations (null = no animations) */
    public static boolean setModel(Entity entity, String model, String texture, String animationSet) {
        if (isClient(entity) || Names.parse(model) == null || Names.parse(texture) == null) return false;
        String set = animationSet == null || animationSet.isBlank()?null:animationSet;
        apply(entity, new Profile(model, texture, set));
        return true;
    }

    /** Texture only: vanilla model and vanilla animations */
    public static boolean setTexture(Entity entity, String texture) {
        if (isClient(entity) || Names.parse(texture) == null) return false;
        apply(entity, new Profile(null, texture, null));
        return true;
    }

    public static boolean clear(Entity entity) {
        if (isClient(entity)) return false;
        apply(entity, null);
        ProfileSync.sendPlay(entity, null, null);
        return true;
    }

    /** Play an animation. Only if a model (with a texture) is already present on the entity. */
    public static boolean play(Entity entity, String animation, PlayMode mode) {
        if (isClient(entity) || animation == null || animation.isBlank() || animation.length() > 128) return false;
        Profile profile = ProfileTags.read(entity);
        if (profile == null || profile.model() == null) return false;
        ProfileSync.sendPlay(entity, animation, mode);
        return true;
    }

    public static boolean stop(Entity entity) {
        if (isClient(entity)) return false;
        ProfileSync.sendPlay(entity, null, null);
        return true;
    }

    private static void apply(Entity entity, Profile profile) {
        ProfileSync.silently(() -> ProfileTags.write(entity, profile));
        ProfileSync.syncProfile(entity);
    }

    private static boolean isClient(Entity entity) {
        return entity.getWorld().isClient();
    }
}