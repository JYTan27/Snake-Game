package com.comp2042.view;

import com.comp2042.controller.BoardColoursController;
import com.comp2042.controller.LeaderboardController;
import com.comp2042.model.GameMode;
import com.comp2042.service.audio.BackgroundMusic;
import com.comp2042.service.audio.SoundService;
import com.comp2042.service.leaderboard.LeaderboardService;
import com.comp2042.view.board.BoardTheme;
import com.comp2042.view.board.BoardThemePreferences;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.prefs.BackingStoreException;
import java.util.function.Consumer;

/**
 * Navigates between menu, reference, and gameplay screens in a shared scene.
 * Retains the selected mode and board palette, manages music during navigation,
 * and finalises active gameplay when leaving its screen. Scene operations run
 * on the JavaFX application thread.
 */
public class ViewManager implements AutoCloseable {

    /** Splash messages shown in order as the main menu is loaded. */
    private static final List<String> MENU_TIPS = List.of(
            "A diamond a day!",
            "Plan your turns!",
            "Impossible to win?",
            "No snakes were harmed!",
            "Leave yourself an exit!",
            "Certified noodle",
            "One more run?",
            "Fill every square!"
    );

    /** Single stage reused for every screen. */
    private final Stage primaryStage;
    /** Shared scene created on first navigation and reused by later screens. */
    private Scene scene;
    /** Shared sound-effect service owned and closed by the application. */
    private final SoundService soundService;
    /** Application-owned soundtrack paused while the gameplay screen is active. */
    private final BackgroundMusic backgroundMusic;
    /** Application-wide run history used by the leaderboard. */
    private final LeaderboardService leaderboardService;
    /** Reads and saves the user's board palette. */
    private final BoardThemePreferences boardThemePreferences = new BoardThemePreferences();
    /** Palette used for new game screens and the initial preview selection. */
    private BoardTheme boardTheme;
    /** Clean-up action for the active game screen, if present. */
    private Runnable onLeaveGame;
    /** Callback that resizes the active game when fullscreen changes. */
    private Consumer<Boolean> updateGameFullscreen;
    /** Session selection restored whenever the main menu is loaded. */
    private GameMode selectedMode = GameMode.CLASSIC;
    /** Position of the next message in the splash rotation. */
    private int nextMenuSplashIndex;

    /** Application icon loaded from the packaged image resources. */
    private final Image snakeLogo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/comp2042/images/ui/SnakeLogoWhite.png")));

    /**
     * Connects navigation to the shared stage and services and loads the saved board palette.
     * Disables JavaFX's default fullscreen hint and exit key so screens can
     * assign their own fullscreen and navigation shortcuts.
     *
     * @param primaryStage the single stage used to display all screens
     * @param soundService the application-owned sound-effect service
     * @param backgroundMusic the application-owned soundtrack played on menu screens
     * @param leaderboardService application-owned result history
     * @throws NullPointerException if a service argument is null
     */
    public ViewManager(Stage primaryStage, SoundService soundService, BackgroundMusic backgroundMusic,
                       LeaderboardService leaderboardService) {
        this.primaryStage = primaryStage;
        this.soundService = java.util.Objects.requireNonNull(soundService);
        this.backgroundMusic = Objects.requireNonNull(backgroundMusic);
        this.leaderboardService = java.util.Objects.requireNonNull(leaderboardService);
        this.boardTheme = boardThemePreferences.load();
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        primaryStage.fullScreenProperty().addListener((property, wasFullscreen, fullscreen) -> {
            if (updateGameFullscreen != null) {
                updateGameFullscreen.accept(fullscreen);
            }
            if (!fullscreen) {
                // Resize after JavaFX has told the native window to leave fullscreen.
                Platform.runLater(() -> {
                    if (primaryStage.isShowing() && !primaryStage.isFullScreen()) {
                        primaryStage.sizeToScene();
                    }
                });
            }
        });
    }

