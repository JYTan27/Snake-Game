package com.comp2042.service.audio;

/**
 * Application-scoped sound operations used by controllers without depending on
 * JavaFX audio classes. The application owns the shared service and closes it
 * at shutdown; individual screens can stop playback without closing the service.
 */
public interface SoundService extends AutoCloseable {
    /**
     * Requests playback of a catalogued effect at the configured volume.
     *
     * @param effect the sound to play
     */
    void play(SoundEffect effect);

    /** Stops all currently playing effects managed by this service. */
    void stopAll();

    /**
     * Stops playback when the owner releases the service. Implementations may
     * additionally release cached resources; the default only calls {@code stopAll()}.
     */
    @Override
    default void close() { stopAll(); }
}
