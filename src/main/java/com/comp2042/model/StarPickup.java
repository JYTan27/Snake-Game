package com.comp2042.model;

import javafx.scene.image.Image;

public class StarPickup extends Pickup {
    private static final Image STAR_IMAGE = new Image(StarPickup.class.getResourceAsStream("/com/comp2042/images/coins/star.png"));
    private static final int STAR_POINTS = 15;

    public StarPickup(int gridWidth, int gridHeight, Snake snake, Obstacle obstacle) {
        super(gridWidth, gridHeight, snake, obstacle, STAR_IMAGE);
    }

    @Override
    public int getPoints() {
        return STAR_POINTS;
    }

    @Override
    public PickupType getType() { return PickupType.STAR; }
}
