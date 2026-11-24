package com.comp2042.controller;

import com.comp2042.service.audio.SoundEffect;
import com.comp2042.view.ViewManager;
import com.comp2042.view.board.BoardTheme;
import com.comp2042.view.board.CheckerboardBoardRenderer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.Objects;

/** Previews preset board palettes with gameplay artwork. */
public final class BoardColoursController {
    private static final int TILE_SIZE = 24;

    private final ViewManager viewManager;
    private final ToggleGroup paletteSelection = new ToggleGroup();
    private final CheckerboardBoardRenderer renderer = new CheckerboardBoardRenderer();
    private final Image snakeHead = loadSprite("snake/snakehead.png");
    private final Image snakeBody = loadSprite("snake/snakebody.png");
    private final Image snakeTail = loadSprite("snake/snaketail.png");
    private final Image diamond = loadSprite("coins/diamond.png");

    @FXML private VBox paletteOptions;
    @FXML private Canvas boardPreview;

    public BoardColoursController(ViewManager viewManager) {
        this.viewManager = Objects.requireNonNull(viewManager);
    }

    @FXML
    private void initialize() {
        BoardTheme appliedTheme = viewManager.getBoardTheme();
        for (BoardTheme preset : BoardTheme.values()) {
            RadioButton option = new RadioButton(preset.getDisplayName());
            option.setUserData(preset);
            option.setToggleGroup(paletteSelection);
            paletteOptions.getChildren().add(option);
            if (preset == appliedTheme) paletteSelection.selectToggle(option);
        }
        paletteSelection.selectedToggleProperty().addListener((observable, previous, selected) -> {
            if (selected != null) drawPreview();
        });
        drawPreview();
    }

    private BoardTheme selectedTheme() {
        return (BoardTheme) paletteSelection.getSelectedToggle().getUserData();
    }

    @FXML
    private void handleApply() {
        viewManager.getSoundService().play(SoundEffect.BUTTON);
        viewManager.applyBoardTheme(selectedTheme());
        viewManager.showMainMenu();
    }

    @FXML
    private void handleBack() {
        viewManager.getSoundService().play(SoundEffect.BUTTON);
        viewManager.showMainMenu();
    }

    private void drawPreview() {
        var gc = boardPreview.getGraphicsContext2D();
        int columns = (int) boardPreview.getWidth() / TILE_SIZE;
        int rows = (int) boardPreview.getHeight() / TILE_SIZE;
        renderer.draw(gc, columns, rows, TILE_SIZE, selectedTheme());
        gc.setImageSmoothing(false);
        gc.drawImage(snakeHead, 3 * TILE_SIZE, 3 * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        gc.drawImage(snakeBody, 3 * TILE_SIZE, 4 * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        gc.drawImage(snakeTail, 3 * TILE_SIZE, 5 * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        gc.drawImage(diamond, 5 * TILE_SIZE + 2, 2 * TILE_SIZE + 2, TILE_SIZE - 4, TILE_SIZE - 4);
        gc.setFill(Color.BLACK);
        gc.fillRect(6 * TILE_SIZE, 4 * TILE_SIZE, TILE_SIZE, TILE_SIZE);
    }

    private static Image loadSprite(String path) {
        return new Image(Objects.requireNonNull(BoardColoursController.class.getResourceAsStream(
                "/com/comp2042/images/" + path)));
    }
}
