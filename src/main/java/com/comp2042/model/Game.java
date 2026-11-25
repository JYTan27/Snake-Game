package com.comp2042.model;

import com.comp2042.model.pickup.Pickup;

import java.util.Objects;

/**
 * Stores board dimensions, display points, completion state, and the selected mode.
 * Applies pickup effects while delegating base speed and border rules to the mode.
 * The controller schedules movement and records victory when pickup placement
 * reports that no free board cell remains.
 */
public class Game {

    /** Base cell size in pixels for boards up to the height threshold. */
    private static final double SMALL_BOARD_MULTIPLIER = 20.0;
    /** Base cell size in pixels for boards above the height threshold. */
    private static final double LARGE_BOARD_MULTIPLIER = 10.0;
    /** Board height in cells above which the smaller cell size is used. */
    private static final int LARGE_BOARD_THRESHOLD = 50;

    /** Board width in grid cells. */
    private final int width;
    /** Board height in grid cells. */
    private final int height;
    /** Base cell size in pixels before fullscreen scaling. */
    private final double multiplier;
    /** Points awarded by pickups, independent of the win condition. */
    private int displayScore;
    /** Whether full-board completion has been recorded. */
    private boolean completed;
    /** Immutable rule configuration retained on reset. */
    private final GameMode mode;

    /**
     * Creates a game with zero points and no recorded completion.
     * Selects the base cell size from the board height.
     *
     * @param width the board width in grid cells
     * @param height the board height in grid cells
     * @param mode the selected game-mode configuration
     * @throws NullPointerException if {@code mode} is null
     */
    public Game(int width, int height, GameMode mode) {
        this.mode = Objects.requireNonNull(mode);
        this.width = width;
        this.height = height;
        multiplier = (height > LARGE_BOARD_THRESHOLD) ? LARGE_BOARD_MULTIPLIER : SMALL_BOARD_MULTIPLIER;
    }

    /**
     * Reports recorded completion without inspecting board occupancy.
     *
     * @return {@code true} after {@link #markWon()} and until reset
     */
    public boolean gameWon() {
        return completed;
    }

    /** Records victory after the controller detects exhausted pickup placement. */
    public void markWon() {
        completed = true;
    }

    /**
     * Applies a collected pickup's effects and awards display points.
     * Call exactly once after the caller has detected collection.
     * Temporary speed and border effects are replaced by the mode defaults
     * plus the newly collected pickup's effects.
     *
     * @param pickup the pickup that was collected
     * @param snake the snake receiving the pickup's effect
     * @param obstacle the current board's obstacles
     * @return the movement speed in moves per second after collection
     */
    public double collectPickup(Pickup pickup, Snake snake, Obstacle obstacle) {
        // Start from the base speed so consecutive bonuses do not accumulate.
        double movementSpeed = getSpeed() + pickup.getSpeedBonus();
        // Restore default borders before a ring can enable wrapping again.
        snake.ignoreBorders(isNoBorder());
        pickup.applyEffect(this, snake, obstacle);
        addPoints(pickup.getPoints());
        return movementSpeed;
    }

    /**
     * Returns the board width in grid cells.
     *
     * @return the board width in grid cells
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the board height in grid cells.
     *
     * @return the board height in grid cells
     */
    public int getHeight() {
        return height;
    }

    /**
     * Clears display points and completion while retaining dimensions and mode.
     * The controller separately resets the snake, obstacles, pickup, and animation.
     */
    public void resetScore() {
        displayScore = 0;
        completed = false;
    }

    /**
     * Returns the base grid-cell size in pixels, before fullscreen scaling.
     *
     * @return the base grid-cell size in pixels, before fullscreen scaling
     */
    public double getMultiplier() {
        return multiplier;
    }

    /**
     * Adds to the display score without changing completion state.
     *
     * @param n the signed number of points to add
     */
    public void addPoints(int n) {
        displayScore += n;
    }

    /**
     * Returns the current display points total.
     *
     * @return the current display points total
     */
    public int getDisplayScore() {
        return displayScore;
    }

    /**
     * Returns the selected immutable game-mode configuration.
     *
     * @return the selected immutable game-mode configuration
     */
    public GameMode getMode() {
        return mode;
    }

    /**
     * Returns the mode's base speed in moves per second, excluding pickup bonuses.
     *
     * @return the mode's base speed in moves per second, excluding pickup bonuses
     */
    public double getSpeed() {
        return mode.getSpeed();
    }

    /**
     * Reports whether the mode enables border wrapping by default, excluding ring effects.
     *
     * @return whether the mode enables border wrapping by default, excluding ring effects
     */
    public boolean isNoBorder() {
        return mode.isNoBorder();
    }
}
