package com.comp2042.controller;

import com.comp2042.Main;
import com.comp2042.model.pickup.Pickup;
import com.comp2042.model.pickup.DiamondPickup;
import com.comp2042.model.Game;
import com.comp2042.model.GameMode;
import com.comp2042.model.Obstacle;
import com.comp2042.model.Snake;
import com.comp2042.view.GameView;
import com.comp2042.view.GameModeSelector;
import com.comp2042.view.ViewManager;
import com.comp2042.view.board.CheckerboardBoardRenderer;
import com.comp2042.service.audio.SoundEffect;
import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * Handles the FXML main menu, session mode selection, and game-screen construction.
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
    /** Label for the message selected from the session's splash rotation. */
    @FXML
    private Label menuSplash;

    /** Application-wide navigation, session selection, and audio access. */
    private final ViewManager viewManager = Main.getViewManager();

    /** Creates a menu controller for FXML loading; controls are configured after injection. */
    public MainMenuController() {
    }

    /**
     * Populates the mode selector after FXML injection, restores the session's
     * selection, plays button audio on later mode changes, and configures display
     * names and fullscreen-aware popup sizing. Sets the splash message for this visit.
     */
    @FXML
    private void initialize() {
        GameModeSelector.configure(modeSelector, viewManager.getSelectedMode(), viewManager.getSoundService());
        menuSplash.setText(viewManager.nextMenuSplash());
    }

    /** Remembers the chosen mode and opens a fresh game. */
    public void handleStart() {
        viewManager.getSoundService().play(SoundEffect.BUTTON);
        // Capture the selected mode before replacing the main menu.
        GameMode mode = modeSelector.getValue();
        viewManager.setSelectedMode(mode);
        showNewGame(mode);
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

    /** Opens the board colour editor while retaining the selected game mode. */
    public void handleBoardColours() {
        viewManager.getSoundService().play(SoundEffect.BUTTON);
        viewManager.setSelectedMode(modeSelector.getValue());
        viewManager.showBoardColours();
    }

    /** Requests button audio and exits immediately without waiting for playback to finish. */
    public void handleExit() {
        viewManager.getSoundService().play(SoundEffect.BUTTON);
        Platform.exit();
    }

    /**
     * Builds and displays the gameplay layout for the selected mode.
     *
     * @param mode the rule configuration for the new game
     */
    private void showNewGame(GameMode mode) {
        // Build fresh board state from the configured dimensions and the chosen mode.
        Game game = new Game(X_VALUE, Y_VALUE, mode);
        Obstacle obstacle = new Obstacle(game.getWidth(), game.getHeight());
        Snake snake = new Snake(game.getWidth(), game.getHeight(), game.isNoBorder(), obstacle);
        // Initial games always begin with a diamond; later pickups use mode weights.
        Pickup pickup = new DiamondPickup(game.getWidth(), game.getHeight(), snake, obstacle);

        // Connect the game's state to its renderer, input controller, and shared services.
        GameView gameView = new GameView(game, new CheckerboardBoardRenderer(), viewManager.getBoardTheme());
        GameController controller = new GameController(snake, pickup, game, gameView, obstacle,
                viewManager.getSoundService(), viewManager.getLeaderboardService(), viewManager::showMainMenu);

        // Stack the board canvas with the score and end-of-run messages.
        StackPane gamePane = new StackPane();
        gameView.initialize(snake, pickup);
        gamePane.setPrefSize(gameView.gridCanvas.getWidth(), gameView.gridCanvas.getHeight());
        // Keep windowed sizing tied to the board even when the canvas scales up.
        gamePane.setMinSize(StackPane.USE_PREF_SIZE, StackPane.USE_PREF_SIZE);
        gamePane.setFocusTraversable(true);
        gamePane.setOnKeyPressed(controller::handle);
        gamePane.getChildren().addAll(gameView.gridCanvas, gameView.gameOverLabel, gameView.gameWonLabel, gameView.scoreLabel);
        gamePane.setPadding(new Insets(20));
        StackPane.setAlignment(gameView.gameOverLabel, Pos.TOP_CENTER);
        StackPane.setAlignment(gameView.gameWonLabel, Pos.TOP_CENTER);
        StackPane.setAlignment(gameView.scoreLabel, Pos.BOTTOM_CENTER);
        viewManager.showGameScreen(gamePane, controller::close, controller::updateFullscreenLayout);
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
