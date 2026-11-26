package com.comp2042.service.leaderboard;

import com.comp2042.model.GameMode;
import com.comp2042.model.stats.ModeStatistics;
import com.comp2042.model.stats.RunOutcome;
import com.comp2042.model.stats.RunResult;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeaderboardServiceTest {

    @Test
    void ranksWithinOneModeAndUsesRecordingOrderForTies() {
        RunResult lower = result(GameMode.CLASSIC, 6, RunOutcome.ABANDONED, 200, 10);
        RunResult firstTie = result(GameMode.CLASSIC, 12, RunOutcome.LOSS, 500, 20);
        RunResult secondTie = result(GameMode.CLASSIC, 12, RunOutcome.WIN, 800, 5);
        RunResult otherMode = result(GameMode.OBSTACLES, 100, RunOutcome.WIN, 9_000, 15);
        MemoryRepository history = new MemoryRepository(List.of(lower, firstTie, otherMode));
        LeaderboardService service = new LeaderboardService(history);

        service.recordResult(secondTie);

        assertEquals(List.of(firstTie, secondTie), service.getTopRuns(GameMode.CLASSIC, 2));
        assertEquals(List.of(otherMode), service.getTopRuns(GameMode.OBSTACLES, 2));
        assertEquals(new ModeStatistics(3, 1, 1, 1, 12, 10.0, 1_500),
                service.getStatistics(GameMode.CLASSIC));
        assertEquals(new ModeStatistics(0, 0, 0, 0, 0, 0.0, 0),
                service.getStatistics(GameMode.WRAPAROUND));
    }

    @Test
    void recordingTheSameIdAgainDoesNotInsertOrSaveADuplicate() {
        MemoryRepository history = new MemoryRepository(List.of());
        LeaderboardService service = new LeaderboardService(history);
        RunResult first = result(GameMode.CLASSIC, 5, RunOutcome.LOSS, 100, 10);
        RunResult duplicate = new RunResult(first.id(), first.modeId(), 25, RunOutcome.WIN,
                200, 4, first.recordedAt());

        service.recordResult(first);
        service.recordResult(duplicate);

        assertEquals(List.of(first), service.getTopRuns(GameMode.CLASSIC, 10));
        assertEquals(List.of(first), history.saved);
        assertEquals(1, history.saveAttempts);
    }

    @Test
    void retriesWithTheFullHistoryWhenTheNextResultIsRecordedAfterASaveFailure() {
        RunResult saved = result(GameMode.OBSTACLES, 4, RunOutcome.LOSS, 100, 10);
        MemoryRepository history = new MemoryRepository(List.of(saved));
        history.failNextSave = true;
        LeaderboardService service = new LeaderboardService(history);
        RunResult failedSave = result(GameMode.CLASSIC, 10, RunOutcome.ABANDONED, 200, 20);
        RunResult next = result(GameMode.CLASSIC, 20, RunOutcome.WIN, 300, 30);

        service.recordResult(failedSave);

        assertTrue(service.getWarning().isPresent());
        assertEquals(List.of(saved), history.saved);
        assertEquals(List.of(failedSave), service.getTopRuns(GameMode.CLASSIC, 10));

        service.recordResult(next);

        assertEquals(List.of(saved, failedSave, next), history.saved);
        assertEquals(2, history.saveAttempts);
        assertTrue(service.getWarning().isEmpty());
    }

    private static RunResult result(GameMode mode, int score, RunOutcome outcome, long duration, long recordedAt) {
        return new RunResult(UUID.randomUUID(), mode.name(), score, outcome, duration, 2,
                Instant.ofEpochSecond(recordedAt));
    }

    private static final class MemoryRepository implements LeaderboardRepository {
        private List<RunResult> saved;
        private int saveAttempts;
        private boolean failNextSave;

        private MemoryRepository(List<RunResult> saved) {
            this.saved = saved;
        }

        @Override
        public List<RunResult> load() {
            return saved;
        }

        @Override
        public void save(List<RunResult> results) throws IOException {
            saveAttempts++;
            if (failNextSave) {
                failNextSave = false;
                throw new IOException("Simulated write failure");
            }
            saved = List.copyOf(results);
        }
    }
}
