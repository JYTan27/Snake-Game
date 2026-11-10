package com.comp2042.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javafx.scene.image.Image;

public class Coin {
    protected static final Random RANDOM = new Random();
    private static final Image RED_COIN_IMAGE = new Image(Coin.class.getResourceAsStream("/com/comp2042/images/coins/redcoin.png"));

    private GridPoint pos;
    private final Image image;

    public Coin(int gridWidth, int gridHeight, Snake snek, Obstacle obstacle) {
        this(gridWidth, gridHeight, snek, obstacle, RED_COIN_IMAGE);
    }

    protected Coin(int gridWidth, int gridHeight, Snake snek, Obstacle obstacle, Image image) {
        this.image = image;
        generatePosition(gridWidth, gridHeight, snek, obstacle);
    }

    private void generatePosition(int gridWidth, int gridHeight, Snake snek, Obstacle obstacle) {
        List<GridPoint> occupied = new ArrayList<>();
        occupied.add(snek.getHead());
        occupied.addAll(snek.getBody());
        if (obstacle != null) {
            occupied.addAll(obstacle.getObstacles());
        }

        List<GridPoint> freeCells = new ArrayList<>();
        for (int cx = 0; cx < gridWidth; cx++) {
            for (int cy = 0; cy < gridHeight; cy++) {
                GridPoint candidate = new GridPoint(cx, cy);
                if (!occupied.contains(candidate)) {
                    freeCells.add(candidate);
                }
            }
        }

        if (freeCells.isEmpty()) {
            // No free cell available; place at (0,0) as fallback
            pos = new GridPoint(0, 0);
            return;
        }

        pos = freeCells.get(RANDOM.nextInt(freeCells.size()));
    }

    public GridPoint getPos() {
        return new GridPoint(pos);
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
