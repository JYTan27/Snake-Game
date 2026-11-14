package com.comp2042.controller;

import com.comp2042.Main;
import javafx.fxml.FXML;

/** Handles navigation from the gameplay reference back to the menu. */
public class HowToPlayController {
    @FXML
    private void handleBack() {
        Main.getViewManager().showMainMenu();
    }
}
