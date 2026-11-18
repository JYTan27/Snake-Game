package com.comp2042.view.board;

import javafx.scene.canvas.GraphicsContext;

/** Draws a board background independently of gameplay entities and colour themes. */
public interface BoardRenderer {
    /**
     * Draws a board background using the supplied grid geometry and palette.
     * Implementations must restore any graphics state they change so subsequent
     * entity rendering can use the same context.
     *
     * @param gc the canvas drawing context
     * @param columns the board width in grid cells
     * @param rows the board height in grid cells
     * @param cellSize the scaled width and height of each cell in pixels
     * @param theme the colour palette for the background
     */
    void draw(GraphicsContext gc, int columns, int rows, double cellSize, BoardTheme theme);
}
