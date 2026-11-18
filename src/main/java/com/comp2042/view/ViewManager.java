package com.comp2042.view;

import com.comp2042.model.GameMode;
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

public class ViewManager {

    private Stage primaryStage;
    private AnchorPane root;
    private GameMode selectedMode = GameMode.CLASSIC;

    private Image snakeLogo = new Image(getClass().getResourceAsStream("/com/comp2042/images/ui/SnakeLogoWhite.png"));

    public ViewManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
    }

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

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public GameMode getSelectedMode() {
        return selectedMode;
    }

    public void setSelectedMode(GameMode mode) {
        selectedMode = java.util.Objects.requireNonNull(mode);
    }

    public void showGameScene(Scene gameScene) {
        showScene(gameScene);
    }

    private void showScene(Scene scene) {
        boolean fullScreen = primaryStage.isFullScreen();
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        if (fullScreen) {
            primaryStage.setFullScreen(true);
        }
    }

    public void showMainMenu() {
        initializeMainMenu();
    }

    /** Scales the complete menu uniformly to fit the available scene, then centres it. */
    private Scene createMenuScene(Region content) {
        content.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        content.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        Group scaledContent = new Group(content);
        StackPane container = new StackPane(scaledContent);
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
