package com.comp2042.model;

import javafx.scene.image.Image;

/** The standard pickup, available in every game mode. */
public class DiamondPickup extends Pickup {
    private static final Image DIAMOND_IMAGE = new Image(DiamondPickup.class.getResourceAsStream("/com/comp2042/images/coins/diamond.png"));

    public DiamondPickup(int gridWidth, int gridHeight, Snake snake, Obstacle obstacle) {
        super(gridWidth, gridHeight, snake, obstacle, DIAMOND_IMAGE);
    }

    @Override
    public PickupType getType() { return PickupType.DIAMOND; }
}
