package com.comp2042.model;

import java.util.Objects;
import java.util.Random;

/** Selects coins according to the game's rules and current board state. */
public class CoinFactory {
    private final Random random;

    public CoinFactory() {
        this(new Random());
    }

    public CoinFactory(Random random) {
        this.random = Objects.requireNonNull(random);
    }

    public Coin createCoin(Game game, Snake snake, Obstacle obstacle) {
        int width = game.getWidth();
        int height = game.getHeight();
        if (game.isPowerUp()) {
            int roll = random.nextInt(12);
            if (roll == 1) {
                return new GoldCoin(width, height, snake, obstacle);
            } else if (roll == 2) {
                return new RainbowCoin(width, height, snake, obstacle);
            } else if (roll == 3 && !game.isNoBorder()) {
                return new BlueCoin(width, height, snake, obstacle);
            } else if (roll == 4 && obstacle.getObstacles().isEmpty()) {
                return new SilverCoin(width, height, snake, obstacle);
            }
        }
        return new Coin(width, height, snake, obstacle);
    }
}
