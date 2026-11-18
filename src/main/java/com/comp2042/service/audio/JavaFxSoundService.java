package com.comp2042.service.audio;

import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import javafx.scene.media.AudioClip;

/** Caches short clips once; unavailable sounds never prevent gameplay. */
public final class JavaFxSoundService implements SoundService {
    private static final System.Logger LOGGER = System.getLogger(JavaFxSoundService.class.getName());
    private final Map<SoundEffect, AudioClip> clips = new EnumMap<>(SoundEffect.class);
    private double volume = SoundEffect.DEFAULT_VOLUME;
    private boolean closed;

    public JavaFxSoundService() {
        for (SoundEffect effect : SoundEffect.values()) {
            try {
                URL resource = Objects.requireNonNull(getClass().getResource(effect.getResourcePath()),
                        "Missing audio resource: " + effect.getResourcePath());
                clips.put(effect, new AudioClip(resource.toExternalForm()));
            } catch (RuntimeException exception) {
                logFailure(effect, exception);
            }
        }
    }

    @Override
    public void play(SoundEffect effect) {
        AudioClip clip = clips.get(effect);
        if (closed || clip == null) return;
        try {
            clip.play(volume);
        } catch (RuntimeException exception) {
            clips.remove(effect);
            logFailure(effect, exception);
        }
    }

    @Override
    public void stopAll() {
        var iterator = clips.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            try {
                entry.getValue().stop();
            } catch (RuntimeException exception) {
                iterator.remove();
                logFailure(entry.getKey(), exception);
            }
        }
    }

    @Override
    public void setVolume(double volume) {
        if (!Double.isFinite(volume) || volume < 0 || volume > 1) {
            throw new IllegalArgumentException("Volume must be between 0 and 1");
        }
        this.volume = volume;
    }

    @Override
    public double getVolume() { return volume; }

    @Override
    public void close() {
        stopAll();
        clips.clear();
        closed = true;
    }

    private static void logFailure(SoundEffect effect, RuntimeException exception) {
        LOGGER.log(System.Logger.Level.WARNING, "Sound disabled: " + effect.getResourcePath(), exception);
    }
}
