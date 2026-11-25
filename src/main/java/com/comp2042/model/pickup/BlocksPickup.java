package com.comp2042.model.pickup;

import com.comp2042.model.Game;
import com.comp2042.model.Obstacle;
import com.comp2042.model.Snake;
import javafx.scene.image.Image;

/** Pickup that requests additional obstacles and inherits the default point reward. */
public class BlocksPickup extends Pickup {
    /** Shared blocks sprite loaded once for all instances. */
    private static final Image BLOCKS_IMAGE = new Image(BlocksPickup.class.getResourceAsStream("/com/comp2042/images/pickups/blocks.png"));

    /**
     * Creates a blocks pickup in a free grid cell.
     *
     * @param gridWidth the board width in cells
     * @param gridHeight the board height in cells
     * @param snake the snake excluded from placement
     * @param obstacle the obstacles excluded from placement
     * @throws Pickup.NoFreeCellException if no free cell remains
     */
    public BlocksPickup(int gridWidth, int gridHeight, Snake snake, Obstacle obstacle) {
        super(gridWidth, gridHeight, snake, obstacle, BLOCKS_IMAGE);
    }

    /**
     * Requests a random number of new obstacles based on board area.
     * Actual additions are limited by the obstacle cap and free cells.
     *
     * @param game the source of board dimensions
     * @param snake the snake whose cells must remain free of obstacles
     * @param obstacle the collection receiving the new barriers
     */
    @Override
    public void applyEffect(Game game, Snake snake, Obstacle obstacle) {
        // A positive random bound also permits requests on boards smaller than 20 cells.
        int obstacleCount = RANDOM.nextInt(Math.max(1, game.getWidth() * game.getHeight() / 20)) + 3;
        obstacle.newObstacles(snake, this, obstacleCount);
    }

    /**
     * Identifies this pickup as {@link PickupType#BLOCKS}.
     *
     * @return {@link PickupType#BLOCKS}
     */
    @Override
    public PickupType getType() { return PickupType.BLOCKS; }
}