    /**
     * Loads a fresh main-menu layout, applies the window title and icon, and shows it.
     * Retains the session's selected mode and starts or resumes the soundtrack
     * after the menu opens. Displays an error if FXML loading fails.
     */
    public void initializeMainMenu() {
        // Keep the loaded root available for mounting after FXML processing succeeds.
        AnchorPane root;
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/comp2042/view/MainMenu.fxml"));
            root = loader.load();
        } catch (IOException exception) {
            exception.printStackTrace();
            // Show error alert and return early to prevent NPE
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR,
                    "Failed to load main menu: " + exception.getMessage()
            );
            alert.showAndWait();
            return;
        }

        if (primaryStage.getIcons().isEmpty()) {
            primaryStage.getIcons().add(snakeLogo);
        }
        primaryStage.setTitle("Snake Game");
        showScreen(createMenuContent(root));
        backgroundMusic.play();
    }

    /**
     * Provides access to sound effects during navigation and gameplay.
     *
     * @return the application-owned sound-effect service
     */
    public SoundService getSoundService() { return soundService; }

    /**
     * Provides access to run recording and leaderboard rankings.
     *
     * @return the shared leaderboard service
     */
    public LeaderboardService getLeaderboardService() { return leaderboardService; }

    /** {@return the applied palette used by new games and the colour editor} */
    public BoardTheme getBoardTheme() { return boardTheme; }

    /**
     * Applies a palette to new games and attempts to save it for future launches.
     * Displays a warning if saving fails while retaining the choice for the session.
     *
     * @param theme the selected board palette
     * @throws NullPointerException if {@code theme} is null
     */
    public void applyBoardTheme(BoardTheme theme) {
        boardTheme = Objects.requireNonNull(theme);
        try {
            boardThemePreferences.save(theme);
        } catch (BackingStoreException | SecurityException exception) {
            // Report the save failure after keeping the new palette for this session.
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    "Could not save board colours. Your choice will be used for this session.");
            alert.initOwner(primaryStage);
            alert.showAndWait();
        }
    }

    /**
     * Returns the session's current mode selection, initially Classic.
     *
     * @return the session's current mode selection, initially Classic
     */
    public GameMode getSelectedMode() {
        return selectedMode;
    }

    /**
     * Remembers a mode for subsequent menu loads and game creation.
     *
     * @param mode the mode selected by the player
     * @throws NullPointerException if {@code mode} is null
     */
    public void setSelectedMode(GameMode mode) {
        selectedMode = java.util.Objects.requireNonNull(mode);
    }

    /**
     * Advances the rotation for the next visit, wrapping back to the first message.
     *
     * @return the splash message for the current menu visit
     */
    public String nextMenuSplash() {
        // Read the current tip before moving the pointer forward.
        String tip = MENU_TIPS.get(nextMenuSplashIndex);
        nextMenuSplashIndex = (nextMenuSplashIndex + 1) % MENU_TIPS.size();
        return tip;
    }

    /**
     * Shows the prepared game screen, pauses the soundtrack, and focuses the layout
     * for keyboard input. Initialises the fullscreen layout if the stage is already fullscreen.
     *
     * @param gamePane the prepared gameplay layout
     * @param onLeave clean-up that finalises the active run when navigation or shutdown occurs
     * @param updateFullscreen applies the stage's fullscreen state to the gameplay layout
     */
    public void showGameScreen(Region gamePane, Runnable onLeave, Consumer<Boolean> updateFullscreen) {
        java.util.Objects.requireNonNull(onLeave);
        java.util.Objects.requireNonNull(updateFullscreen);
        if (primaryStage.isFullScreen()) {
            updateFullscreen.accept(true);
        }
        showScreen(gamePane);
        backgroundMusic.pause();
        onLeaveGame = onLeave;
        updateGameFullscreen = updateFullscreen;
        gamePane.requestFocus();
    }

    /**
     * Mounts the screen in the shared scene so navigation preserves native fullscreen.
     *
     * @param content the root of the screen to display
     */
    private void showScreen(Region content) {
        close();
        if (scene == null) {
            scene = new Scene(content);
            scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.F) {
                    primaryStage.setFullScreen(!primaryStage.isFullScreen());
                    event.consume();
                }
            });
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
        } else {
            scene.setRoot(content);
        }
        if (!primaryStage.isFullScreen()) {
            primaryStage.sizeToScene();
        }
        if (!primaryStage.isShowing()) {
            primaryStage.show();
        }
    }

    /** Returns to the main menu and resumes the soundtrack if it was paused for gameplay. */
    public void showMainMenu() {
        initializeMainMenu();
    }

    /**
     * Wraps menu or reference content in a centred, uniformly scaled layout.
     *
     * @param content the FXML root whose preferred size defines its layout
     * @return a styled layout with a preferred windowed size of 550 by 400 pixels
     */
    private StackPane createMenuContent(Region content) {
        content.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        content.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        // Wrap the fixed-size content in a group that scales inside the window-sized container.
        Group scaledContent = new Group(content);
        StackPane container = new StackPane(scaledContent);
        container.setPrefSize(550, 400);
        // Fit both dimensions with one scale to preserve the content's proportions.
        var scale = Bindings.min(container.widthProperty().divide(550), container.heightProperty().divide(400));
        content.scaleXProperty().bind(scale);
        content.scaleYProperty().bind(scale);
        container.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/comp2042/view/styling.css")).toExternalForm());
        return container;
    }

    /**
     * Shows a secondary menu screen, keeps the soundtrack playing, and binds Escape to the main menu.
     *
     * @param content the loaded screen layout
     * @param focusSelector the CSS selector for the control to focus when the screen opens
     */
    private void showMenuScreen(Region content, String focusSelector) {
        // Use the scaled menu wrapper for focus handling and Escape navigation.
        StackPane screen = createMenuContent(content);
        screen.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                showMainMenu();
                event.consume();
            }
        });
        showScreen(screen);
        backgroundMusic.play();
        screen.lookup(focusSelector).requestFocus();
    }

    /**
     * Loads and shows a fresh help screen with Escape-to-menu navigation.
     * Focuses the scroll pane for keyboard access. Logs FXML I/O failures and
     * displays an error alert if loading fails.
     */
    public void showHowToPlay() {
        try {
            // Load the help layout together with its FXML controller.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/comp2042/view/HowToPlay.fxml"));
            showMenuScreen(loader.load(), "#helpScroll");
        } catch (IOException exception) {
            exception.printStackTrace();
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR,
                    "Failed to load How to Play: " + exception.getMessage()
            );
            alert.showAndWait();
        }
    }

    /** Opens the mode-filtered leaderboard at the menu's selected mode. */
    public void showLeaderboards() {
        try {
            // Inject the shared services and selected mode into the leaderboard controller.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/comp2042/view/Leaderboards.fxml"));
            loader.setControllerFactory(type -> new LeaderboardController(leaderboardService, soundService, selectedMode, this::showMainMenu));
            showMenuScreen(loader.load(), "#leaderboardScroll");
        } catch (IOException exception) {
            new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR,
                    "Failed to load leaderboards: " + exception.getMessage()).showAndWait();
        }
    }

    /** Opens an editor with a draft of the currently applied board colours. */
    public void showBoardColours() {
        try {
            // Supply the editor controller with access to the applied palette and navigation.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/comp2042/view/BoardColours.fxml"));
            loader.setControllerFactory(type -> new BoardColoursController(this));
            showMenuScreen(loader.load(), ".radio-button");
        } catch (IOException exception) {
            new Alert(Alert.AlertType.ERROR, "Failed to load Board Colours: " + exception.getMessage()).showAndWait();
        }
    }

    /** Finalises active gameplay once by clearing its callbacks before invoking clean-up. */
    @Override
    public void close() {
        // Keep a copy of the clean-up callback before detaching it from the active screen.
        Runnable cleanup = onLeaveGame;
        onLeaveGame = null;
        updateGameFullscreen = null;
        if (cleanup != null) cleanup.run();
    }
}
