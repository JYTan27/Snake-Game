package com.comp2042.view;

import com.comp2042.model.GameMode;
import com.comp2042.service.audio.SoundService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.beans.binding.Bindings;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Navigates between menu, help, and gameplay scenes on a single application stage.
 * Retains the selected mode for the session and exposes the application-owned
 * sound service. Scene operations run on the JavaFX application thread; callers
 * remain responsible for stopping gameplay animation and audio when leaving a game.
 */
public class ViewManager {

    /** Single stage reused for every screen. */
    private Stage primaryStage;
    /** Shared audio service owned and closed by the application. */
    private final SoundService soundService;
    /** Root loaded for the most recently created main-menu scene. */
    private AnchorPane root;
    /** Session selection restored whenever the main menu is loaded. */
    private GameMode selectedMode = GameMode.CLASSIC;

    /** Application icon loaded from the packaged image resources. */
    private Image snakeLogo = new Image(getClass().getResourceAsStream("/com/comp2042/images/ui/SnakeLogoWhite.png"));

    /**
     * Connects navigation to the application stage and shared audio service.
     * Disables JavaFX's default fullscreen hint and exit key so screens can
     * assign their own fullscreen and navigation shortcuts.
     *
     * @param primaryStage the single stage used to display all scenes
     * @param soundService the application-owned audio service
     * @throws NullPointerException if either argument is null
     */
    public ViewManager(Stage primaryStage, SoundService soundService) {
        this.primaryStage = primaryStage;
        this.soundService = java.util.Objects.requireNonNull(soundService);
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
    }

    /**
     * Loads a fresh main-menu layout, applies the window title and icon, and shows it.
     * Retains the session's selected mode. Logs FXML I/O failures, displays an error
     * alert, and returns without showing a new menu when loading fails.
     */
    public void initializeMainMenu() {
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

        Scene mainScene = createMenuScene(root);

        if (primaryStage.getIcons().isEmpty()) {
            primaryStage.getIcons().add(snakeLogo);
        }
        primaryStage.setTitle("Snake Game");
        showScene(mainScene);
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
     * Displays an already constructed game scene on the shared stage.
     * Gameplay layout and fullscreen board scaling are configured by its callers.
     *
     * @param gameScene the prepared gameplay scene
     */
    public void showGameScene(Scene gameScene) {
        showScene(gameScene);
    }

    /**
     * Replaces the stage's scene, disables manual window resizing, and shows it.
     * Reapplies fullscreen if it was active before the scene switch.
     *
     * @param scene the scene to display
     */
    private void showScene(Scene scene) {
        boolean fullScreen = primaryStage.isFullScreen();
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        if (fullScreen) {
            primaryStage.setFullScreen(true);
        }
    }

    /** Reloads and shows the main menu while retaining the session's selected mode. */
    public void showMainMenu() {
        initializeMainMenu();
    }

    /**
     * Wraps menu or help content in a centred, uniformly scaled 550-by-400 scene.
     * Applies shared styling and installs F as the fullscreen toggle.
     *
     * @param content the FXML root whose preferred size defines its layout
     * @return a scene with proportional content scaling and fullscreen handling
     */
    private Scene createMenuScene(Region content) {
        content.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        content.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        Group scaledContent = new Group(content);
        StackPane container = new StackPane(scaledContent);
        // Fit both scene dimensions using one scale to preserve the content's proportions.
        var scale = Bindings.min(container.widthProperty().divide(550), container.heightProperty().divide(400));
        content.scaleXProperty().bind(scale);
        content.scaleYProperty().bind(scale);
        container.getStylesheets().add(getClass().getResource("/com/comp2042/view/styling.css").toExternalForm());
        Scene scene = new Scene(container, 550, 400);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.F) {
                primaryStage.setFullScreen(!primaryStage.isFullScreen());
                event.consume();
            }
        });
        return scene;
    }

    /**
     * Loads and shows a fresh help screen with Escape-to-menu navigation.
     * Focuses the scroll pane for keyboard access. Logs FXML I/O failures and
     * displays an error alert if loading fails.
     */
    public void showHowToPlay() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/comp2042/view/HowToPlay.fxml"));
            Scene helpScene = createMenuScene(loader.load());
            helpScene.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    showMainMenu();
                    event.consume();
                }
            });
            showScene(helpScene);
            helpScene.lookup("#helpScroll").requestFocus();
        } catch (IOException exception) {
            exception.printStackTrace();
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR,
                    "Failed to load How to Play: " + exception.getMessage()
            );
            alert.showAndWait();
        }
    }
}
