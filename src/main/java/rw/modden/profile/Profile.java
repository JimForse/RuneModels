package rw.modden.profile;

public record Profile(String model, String texture, String animations) {
    public static final Profile EMPTY = new Profile(null, null, null);
}