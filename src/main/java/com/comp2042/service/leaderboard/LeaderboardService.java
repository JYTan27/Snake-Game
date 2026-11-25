package com.comp2042.service.leaderboard;

import com.comp2042.model.GameMode;
import com.comp2042.model.stats.ModeStatistics;
import com.comp2042.model.stats.RunOutcome;
import com.comp2042.model.stats.RunResult;
import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Owns run history and saves each new result. Use from a single thread. */
public final class LeaderboardService {

    /** Logger for persistence failures. */
    private static final System.Logger LOGGER = System.getLogger(LeaderboardService.class.getName());
    /** Repository used to load and replace the saved history. */
    private final LeaderboardRepository repository;
    /** Results indexed by ID in recording order for deduplication and tie-breaking. */
    private final LinkedHashMap<UUID, RunResult> results = new LinkedHashMap<>();
    /** Whether saving is permitted after the initial read. */
    private boolean writesEnabled = true;
    /** Current storage warning, or null when there is no warning. */
    private String warning;

    /**
     * Loads saved results once. A read failure disables writes for this session.
     *
     * @param repository the storage for run history
     * @throws NullPointerException if {@code repository} is null
     */
    public LeaderboardService(LeaderboardRepository repository) {
        this.repository = Objects.requireNonNull(repository);
        try {
            // Index loaded runs by ID while keeping the original recording order.
            for (RunResult result : repository.load())
                results.putIfAbsent(result.id(), result);
        } catch (IOException exception) {
            writesEnabled = false;
            warning = "Saved results could not be loaded. New results are kept for this session only.";
            LOGGER.log(System.Logger.Level.WARNING, warning, exception);
        }
    }

    /**
     * Keeps each result ID once and attempts to save the full history.
     * After a failed save, the next new result retries with all in-memory runs.
     *
     * @param result the run result to record
     * @throws NullPointerException if {@code result} is null
     */
    public void recordResult(RunResult result) {
        Objects.requireNonNull(result);
        if (results.putIfAbsent(result.id(), result) != null) return;
        if (!writesEnabled) return;

        try {
            repository.save(List.copyOf(results.values()));
            warning = null;
        } catch (IOException exception) {
            warning = "Results could not be saved. They remain available for this session; a later save will retry.";
            LOGGER.log(System.Logger.Level.WARNING, warning, exception);
        }
    }

    /**
     * Ranks a mode's runs by descending score, preserving recording order on ties.
     *
     * @param mode the game mode to rank
     * @param limit the maximum number of results to return
     * @return an unmodifiable list of ranked results
     * @throws IllegalArgumentException if {@code limit} is negative
     */
    public List<RunResult> getTopRuns(GameMode mode, int limit) {
        if (limit < 0) throw new IllegalArgumentException("Negative result limit");
        return modeResults(mode).stream().sorted(Comparator.comparingInt(RunResult::score).reversed())
                .limit(limit).toList();
    }

    /**
     * Summarises the full in-memory history for a mode, including abandoned runs.
     *
     * @param mode the game mode to summarise
     * @return counts, score statistics, and total active time for the mode
     */
    public ModeStatistics getStatistics(GameMode mode) {
        // Use all recorded runs in the selected mode when calculating the summary.
        List<RunResult> runs = modeResults(mode);
        return new ModeStatistics(runs.size(), count(runs, RunOutcome.WIN), count(runs, RunOutcome.LOSS),
                count(runs, RunOutcome.ABANDONED), runs.stream().mapToInt(RunResult::score).max().orElse(0),
                runs.stream().mapToInt(RunResult::score).average().orElse(0),
                runs.stream().mapToLong(RunResult::activeTimeMillis).sum());
    }

    /** {@return the current storage warning, or an empty optional if storage reports no problem} */
    public Optional<String> getWarning() {
        return Optional.ofNullable(warning);
    }

    /**
     * Filters the in-memory history without changing its recording order.
     *
     * @param mode the game mode to select
     * @return the recorded runs for that mode
     */
    private List<RunResult> modeResults(GameMode mode) {
        return results.values().stream().filter(run -> run.modeId().equals(mode.name())).toList();
    }

    /**
     * Counts the runs that ended with the specified outcome.
     *
     * @param runs the history to examine
     * @param outcome the outcome to count
     * @return the number of matching runs
     */
    private static long count(List<RunResult> runs, RunOutcome outcome) {
        return runs.stream().filter(run -> run.outcome() == outcome).count();
    }
}
