package com.comp2042.model;

import javafx.scene.image.Image;

/**
 * Standard pickup available in every game mode and used for initial/reset spawning.
 * Inherits the default five-point reward, zero speed bonus, and empty special effect.
 */
public class DiamondPickup extends Pickup {
    /** Shared diamond sprite loaded once for all instances. */
    private static final Image DIAMOND_IMAGE = new Image(DiamondPickup.class.getResourceAsStream("/com/comp2042/images/coins/diamond.png"));

    /**
     * Creates a diamond in a free grid cell.
     *
     * @param gridWidth the board width in cells
     * @param gridHeight the board height in cells
     * @param snake the snake excluded from placement
     * @param obstacle the obstacles excluded from placement
     * @throws Pickup.NoFreeCellException if no free cell remains
     */
    public DiamondPickup(int gridWidth, int gridHeight, Snake snake, Obstacle obstacle) {
        super(gridWidth, gridHeight, snake, obstacle, DIAMOND_IMAGE);
    }

    /**
     * Identifies this pickup as {@link PickupType#DIAMOND}.
     *
     * @return {@link PickupType#DIAMOND}
     */
    @Override
    public PickupType getType() { return PickupType.DIAMOND; }
}
