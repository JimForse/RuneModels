package rw.modden.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import rw.modden.RuneModels;
import rw.modden.profile.AnimationSets;
import rw.modden.profile.Names;
import rw.modden.profile.Profile;
import software.bernie.geckolib.cache.GeckoLibCache;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ProfileValidator {
    private static final Map<Profile, Optional<Profile>> CACHE = new ConcurrentHashMap<>();

    private ProfileValidator() {}

    /** The profile as it will be drawn: model and texture filled in from the name. null = cannot be drawn */
    public static Profile resolve(Profile p) {
        return CACHE.computeIfAbsent(p, ProfileValidator::check).orElse(null);
    }
    public static void reset() {
        CACHE.clear();
    }

    /**
     * Explicit model and texture take priority over the name.
     * The name gives a model only if such a model file exists; a model without a texture uses the texture of the same name
     */
    private static Profile fill(Profile p) {
        String model = p.model();
        if (model == null && p.name() != null) {
            Identifier geo = Names.model(p.name());
            if (geo != null && GeckoLibCache.getBakedModels().containsKey(geo)) model = p.name();
        }
        String texture = p.texture() != null?p.texture():p.model() != null?p.model():p.name();
        return new Profile(p.name(), model, texture, p.animations());
    }

    private static Optional<Profile> check(Profile raw) {
        Profile p = fill(raw);
        if (p.model() == null && p.texture() == null) return fail(raw, "profile is empty");

        Identifier texture = Names.texture(p.texture());
        if (texture == null) return fail(raw, "invalid texture name '" + p.texture() + "'");
        if (!resourceExists(texture)) return fail(raw, "texture file not found: " + texture);

        if (p.model() != null) {
            Identifier geo = Names.model(p.model());
            if (geo == null) return fail(raw, "invalid model name '" + p.model() + "'");
            if (!GeckoLibCache.getBakedModels().containsKey(geo)) return fail(raw, "model file not found or invalid: " + geo);

            if (p.animations() != null) {
                if (!AnimationSets.exists(p.animations()))
                    RuneModels.LOGGER.warn("RuneModels: {} - animation set '{}' does not exist, model will not animate", raw, p.animations());
                Identifier anim = Names.animation(p.model());
                if (!GeckoLibCache.getBakedAnimations().containsKey(anim))
                    RuneModels.LOGGER.warn("RuneModels: {} - animation file not found: {}", raw, anim);
            }
        }
        return Optional.of(p);
    }

    private static boolean resourceExists(Identifier id) {
        return MinecraftClient.getInstance().getResourceManager().getResource(id).isPresent();
    }
    private static Optional<Profile> fail(Profile p, String reason) {
        RuneModels.LOGGER.warn("RuneModels: profile {} rejected - {}. The entity will render normally.", p, reason);
        return Optional.empty();
    }
}
