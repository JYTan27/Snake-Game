package com.comp2042.controller;

import com.comp2042.Main;
import com.comp2042.model.GameMode;
import com.comp2042.service.audio.SoundEffect;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Populates mode explanations on the FXML help screen and handles its Back button.
 * Descriptions come from {@link GameMode} so help text shares gameplay configuration.
 */
public class HowToPlayController {

    /** Container injected from FXML for the generated mode descriptions. */
    @FXML
    private VBox modeDescriptions;

    /** Creates a help controller for FXML loading; descriptions are added after injection. */
    public HowToPlayController() {
    }

    /**
     * Adds each mode's display name and wrapping description after FXML injection.
     */
    @FXML
    private void initialize() {
        // Build one section per mode using the display name and description from the preset.
        for (GameMode mode : GameMode.values()) {
            Label heading = new Label(mode.getDisplayName());
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

    /** Plays button audio and returns to the menu, retaining the session's selected mode. */
    @FXML
    private void handleBack() {
        Main.getViewManager().getSoundService().play(SoundEffect.BUTTON);
        Main.getViewManager().showMainMenu();
    }
}
