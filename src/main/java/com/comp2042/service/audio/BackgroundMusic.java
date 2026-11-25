package com.comp2042.service.audio;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

/**
 * Rotates through the packaged soundtrack with one media player at a time.
 * Completed tracks return to the end of the playlist. A track that fails to load
 * or play is skipped for the rest of the session.
 * Call lifecycle methods on the JavaFX application thread.
 */
public final class BackgroundMusic implements AutoCloseable {

    /** Logger for tracks that cannot be played. */
    private static final System.Logger LOGGER = System.getLogger(BackgroundMusic.class.getName());
    /** Classpath directory containing the soundtrack. */
    private static final String AUDIO_DIRECTORY = "/com/comp2042/audio/";
    /** Soundtrack volume on the zero-to-one scale. */
    private static final double VOLUME = 0.1;
    /** Track filenames in their initial playback order. */
    private static final List<String> TRACKS = List.of(
            "Aventure - Afternoon Coffee.mp3",
            "Aventure - Sunset.mp3",
            "ruskerdax - puzzling.mp3"
    );

    /** Tracks waiting to play, with completed tracks appended at the end. */
    private final Deque<String> playlist = new ArrayDeque<>(TRACKS);
    /** Player for the current track, if one is loaded. */
    private MediaPlayer player;
    /** Desired playback state, which can change before the media player is ready. */
    private boolean playbackRequested;

    /**
     * Requests playback from the current position or starts the next available track.
     * Repeated requests leave playback at its current position.
     */
    public void play() {
        if (playbackRequested) return;
        playbackRequested = true;
        if (player == null) {
            playNextTrack();
        } else {
            player.play();
        }
    }

    /** Pauses the current track and suspends playlist advancement until {@link #play()} is called. */
    public void pause() {
        playbackRequested = false;
        if (player != null) player.pause();
    }

    /** Tries queued tracks in order until one accepts a playback request or the queue is empty. */
    private void playNextTrack() {
        while (!playlist.isEmpty()) {
            // Resolve the next queued filename from the application classpath.
            String track = playlist.removeFirst();
            String path = AUDIO_DIRECTORY + track;
            try {
                // Open the source and configure its player before requesting playback.
                URL resource = Objects.requireNonNull(getClass().getResource(path), "Missing audio resource");
                Media media = new Media(resource.toExternalForm());
                MediaPlayer nextPlayer = new MediaPlayer(media);
                player = nextPlayer;
                nextPlayer.setVolume(VOLUME);
                nextPlayer.setOnEndOfMedia(() -> advance(nextPlayer, track, null));
                nextPlayer.setOnError(() -> advance(nextPlayer, track, nextPlayer.getError()));
                media.setOnError(() -> advance(nextPlayer, track, media.getError()));
                nextPlayer.play();
                return;
            } catch (RuntimeException exception) {
                logFailure(track, exception);
                disposePlayer();
            }
        }
    }

    /**
     * Handles completion or failure from the active player and ignores callbacks from older players.
     *
     * @param finishedPlayer the player that raised the callback
     * @param track the filename associated with that player
     * @param error the playback error, or {@code null} on normal completion
     */
    private void advance(MediaPlayer finishedPlayer, String track, RuntimeException error) {
        if (player != finishedPlayer) return;

        if (error == null) {
            playlist.addLast(track);
        } else {
            logFailure(track, error);
        }
        disposePlayer();
        if (playbackRequested) playNextTrack();
    }

    /** Clears the active reference and releases the previous player. */
    private void disposePlayer() {
        // Keep the player to dispose even after its active reference is cleared.
        MediaPlayer currentPlayer = player;
        // Clear the active reference so advance ignores callbacks from this player.
        player = null;
        if (currentPlayer != null) currentPlayer.dispose();
    }

    /**
     * Records why a track was removed from the rotation.
     *
     * @param track the filename of the failing track
     * @param error the failure that disabled the track
     */
    private static void logFailure(String track, RuntimeException error) {
        LOGGER.log(System.Logger.Level.WARNING, "Background track disabled: " + AUDIO_DIRECTORY + track, error);
    }

    /** Clears the playlist and disposes the current player, ending playback for this instance. */
    @Override
    public void close() {
        playbackRequested = false;
        playlist.clear();
        disposePlayer();
    }
}
