package com.comp2042.model;

import java.util.Objects;
import java.util.Optional;
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

    /** Returns empty when the board has no free cell; other failures propagate. */
    public Optional<Coin> createCoin(Game game, Snake snake, Obstacle obstacle) {
        try {
            return Optional.of(selectCoin(game, snake, obstacle));
        } catch (Coin.NoFreeCellException exception) {
            return Optional.empty();
        }
    }

    private Coin selectCoin(Game game, Snake snake, Obstacle obstacle) {
        int width = game.getWidth();
        int height = game.getHeight();
        CoinType type = game.isPowerUp() ? switch (random.nextInt(12)) {
            case 1 -> CoinType.GOLD;
            case 2 -> CoinType.RAINBOW;
            case 3 -> CoinType.BLUE;
            case 4 -> CoinType.SILVER;
            default -> CoinType.REGULAR;
        } : CoinType.REGULAR;

        if (!game.allowsCoin(type)
                || (type == CoinType.BLUE && game.isNoBorder())
                || (type == CoinType.SILVER && !obstacle.getObstacles().isEmpty())) {
            type = CoinType.REGULAR;
        }
        return switch (type) {
            case REGULAR -> new Coin(width, height, snake, obstacle);
            case GOLD -> new GoldCoin(width, height, snake, obstacle);
            case RAINBOW -> new RainbowCoin(width, height, snake, obstacle);
            case BLUE -> new BlueCoin(width, height, snake, obstacle);
            case SILVER -> new SilverCoin(width, height, snake, obstacle);
        };
    }
}
