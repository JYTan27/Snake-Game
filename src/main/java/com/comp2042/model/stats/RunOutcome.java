package com.comp2042.model.stats;

/** The reason a started run ended. */
public enum RunOutcome {
    WIN("Win"), LOSS("Loss"), ABANDONED("Abandoned");

    private final String displayName;

    RunOutcome(String displayName) { this.displayName = displayName; }

    public String getDisplayName() { return displayName; }
}
