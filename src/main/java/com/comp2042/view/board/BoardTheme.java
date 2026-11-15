package com.comp2042.view.board;

import javafx.scene.paint.Color;

/** Colour palettes shared by board-pattern renderers. */
public enum BoardTheme {
    SOFT_CREAM("Soft Cream", Color.web("#FEFFE4"), Color.web("#F2F3DA"));

    private final String displayName;
    private final Color primaryColor;
    private final Color secondaryColor;

    BoardTheme(String displayName, Color primaryColor, Color secondaryColor) {
        this.displayName = displayName;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
    }

    public String getDisplayName() { return displayName; }
    public Color getPrimaryColor() { return primaryColor; }
    public Color getSecondaryColor() { return secondaryColor; }
}
