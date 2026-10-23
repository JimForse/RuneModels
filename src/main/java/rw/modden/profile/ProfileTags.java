package rw.modden.profile;

import net.minecraft.entity.Entity;
import java.util.ArrayList;

/** Reads and writes the profile within an entity's tags. The only place that understands the tag format */
public final class ProfileTags {
    public static final String PREFIX = "runemodels.";
    private static final String MODEL = PREFIX + "model.";
    private static final String TEXTURE = PREFIX + "texture.";
    private static final String SET = PREFIX + "set.";

    private ProfileTags() {}

    // Colons are not allowed in tags, so "mymod:name" is stored as "mymod+name"
    // Resource names cannot contain "+", so the reverse transformation is unambiguous
    private static String encode(String name) { return name.toLowerCase().replace(':', '+'); }
    private static String decode(String part) { return part.replace('+', ':'); }

    /** A name like "set.x" would be stored as "runemodels.set.x" and read back as an animation set, not as a name */
    public static boolean isReserved(String name) {
        String n = name.toLowerCase();
        return n.startsWith("model.") || n.startsWith("texture.") || n.startsWith("set.");
    }

    /** null, if the entity has neither a name, nor a model, nor a texture (an animation set alone is not a profile) */
    public static Profile read(Entity entity) {
        Profile p = readAll(entity);
        return p.name() == null && p.model() == null && p.texture() == null?null:p;
    }

    public static Profile readAll(Entity entity) {
        String name = null, model = null, texture = null, set = null;
        for (String tag: entity.getCommandTags()) {
            if (!tag.startsWith(PREFIX)) continue;
            if (tag.startsWith(MODEL)) model = decode(tag.substring(MODEL.length()));
            else if (tag.startsWith(TEXTURE)) texture = decode(tag.substring(TEXTURE.length()));
            else if (tag.startsWith(SET)) set = tag.substring(SET.length());
            else name = decode(tag.substring(PREFIX.length()));
        }
        return new Profile(name, model, texture, set);
    }

    /** Replaces all profile tags. Must be called within ProfileSync.silently(...) */
    public static void write(Entity entity, Profile p) {
        for (String tag: new ArrayList<>(entity.getCommandTags())) {
            if (tag.startsWith(PREFIX)) entity.removeScoreboardTag(tag);
        }
        if (p == null) return;
        if (p.name() != null) entity.addCommandTag(PREFIX + encode(p.name()));
        if (p.model() != null) entity.addCommandTag(MODEL + encode(p.model()));
        if (p.texture() != null) entity.addCommandTag(TEXTURE + encode(p.texture()));
        if (p.animations() != null) entity.addCommandTag(SET + p.animations());
    }
}
