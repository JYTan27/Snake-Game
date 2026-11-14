package com.comp2042.controller;

import com.comp2042.Main;
import com.comp2042.model.Coin;
import com.comp2042.model.Game;
import com.comp2042.model.Obstacle;
import com.comp2042.model.Snake;
import com.comp2042.view.GameView;
import com.comp2042.view.ViewManager;
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
    private static final boolean POWER_UP = true;
    @FXML
    private ComboBox<Game.Difficulty> difficultySelector;
    private final DoubleProperty optionScale = new SimpleDoubleProperty(1);

    private Scene gameScene;

    private ViewManager viewManager = Main.getViewManager();

    @FXML
    private void initialize() {
        difficultySelector.getItems().setAll(Game.Difficulty.values());
        difficultySelector.setConverter(new StringConverter<>() {
            @Override
            public String toString(Game.Difficulty difficulty) {
                if (difficulty == null) return "";
                return switch (difficulty) {
                    case EASY -> "Easy";
                    case NORMAL -> "Normal";
                    case HARD -> "Hard";
                };
            }

            @Override
            public Game.Difficulty fromString(String text) {
                return Game.Difficulty.valueOf(text.toUpperCase(java.util.Locale.ROOT));
            }
        });
        difficultySelector.setValue(viewManager.getSelectedDifficulty());
        difficultySelector.setCellFactory(list -> {
            ListCell<Game.Difficulty> cell = new ListCell<>() {
                @Override
                protected void updateItem(Game.Difficulty difficulty, boolean empty) {
                    super.updateItem(difficulty, empty);
                    setText(empty || difficulty == null ? null : difficultySelector.getConverter().toString(difficulty));
                }
            };
            cell.styleProperty().bind(Bindings.concat("-fx-font-size: ", optionScale.multiply(18),
                    "px; -fx-padding: ", optionScale.multiply(5), " ", optionScale.multiply(10), ";"));
            return cell;
        });
        // The dropdown popup is a separate window and does not inherit the menu transform.
        difficultySelector.setOnShowing(event -> {
            difficultySelector.applyCss();
            double scale = difficultySelector.localToScreen(difficultySelector.getBoundsInLocal()).getWidth()
                    / difficultySelector.getWidth();
            ListView<?> options = (ListView<?>) ((ComboBoxListViewSkin<?>) difficultySelector.getSkin()).getPopupContent();
            optionScale.set(scale);
            options.setFixedCellSize(32 * scale);
            options.setPrefWidth(difficultySelector.getWidth() * scale);
        });
    }

    public void handleStart() {
        boolean fullScreen = viewManager.getPrimaryStage().isFullScreen();
        Game.Difficulty difficulty = difficultySelector.getValue();
        viewManager.setSelectedDifficulty(difficulty);
        GameController controller = buildGameScene(viewManager.getPrimaryStage(), difficulty);
        viewManager.showGameScene(gameScene);
        if (fullScreen) {
            controller.setFullScreen(true);
        }
    }

    public void handleHowToPlay() {
        viewManager.setSelectedDifficulty(difficultySelector.getValue());
        viewManager.showHowToPlay();
    }

    public void handleExit() {
        Platform.exit();
    }

    private GameController buildGameScene(Stage stage, Game.Difficulty difficulty) {
        Game game = new Game(X_VALUE, Y_VALUE, POWER_UP, difficulty);
        Obstacle obstacle = new Obstacle(game.getWidth(), game.getHeight());
        Snake snake = new Snake(game.getWidth(), game.getHeight(), game.isNoBorder(), obstacle);
        Coin coin = new Coin(game.getWidth(), game.getHeight(), snake, obstacle);

        GameView gameView = new GameView(game);
        GameController controller = new GameController(snake, coin, game, gameView, obstacle, stage, () -> viewManager.showMainMenu());

        StackPane gamePane = new StackPane();
        gameScene = new Scene(gamePane, game.getWidth() * game.getMultiplier(), game.getHeight() * game.getMultiplier());

        gameScene.setOnKeyPressed(controller::handle);

        gameView.makeScene(gameScene, snake, coin);
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
