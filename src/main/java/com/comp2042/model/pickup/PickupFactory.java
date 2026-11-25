package com.comp2042.model.pickup;

import com.comp2042.model.Game;
import com.comp2042.model.Obstacle;
import com.comp2042.model.Snake;

import java.util.Optional;
import java.util.Random;

/**
 * Creates replacement pickups using the selected mode's weights and board state.
 * Replaces blocks with diamonds at the obstacle cap and reports exhausted placement
 * as an empty result. Initial and reset diamonds are created directly by callers.
 */
public class PickupFactory {

    /** Weighted type selector; pickup positions use a separate random generator. */
    private final WeightedPickupSelector selector;

    /** Creates a factory with its own random generator for type selection. */
    public PickupFactory() {
        this(new Random());
    }

    /**
     * Creates a factory with a caller-supplied generator for reproducible type rolls.
     * The supplied generator does not control pickup positions or obstacle placement.
     *
     * @param random the generator passed to the weighted selector
     * @throws NullPointerException if {@code random} is null
     */
    public PickupFactory(Random random) {
        this.selector = new WeightedPickupSelector(random);
    }

    /**
     * Selects and constructs a pickup without overlapping the snake or obstacles.
     * Converts only {@link Pickup.NoFreeCellException} into an empty result;
     * other failures propagate to the caller.
     *
     * @param game the source of board dimensions and mode weights
     * @param snake the snake occupying cells on the board
     * @param obstacle the obstacle collection and capacity used for eligibility
     * @return the new pickup, or an empty result when no free cell remains
     */
    public Optional<Pickup> createPickup(Game game, Snake snake, Obstacle obstacle) {
        try {
            return Optional.of(selectPickup(game, snake, obstacle));
        } catch (Pickup.NoFreeCellException exception) {
            return Optional.empty();
        }
    }

    /**
     * Rolls a weighted type, applies the obstacle-cap fallback, and constructs it.
     * Construction chooses the position independently of the type roll.
     *
     * @param game the source of board dimensions and pickup weights
     * @param snake the snake to exclude from placement
     * @param obstacle the obstacles to exclude and check for remaining capacity
     * @return the constructed pickup
     * @throws Pickup.NoFreeCellException if construction finds no free cell
     */
    private Pickup selectPickup(Game game, Snake snake, Obstacle obstacle) {
        // Use the current board dimensions and sample a type from the mode's pickup weights.
        int width = game.getWidth();
        int height = game.getHeight();
        PickupType type = selector.select(game.getMode().getPickupWeights());

        // Preserve the roll's probability mass as diamonds rather than rolling again.
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
