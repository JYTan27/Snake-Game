package com.comp2042.model.stats;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Immutable result. A stable mode ID keeps storage independent of menu labels. */
public record RunResult(UUID id, String modeId, int score, RunOutcome outcome,
                        long activeTimeMillis, int snakeLength, Instant recordedAt) {
    public RunResult {
        Objects.requireNonNull(id);
        Objects.requireNonNull(modeId);
        Objects.requireNonNull(outcome);
        Objects.requireNonNull(recordedAt);
        if (modeId.isBlank() || score < 0 || activeTimeMillis < 0 || snakeLength < 1) {
            throw new IllegalArgumentException("Invalid run result");
        }
    }
}
