package rw.modden.profile;

import net.minecraft.util.Identifier;
import rw.modden.RuneModels;

/** Converts a name ("name" or "mymod:name") into file paths */
public final class Names {
    private Names() {}

    /** If no namespace is provided, `runemodels` is used. An invalid name results in `null`. */
    public static Identifier parse(String name) {
        if (name == null || name.isBlank() || name.length() > 128) return null;
        String n = name.toLowerCase();
        return Identifier.tryParse(n.contains(":")?n:RuneModels.MOD_ID + ":" + n);
    }

    public static Identifier model(String name)     { return build(name, "geo/", ".geo.json"); }
    public static Identifier animation(String name) { return build(name, "animations/", ".animation.json"); }
    public static Identifier texture(String name)   { return build(name, "textures/entity/", ".png"); }

    private static Identifier build(String name, String prefix, String suffix) {
        Identifier id = parse(name);
        return id == null?null:new Identifier(id.getNamespace(), prefix + id.getPath() + suffix);
    }
}