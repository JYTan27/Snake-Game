package com.comp2042.view.board;

import javafx.scene.paint.Color;

/** Preset colour palettes shared by board-pattern renderers. */
public enum BoardTheme {
    /** Subtle cream palette used by the default checkerboard. */
    SOFT_CREAM("soft-cream", "Soft Cream", Color.web("#FEFFE4"), Color.web("#F2F3DA")),
    MINT("mint", "Mint", Color.web("#E9F7E4"), Color.web("#DDEFD7")),
    SKY("sky", "Sky", Color.web("#EAF5FF"), Color.web("#DAEAF7"));

    private final String id;
    /** Human-readable palette name. */
    private final String displayName;
    /** Base background colour. */
    private final Color primaryColor;
    /** Contrasting colour used for alternating cells. */
    private final Color secondaryColor;

    BoardTheme(String id, String displayName, Color primaryColor, Color secondaryColor) {
        this.id = id;
        this.displayName = displayName;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
    }

    String getId() { return id; }

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
