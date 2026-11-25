package com.comp2042.model.stats;

import com.comp2042.model.GameMode;
import java.time.Clock;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

/** Times one run from the first movement input and finalises it at most once. */
public final class RunSession {

    /** Mode whose identifier will be attached to the result. */
    private final GameMode mode;
    /** Wall clock for the result's recording timestamp. */
    private final Clock clock;
    /** Monotonic nanosecond source for measuring elapsed play. */
    private final LongSupplier nanoTime;
    /** Whether the player has begun this attempt. */
    private boolean started;
    /** Whether the current stretch of active play is being timed. */
    private boolean running;
    /** Whether this attempt has already produced a result. */
    private boolean finalized;
    /** Clock reading at the start of the current stretch of active play. */
    private long resumedAt;
    /** Active nanoseconds accumulated across completed stretches of play. */
    private long activeNanos;

    /**
     * Creates an unstarted session using the system clocks.
     *
     * @param mode the game mode whose results will be recorded
     */
    public RunSession(GameMode mode) {
        this(mode, Clock.systemUTC(), System::nanoTime);
    }

    /**
     * Creates an unstarted session with explicit clock sources.
     *
     * @param mode the game mode whose results will be recorded
     * @param clock the wall clock used for the result timestamp
     * @param nanoTime a monotonic time source in nanoseconds for measuring active play
     * @throws NullPointerException if any argument is null
     */
    public RunSession(GameMode mode, Clock clock, LongSupplier nanoTime) {
        this.mode = Objects.requireNonNull(mode);
        this.clock = Objects.requireNonNull(clock);
        this.nanoTime = Objects.requireNonNull(nanoTime);
    }

    /** Starts counting active time if the run is idle and still open. */
    public void startOrResume() {
        if (running || finalized) return;
        resumedAt = nanoTime.getAsLong();
        started = true;
        running = true;
    }

    /** Accumulates the current stretch of active play and suspends the timer. */
    public void pause() {
        if (!running) return;
        activeNanos += Math.max(0, nanoTime.getAsLong() - resumedAt);
        running = false;
    }

    /**
     * Stops the timer and creates the result for an open, started run.
     *
     * @param outcome the reason the run ended
     * @param score the final points total
     * @param snakeLength the last valid length, including the head
     * @return the completed result, or an empty optional if the run never started
     *         or already produced a result
     */
    public Optional<RunResult> finish(RunOutcome outcome, int score, int snakeLength) {
        if (!started || finalized) return Optional.empty();
        pause();
        // Snapshot the completed attempt with its active duration and a fresh result ID.
        RunResult result = new RunResult(UUID.randomUUID(), mode.name(), score, outcome,
                TimeUnit.NANOSECONDS.toMillis(activeNanos), snakeLength, clock.instant());
        finalized = true;
        return Optional.of(result);
    }
}
