package com.comp2042.model.pickup;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.comp2042.model.Game;
import com.comp2042.model.GridPoint;
import com.comp2042.model.Obstacle;
import com.comp2042.model.Snake;
import javafx.scene.image.Image;

/**
 * Base for collectibles with a grid position, sprite, points, and optional effects.
 * Construction selects a free cell; subclasses identify their type and override
 * only the reward or effect that differs from the defaults.
 */
public abstract class Pickup {
    /** Signals that the board has no unoccupied cell for a pickup. */
    public static class NoFreeCellException extends IllegalStateException {
        /** Creates an exception indicating that pickup placement is exhausted. */
        public NoFreeCellException() {
            super("No free cell is available for a pickup");
        }
    }

    /** Shared generator for placement and subclass effects, separate from type selection. */
    protected static final Random RANDOM = new Random();

    /** Chosen grid cell, excluding the snake and supplied obstacles. */
    private GridPoint pos;
    /** Sprite supplied by the concrete pickup subclass. */
    private final Image image;

    /**
     * Stores the sprite and places the pickup in a randomly selected free cell.
     *
     * @param gridWidth the board width in grid cells
     * @param gridHeight the board height in grid cells
     * @param snake the snake whose head and body cells must be excluded
     * @param obstacle the obstacles to exclude, or {@code null} if none are supplied
     * @param image the sprite used to draw this pickup
     * @throws NoFreeCellException if the board has no free cell
     */
    protected Pickup(int gridWidth, int gridHeight, Snake snake, Obstacle obstacle, Image image) {
        this.image = image;
        generatePosition(gridWidth, gridHeight, snake, obstacle);
    }

    /**
     * Enumerates unoccupied cells and selects one uniformly without retrying guesses.
     *
     * @param gridWidth the number of columns to search
     * @param gridHeight the number of rows to search
     * @param snake the snake occupying cells on the board
     * @param obstacle the obstacles to exclude, or {@code null}
     * @throws NoFreeCellException if no unoccupied cell exists
     */
    private void generatePosition(int gridWidth, int gridHeight, Snake snake, Obstacle obstacle) {
        // Mark the cells occupied by the snake and obstacles.
        List<GridPoint> occupied = new ArrayList<>();
        occupied.add(snake.getHead());
        occupied.addAll(snake.getBody());
        if (obstacle != null) {
            occupied.addAll(obstacle.getObstacles());
        }

        // Enumerate the remaining cells so placement can sample uniformly without retries.
        List<GridPoint> freeCells = new ArrayList<>();
        for (int cx = 0; cx < gridWidth; cx++) {
            for (int cy = 0; cy < gridHeight; cy++) {
                GridPoint candidate = new GridPoint(cx, cy);
                if (!occupied.contains(candidate)) {
                    freeCells.add(candidate);
                }
            }
        }

        // Explicit exhaustion lets the factory distinguish a full board from other failures.
        if (freeCells.isEmpty()) {
            throw new NoFreeCellException();
        }

        pos = freeCells.get(RANDOM.nextInt(freeCells.size()));
    }

    /**
     * Returns a defensive copy of this pickup's grid position.
     *
     * @return a defensive copy of this pickup's grid position
     */
    public GridPoint getPos() {
        return new GridPoint(pos);
    }

    /**
     * Returns the pickup identity used by configuration and sound selection.
     *
     * @return the pickup identity used by configuration and sound selection
     */
    public abstract PickupType getType();

    /**
     * Returns the display-point reward, which defaults to 5.
     *
     * @return the display-point reward, which defaults to 5
     */
    public int getPoints() {
        return 5;
    }

    /**
     * Returns the bonus in moves per second, which defaults to zero.
     *
     * @return the bonus in moves per second, which defaults to zero
     */
    public double getSpeedBonus() {
        return 0.0;
    }

    /**
     * Applies a subclass's special collection effect. The base implementation
     * does nothing; {@link Game#collectPickup(Pickup, Snake, Obstacle)} handles
     * awarding points and calculating movement speed separately.
     *
     * @param game the game receiving the collection
     * @param snake the snake receiving any temporary effect
     * @param obstacle the board's obstacles, used by effects that add barriers
     */
    public void applyEffect(Game game, Snake snake, Obstacle obstacle) {
        // Base pickup has no special side effect
    }

    /**
     * Returns the sprite supplied when this pickup was constructed.
     *
     * @return the sprite supplied when this pickup was constructed
     */
    public Image getImage() {
        return image;
    }
}
