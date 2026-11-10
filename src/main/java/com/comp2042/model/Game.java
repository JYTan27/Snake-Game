package com.comp2042.model;

public class Game {
    private static final double SMALL_BOARD_MULTIPLIER = 20.0;
    private static final double LARGE_BOARD_MULTIPLIER = 10.0;
    private static final int LARGE_BOARD_THRESHOLD = 50;

    private int width;
    private int height;
    private int coinsEaten;
    private int maxScore;
    private double multiplier;
    private int displayScore;
    private double speed;
    private boolean powerUp;
    private boolean noBorder;

    public enum Difficulty {
        EASY(8.5, true),
        NORMAL(8.5, false),
        HARD(13.0, false);

        private final double speed;
        private final boolean noBorder;

        Difficulty(double speed, boolean noBorder) {
            this.speed = speed;
            this.noBorder = noBorder;
        }

        public double getSpeed() { return speed; }
        public boolean isNoBorder() { return noBorder; }
    }

    public Game(int x, int y, boolean powerUp, Difficulty difficulty) {
        this.speed = difficulty.getSpeed();
        this.noBorder = difficulty.isNoBorder();
        this.powerUp = powerUp;
        width = x;
        height = y;
        maxScore = x * y - 2;
        multiplier = (height > LARGE_BOARD_THRESHOLD) ? LARGE_BOARD_MULTIPLIER : SMALL_BOARD_MULTIPLIER;
    }

    public boolean gameWon() {
        return coinsEaten == maxScore;
    }

    public void incScore() {
        coinsEaten++;
    }

    /**
     * Reduces maxScore by (num - 1) to account for the grid cells now occupied
     * by obstacles. The -1 accounts for the coin cell that was just consumed.
     */
    public void decMaxScore(int num) {
        if (maxScore - (num - 1) > 0) {
            maxScore -= (num - 1);
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void resetScore() {
        coinsEaten = 0;
        displayScore = 0;
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

    public double getSpeed() {
        return speed;
    }

    public boolean isNoBorder() {
        return noBorder;
    }
}
