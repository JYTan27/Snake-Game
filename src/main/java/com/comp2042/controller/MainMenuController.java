package com.comp2042.controller;

import com.comp2042.Main;
import com.comp2042.model.Pickup;
import com.comp2042.model.DiamondPickup;
import com.comp2042.model.Game;
import com.comp2042.model.GameMode;
import com.comp2042.model.Obstacle;
import com.comp2042.model.Snake;
import com.comp2042.view.GameView;
import com.comp2042.view.ViewManager;
import com.comp2042.service.audio.SoundEffect;
import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

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
    /** Scale applied to popup cells, which do not inherit the menu's transform. */
    private final DoubleProperty optionScale = new SimpleDoubleProperty(1);

    /** Most recently constructed game scene, ready to be shown by the view manager. */
    private Scene gameScene;

    /** Application-wide navigation, session selection, stage, and audio access. */
    private ViewManager viewManager = Main.getViewManager();

    /** Creates a menu controller for FXML loading; controls are configured after injection. */
    public MainMenuController() {
    }

    /**
     * Populates the mode selector after FXML injection, restores the session's
     * selection, and configures display names and fullscreen-aware popup sizing.
     */
    @FXML
    private void initialize() {
        modeSelector.getItems().setAll(GameMode.values());
        modeSelector.setConverter(new StringConverter<>() {
            /**
             * Converts a mode to its menu label.
             *
             * @param mode the mode, or {@code null} for an empty label
             * @return the display name or an empty string
             */
            @Override
            public String toString(GameMode mode) {
                if (mode == null) return "";
                return mode.getDisplayName();
            }

            /**
             * Resolves a display name without case sensitivity.
             *
             * @param text the mode's display name
             * @return the matching mode
             * @throws IllegalArgumentException if the text matches no mode
             */
            @Override
            public GameMode fromString(String text) {
                return java.util.Arrays.stream(GameMode.values())
                        .filter(mode -> mode.getDisplayName().equalsIgnoreCase(text))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Unknown game mode: " + text));
            }
        });
        modeSelector.setValue(viewManager.getSelectedMode());
        modeSelector.setCellFactory(list -> {
            ListCell<GameMode> cell = new ListCell<>() {
                /**
                 * Refreshes the popup cell, clearing text for empty entries.
                 *
                 * @param mode the mode assigned to this cell, possibly {@code null}
                 * @param empty whether the cell has no item to display
                 */
                @Override
                protected void updateItem(GameMode mode, boolean empty) {
                    super.updateItem(mode, empty);
                    setText(empty || mode == null ? null : modeSelector.getConverter().toString(mode));
                }
            };
            cell.styleProperty().bind(Bindings.concat("-fx-font-size: ", optionScale.multiply(18),
                    "px; -fx-padding: ", optionScale.multiply(5), " ", optionScale.multiply(10), ";"));
            return cell;
        });
        // The dropdown popup is a separate window and does not inherit the menu transform.
        modeSelector.setOnShowing(event -> {
            modeSelector.applyCss();
            double scale = modeSelector.localToScreen(modeSelector.getBoundsInLocal()).getWidth()
                    / modeSelector.getWidth();
            ListView<?> options = (ListView<?>) ((ComboBoxListViewSkin<?>) modeSelector.getSkin()).getPopupContent();
            optionScale.set(scale);
            options.setFixedCellSize(32 * scale);
            options.setPrefWidth(modeSelector.getWidth() * scale);
        });
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
        viewManager.showGameScene(gameScene);
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
        GameController controller = new GameController(snake, pickup, game, gameView, obstacle, stage, viewManager.getSoundService(), () -> viewManager.showMainMenu());

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
