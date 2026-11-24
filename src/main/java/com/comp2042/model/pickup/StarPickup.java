package com.comp2042.model.pickup;

import com.comp2042.model.Obstacle;
import com.comp2042.model.Snake;
import javafx.scene.image.Image;

/** Pickup worth fifteen display points, with no speed bonus or special effect. */
public class StarPickup extends Pickup {
    /** Shared star sprite loaded once for all instances. */
    private static final Image STAR_IMAGE = new Image(StarPickup.class.getResourceAsStream("/com/comp2042/images/coins/star.png"));
    /** Display-point reward for collecting a star. */
    private static final int STAR_POINTS = 15;

    /**
     * Creates a star pickup in a free grid cell.
     *
     * @param gridWidth the board width in cells
     * @param gridHeight the board height in cells
     * @param snake the snake excluded from placement
     * @param obstacle the obstacles excluded from placement
     * @throws Pickup.NoFreeCellException if no free cell remains
     */
    public StarPickup(int gridWidth, int gridHeight, Snake snake, Obstacle obstacle) {
        super(gridWidth, gridHeight, snake, obstacle, STAR_IMAGE);
    }

    /**
     * Returns the fifteen-point collection reward.
     *
     * @return the fifteen-point collection reward
     */
    @Override
    public int getPoints() {
        return STAR_POINTS;
    }

    /**
     * Identifies this pickup as {@link PickupType#STAR}.
     *
     * @return {@link PickupType#STAR}
     */
    @Override
    public PickupType getType() { return PickupType.STAR; }
}
