package com.comp2042.model;

import java.util.Set;

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
    private boolean completed;
    private final Difficulty difficulty;

    public enum Difficulty {
        EASY("Easy", 7.0, true, Set.of(CoinType.REGULAR, CoinType.GOLD, CoinType.RAINBOW)),
        NORMAL("Normal", 9.0, false, Set.of(CoinType.REGULAR, CoinType.GOLD, CoinType.RAINBOW, CoinType.BLUE)),
        HARD("Hard", 12.0, false, Set.of(CoinType.REGULAR, CoinType.GOLD, CoinType.RAINBOW, CoinType.BLUE, CoinType.SILVER));

        private final String displayName;
        private final double speed;
        private final boolean noBorder;
        private final Set<CoinType> allowedCoins;

        Difficulty(String displayName, double speed, boolean noBorder, Set<CoinType> allowedCoins) {
            this.displayName = displayName;
            this.speed = speed;
            this.noBorder = noBorder;
            this.allowedCoins = Set.copyOf(allowedCoins);
        }

        public String getDisplayName() { return displayName; }
        public double getSpeed() { return speed; }
        public boolean isNoBorder() { return noBorder; }
        public boolean allowsCoin(CoinType type) { return allowedCoins.contains(type); }
    }

    public Game(int x, int y, boolean powerUp, Difficulty difficulty) {
        this.difficulty = difficulty;
        this.speed = difficulty.getSpeed();
        this.noBorder = difficulty.isNoBorder();
        this.powerUp = powerUp;
        width = x;
        height = y;
        maxScore = x * y - 2;
        multiplier = (height > LARGE_BOARD_THRESHOLD) ? LARGE_BOARD_MULTIPLIER : SMALL_BOARD_MULTIPLIER;
    }

    public boolean gameWon() {
        return completed || coinsEaten == maxScore;
    }

    public void markWon() {
        completed = true;
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
        maxScore = width * height - 2;
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

    public boolean allowsCoin(CoinType type) {
        return difficulty.allowsCoin(type);
    }

    public double getSpeed() {
        return speed;
    }

    public boolean isNoBorder() {
        return noBorder;
    }
}
