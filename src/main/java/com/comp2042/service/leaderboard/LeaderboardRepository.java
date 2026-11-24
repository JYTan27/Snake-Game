package com.comp2042.service.leaderboard;

import com.comp2042.model.stats.RunResult;
import java.io.IOException;
import java.util.List;

/** Storage boundary; implementations preserve result order for score ties. */
public interface LeaderboardRepository {
    List<RunResult> load() throws IOException;
    void save(List<RunResult> results) throws IOException;
}
