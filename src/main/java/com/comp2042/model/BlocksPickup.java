package com.comp2042.model;

import javafx.scene.image.Image;

public class BlocksPickup extends Pickup {
    private static final Image BLOCKS_IMAGE = new Image(BlocksPickup.class.getResourceAsStream("/com/comp2042/images/coins/blocks.png"));

    public BlocksPickup(int gridWidth, int gridHeight, Snake snek, Obstacle obstacle) {
        super(gridWidth, gridHeight, snek, obstacle, BLOCKS_IMAGE);
    }

    @Override
    public void applyEffect(Game game, Snake snake, Obstacle obstacle) {
        int obstacleCount = RANDOM.nextInt(game.getWidth() * game.getHeight() / 20) + 3;
        obstacle.newObstacles(snake, this, obstacleCount);
        game.decMaxScore(obstacle.getObstacles().size());
    }
}
