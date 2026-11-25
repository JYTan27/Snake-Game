package com.comp2042.view.board;

import javafx.scene.canvas.GraphicsContext;

/** A low-contrast checkerboard with shared pixel boundaries and no grid outlines. */
public final class CheckerboardBoardRenderer implements BoardRenderer {

    /** Creates a checkerboard renderer that uses the palette supplied to each draw call. */
    public CheckerboardBoardRenderer() {}

    /**
     * Fills the canvas with the primary colour and draws alternating grid cells
     * in the secondary colour. Rounds shared cell boundaries to avoid gaps at
     * fractional display scales, restoring graphics state even if drawing fails.
     *
     * @param gc the canvas drawing context
     * @param columns the number of grid columns
     * @param rows the number of grid rows
     * @param cellSize the scaled cell size in pixels
     * @param theme the palette supplying the two checkerboard colours
     */
    @Override
    public void draw(GraphicsContext gc, int columns, int rows, double cellSize, BoardTheme theme) {
        gc.save();
        try {
            gc.setFill(theme.getPrimaryColor());
            gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
            gc.setFill(theme.getSecondaryColor());
            // Round both edges of each row so neighbouring cells share the same pixel boundary.
            for (int row = 0; row < rows; row++) {
                double top = Math.round(row * cellSize);
                double bottom = Math.round((row + 1) * cellSize);
                // Alternate the fill by row and column, rounding horizontal edges the same way.
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
