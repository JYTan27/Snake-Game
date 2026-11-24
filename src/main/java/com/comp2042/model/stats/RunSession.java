package com.comp2042.model.stats;

import com.comp2042.model.GameMode;
import java.time.Clock;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

/** Measures active time and finalises a started run at most once, without JavaFX. */
public final class RunSession {
    private final GameMode mode;
    private final Clock clock;
    private final LongSupplier nanoTime;
    private boolean started;
    private boolean running;
    private boolean finalized;
    private long resumedAt;
    private long activeNanos;

    public RunSession(GameMode mode) {
        this(mode, Clock.systemUTC(), System::nanoTime);
    }

    /** Inject wall and monotonic clocks to test timestamps and pause accounting. */
    public RunSession(GameMode mode, Clock clock, LongSupplier nanoTime) {
        this.mode = Objects.requireNonNull(mode);
        this.clock = Objects.requireNonNull(clock);
        this.nanoTime = Objects.requireNonNull(nanoTime);
    }

    public void startOrResume() {
        if (running || finalized) return;
        resumedAt = nanoTime.getAsLong();
        started = true;
        running = true;
    }

    public void pause() {
        if (!running) return;
        activeNanos += Math.max(0, nanoTime.getAsLong() - resumedAt);
        running = false;
    }

    /** Empty for an unstarted or already-recorded run. */
    public Optional<RunResult> finish(RunOutcome outcome, int score, int snakeLength) {
        if (!started || finalized) return Optional.empty();
        pause();
        RunResult result = new RunResult(UUID.randomUUID(), mode.name(), score, outcome,
                TimeUnit.NANOSECONDS.toMillis(activeNanos), snakeLength, clock.instant());
        finalized = true;
        return Optional.of(result);
    }
}
