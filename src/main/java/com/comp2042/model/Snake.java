package com.comp2042.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import javafx.scene.image.Image;

/**
 * Stores the snake's grid positions, movement direction, wrapping rule, and death state.
 * Body entries are ordered from tail to neck and exclude the head. The controller
 * prevents reverse turns and resolves pickup collection after each surviving move.
 * Also supplies cached sprites and orientation calculations used by the view.
 */
public class Snake {
    /** Shared head sprite loaded once for all snakes. */
    private static final Image IMG_HEAD = new Image(Snake.class.getResourceAsStream("/com/comp2042/images/snake/snakehead.png"));
    /** Shared straight body sprite. */
    private static final Image IMG_BODY = new Image(Snake.class.getResourceAsStream("/com/comp2042/images/snake/snakebody.png"));
    /** Shared corner sprite, rotated by the view to match each turn. */
    private static final Image IMG_TURN = new Image(Snake.class.getResourceAsStream("/com/comp2042/images/snake/snaketurnleft.png"));
    /** Shared tail sprite. */
    private static final Image IMG_TAIL = new Image(Snake.class.getResourceAsStream("/com/comp2042/images/snake/snaketail.png"));

    /** Whether a fatal collision has occurred. */
    private boolean dead = false;
    /** Mutable head position in grid cells. */
    private final GridPoint head = new GridPoint(0, 0);

    /** Body positions with the tail first and the segment nearest the head last. */
    private final Deque<GridPoint> body = new ArrayDeque<>();

    /** Number of grid columns used for bounds and wrapping calculations. */
    private final int gameWidth;
    /** Number of grid rows used for bounds and wrapping calculations. */
    private final int gameHeight;

    /** Whether crossing an edge wraps to the opposite side instead of causing death. */
    private boolean borderWrap;

    /** Obstacles checked when validating the next head position. */
    private final Obstacle obstacle;
    /** Current head sprite rotation in clockwise degrees, initially facing left. */
    private int snakeDir = 270;

    /** Head sprite rotation for upward movement. */
    private static final int DIR_UP    = 0;
    /** Head sprite rotation for rightward movement. */
    private static final int DIR_RIGHT = 90;
    /** Head sprite rotation for downward movement. */
    private static final int DIR_DOWN  = 180;
    /** Head sprite rotation for leftward movement. */
    private static final int DIR_LEFT  = 270;

    /**
     * Creates a left-facing snake with its head at the board centre and one
     * body segment immediately to the right.
     *
     * @param gameWidth the board width in grid cells
     * @param gameHeight the board height in grid cells
     * @param noBorder whether border wrapping is initially enabled
     * @param obstacle the board's obstacle collection
     */
    public Snake(int gameWidth, int gameHeight, boolean noBorder, Obstacle obstacle) {
        this.obstacle = obstacle;
        this.borderWrap = noBorder;
        this.gameWidth  = gameWidth;
        this.gameHeight = gameHeight;
        this.head.setLocation(gameWidth / 2, gameHeight / 2);
        this.body.addLast(new GridPoint(gameWidth / 2 + 1, gameHeight / 2));
    }

    /**
     * Attempts one grid-cell move and appends the old head position to the body
     * after successful movement. Blocked border or obstacle collisions mark the
     * snake dead without changing its pose. Dead snakes and unknown directions
     * do not move. Reverse-turn prevention is the caller's responsibility.
     * After a successful move that leaves the snake alive, call
     * {@link #atePickup(Pickup)} once to resolve tail removal or growth.
     *
     * @param dir {@code 'U'}, {@code 'D'}, {@code 'L'}, or {@code 'R'}
     */
    public void move(char dir) {
        if (dead) {
            return;
        }

        GridPoint previousHead = new GridPoint(head);
        boolean moved = switch (dir) {
            case 'U' -> attemptMove(DIR_UP,    0, -1);
            case 'D' -> attemptMove(DIR_DOWN,  0,  1);
            case 'L' -> attemptMove(DIR_LEFT, -1,  0);
            case 'R' -> attemptMove(DIR_RIGHT, 1,  0);
            default -> false;
        };

        if (!moved) {
            return;
        }

        body.addLast(previousHead);

        // The current tail cell is allowed because a non-growing move vacates it.
        if (body.size() > 4 && hitSelf() && !body.peekFirst().equals(head)) {
            dead = true;
        }
    }

    /**
     * Validates the destination before updating the head and direction.
     * Checks obstacles after resolving any border wrap.
     *
     * @param dirAngle the new head rotation in degrees
     * @param dx the horizontal displacement in grid cells
     * @param dy the vertical displacement in grid cells
     * @return {@code true} if the head moved; {@code false} after a fatal blocked move
     */
    private boolean attemptMove(int dirAngle, int dx, int dy) {
        GridPoint target = new GridPoint(head.getX() + dx, head.getY() + dy);

        if (isOutOfBounds(target)) {
            if (!borderWrap) {
                dead = true;
                return false;
            }
            target = wrapCoordinate(target);
        }

        if (obstacle.getObstacles().contains(target)) {
            dead = true;
            return false;
        }

        head.setLocation(target);
        snakeDir = dirAngle;
        return true;
    }

