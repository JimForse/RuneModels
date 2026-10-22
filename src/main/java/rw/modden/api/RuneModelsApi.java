package rw.modden.api;

import net.minecraft.entity.Entity;
import rw.modden.ProfileSync;
import rw.modden.profile.ProfileTags;
import rw.modden.profile.Names;
import rw.modden.profile.Profile;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * Public server-side API. Each method returns false if it performed no action.
 * apply / setModel / setTexture replace the look of the entity but keep its animation set
 */
public final class RuneModelsApi {
    private static final Pattern SET_NAME = Pattern.compile("[a-z0-9_.\\-]+");

    private RuneModelsApi() {}

    /** Everything by one name (tag runemodels.<name>): the texture, plus the model if such a file exists on the client */
    public static boolean apply(Entity entity, String name) {
        if (isClient(entity) || Names.parse(name) == null) return false;
        update(entity, p -> new Profile(name, null, null, p.animations()));
        return true;
    }

    /** Model and the texture of the same name (tag runemodels.model.<model>) */
    public static boolean setModel(Entity entity, String model) {
        return setModel(entity, model, null);
    }

    /** Model with a texture of another name. texture == null means the texture of the same name as the model */
    public static boolean setModel(Entity entity, String model, String texture) {
        if (isClient(entity) || Names.parse(model) == null || texture != null && Names.parse(texture) == null) return false;
        update(entity, p -> new Profile(null, model, texture, p.animations()));
        return true;
    }

    /** Texture only: vanilla model and vanilla animations */
    public static boolean setTexture(Entity entity, String texture) {
        if (isClient(entity) || Names.parse(texture) == null) return false;
        update(entity, p -> new Profile(null, null, texture, p.animations()));
        return true;
    }

    /** Animation set (tag runemodels.set.<set>). null removes it. The model and texture are not touched */
    public static boolean setAnimationSet(Entity entity, String set) {
        String s = set == null?null:set.toLowerCase();
        if (isClient(entity) || s != null && !SET_NAME.matcher(s).matches()) return false;
        update(entity, p -> new Profile(p.name(), p.model(), p.texture(), s));
        return true;
    }

    public static boolean clear(Entity entity) {
        if (isClient(entity)) return false;
        write(entity, null);
        ProfileSync.sendPlay(entity, null, null);
        return true;
    }

    /** Play an animation. Only if the entity has a model, or a name that may give one (checked on the client) */
    public static boolean play(Entity entity, String animation, PlayMode mode) {
        if (isClient(entity) || animation == null || animation.isBlank() || animation.length() > 128) return false;
        Profile profile = ProfileTags.read(entity);
        if (profile == null || profile.model() == null && profile.name() == null) return false;
        ProfileSync.sendPlay(entity, animation, mode);
        return true;
    }

    public static boolean stop(Entity entity) {
        if (isClient(entity)) return false;
        ProfileSync.sendPlay(entity, null, null);
        return true;
    }

    private static void update(Entity entity, UnaryOperator<Profile> change) {
        write(entity, change.apply(ProfileTags.readAll(entity)));
    }

    private static void write(Entity entity, Profile profile) {
        ProfileSync.silently(() -> ProfileTags.write(entity, profile));
        ProfileSync.syncProfile(entity);
    }

    private static boolean isClient(Entity entity) {
        return entity.getWorld().isClient();
    }
}
