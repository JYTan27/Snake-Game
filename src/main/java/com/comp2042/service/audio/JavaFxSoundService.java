package com.comp2042.service.audio;

import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import javafx.scene.media.AudioClip;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;

/**
 * JavaFX implementation that caches available short clips at construction.
 * Resource-loading, playback, and stopping failures are logged and disable the
 * affected clip without propagating those runtime failures to gameplay callers.
 * On Windows an unplayed Java Sound line keeps audio output initialised between effects.
 * Closing releases that line, stops playback, and makes later play requests no-ops.
 */
public final class JavaFxSoundService implements SoundService {
    /** Logger for missing resources and failed audio operations. */
    private static final System.Logger LOGGER = System.getLogger(JavaFxSoundService.class.getName());
    /** Successfully loaded clips, excluding any subsequently disabled effects. */
    private final Map<SoundEffect, AudioClip> clips = new EnumMap<>(SoundEffect.class);
    /** Unplayed Windows output line retained until shutdown to avoid short-effect latency. */
    private Clip outputLine;
    /** Volume supplied to future playback requests. */
    private double volume = SoundEffect.DEFAULT_VOLUME;
    /** Whether the service has been permanently closed. */
    private boolean closed;

    /**
     * Attempts to cache every catalogued effect using the default volume.
     * Logs unavailable resources individually so remaining sounds can still be used.
     */
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
        initialiseWindowsOutput();
    }

    /**
     * Keeps Windows output initialised without playing any sound. In the observed
     * device setup, opening this line removed delay from JavaFX effects and closing
     * it restored the delay. Failure leaves ordinary JavaFX playback available.
     */
    private void initialiseWindowsOutput() {
        if (!System.getProperty("os.name", "").startsWith("Windows")) return;
        Clip line = null;
        try {
            line = AudioSystem.getClip();
            AudioFormat format = new AudioFormat(44_100, 16, 1, true, false);
            // A quarter-second PCM buffer; the line is opened but never started.
            byte[] silence = new byte[(int) (format.getFrameRate() * format.getFrameSize() / 4)];
            line.open(format, silence, 0, silence.length);
            outputLine = line;
        } catch (LineUnavailableException | RuntimeException exception) {
            if (line != null) {
                try {
                    line.close();
                } catch (RuntimeException closeFailure) {
                    exception.addSuppressed(closeFailure);
                }
            }
            LOGGER.log(System.Logger.Level.WARNING,
                    "Windows audio output could not be kept initialised; using standard playback", exception);
        }
    }

    /**
     * Plays a cached effect. Closed services and effects absent from the cache
     * are ignored. A synchronous playback failure removes the clip and is logged.
     *
     * @param effect the catalogued effect to play
     */
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

    /** Stops effects while retaining the output line; logs and removes failing clips. */
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

    /**
     * Sets the volume for subsequent plays without adjusting clips already playing.
     *
     * @param volume a finite value from zero to one, inclusive
     * @throws IllegalArgumentException if the value is non-finite or outside that range
     */
    @Override
    public void setVolume(double volume) {
        if (!Double.isFinite(volume) || volume < 0 || volume > 1) {
            throw new IllegalArgumentException("Volume must be between 0 and 1");
        }
        this.volume = volume;
    }

    /**
     * Returns the configured volume for subsequent plays.
     *
     * @return the configured volume for subsequent plays
     */
    @Override
    public double getVolume() { return volume; }

    /** Stops all clips, releases the Windows output line, and disables subsequent playback. */
    @Override
    public void close() {
        if (closed) return;
        closed = true;
        stopAll();
        clips.clear();
        if (outputLine != null) {
            try {
                outputLine.close();
            } catch (RuntimeException exception) {
                LOGGER.log(System.Logger.Level.WARNING, "Audio output line could not be closed", exception);
            } finally {
                outputLine = null;
            }
        }
    }

    /**
     * Records an audio failure together with its resource path.
     *
     * @param effect the effect whose resource or operation failed
     * @param exception the runtime failure to include in the warning
     */
    private static void logFailure(SoundEffect effect, RuntimeException exception) {
        LOGGER.log(System.Logger.Level.WARNING, "Sound disabled: " + effect.getResourcePath(), exception);
    }
}
