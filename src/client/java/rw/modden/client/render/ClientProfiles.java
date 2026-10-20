package rw.modden.client.render;

import rw.modden.profile.Profile;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** "Entity ID -> profile" and "Entity ID -> requested animation" tables */
public final class ClientProfiles {
    private static final Map<Integer, Profile> PROFILES = new ConcurrentHashMap<>();
    private static final Map<Integer, PlayRequest> PLAYS = new ConcurrentHashMap<>();

    private ClientProfiles() {}

    /** A profile that can be safely drawn. Otherwise, null: the entity is drawn as usual. */
    public static Profile getUsable(int entityId) {
        Profile p = PROFILES.get(entityId);
        return p != null && ProfileValidator.isUsable(p) ? p : null;
    }

    /** Saves the profile and returns whether it was applied (false = rejected; the reason can be found in the log) */
    public static boolean set(int entityId, Profile profile) {
        PROFILES.put(entityId, profile);
        PLAYS.remove(entityId);
        return ProfileValidator.isUsable(profile);
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