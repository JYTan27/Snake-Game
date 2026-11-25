package com.comp2042.model.pickup;

import com.comp2042.model.Game;
import com.comp2042.model.Obstacle;
import com.comp2042.model.Snake;
import javafx.scene.image.Image;

/**
 * Pickup that enables border wrapping until the next collection restores mode defaults.
 * Inherits the default five-point reward and zero speed bonus.
 */
public class RingPickup extends Pickup {
    /** Shared ring sprite loaded once for all instances. */
    private static final Image RING_IMAGE = new Image(RingPickup.class.getResourceAsStream("/com/comp2042/images/pickups/ring.png"));

    /**
     * Creates a ring pickup in a free grid cell.
     *
     * @param gridWidth the board width in cells
     * @param gridHeight the board height in cells
     * @param snake the snake excluded from placement
     * @param obstacle the obstacles excluded from placement
     * @throws Pickup.NoFreeCellException if no free cell remains
     */
    public RingPickup(int gridWidth, int gridHeight, Snake snake, Obstacle obstacle) {
        super(gridWidth, gridHeight, snake, obstacle, RING_IMAGE);
    }

    /**
     * Enables wrapping for the snake. Other collision rules remain unchanged.
     *
     * @param game the collecting game; unused by this effect
     * @param snake the snake whose wrapping rule is enabled
     * @param obstacle the board's obstacles; unused by this effect
     */
    @Override
    public void applyEffect(Game game, Snake snake, Obstacle obstacle) {
        snake.ignoreBorders(true);
    }

    /**
     * Identifies this pickup as {@link PickupType#RING}.
     *
     * @return {@link PickupType#RING}
     */
    @Override
    public PickupType getType() { return PickupType.RING; }
}
