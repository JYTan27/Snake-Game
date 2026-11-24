package com.comp2042.controller;

import com.comp2042.Main;
import com.comp2042.model.Pickup;
import com.comp2042.model.DiamondPickup;
import com.comp2042.model.Game;
import com.comp2042.model.GameMode;
import com.comp2042.model.Obstacle;
import com.comp2042.model.Snake;
import com.comp2042.view.GameView;
import com.comp2042.view.GameModeSelector;
import com.comp2042.view.ViewManager;
import com.comp2042.service.audio.SoundEffect;
import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Handles the FXML main menu, session mode selection, and game-scene construction.
 * Uses the application's shared view manager and sound service. FXML initialisation
 * and button callbacks run on the JavaFX application thread.
 */
public class MainMenuController {

    /** Width of newly created game boards in grid cells. */
    private static final int X_VALUE = 20;
    /** Height of newly created game boards in grid cells. */
    private static final int Y_VALUE = 20;
    /** Mode selector injected from the main-menu FXML. */
    @FXML
    private ComboBox<GameMode> modeSelector;

    /** Most recently constructed game scene, ready to be shown by the view manager. */
    private Scene gameScene;

    /** Application-wide navigation, session selection, stage, and audio access. */
    private ViewManager viewManager = Main.getViewManager();

    /** Creates a menu controller for FXML loading; controls are configured after injection. */
    public MainMenuController() {
    }

    /**
     * Populates the mode selector after FXML injection, restores the session's
     * selection, plays button audio on later mode changes, and configures display
     * names and fullscreen-aware popup sizing.
     */
    @FXML
    private void initialize() {
        GameModeSelector.configure(modeSelector, viewManager.getSelectedMode(), viewManager.getSoundService());
    }

    /**
     * Remembers the chosen mode, constructs and shows a fresh game, and applies
     * fullscreen board scaling if the menu was already fullscreen.
     */
    public void handleStart() {
        viewManager.getSoundService().play(SoundEffect.BUTTON);
        boolean fullScreen = viewManager.getPrimaryStage().isFullScreen();
        GameMode mode = modeSelector.getValue();
        viewManager.setSelectedMode(mode);
        GameController controller = buildGameScene(viewManager.getPrimaryStage(), mode);
        viewManager.showGameScene(gameScene, controller::close);
        if (fullScreen) {
            controller.setFullScreen(true);
        }
    }

    /** Plays button audio, remembers the current mode, and opens the help screen. */
    public void handleHowToPlay() {
        viewManager.getSoundService().play(SoundEffect.BUTTON);
        viewManager.setSelectedMode(modeSelector.getValue());
        viewManager.showHowToPlay();
    }

    /** Opens the shared leaderboard screen while retaining the selected game mode. */
    public void handleLeaderboards() {
        viewManager.getSoundService().play(SoundEffect.BUTTON);
        viewManager.setSelectedMode(modeSelector.getValue());
        viewManager.showLeaderboards();
    }

    /** Requests button audio and exits immediately without waiting for playback to finish. */
    public void handleExit() {
        viewManager.getSoundService().play(SoundEffect.BUTTON);
        Platform.exit();
    }

    /**
     * Creates the model entities, view, controller, layout, and keyboard handler
     * for a fresh game. Stores the resulting scene without displaying it.
     *
     * @param stage the application stage used by the game controller
     * @param mode the rule configuration for the new game
     * @return the controller used to apply fullscreen settings after scene navigation
     */
    private GameController buildGameScene(Stage stage, GameMode mode) {
        Game game = new Game(X_VALUE, Y_VALUE, mode);
        Obstacle obstacle = new Obstacle(game.getWidth(), game.getHeight());
        Snake snake = new Snake(game.getWidth(), game.getHeight(), game.isNoBorder(), obstacle);
        // Initial games always begin with a diamond; later pickups use mode weights.
        Pickup pickup = new DiamondPickup(game.getWidth(), game.getHeight(), snake, obstacle);

        GameView gameView = new GameView(game);
        GameController controller = new GameController(snake, pickup, game, gameView, obstacle, stage,
                viewManager.getSoundService(), viewManager.getLeaderboardService(), viewManager::showMainMenu);

        StackPane gamePane = new StackPane();
        gameScene = new Scene(gamePane, game.getWidth() * game.getMultiplier(), game.getHeight() * game.getMultiplier());

        gameScene.setOnKeyPressed(controller::handle);

        gameView.makeScene(gameScene, snake, pickup);
        gamePane.getChildren().addAll(gameView.gridCanvas, gameView.gameOverLabel, gameView.gameWonLabel, gameView.scoreLabel);
        gamePane.setPadding(new Insets(20));
        StackPane.setAlignment(gameView.gameOverLabel, Pos.TOP_CENTER);
        StackPane.setAlignment(gameView.gameWonLabel, Pos.TOP_CENTER);
        StackPane.setAlignment(gameView.scoreLabel, Pos.BOTTOM_CENTER);
        return controller;
    }

    /**
     * Returns the shared application view manager.
     *
     * @return the shared application view manager
     */
    public ViewManager getViewManager() {
        return viewManager;
    }

    /**
     * Returns the configured board width in grid cells.
     *
     * @return the configured board width in grid cells
     */
    public static int getxValue() {
        return X_VALUE;
    }

    /**
     * Returns the configured board height in grid cells.
     *
     * @return the configured board height in grid cells
     */
    public static int getyValue() {
        return Y_VALUE;
    }
}
