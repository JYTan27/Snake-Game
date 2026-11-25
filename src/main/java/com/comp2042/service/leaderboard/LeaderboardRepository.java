package com.comp2042.service.leaderboard;

import com.comp2042.model.stats.RunResult;
import java.io.IOException;
import java.util.List;

/** Stores run history in recording order so rankings can preserve that order for ties. */
public interface LeaderboardRepository {
    /**
     * Reads the saved history.
     *
     * @return saved results in recording order, or an empty list if there is no saved history
     * @throws IOException if the saved history cannot be read
     */
    List<RunResult> load() throws IOException;

    /**
     * Replaces the saved history with the supplied results.
     *
     * @param results the full run history in recording order
     * @throws IOException if the history cannot be saved
     */
    void save(List<RunResult> results) throws IOException;
}
