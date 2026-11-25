package com.comp2042.service.audio;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

/** Plays the game's soundtrack on the JavaFX application thread. */
public final class BackgroundMusic implements AutoCloseable {
    private static final System.Logger LOGGER = System.getLogger(BackgroundMusic.class.getName());
    private static final String AUDIO_DIRECTORY = "/com/comp2042/audio/";
    private static final double VOLUME = 0.2;
    private static final List<String> TRACKS = List.of(
            "Aventure - Afternoon Coffee.mp3",
            "Aventure - Sunset.mp3",
            "ruskerdax - puzzling.mp3"
    );

    private final Deque<String> playlist = new ArrayDeque<>(TRACKS);
    private MediaPlayer player;
    private boolean playbackRequested;

    /** Starts the playlist or resumes the current track. */
    public void play() {
        if (playbackRequested) return;
        playbackRequested = true;
        if (player == null) {
            playNextTrack();
        } else {
            player.play();
        }
    }

    /** Pauses the playlist and keeps the current track's position. */
    public void pause() {
        playbackRequested = false;
        if (player != null) player.pause();
    }

    private void playNextTrack() {
        while (!playlist.isEmpty()) {
            String track = playlist.removeFirst();
            String path = AUDIO_DIRECTORY + track;
            try {
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

    private void disposePlayer() {
        MediaPlayer currentPlayer = player;
        player = null;
        if (currentPlayer != null) currentPlayer.dispose();
    }

    private static void logFailure(String track, RuntimeException error) {
        LOGGER.log(System.Logger.Level.WARNING, "Background track disabled: " + AUDIO_DIRECTORY + track, error);
    }

    /** Stops playback and releases the current player. */
    @Override
    public void close() {
        playbackRequested = false;
        playlist.clear();
        disposePlayer();
    }
}
