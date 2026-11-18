package com.comp2042.service.audio;

import com.comp2042.model.PickupType;

/**
 * Maps gameplay and menu sound identities to classpath audio resources.
 * Controllers select identities; the sound service loads and plays their files.
 */
public enum SoundEffect {
    /** Diamond collection effect. */
    DIAMOND("diamond.mp3"),
    /** Star collection effect. */
    STAR("star.mp3"),
    /** Ring collection effect. */
    RING("ring.mp3"),
    /** Bolt collection effect. */
    BOLT("bolt.mp3"),
    /** Blocks collection effect. */
    BLOCKS("blocks.mp3"),
    /** Menu button activation and game-mode selection effect. */
    BUTTON("button.mp3"),
    /** Fatal-collision effect requested when gameplay ends in death. */
    DEATH("death.mp3"),
    /** Full-board completion effect. */
    WIN("win.mp3");

    /** Initial playback volume on the zero-to-one scale. */
    public static final double DEFAULT_VOLUME = 0.5;
    /** Absolute classpath location of the audio file. */
    private final String resourcePath;

    /**
     * Builds a resource path beneath the application's audio directory.
     *
     * @param filename the audio filename including its extension
     */
    SoundEffect(String filename) {
        resourcePath = "/com/comp2042/audio/" + filename;
    }

    /**
     * Returns the classpath location of this sound effect.
     *
     * @return the absolute classpath path used to locate this effect
     */
    public String getResourcePath() {
        return resourcePath;
    }

    /**
     * Maps a collected pickup's identity to its collection sound.
     *
     * @param type the collected pickup type
     * @return the matching collection effect
     * @throws NullPointerException if {@code type} is null
     */
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
