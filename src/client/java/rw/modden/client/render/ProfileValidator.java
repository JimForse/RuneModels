package rw.modden.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import rw.modden.RuneModels;
import rw.modden.profile.AnimationSets;
import rw.modden.profile.Names;
import rw.modden.profile.Profile;
import software.bernie.geckolib.cache.GeckoLibCache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ProfileValidator {
    private static final Map<Profile, Boolean> CACHE = new ConcurrentHashMap<>();

    private ProfileValidator() {}

    public static boolean isUsable(Profile p) {
        return CACHE.computeIfAbsent(p, ProfileValidator::check);
    }
    public static void reset() {
        CACHE.clear();
    }

    private static boolean check(Profile p) {
        boolean hasModel = p.model() != null;
        boolean hasTexture = p.texture() != null;

        if (!hasModel && !hasTexture) return fail(p, "profile is empty");
        if (hasModel && !hasTexture) return fail(p, "a model requires a texture");

        Identifier texture = Names.texture(p.texture());
        if (texture == null) return fail(p, "invalid texture name '" + p.texture() + "'");
        if (!resourceExists(texture)) return fail(p, "texture file not found: " + texture);

        if (hasModel) {
            Identifier geo = Names.model(p.model());
            if (geo == null) return fail(p, "invalid model name '" + p.model() + "'");
            if (!GeckoLibCache.getBakedModels().containsKey(geo)) return fail(p, "model file not found or invalid: " + geo);

            if (p.animations() != null) {
                if (!AnimationSets.exists(p.animations()))
                    RuneModels.LOGGER.warn("RuneModels: {} - animation set '{}' does not exist, model will not animate", p, p.animations());
                Identifier anim = Names.animation(p.model());
                if (!GeckoLibCache.getBakedAnimations().containsKey(anim))
                    RuneModels.LOGGER.warn("RuneModels: {} - animation file not found: {}", p, anim);
            }
        }
        return true;
    }

    private static boolean resourceExists(Identifier id) {
        return MinecraftClient.getInstance().getResourceManager().getResource(id).isPresent();
    }
    private static boolean fail(Profile p, String reason) {
        RuneModels.LOGGER.warn("RuneModels: profile {} rejected - {}. The entity will render normally.", p, reason);
        return false;
    }
}