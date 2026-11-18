package com.comp2042.model;

import javafx.scene.image.Image;

public class RingPickup extends Pickup {
    private static final Image RING_IMAGE = new Image(RingPickup.class.getResourceAsStream("/com/comp2042/images/coins/ring.png"));

    public RingPickup(int gridWidth, int gridHeight, Snake snake, Obstacle obstacle) {
        super(gridWidth, gridHeight, snake, obstacle, RING_IMAGE);
    }

    @Override
    public void applyEffect(Game game, Snake snake, Obstacle obstacle) {
        snake.ignoreBorders(true);
    }
}
