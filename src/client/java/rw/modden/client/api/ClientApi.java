package rw.modden.client.api;

import net.minecraft.entity.Entity;
import rw.modden.RuneModels;
import rw.modden.api.PlayMode;
import rw.modden.client.render.ClientProfiles;
import rw.modden.client.render.PlayRequest;
import rw.modden.profile.Names;
import rw.modden.profile.Profile;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.loading.object.BakedAnimations;
import net.minecraft.util.Identifier;

import java.util.Arrays;

/** Public client API. For client-side code only (src/client) */
public final class ClientApi {
    private ClientApi() {}

    /** Model + mandatory texture + optional set of animations (null = no animations) */
    public static boolean setModel(Entity entity, String model, String texture, String animationSet) {
        return ClientProfiles.set(entity.getId(), new Profile(model, texture, animationSet));
    }
    /** Texture only: vanilla model and vanilla animations */
    public static boolean setTexture(Entity entity, String texture) {
        return ClientProfiles.set(entity.getId(), new Profile(null, texture, null));
    }

    public static void clear(Entity entity) {
        ClientProfiles.clear(entity.getId());
    }

    /**
     * Play a specific animation. This only works if the entity already has a model with a texture applied,
     * and the animation is present within that model file. Otherwise, it returns false and logs a single line.
     */
    public static boolean playAnimation(Entity entity, String animation, PlayMode loop) {
        Profile profile = ClientProfiles.getUsable(entity.getId());
        if (profile == null || profile.model() == null) {
            RuneModels.LOGGER.warn("RuneModels: cannot play '{}': entity has no applied model", animation);
            return false;
        }
        if (!Arrays.asList(listAnimations(profile.model())).contains(animation)) {
            RuneModels.LOGGER.warn("RuneModels: cannot play '{}': no such animation in model '{}'", animation, profile.model());
            return false;
        }
        ClientProfiles.putPlay(entity.getId(), new PlayRequest(animation, loop));
        return true;
    }

    /** Capture the requested animation (required for looped animations) */
    public static void stopAnimation(Entity entity) {
        ClientProfiles.clearPlay(entity.getId());
    }

    /**
     * Animation names from the model's animation file, sorted alphabetically
     * Accepts a name without an extension ("Steve" or "mymod:Steve"). If the file is missing, returns an empty array
     */
    public static String[] listAnimations(String modelName) {
        Identifier id = Names.animation(modelName);
        if (id == null) return new String[0];
        BakedAnimations baked = GeckoLibCache.getBakedAnimations().get(id);
        if (baked == null) return new String[0];
        String[] names = baked.animations().keySet().toArray(new String[0]);
        Arrays.sort(names);
        return names;
    }
}