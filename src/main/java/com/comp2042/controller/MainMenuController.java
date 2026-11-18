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

public class MainMenuController {

    private static final int X_VALUE = 20;
    private static final int Y_VALUE = 20;
    @FXML
    private ComboBox<GameMode> modeSelector;
    private final DoubleProperty optionScale = new SimpleDoubleProperty(1);

    private Scene gameScene;

    private ViewManager viewManager = Main.getViewManager();

    @FXML
    private void initialize() {
        modeSelector.getItems().setAll(GameMode.values());
        modeSelector.setConverter(new StringConverter<>() {
            @Override
            public String toString(GameMode mode) {
                if (mode == null) return "";
                return mode.getDisplayName();
            }

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

    public void handleHowToPlay() {
        viewManager.getSoundService().play(SoundEffect.BUTTON);
        viewManager.setSelectedMode(modeSelector.getValue());
        viewManager.showHowToPlay();
    }

    public void handleExit() {
        viewManager.getSoundService().play(SoundEffect.BUTTON);
        Platform.exit();
    }

    private GameController buildGameScene(Stage stage, GameMode mode) {
        Game game = new Game(X_VALUE, Y_VALUE, mode);
        Obstacle obstacle = new Obstacle(game.getWidth(), game.getHeight());
        Snake snake = new Snake(game.getWidth(), game.getHeight(), game.isNoBorder(), obstacle);
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

    public ViewManager getViewManager() {
        return viewManager;
    }

    public static int getxValue() {
        return X_VALUE;
    }

    public static int getyValue() {
        return Y_VALUE;
    }
}
