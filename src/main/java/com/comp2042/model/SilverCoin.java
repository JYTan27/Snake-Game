package com.comp2042.model;

import javafx.scene.image.Image;

public class SilverCoin extends Coin {
    private static final Image SILVER_COIN_IMAGE = new Image(SilverCoin.class.getResourceAsStream("/com/comp2042/images/coins/silvercoin.png"));

    public SilverCoin(int gridWidth, int gridHeight, Snake snek, Obstacle obstacle) {
        super(gridWidth, gridHeight, snek, obstacle, SILVER_COIN_IMAGE);
    }

    @Override
    public void applyEffect(Game game, Snake snake, Obstacle obstacle) {
        int obstacleCount = RANDOM.nextInt(game.getWidth() * game.getHeight() / 20) + 3;
        obstacle.newObstacles(snake, this, obstacleCount);
        game.decMaxScore(obstacle.getObstacles().size());
    }
}
