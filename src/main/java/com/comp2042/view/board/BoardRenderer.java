package com.comp2042.view.board;

import javafx.scene.canvas.GraphicsContext;

/** Draws a board background independently of gameplay entities and colour themes. */
public interface BoardRenderer {
    /** Implementations must restore any graphics state they change. */
    void draw(GraphicsContext gc, int columns, int rows, double cellSize, BoardTheme theme);
}
