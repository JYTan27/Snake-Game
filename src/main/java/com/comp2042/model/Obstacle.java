package com.comp2042.model;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Manages obstacle positions and their rendering colour for one board.
 * Random additions avoid occupied cells and are capped at one quarter of the
 * board area, rounded down. Obstacles persist until {@link #reset()} is called.
 */
public class Obstacle {
    /** Shared random source for choosing obstacle positions. */
    private static final Random RANDOM = new Random();

    /** Colour used by the view when drawing obstacle cells. */
    private Color color = Color.BLACK;
    /** Positions of all current obstacles. */
    private ArrayList<GridPoint> obstacles = new ArrayList<>();
    /** Board width in grid cells. */
    private int boardWidth;
    /** Board height in grid cells. */
    private int boardHeight;

    /**
     * Creates an empty obstacle collection for the supplied board dimensions.
     *
     * @param boardWidth the number of grid columns
     * @param boardHeight the number of grid rows
     */
    public Obstacle(int boardWidth, int boardHeight) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
    }

    /**
     * Adds up to the requested number of distinct obstacles in randomly chosen
     * free cells. Excludes the snake, the supplied pickup, and existing obstacles.
     * Stops at the obstacle cap or when free cells are exhausted. A non-positive
     * request or an already reached cap leaves the collection unchanged.
     *
     * @param snake the snake whose occupied cells must be excluded
     * @param pickup the pickup whose position must be excluded
     * @param amount the requested number of additional obstacles
     */
    public void newObstacles(Snake snake, Pickup pickup, int amount) {
        int remainingCapacity = boardWidth * boardHeight / 20 - obstacles.size();
        if (remainingCapacity <= 0 || amount <= 0) {
            return;
        }

        List<GridPoint> occupied = new ArrayList<>();
        occupied.add(snake.getHead());
        occupied.addAll(snake.getBody());
        occupied.add(pickup.getPos());
        occupied.addAll(obstacles);

        List<GridPoint> freeCells = new ArrayList<>();
        for (int cx = 0; cx < boardWidth; cx++) {
            for (int cy = 0; cy < boardHeight; cy++) {
                GridPoint candidate = new GridPoint(cx, cy);
                if (!occupied.contains(candidate)) {
                    freeCells.add(candidate);
                }
            }
        }

        int additions = Math.min(amount, remainingCapacity);
        for (int i = 0; i < additions && !freeCells.isEmpty(); i++) {
            int idx = RANDOM.nextInt(freeCells.size());
            // Removing the chosen candidate prevents duplicate positions in this batch.
            obstacles.add(freeCells.remove(idx));
        }
    }

    /**
     * Returns the colour used to render obstacle cells.
     *
     * @return the colour used to render obstacle cells
     */
    public Color getColor() {
        return color;
    }

    /**
     * Checks remaining obstacle capacity without searching for free placement cells.
     *
     * @return {@code true} if the obstacle count is below the proportional cap
     */
    public boolean canAddObstacles() {
        return obstacles.size() < boardWidth * boardHeight / 4;
    }

    /**
     * Returns a live, structurally unmodifiable view of obstacle positions.
     * The mutable points remain shared and should be treated as read-only.
     *
     * @return a list view reflecting subsequent additions and resets
     */
    public List<GridPoint> getObstacles() {
        return Collections.unmodifiableList(obstacles);
    }

    /** Removes all obstacles while retaining board dimensions and rendering colour. */
    public void reset() {
        obstacles.clear();
    }
}
