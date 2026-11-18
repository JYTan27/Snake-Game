package com.comp2042.model;

import java.util.Optional;
import java.util.Random;

/** Selects pickups according to the game's rules and current board state. */
public class PickupFactory {
    private final WeightedPickupSelector selector;

    public PickupFactory() {
        this(new Random());
    }

    public PickupFactory(Random random) {
        this.selector = new WeightedPickupSelector(random);
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
        PickupType type = selector.select(game.getMode().getPickupWeights());

        if (type == PickupType.BLOCKS && !obstacle.canAddObstacles()) {
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
