package com.comp2042.model.stats;

/**
 * Summary calculated from every recorded run in a mode, including abandoned runs.
 *
 * @param runs total number of recorded runs
 * @param wins number of runs that cleared the board
 * @param losses number of runs that ended in a collision
 * @param abandoned number of runs that ended before a win or loss
 * @param bestScore highest score, or zero for an empty history
 * @param averageScore mean score, or zero for an empty history
 * @param totalActiveTimeMillis combined play time in milliseconds, excluding pauses
 */
public record ModeStatistics(long runs, long wins, long losses, long abandoned,
                             int bestScore, double averageScore, long totalActiveTimeMillis) {
}
