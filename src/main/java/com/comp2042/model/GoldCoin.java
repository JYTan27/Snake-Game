package com.comp2042.model;

import javafx.scene.image.Image;

public class GoldCoin extends Coin {
    private Image image = new Image(getClass().getResourceAsStream("/com/comp2042/images/coins/goldcoin.png"));

    public GoldCoin(int x, int y, Snake snek, Obstacle obstacle) {
        super(x, y, snek, obstacle);
    }

    public Image getImage() {
        return image;
    }

    public int getPoints() {
        int points = 15;
        return points;
    }
}
