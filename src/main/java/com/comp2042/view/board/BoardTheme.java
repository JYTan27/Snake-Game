package com.comp2042.view.board;

import javafx.scene.paint.Color;

/** Colour palettes shared by board-pattern renderers. */
public enum BoardTheme {
    /** Subtle cream palette used by the default checkerboard. */
    SOFT_CREAM("Soft Cream", Color.web("#FEFFE4"), Color.web("#F2F3DA"));

    /** Human-readable palette name. */
    private final String displayName;
    /** Base background colour. */
    private final Color primaryColor;
    /** Contrasting colour used for alternating cells. */
    private final Color secondaryColor;

    /**
     * Defines a named pair of colours for board renderers.
     *
     * @param displayName the palette's visible name
     * @param primaryColor the base background colour
     * @param secondaryColor the alternating-cell colour
     */
    BoardTheme(String displayName, Color primaryColor, Color secondaryColor) {
        this.displayName = displayName;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
    }

    /**
     * Returns the palette's human-readable name.
     *
     * @return the palette's human-readable name
     */
    public String getDisplayName() { return displayName; }
    /**
     * Returns the base background colour.
     *
     * @return the base background colour
     */
    public Color getPrimaryColor() { return primaryColor; }
    /**
     * Returns the alternating-cell colour.
     *
     * @return the alternating-cell colour
     */
    public Color getSecondaryColor() { return secondaryColor; }
}
