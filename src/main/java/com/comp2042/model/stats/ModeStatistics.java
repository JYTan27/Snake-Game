package com.comp2042.model.stats;

/** Summary calculated from every recorded run in a mode, including abandonment. */
public record ModeStatistics(long runs, long wins, long losses, long abandoned,
                             int bestScore, double averageScore, long totalActiveTimeMillis) {
}
