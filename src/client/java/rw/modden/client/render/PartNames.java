package rw.modden.client.render;

import net.minecraft.client.model.ModelPart;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/** Names of vanilla model parts. Weak keys: models dropped after a resource reload do not stay in memory */
public final class PartNames {
    private static final Map<ModelPart, String> NAMES = Collections.synchronizedMap(new WeakHashMap<>());

    private PartNames() {}

    public static void remember(ModelPart part, String name) { NAMES.put(part, name); }
    public static String get(ModelPart part) { return NAMES.get(part); }
}
