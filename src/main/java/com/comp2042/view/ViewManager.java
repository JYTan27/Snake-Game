package com.comp2042.view;

import com.comp2042.controller.BoardColoursController;
import com.comp2042.controller.LeaderboardController;
import com.comp2042.model.GameMode;
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
 * Retains the selected mode, exposes application-owned services, and finalises
 * active gameplay when leaving its screen. Scene operations run on the JavaFX thread.
 */
public class ViewManager implements AutoCloseable {

    private static final List<String> MENU_TIPS = List.of(
            "A diamond a day!",
            "Plan your turns!",
            "Impossible to win?",
            "No snakes were harmed!",
            "Certified noodle",
            "Fill every square!"
    );

    /** Single stage reused for every screen. */
    private final Stage primaryStage;
    private Scene scene;
    /** Shared audio service owned and closed by the application. */
    private final SoundService soundService;
    private final LeaderboardService leaderboardService;
    private final BoardThemePreferences boardThemePreferences = new BoardThemePreferences();
    private BoardTheme boardTheme;
    private Runnable onLeaveGame;
    private Consumer<Boolean> updateGameFullscreen;
    /** Session selection restored whenever the main menu is loaded. */
    private GameMode selectedMode = GameMode.CLASSIC;
    private int nextMenuSplashIndex;

    /** Application icon loaded from the packaged image resources. */
    private final Image snakeLogo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/comp2042/images/ui/SnakeLogoWhite.png")));

    /**
     * Connects navigation to the application stage and shared audio service.
     * Disables JavaFX's default fullscreen hint and exit key so screens can
     * assign their own fullscreen and navigation shortcuts.
     *
     * @param primaryStage the single stage used to display all screens
     * @param soundService the application-owned audio service
     * @param leaderboardService application-owned result history
     * @throws NullPointerException if a service argument is null
     */
    public ViewManager(Stage primaryStage, SoundService soundService, LeaderboardService leaderboardService) {
        this.primaryStage = primaryStage;
        this.soundService = java.util.Objects.requireNonNull(soundService);
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
     * Retains the session's selected mode. Logs FXML I/O failures, displays an error
     * alert, and returns without showing a new menu when loading fails.
     */
    public void initializeMainMenu() {
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
    }

    /**
     * Returns the shared application stage.
     *
     * @return the shared application stage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Returns the shared audio service; screens must not close it.
     *
     * @return the shared audio service; screens must not close it
     */
    public SoundService getSoundService() { return soundService; }

    /** Returns shared result history; the application alone owns its lifecycle. */
    public LeaderboardService getLeaderboardService() { return leaderboardService; }

    /** Returns the applied board palette for new games and the colour editor. */
    public BoardTheme getBoardTheme() { return boardTheme; }

    /** Applies the palette for this session and saves it for future launches. */
    public void applyBoardTheme(BoardTheme theme) {
        boardTheme = Objects.requireNonNull(theme);
        try {
            boardThemePreferences.save(theme);
        } catch (BackingStoreException | SecurityException exception) {
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

    /** Returns the tip for a new menu visit and advances the session's rotation. */
    public String nextMenuSplash() {
        String tip = MENU_TIPS.get(nextMenuSplashIndex);
        nextMenuSplashIndex = (nextMenuSplashIndex + 1) % MENU_TIPS.size();
        return tip;
    }

    /**
     * Displays a prepared game screen and focuses it for keyboard input.
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

    /** Reloads and shows the main menu while retaining the session's selected mode. */
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
        Group scaledContent = new Group(content);
        StackPane container = new StackPane(scaledContent);
        container.setPrefSize(550, 400);
        // Fit both dimensions with one scale to preserve the content's proportions.
        var scale = Bindings.min(container.widthProperty().divide(550), container.heightProperty().divide(400));
        content.scaleXProperty().bind(scale);
        content.scaleYProperty().bind(scale);
        container.getStylesheets().add(getClass().getResource("/com/comp2042/view/styling.css").toExternalForm());
        return container;
    }

    private void showMenuScreen(Region content, String focusSelector) {
        StackPane screen = createMenuContent(content);
        screen.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                showMainMenu();
                event.consume();
            }
        });
        showScreen(screen);
        screen.lookup(focusSelector).requestFocus();
    }

    /**
     * Loads and shows a fresh help screen with Escape-to-menu navigation.
     * Focuses the scroll pane for keyboard access. Logs FXML I/O failures and
     * displays an error alert if loading fails.
     */
    public void showHowToPlay() {
        try {
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/comp2042/view/BoardColours.fxml"));
            loader.setControllerFactory(type -> new BoardColoursController(this));
            showMenuScreen(loader.load(), ".radio-button");
        } catch (IOException exception) {
            new Alert(Alert.AlertType.ERROR, "Failed to load Board Colours: " + exception.getMessage()).showAndWait();
        }
    }

    /** Detaches before invoking clean-up so reentrant or repeated navigation is safe. */
    @Override
    public void close() {
        Runnable cleanup = onLeaveGame;
        onLeaveGame = null;
        updateGameFullscreen = null;
        if (cleanup != null) cleanup.run();
    }
}
