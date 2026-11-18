package com.comp2042.service.audio;

/** Application-scoped playback, replaceable without JavaFX in event tests. */
public interface SoundService extends AutoCloseable {
    void play(SoundEffect effect);
    void stopAll();
    void setVolume(double volume);
    double getVolume();

    @Override
    default void close() { stopAll(); }
}
