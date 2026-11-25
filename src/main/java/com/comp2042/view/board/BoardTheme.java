package com.comp2042.view.board;

import javafx.scene.paint.Color;

/** Two-colour presets shared by the gameplay board and the board preview. */
public enum BoardTheme {
    /** Subtle cream palette used by the default checkerboard. */
    SOFT_CREAM("soft-cream", "Soft Cream", Color.web("#FEFFE4"), Color.web("#F2F3DA")),
    /** Light green palette. */
    MINT("mint", "Mint", Color.web("#E9F7E4"), Color.web("#DDEFD7")),
    /** Pale blue palette. */
    SKY("sky", "Sky", Color.web("#EAF5FF"), Color.web("#DAEAF7")),
    /** Soft purple palette. */
    LAVENDER("lavender", "Lavender", Color.web("#F2E9FC"), Color.web("#E6D9F5")),
    /** Warm peach palette. */
    PEACH("peach", "Peach", Color.web("#FCE8D8"), Color.web("#F4D9C4")),
    /** Cool blue-grey palette. */
    SLATE("slate", "Slate", Color.web("#E2E7ED"), Color.web("#CED6DF"));

    /** Stable preference key value independent of the display name. */
    private final String id;
    /** Human-readable palette name. */
    private final String displayName;
    /** Base background colour. */
    private final Color primaryColor;
    /** Contrasting colour used for alternating cells. */
    private final Color secondaryColor;

    /**
     * Defines a named two-colour preset.
     *
     * @param id the stable identifier used in preferences
     * @param displayName the name shown in the palette editor
     * @param primaryColor the base checkerboard colour
     * @param secondaryColor the alternating cell colour
     */
    BoardTheme(String id, String displayName, Color primaryColor, Color secondaryColor) {
        this.id = id;
        this.displayName = displayName;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
    }

    /** {@return the stable identifier stored by {@link BoardThemePreferences}} */
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
