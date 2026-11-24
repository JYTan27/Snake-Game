package com.comp2042.model.pickup;

import com.comp2042.model.Obstacle;
import com.comp2042.model.Snake;
import javafx.scene.image.Image;

/** Pickup that adds five moves per second until the next pickup is collected. */
public class BoltPickup extends Pickup {
    /** Shared bolt sprite loaded once for all instances. */
    private static final Image BOLT_IMAGE = new Image(BoltPickup.class.getResourceAsStream("/com/comp2042/images/coins/bolt.png"));
    /** Movement-rate bonus in grid cells per second. */
    private static final double SPEED_BONUS = 5.0;

    /**
     * Creates a bolt pickup in a free grid cell.
     *
     * @param gridWidth the board width in cells
     * @param gridHeight the board height in cells
     * @param snake the snake excluded from placement
     * @param obstacle the obstacles excluded from placement
     * @throws Pickup.NoFreeCellException if no free cell remains
     */
    public BoltPickup(int gridWidth, int gridHeight, Snake snake, Obstacle obstacle) {
        super(gridWidth, gridHeight, snake, obstacle, BOLT_IMAGE);
    }

    /**
     * Returns the bonus of five moves per second, applied by the game's collection logic.
     *
     * @return the bonus of five moves per second, applied by the game's collection logic
     */
    @Override
    public double getSpeedBonus() {
        return SPEED_BONUS;
    }

    /**
     * Identifies this pickup as {@link PickupType#BOLT}.
     *
     * @return {@link PickupType#BOLT}
     */
    @Override
    public PickupType getType() { return PickupType.BOLT; }
}
