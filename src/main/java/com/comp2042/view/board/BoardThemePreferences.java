package com.comp2042.view.board;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/** Stores the selected preset board palette as a user preference. */
public final class BoardThemePreferences {
    private static final String KEY = "board-colours";

    public BoardTheme load() {
        try {
            return decode(preferences().get(KEY, ""));
        } catch (SecurityException exception) {
            return BoardTheme.SOFT_CREAM;
        }
    }

    public void save(BoardTheme theme) throws BackingStoreException {
        Preferences preferences = preferences();
        preferences.put(KEY, theme.getId());
        preferences.flush();
    }

    private Preferences preferences() {
        return Preferences.userNodeForPackage(BoardThemePreferences.class);
    }

    static BoardTheme decode(String saved) {
        for (BoardTheme preset : BoardTheme.values()) {
            if (preset.getId().equals(saved)) return preset;
        }
        return BoardTheme.SOFT_CREAM;
    }
}
