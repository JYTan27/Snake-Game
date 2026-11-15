package com.comp2042.view.board;

import javafx.scene.canvas.GraphicsContext;

/** A low-contrast checkerboard with shared pixel boundaries and no grid outlines. */
public final class CheckerboardBoardRenderer implements BoardRenderer {
    @Override
    public void draw(GraphicsContext gc, int columns, int rows, double cellSize, BoardTheme theme) {
        gc.save();
        try {
            gc.setFill(theme.getPrimaryColor());
            gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
            gc.setFill(theme.getSecondaryColor());
            for (int row = 0; row < rows; row++) {
                double top = Math.round(row * cellSize);
                double bottom = Math.round((row + 1) * cellSize);
                for (int column = 0; column < columns; column++) {
                    if ((column + row) % 2 == 0) continue;
                    double left = Math.round(column * cellSize);
                    double right = Math.round((column + 1) * cellSize);
                    gc.fillRect(left, top, right - left, bottom - top);
                }
            }
        } finally {
            gc.restore();
        }
    }
}
