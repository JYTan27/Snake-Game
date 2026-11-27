package com.comp2042.view.board;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/** Stores the selected preset board palette as a user preference. */
public final class BoardThemePreferences {

    /** Preference key containing the selected preset's stable ID. */
    private static final String KEY = "board-colours";

    /** Creates a preference store for the current user's board palette. */
    public BoardThemePreferences() {}

    /**
     * Reads the current user's saved palette.
     *
     * @return the saved preset, or {@link BoardTheme#SOFT_CREAM} if the preference
     *         is absent, unrecognised, or inaccessible
     */
    public BoardTheme load() {
        try {
            return decode(preferences().get(KEY, ""));
        } catch (SecurityException exception) {
            return BoardTheme.SOFT_CREAM;
        }
    }

    /**
     * Writes the preset's stable ID to the current user's preferences for future launches.
     *
     * @param theme the palette to save
     * @throws BackingStoreException if the preference cannot be flushed to storage
     * @throws SecurityException if access to the user preference node is denied
     */
    public void save(BoardTheme theme) throws BackingStoreException {
        // Write to the same user preference node used when loading the preset.
        Preferences preferences = preferences();
        preferences.put(KEY, theme.getId());
        preferences.flush();
    }

    /** {@return the current user's preference node for board appearance} */
    private Preferences preferences() {
        return Preferences.userNodeForPackage(BoardThemePreferences.class);
    }

    /**
     * Resolves the stored ID to a preset.
     *
     * @param saved the saved palette identifier
     * @return the matching preset or Soft Cream for an unknown identifier
     */
    static BoardTheme decode(String saved) {
        // Match by stable ID so changing the visible label preserves saved choices.
        for (BoardTheme preset : BoardTheme.values()) {
            if (preset.getId().equals(saved)) return preset;
        }
        return BoardTheme.SOFT_CREAM;
    }
}
