package com.comp2042;

import com.comp2042.view.ViewManager;
import com.comp2042.service.audio.JavaFxSoundService;
import com.comp2042.service.audio.SoundService;
import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {

    private static ViewManager viewManager;
    private SoundService sounds;

    @Override
    public void start(Stage primaryStage) {
        // Load the fonts programmatically to bypass the CSS path bug
        Font.loadFont(getClass().getResourceAsStream("/com/comp2042/fonts/SFPixelate.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/com/comp2042/fonts/SFPixelateShaded-Bold.ttf"), 14);

        sounds = new JavaFxSoundService();
        viewManager = new ViewManager(primaryStage, sounds);
        viewManager.initializeMainMenu();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() {
        if (sounds != null) sounds.close();
    }

    public static ViewManager getViewManager() {
        return viewManager;
    }
}
