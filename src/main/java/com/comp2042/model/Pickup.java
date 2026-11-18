package com.comp2042.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javafx.scene.image.Image;

public abstract class Pickup {
    /** Signals that the board has no unoccupied cell for a pickup. */
    public static class NoFreeCellException extends IllegalStateException {
        public NoFreeCellException() {
            super("No free cell is available for a pickup");
        }
    }

    protected static final Random RANDOM = new Random();

    private GridPoint pos;
    private final Image image;

    protected Pickup(int gridWidth, int gridHeight, Snake snake, Obstacle obstacle, Image image) {
        this.image = image;
        generatePosition(gridWidth, gridHeight, snake, obstacle);
    }

    private void generatePosition(int gridWidth, int gridHeight, Snake snake, Obstacle obstacle) {
        List<GridPoint> occupied = new ArrayList<>();
        occupied.add(snake.getHead());
        occupied.addAll(snake.getBody());
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
            throw new NoFreeCellException();
        }

        pos = freeCells.get(RANDOM.nextInt(freeCells.size()));
    }

    public GridPoint getPos() {
        return new GridPoint(pos);
    }

    public abstract PickupType getType();

    public int getPoints() {
        return 5;
    }

    public double getSpeedBonus() {
        return 0.0;
    }

    public void applyEffect(Game game, Snake snake, Obstacle obstacle) {
        // Base pickup has no special side effect
    }

    public Image getImage() {
        return image;
    }
}
