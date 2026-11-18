package com.comp2042.model;

import javafx.scene.image.Image;

public class BoltPickup extends Pickup {
    private static final Image BOLT_IMAGE = new Image(BoltPickup.class.getResourceAsStream("/com/comp2042/images/coins/bolt.png"));
    private static final double SPEED_BONUS = 5.0;

    public BoltPickup(int gridWidth, int gridHeight, Snake snake, Obstacle obstacle) {
        super(gridWidth, gridHeight, snake, obstacle, BOLT_IMAGE);
    }

    @Override
    public double getSpeedBonus() {
        return SPEED_BONUS;
    }

    @Override
    public PickupType getType() { return PickupType.BOLT; }
}
