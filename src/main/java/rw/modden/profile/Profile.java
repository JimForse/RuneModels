package rw.modden.profile;

/**
 * name: "everything by one name" (runemodels.<name>) - the texture, plus the model if such a file exists. Resolved on the client.
 * model, texture: explicit values (runemodels.model.<x>, runemodels.texture.<y>), they take priority over name
 */
public record Profile(String name, String model, String texture, String animations) {
    public static final Profile EMPTY = new Profile(null, null, null, null);
    /** Animation set meaning "no custom animations at all": only vanilla ones, even for a model with its own animation file */
    public static final String NO_ANIMATIONS = "none";
}
