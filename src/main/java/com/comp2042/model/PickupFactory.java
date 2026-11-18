package com.comp2042.model;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;

/** Selects pickups according to the game's rules and current board state. */
public class PickupFactory {
    private final Random random;

    public PickupFactory() {
        this(new Random());
    }

    public PickupFactory(Random random) {
        this.random = Objects.requireNonNull(random);
    }

    /** Returns empty when the board has no free cell; other failures propagate. */
    public Optional<Pickup> createPickup(Game game, Snake snake, Obstacle obstacle) {
        try {
            return Optional.of(selectPickup(game, snake, obstacle));
        } catch (Pickup.NoFreeCellException exception) {
            return Optional.empty();
        }
    }

    private Pickup selectPickup(Game game, Snake snake, Obstacle obstacle) {
        int width = game.getWidth();
        int height = game.getHeight();
        PickupType type = game.isPowerUp() ? switch (random.nextInt(12)) {
            case 1 -> PickupType.STAR;
            case 2 -> PickupType.BOLT;
            case 3 -> PickupType.RING;
            case 4 -> PickupType.BLOCKS;
            default -> PickupType.DIAMOND;
        } : PickupType.DIAMOND;

        if (!game.allowsPickup(type)
                || (type == PickupType.RING && game.isNoBorder())
                || (type == PickupType.BLOCKS && !obstacle.canAddObstacles())) {
            type = PickupType.DIAMOND;
        }
        return switch (type) {
            case DIAMOND -> new DiamondPickup(width, height, snake, obstacle);
            case STAR -> new StarPickup(width, height, snake, obstacle);
            case BOLT -> new BoltPickup(width, height, snake, obstacle);
            case RING -> new RingPickup(width, height, snake, obstacle);
            case BLOCKS -> new BlocksPickup(width, height, snake, obstacle);
        };
    }
}
