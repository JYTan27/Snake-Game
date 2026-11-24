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
    private static final System.Logger LOGGER = System.getLogger(LeaderboardService.class.getName());
    private final LeaderboardRepository repository;
    private final LinkedHashMap<UUID, RunResult> results = new LinkedHashMap<>();
    private boolean writesEnabled = true;
    private String warning;

    /** Loads once. Unreadable files remain untouched and disable writes for this session. */
    public LeaderboardService(LeaderboardRepository repository) {
        this.repository = Objects.requireNonNull(repository);
        try {
            for (RunResult result : repository.load())
                results.putIfAbsent(result.id(), result);
        } catch (IOException exception) {
            writesEnabled = false;
            warning = "Saved results could not be loaded. New results are kept for this session only.";
            LOGGER.log(System.Logger.Level.WARNING, warning, exception);
        }
    }

    /** Keeps each result ID once and saves the full history when storage is available. */
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

    /** Ordered streams and stable score sorting preserve recording order on ties. */
    public List<RunResult> getTopRuns(GameMode mode, int limit) {
        if (limit < 0) throw new IllegalArgumentException("Negative result limit");
        return modeResults(mode).stream().sorted(Comparator.comparingInt(RunResult::score).reversed())
                .limit(limit).toList();
    }

    public ModeStatistics getStatistics(GameMode mode) {
        List<RunResult> runs = modeResults(mode);
        return new ModeStatistics(runs.size(), count(runs, RunOutcome.WIN), count(runs, RunOutcome.LOSS),
                count(runs, RunOutcome.ABANDONED), runs.stream().mapToInt(RunResult::score).max().orElse(0),
                runs.stream().mapToInt(RunResult::score).average().orElse(0),
                runs.stream().mapToLong(RunResult::activeTimeMillis).sum());
    }

    public Optional<String> getWarning() {
        return Optional.ofNullable(warning);
    }

    private List<RunResult> modeResults(GameMode mode) {
        return results.values().stream().filter(run -> run.modeId().equals(mode.name())).toList();
    }

    private static long count(List<RunResult> runs, RunOutcome outcome) {
        return runs.stream().filter(run -> run.outcome() == outcome).count();
    }
}
