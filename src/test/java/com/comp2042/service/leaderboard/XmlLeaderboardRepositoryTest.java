package com.comp2042.service.leaderboard;

import com.comp2042.model.GameMode;
import com.comp2042.model.stats.RunOutcome;
import com.comp2042.model.stats.RunResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XmlLeaderboardRepositoryTest {

    @TempDir
    Path directory;

    @Test
    void missingFileLoadsAnEmptyHistory() throws IOException {
        Path file = directory.resolve("new-player").resolve("leaderboard.xml");

        assertTrue(new XmlLeaderboardRepository(file).load().isEmpty());
    }

    @Test
    void savedHistorySurvivesReloadAndIncludesUnrecognisedModes() throws IOException {
        Path file = directory.resolve("new-player").resolve("leaderboard.xml");
        XmlLeaderboardRepository repository = new XmlLeaderboardRepository(file);
        RunResult first = result(GameMode.CLASSIC.name());
        RunResult second = result(GameMode.WRAPAROUND.name());
        RunResult unknownMode = result("future & mode");

        repository.save(List.of(first, unknownMode, second));

        assertEquals(List.of(first, unknownMode, second), new XmlLeaderboardRepository(file).load());

        repository.save(List.of(second));

        assertEquals(List.of(second), new XmlLeaderboardRepository(file).load());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "<leaderboard version=\"2\"/>",
        "<leaderboard version=\"1\"><run id=\"invalid\"/></leaderboard>",
        "<leaderboard version=\"1\">"
    })
    void invalidSavedHistoryIsPreservedWhileNewRunsRemainAvailableInMemory(String savedXml) throws IOException {
        Path file = directory.resolve("leaderboard.xml");
        Files.writeString(file, savedXml);
        XmlLeaderboardRepository repository = new XmlLeaderboardRepository(file);
        assertThrows(IOException.class, repository::load);
        LeaderboardService service = new LeaderboardService(repository);
        RunResult newRun = result(GameMode.CLASSIC.name());

        service.recordResult(newRun);

        assertTrue(service.getWarning().isPresent());
        assertEquals(List.of(newRun), service.getTopRuns(GameMode.CLASSIC, 10));
        assertEquals(savedXml, Files.readString(file));
    }

    @Test
    void rejectsSavedHistoryContainingDuplicateIds() throws IOException {
        XmlLeaderboardRepository repository = new XmlLeaderboardRepository(directory.resolve("leaderboard.xml"));
        RunResult result = result(GameMode.OBSTACLES.name());
        repository.save(List.of(result, result));

        assertThrows(IOException.class, repository::load);
    }

    private static RunResult result(String modeId) {
        return new RunResult(UUID.randomUUID(), modeId, 15, RunOutcome.WIN, 1_234, 5,
                Instant.parse("2026-11-25T09:00:00Z"));
    }
}
