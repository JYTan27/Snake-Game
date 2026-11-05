package com.comp2042.model;

import javafx.scene.image.Image;

public class RainbowCoin extends Coin {
    private static final Image RAINBOW_COIN_IMAGE = new Image(RainbowCoin.class.getResourceAsStream("/com/comp2042/images/coins/rainbowcoin.png"));
    private static final double SPEED_BONUS = 5.0;

    public RainbowCoin(int gridWidth, int gridHeight, Snake snek, Obstacle obstacle) {
        super(gridWidth, gridHeight, snek, obstacle, RAINBOW_COIN_IMAGE);
    }

    @Override
    public double getSpeedBonus() {
        return SPEED_BONUS;
    }
}