    /**
     * Maps a position into the board using modular arithmetic.
     *
     * @param p the position that may lie outside the board
     * @return a new point inside the board's bounds
     */
    private GridPoint wrapCoordinate(GridPoint p) {
        int wx = ((p.getX() % gameWidth)  + gameWidth)  % gameWidth;
        int wy = ((p.getY() % gameHeight) + gameHeight) % gameHeight;
        return new GridPoint(wx, wy);
    }

    /**
     * Checks collection and resolves growth after a surviving move. Removes the
     * tail when no pickup was collected; otherwise retains it to grow the snake.
     * Call exactly once after each successful move while the snake is alive.
     *
     * @param pickup the active pickup whose position is compared with the head
     * @return {@code true} if the head occupies the pickup's cell
     */
    public boolean atePickup(Pickup pickup) {
        boolean ate = head.equals(pickup.getPos());
        if (!ate) {
            body.pollFirst();
        }
        return ate;
    }

    /**
     * Checks a point against the board's zero-based cell bounds.
     *
     * @param p the position to check
     * @return {@code true} if either coordinate lies outside the board
     */
    private boolean isOutOfBounds(GridPoint p) {
        return p.getX() < 0 || p.getY() < 0 || p.getX() >= gameWidth || p.getY() >= gameHeight;
    }

    /**
     * Checks whether the head overlaps any stored body position, including the tail.
     * This query does not change the death state.
     *
     * @return {@code true} if the body contains the head's position
     */
    public boolean hitSelf() {
        return body.contains(head);
    }

    /**
     * Calculates the sprite rotation for a straight segment from adjacent points.
     *
     * @param current the segment being drawn
     * @param previous its neighbouring segment toward the tail
     * @return 270 degrees for points on the same row, or 0 degrees otherwise
     */
    public int getBodyDir(GridPoint current, GridPoint previous) {
        if (current.getY() == previous.getY()) {
            return DIR_LEFT;
        } else {
            return DIR_UP;
        }
    }

    /**
     * Calculates the tail rotation toward its neighbour, using the head when
     * only one body segment exists. Returns the head rotation for an empty body
     * or coincident points. Uses shortest wrapped displacement across board edges.
     *
     * @return the tail rotation in clockwise degrees: 0, 90, 180, or 270
     */
    public int getTailDir() {
        if (body.isEmpty()) {
            return snakeDir;
        }

        Iterator<GridPoint> segments = body.iterator();
        GridPoint tail = segments.next();
        GridPoint next = segments.hasNext() ? segments.next() : head;

        int dx = next.getX() - tail.getX();
        int dy = next.getY() - tail.getY();

        // Large coordinate jumps across an edge represent a short wrapped step.
        if (dx > gameWidth / 2) {
            dx -= gameWidth;
        } else if (dx < -gameWidth / 2) {
            dx += gameWidth;
        }

        if (dy > gameHeight / 2) {
            dy -= gameHeight;
        } else if (dy < -gameHeight / 2) {
            dy += gameHeight;
        }

        if (dy < 0) {
            return DIR_UP;
        } else if (dy > 0) {
            return DIR_DOWN;
        } else if (dx < 0) {
            return DIR_LEFT;
        } else if (dx > 0) {
            return DIR_RIGHT;
        }
        return snakeDir;
    }

    /**
     * Updates the wrapping rule, for example when applying or expiring a ring effect.
     *
     * @param wrap whether crossing a border should wrap to the opposite edge
     */
    public void ignoreBorders(boolean wrap) {
        this.borderWrap = wrap;
    }

    /**
     * Returns a shallow list copy ordered from tail to neck, excluding the head.
     * Changing the list structure does not affect the snake, but the mutable points
     * are shared with its internal body and should be treated as read-only.
     *
     * @return a new list containing the current body points
     */
    public List<GridPoint> getBody() {
        return new ArrayList<>(body);
    }
    
    /**
     * Returns a defensive copy of the head position.
     *
     * @return a new point with the head's current grid coordinates
     */
    public GridPoint getHead() {
        return new GridPoint(head);
    }

    /**
     * Reports the recorded death state.
     *
     * @return {@code true} after a fatal collision
     */
    public boolean getDead() {
        return dead;
    }

    /**
     * Returns the head sprite rotation for the most recent successful move.
     *
     * @return the clockwise rotation in degrees, initially 270
     */
    public int getSnakeDir() {
        return snakeDir;
    }

    /**
     * Returns the shared head sprite.
     *
     * @return the shared head sprite
     */
    public Image getImageHead() {
        return IMG_HEAD;
    }

    /**
     * Returns the shared straight body sprite.
     *
     * @return the shared straight body sprite
     */
    public Image getImageBody() {
        return IMG_BODY;
    }

    /**
     * Returns the shared corner sprite.
     *
     * @return the shared corner sprite
     */
    public Image getImageTurn() {
        return IMG_TURN;
    }

    /**
     * Returns the shared tail sprite.
     *
     * @return the shared tail sprite
     */
    public Image getImageTail() {
        return IMG_TAIL;
    }
}
