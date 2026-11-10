package com.comp2042;

import com.comp2042.view.ViewManager;
import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {

    private static ViewManager viewManager;

    @Override
    public void start(Stage primaryStage) {
        // Load the fonts programmatically to bypass the CSS path bug
        Font.loadFont(getClass().getResourceAsStream("/com/comp2042/fonts/SFPixelate.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/com/comp2042/fonts/SFPixelateShaded-Bold.ttf"), 14);

        viewManager = new ViewManager(primaryStage);
        viewManager.initializeMainMenu();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static ViewManager getViewManager() {
        return viewManager;
    }
}
