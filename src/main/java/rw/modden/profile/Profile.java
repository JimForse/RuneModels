package rw.modden.profile;

/**
 * name: "everything by one name" (runemodels.<name>) - the texture, plus the model if such a file exists. Resolved on the client.
 * model, texture: explicit values (runemodels.model.<x>, runemodels.texture.<y>), they take priority over name
 */
public record Profile(String name, String model, String texture, String animations) {
    public static final Profile EMPTY = new Profile(null, null, null, null);
}
