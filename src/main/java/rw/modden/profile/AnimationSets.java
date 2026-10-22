package rw.modden.profile;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class AnimationSets {
    private static final Map<String, Map<String, List<String>>> SETS = new ConcurrentHashMap<>();
    private static volatile String defaultSet = null;

    private AnimationSets() {}

    public static boolean create(String set) {
        return SETS.putIfAbsent(set, new ConcurrentHashMap<>()) == null;
    }
    public static boolean remove(String set) {
        if (set.equals(defaultSet)) defaultSet = null;
        return SETS.remove(set) != null;
    }
    public static void addAnimation(String set, String situation, String anim) {
        List<String> list = SETS.computeIfAbsent(set, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(situation, k -> new CopyOnWriteArrayList<>());
        if (!list.contains(anim)) list.add(anim);
    }
    public static boolean removeAnimation(String set, String situation, String anim) {
        Map<String, List<String>> map = SETS.get(set);
        List<String> list = map == null?null:map.get(situation);
        return list != null && list.remove(anim);
    }
    public static boolean exists(String set) { return SETS.containsKey(set); }
    public static Set<String> names() { return SETS.keySet(); }

    /** What constitutes a "standard" set? It is defined externally (via a file or API), not in the code */
    public static void setDefaultSet(String set) { defaultSet = set; }

    /** No set, no situation, or no animations — empty list */
    public static List<String> get(String set, String situation) {
        String name = set != null?set:defaultSet;
        Map<String, List<String>> map = name == null?null:SETS.get(name);
        List<String> list = map == null ? null:map.get(situation);
        return list == null?List.of():list;
    }
}