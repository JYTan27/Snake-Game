package com.comp2042.model;

import javafx.scene.image.Image;

public class GoldCoin extends Coin {
    private static final Image GOLD_COIN_IMAGE = new Image(GoldCoin.class.getResourceAsStream("/com/comp2042/images/coins/goldcoin.png"));
    private static final int GOLD_COIN_POINTS = 15;

    public GoldCoin(int gridWidth, int gridHeight, Snake snek, Obstacle obstacle) {
        super(gridWidth, gridHeight, snek, obstacle, GOLD_COIN_IMAGE);
    }

    @Override
    public int getPoints() {
        return GOLD_COIN_POINTS;
    }
}
