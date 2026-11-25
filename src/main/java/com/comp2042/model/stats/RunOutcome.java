package com.comp2042.model.stats;

/** The reason a started run ended. */
public enum RunOutcome {
    /** The snake filled the available board. */
    WIN("Win"),
    /** The snake died in a collision. */
    LOSS("Loss"),
    /** The player restarted, left the game, or closed the app before a win or loss. */
    ABANDONED("Abandoned");

    /** Human-readable outcome label used in rankings. */
    private final String displayName;

    /**
     * Associates a display label with the outcome.
     *
     * @param displayName the text shown on the leaderboard
     */
    RunOutcome(String displayName) { this.displayName = displayName; }

    /** {@return the outcome label shown on the leaderboard} */
    public String getDisplayName() { return displayName; }
}
