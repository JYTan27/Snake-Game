package com.comp2042.model;

import javafx.scene.image.Image;

public class BlueCoin extends Coin {
    private static final Image BLUE_COIN_IMAGE = new Image(BlueCoin.class.getResourceAsStream("/com/comp2042/images/coins/bluecoin.png"));

    public BlueCoin(int gridWidth, int gridHeight, Snake snek, Obstacle obstacle) {
        super(gridWidth, gridHeight, snek, obstacle, BLUE_COIN_IMAGE);
    }

    @Override
    public void applyEffect(Game game, Snake snake, Obstacle obstacle) {
        snake.ignoreBorders(true);
    }
}
