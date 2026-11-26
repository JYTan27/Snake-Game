package com.comp2042.view.board;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BoardThemePreferencesTest {

    @ParameterizedTest
    @CsvSource({
        "soft-cream, SOFT_CREAM",
        "mint, MINT",
        "sky, SKY",
        "lavender, LAVENDER",
        "peach, PEACH",
        "slate, SLATE"
    })
    void existingSavedIdsStillResolveToTheirPreset(String savedId, BoardTheme expected) {
        assertEquals(expected, BoardThemePreferences.decode(savedId));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "retired-palette")
    void missingOrUnknownIdsFallBackToSoftCream(String savedId) {
        assertEquals(BoardTheme.SOFT_CREAM, BoardThemePreferences.decode(savedId));
    }
}
