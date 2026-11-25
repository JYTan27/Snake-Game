package com.comp2042.model.stats;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable result of a started run.
 *
 * @param id the unique result ID used to prevent duplicate entries
 * @param modeId the stored mode identifier, independent of the display name
 * @param score the final points total
 * @param outcome the reason the run ended
 * @param activeTimeMillis the elapsed play time in milliseconds, excluding pauses
 * @param snakeLength the last valid snake length, including its head
 * @param recordedAt the time the run was finalised
 */
public record RunResult(UUID id, String modeId, int score, RunOutcome outcome,
                        long activeTimeMillis, int snakeLength, Instant recordedAt) {
    /**
     * Creates a result with validated identifiers and measurements.
     *
     * @param id the result ID
     * @param modeId the stored mode identifier
     * @param score the final points total
     * @param outcome the reason the run ended
     * @param activeTimeMillis the elapsed play time in milliseconds
     * @param snakeLength the final snake length
     * @param recordedAt the time the run was finalised
     * @throws NullPointerException if a reference argument is null
     * @throws IllegalArgumentException if the mode ID is blank, the score or time
     *         is negative, or the snake length is below one
     */
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
