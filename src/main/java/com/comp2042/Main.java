package com.comp2042;

import com.comp2042.view.ViewManager;
import com.comp2042.service.audio.JavaFxSoundService;
import com.comp2042.service.audio.SoundService;
import com.comp2042.service.leaderboard.LeaderboardService;
import com.comp2042.service.leaderboard.XmlLeaderboardRepository;
import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * JavaFX entry point. Owns shared audio, leaderboard, and navigation services;
 * opens the menu and finalises gameplay and storage during orderly shutdown.
 */
public class Main extends Application {

    /** Application-wide view manager made available to FXML controllers after startup. */
    private static ViewManager viewManager;
    /** Sound service owned by this application instance and closed at shutdown. */
    private SoundService sounds;

    /** Creates the application instance; shared services are initialised by JavaFX startup. */
    public Main() {}

    /**
     * Initialises application resources and shows the menu on the JavaFX application thread.
     *
     * @param primaryStage the single application stage supplied by JavaFX
     */
    @Override
    public void start(Stage primaryStage) {
        // Load the fonts programmatically to bypass the CSS path bug
        Font.loadFont(getClass().getResourceAsStream("/com/comp2042/fonts/SFPixelate.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/com/comp2042/fonts/SFPixelateShaded-Bold.ttf"), 14);

        sounds = new JavaFxSoundService();
        LeaderboardService leaderboards = new LeaderboardService(new XmlLeaderboardRepository(XmlLeaderboardRepository.defaultPath()));
        viewManager = new ViewManager(primaryStage, sounds, leaderboards);
        viewManager.initializeMainMenu();
    }

    /**
     * Launches the JavaFX application lifecycle.
     *
     * @param args command-line arguments passed to JavaFX
     */
    public static void main(String[] args) {
        launch(args);
    }

    /** Finalises a started game before releasing audio. */
    @Override
    public void stop() {
        if (viewManager != null) viewManager.close();
        if (sounds != null) sounds.close();
    }

    /**
     * Returns the shared view manager, or {@code null} before startup creates it.
     *
     * @return the shared view manager, or {@code null} before startup creates it
     */
    public static ViewManager getViewManager() {
        return viewManager;
    }
}
