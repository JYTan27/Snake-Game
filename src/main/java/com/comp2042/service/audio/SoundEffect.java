package com.comp2042.service.audio;

import com.comp2042.model.PickupType;

/** Central sound catalog; changing a file here changes every playback site. */
public enum SoundEffect {
    DIAMOND("diamond.mp3"),
    STAR("star.mp3"),
    RING("ring.mp3"),
    BOLT("bolt.mp3"),
    BLOCKS("blocks.mp3"),
    BUTTON("button.mp3"),
    DEATH("death.mp3"),
    WIN("win.mp3");

    public static final double DEFAULT_VOLUME = 0.5;
    private final String resourcePath;

    SoundEffect(String filename) {
        resourcePath = "/com/comp2042/audio/" + filename;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public static SoundEffect forPickup(PickupType type) {
        return switch (type) {
            case DIAMOND -> DIAMOND;
            case STAR -> STAR;
            case RING -> RING;
            case BOLT -> BOLT;
            case BLOCKS -> BLOCKS;
        };
    }
}
