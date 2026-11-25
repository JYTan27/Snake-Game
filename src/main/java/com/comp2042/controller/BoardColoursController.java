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

/**
 * Previews the board palettes defined in {@link BoardTheme} with gameplay artwork.
 * Selection changes redraw the preview. Apply commits the selected palette to the game.
 */
public final class BoardColoursController {

    /** Preview tile size in pixels. */
    private static final int TILE_SIZE = 24;

    /** Shared navigation and applied palette for the editor. */
    private final ViewManager viewManager;
    /** Mutually exclusive selection of the draft palette. */
    private final ToggleGroup paletteSelection = new ToggleGroup();
    /** Draws the same checkerboard pattern used in gameplay. */
    private final CheckerboardBoardRenderer renderer = new CheckerboardBoardRenderer();
    /** Sprite for the preview snake's head. */
    private final Image snakeHead = loadSprite("snake/snakehead.png");
    /** Sprite for each straight segment of the preview snake. */
    private final Image snakeBody = loadSprite("snake/snakebody.png");
    /** Sprite for the preview snake's tail. */
    private final Image snakeTail = loadSprite("snake/snaketail.png");
    /** Pickup sprite shown on the preview board. */
    private final Image diamond = loadSprite("pickups/diamond.png");

    /** FXML container populated with one radio button per preset. */
    @FXML private VBox paletteOptions;
    /** FXML canvas for the checkerboard and sample sprites. */
    @FXML private Canvas boardPreview;

    /**
     * Creates the board colours controller before FXML injects its controls.
     *
     * @param viewManager the shared source of the applied palette and menu navigation
     * @throws NullPointerException if {@code viewManager} is null
     */
    public BoardColoursController(ViewManager viewManager) {
        this.viewManager = Objects.requireNonNull(viewManager);
    }

    /** Creates one radio button per preset and previews the current palette. */
    @FXML
    private void initialize() {
        // Open the editor with the applied palette selected as the draft.
        BoardTheme appliedTheme = viewManager.getBoardTheme();
        // Build the choices in preset order using each preset's display name.
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

    /** {@return the preset associated with the selected radio button} */
    private BoardTheme selectedTheme() {
        return (BoardTheme) paletteSelection.getSelectedToggle().getUserData();
    }

    /** Applies the selected preset and returns to the main menu. */
    @FXML
    private void handleApply() {
        viewManager.getSoundService().play(SoundEffect.BUTTON);
        viewManager.applyBoardTheme(selectedTheme());
        viewManager.showMainMenu();
    }

    /** Returns to the main menu and discards the preview selection. */
    @FXML
    private void handleBack() {
        viewManager.getSoundService().play(SoundEffect.BUTTON);
        viewManager.showMainMenu();
    }

    /** Draws the selected checkerboard with a sample snake, diamond, and obstacle. */
    private void drawPreview() {
        // Derive the preview grid from the canvas dimensions and the fixed tile size.
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

    /**
     * Loads a preview sprite from the application's image resources.
     *
     * @param path the resource path relative to the image directory
     * @return the decoded sprite
     * @throws NullPointerException if the sprite resource is missing
     */
    private static Image loadSprite(String path) {
        return new Image(Objects.requireNonNull(BoardColoursController.class.getResourceAsStream(
                "/com/comp2042/images/" + path)));
    }
}
