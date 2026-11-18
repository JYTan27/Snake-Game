package com.comp2042.model;

import java.util.Set;

public class Game {
    private static final double SMALL_BOARD_MULTIPLIER = 20.0;
    private static final double LARGE_BOARD_MULTIPLIER = 10.0;
    private static final int LARGE_BOARD_THRESHOLD = 50;

    private int width;
    private int height;
    private double multiplier;
    private int displayScore;
    private double speed;
    private boolean powerUp;
    private boolean noBorder;
    private boolean completed;
    private final Difficulty difficulty;

    public enum Difficulty {
        EASY("Easy", 7.0, true, Set.of(PickupType.DIAMOND, PickupType.STAR, PickupType.BOLT)),
        NORMAL("Normal", 9.0, false, Set.of(PickupType.DIAMOND, PickupType.STAR, PickupType.BOLT, PickupType.RING)),
        HARD("Hard", 12.0, false, Set.of(PickupType.DIAMOND, PickupType.STAR, PickupType.BOLT, PickupType.RING, PickupType.BLOCKS));

        private final String displayName;
        private final double speed;
        private final boolean noBorder;
        private final Set<PickupType> allowedPickups;

        Difficulty(String displayName, double speed, boolean noBorder, Set<PickupType> allowedPickups) {
            this.displayName = displayName;
            this.speed = speed;
            this.noBorder = noBorder;
            this.allowedPickups = Set.copyOf(allowedPickups);
        }

        public String getDisplayName() { return displayName; }
        public double getSpeed() { return speed; }
        public boolean isNoBorder() { return noBorder; }
        public boolean allowsPickup(PickupType type) { return allowedPickups.contains(type); }
    }

    public Game(int x, int y, boolean powerUp, Difficulty difficulty) {
        this.difficulty = difficulty;
        this.speed = difficulty.getSpeed();
        this.noBorder = difficulty.isNoBorder();
        this.powerUp = powerUp;
        width = x;
        height = y;
        multiplier = (height > LARGE_BOARD_THRESHOLD) ? LARGE_BOARD_MULTIPLIER : SMALL_BOARD_MULTIPLIER;
    }

    /** Reports whether full-board completion has been recorded. */
    public boolean gameWon() {
        return completed;
    }

    public void markWon() {
        completed = true;
    }

    /**
     * Applies a collected pickup's effects and awards display points.
     * Call exactly once after the caller has detected collection.
     *
     * @param pickup the pickup that was collected
     * @param snake the snake receiving the pickup's effect
     * @param obstacle the current board's obstacles
     * @return the movement speed in moves per second after collection
     */
    public double collectPickup(Pickup pickup, Snake snake, Obstacle obstacle) {
        double movementSpeed = getSpeed() + pickup.getSpeedBonus();
        snake.ignoreBorders(isNoBorder());
        pickup.applyEffect(this, snake, obstacle);
        addPoints(pickup.getPoints());
        return movementSpeed;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void resetScore() {
        displayScore = 0;
        completed = false;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void addPoints(int n) {
        displayScore += n;
    }

    public int getDisplayScore() {
        return displayScore;
    }

    public boolean isPowerUp() {
        return powerUp;
    }

    public boolean allowsPickup(PickupType type) {
        return difficulty.allowsPickup(type);
    }

    public double getSpeed() {
        return speed;
    }

    public boolean isNoBorder() {
        return noBorder;
    }
}
