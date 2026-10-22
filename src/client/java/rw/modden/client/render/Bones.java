package rw.modden.client.render;

import software.bernie.geckolib.cache.object.GeoBone;

import java.util.List;

public final class Bones {
    public static final String HEAD = "head", BODY = "body";
    public static final String RIGHT_ARM = "rightarm", LEFT_ARM = "leftarm";
    public static final String RIGHT_LEG = "rightleg", LEFT_LEG = "leftleg";
    public static final String RIGHT_ITEM = "rightitem", LEFT_ITEM = "leftitem";

    private Bones() {}

    public static boolean is(GeoBone bone, String role) {
        return bone.getName().replace("_", "").equalsIgnoreCase(role);
    }

    public static GeoBone find(List<GeoBone> bones, String role) {
        for (GeoBone bone: bones) {
            if (is(bone, role)) return bone;
            GeoBone found = find(bone.getChildBones(), role);
            if (found != null) return found;
        }
        return null;
    }
}
