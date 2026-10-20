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

    /** null, if the entity has neither a model nor a texture */
    public static Profile read(Entity entity) {
        String model = null, texture = null, set = null;
        for (String tag: entity.getCommandTags()) {
            if (tag.startsWith(MODEL)) model = decode(tag.substring(MODEL.length()));
            else if (tag.startsWith(TEXTURE)) texture = decode(tag.substring(TEXTURE.length()));
            else if (tag.startsWith(SET)) set = tag.substring(SET.length());
        }
        return model == null && texture == null?null:new Profile(model, texture, set);
    }

    /** Replaces all profile tags. Must be called within ProfileSync.silently(...) */
    public static void write(Entity entity, Profile p) {
        for (String tag: new ArrayList<>(entity.getCommandTags())) {
            if (tag.startsWith(MODEL) || tag.startsWith(TEXTURE) || tag.startsWith(SET))
                entity.removeScoreboardTag(tag);
        }
        if (p == null) return;
        if (p.model() != null) entity.addCommandTag(MODEL + encode(p.model()));
        if (p.texture() != null) entity.addCommandTag(TEXTURE + encode(p.texture()));
        if (p.animations() != null) entity.addCommandTag(SET + p.animations());
    }
}