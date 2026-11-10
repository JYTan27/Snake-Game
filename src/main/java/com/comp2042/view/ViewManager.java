package com.comp2042.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class ViewManager {

    private Stage primaryStage;
    private AnchorPane root;

    private Image snakeLogo = new Image(getClass().getResourceAsStream("/com/comp2042/images/ui/SnakeLogoWhite.png"));

    public ViewManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
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

        Scene mainScene = new Scene(root);

        primaryStage.setScene(mainScene);
        primaryStage.setResizable(false);
        if (primaryStage.getIcons().isEmpty()) {
            primaryStage.getIcons().add(snakeLogo);
        }
        primaryStage.setTitle("Snake Game");
        primaryStage.show();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void showGameScene(Scene gameScene) {
        primaryStage.setScene(gameScene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public void showMainMenu() {
        initializeMainMenu();
    }
}
