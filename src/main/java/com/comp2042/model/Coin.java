package com.comp2042.model;

import java.awt.Point;
import java.util.Random;

import javafx.scene.image.Image;

public class Coin {
    protected static final Random RANDOM = new Random();
    private static final Image RED_COIN_IMAGE = new Image(Coin.class.getResourceAsStream("/com/comp2042/images/coins/redcoin.png"));

    private Point pos;
    private final Image image;

    public Coin(int gridWidth, int gridHeight, Snake snek, Obstacle obstacle) {
        this(gridWidth, gridHeight, snek, obstacle, RED_COIN_IMAGE);
    }

    protected Coin(int gridWidth, int gridHeight, Snake snek, Obstacle obstacle, Image image) {
        this.image = image;
        generatePosition(gridWidth, gridHeight, snek, obstacle);
    }

    private void generatePosition(int gridWidth, int gridHeight, Snake snek, Obstacle obstacle) {
        do {
            int candidateX = RANDOM.nextInt(gridWidth);
            int candidateY = RANDOM.nextInt(gridHeight);
            pos = new Point(candidateX, candidateY);
        } while (snek.getHead().equals(pos) || snek.getBody().contains(pos) || (obstacle != null && obstacle.getObstacles().contains(pos)));
    }

    public Point getPos() {
        return new Point(pos);
    }

    public int getPoints() {
        return 5;
    }

    public double getSpeedBonus() {
        return 0.0;
    }

    public void applyEffect(Game game, Snake snake, Obstacle obstacle) {
        // Base coin has no special side effect
    }

    public Image getImage() {
        return image;
    }
}
