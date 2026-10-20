package rw.modden.client.render;

import rw.modden.api.PlayMode;

public final class PlayRequest {
    public final String name;
    boolean started;
    int frames;
    public final PlayMode mode;

    public PlayRequest(String name, PlayMode mode) {
        this.name = name;
        this.mode = mode;
    }
}