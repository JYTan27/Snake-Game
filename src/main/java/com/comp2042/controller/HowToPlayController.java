package com.comp2042.controller;

import com.comp2042.Main;
import com.comp2042.model.GameMode;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/** Handles navigation from the gameplay reference back to the menu. */
public class HowToPlayController {
    @FXML
    private VBox modeDescriptions;

    @FXML
    private void initialize() {
        for (GameMode mode : GameMode.values()) {
            Label heading = new Label(mode.getDisplayName() + " - " + (int) mode.getSpeed() + " moves/sec");
            heading.getStyleClass().add("helpModeTitle");
            heading.setWrapText(true);
            heading.setMinWidth(0);
            Label description = new Label(mode.getDescription());
            description.setWrapText(true);
            description.setMinWidth(0);
            description.setMaxWidth(Double.MAX_VALUE);
            modeDescriptions.getChildren().add(new VBox(6, heading, description));
        }
    }

    @FXML
    private void handleBack() {
        Main.getViewManager().showMainMenu();
    }
}
