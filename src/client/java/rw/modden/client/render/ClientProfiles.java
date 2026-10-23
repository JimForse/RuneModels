package rw.modden.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import rw.modden.profile.Names;
import rw.modden.profile.Profile;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.renderer.GeoRenderer;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** "Entity ID -> profile" and "Entity ID -> requested animation" tables */
public final class ClientProfiles {
    private static final Map<Integer, Profile> PROFILES = new ConcurrentHashMap<>();
    private static final Map<Integer, PlayRequest> PLAYS = new ConcurrentHashMap<>();
    private static final Map<EntityType<?>, Optional<Profile>> TYPES = new ConcurrentHashMap<>();
    private static Map<?, ?> typesWith;

    private ClientProfiles() {}

    /** The profile as it will be drawn (the name is already resolved). Otherwise, null: the entity is drawn as usual */
    public static Profile getUsable(int entityId) {
        Profile p = PROFILES.get(entityId);
        if (p == null) p = byType(entityId);
        return p == null?null:ProfileValidator.resolve(p);
    }

    /** Found once per entity type, and again after each resource reload (GeckoLib then replaces its model table) */
    private static Profile byType(int entityId) {
        ClientWorld world = MinecraftClient.getInstance().world;
        Entity entity = world == null?null:world.getEntityById(entityId);
        if (entity == null) return null;
        Map<?, ?> models = GeckoLibCache.getBakedModels();
        if (typesWith != models) {
            TYPES.clear();
            typesWith = models;
        }
        return TYPES.computeIfAbsent(entity.getType(), type -> Optional.ofNullable(forType(entity))).orElse(null);
    }

    /** Entities already drawn by GeckoLib are left alone: their mod may keep its own model under the same name */
    private static Profile forType(Entity entity) {
        if (MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(entity) instanceof GeoRenderer<?>) return null;
        String name = Registries.ENTITY_TYPE.getId(entity.getType()).toString();
        Identifier model = Names.model(name);
        return model != null && GeckoLibCache.getBakedModels().containsKey(model)?new Profile(name, null, null, null):null;
    }

    /** The profile exactly as it was set (the name is not resolved), or null */
    public static Profile getRaw(int entityId) { return PROFILES.get(entityId); }

    /** Saves the profile and returns whether it was applied (false = rejected; the reason can be found in the log) */
    public static boolean set(int entityId, Profile profile) {
        PROFILES.put(entityId, profile);
        PLAYS.remove(entityId);
        return ProfileValidator.resolve(profile) != null;
    }
    public static void clear(int entityId) {
        PROFILES.remove(entityId);
        PLAYS.remove(entityId);
    }
    public static void clearAll() {
        PROFILES.clear();
        PLAYS.clear();
    }

    public static PlayRequest getPlay(int entityId) { return PLAYS.get(entityId); }
    public static void putPlay(int entityId, PlayRequest r) { PLAYS.put(entityId, r); }
    public static void clearPlay(int entityId) { PLAYS.remove(entityId); }
}
