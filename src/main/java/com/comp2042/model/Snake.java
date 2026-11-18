package com.comp2042.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import javafx.scene.image.Image;

public class Snake {
    private static final Image IMG_HEAD = new Image(Snake.class.getResourceAsStream("/com/comp2042/images/snake/snakehead.png"));
    private static final Image IMG_BODY = new Image(Snake.class.getResourceAsStream("/com/comp2042/images/snake/snakebody.png"));
    private static final Image IMG_TURN = new Image(Snake.class.getResourceAsStream("/com/comp2042/images/snake/snaketurnleft.png"));
    private static final Image IMG_TAIL = new Image(Snake.class.getResourceAsStream("/com/comp2042/images/snake/snaketail.png"));

    private boolean dead = false;
    private final GridPoint head = new GridPoint(0, 0);

    private final Deque<GridPoint> body = new ArrayDeque<>();

    private final int gameWidth;
    private final int gameHeight;

    private boolean borderWrap;

    private final Obstacle obstacle;
    private int snakeDir = 270;

    private static final int DIR_UP    = 0;
    private static final int DIR_RIGHT = 90;
    private static final int DIR_DOWN  = 180;
    private static final int DIR_LEFT  = 270;

    public Snake(int gameWidth, int gameHeight, boolean noBorder, Obstacle obstacle) {
        this.obstacle = obstacle;
        this.borderWrap = noBorder;
        this.gameWidth  = gameWidth;
        this.gameHeight = gameHeight;
        this.head.setLocation(gameWidth / 2, gameHeight / 2);
        this.body.addLast(new GridPoint(gameWidth / 2 + 1, gameHeight / 2));
    }

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

        if (body.size() > 4 && hitSelf() && !body.peekFirst().equals(head)) {
            dead = true;
        }
    }

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

    private GridPoint wrapCoordinate(GridPoint p) {
        int wx = ((p.getX() % gameWidth)  + gameWidth)  % gameWidth;
        int wy = ((p.getY() % gameHeight) + gameHeight) % gameHeight;
        return new GridPoint(wx, wy);
    }

    public boolean atePickup(Pickup pickup) {
        boolean ate = head.equals(pickup.getPos());
        if (!ate) {
            body.pollFirst();
        }
        return ate;
    }

    private boolean isOutOfBounds(GridPoint p) {
        return p.getX() < 0 || p.getY() < 0 || p.getX() >= gameWidth || p.getY() >= gameHeight;
    }

    public boolean hitSelf() {
        return body.contains(head);
    }

    /** Returns a straight segment's rotation from two adjacent body points. */
    public int getBodyDir(GridPoint current, GridPoint previous) {
        if (current.getY() == previous.getY()) {
            return DIR_LEFT;
        } else {
            return DIR_UP;
        }
    }

    public int getTailDir() {
        if (body.isEmpty()) {
            return snakeDir;
        }

        Iterator<GridPoint> segments = body.iterator();
        GridPoint tail = segments.next();
        GridPoint next = segments.hasNext() ? segments.next() : head;

        int dx = next.getX() - tail.getX();
        int dy = next.getY() - tail.getY();

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

    public void ignoreBorders(boolean wrap) {
        this.borderWrap = wrap;
    }

    public List<GridPoint> getBody() {
        return new ArrayList<>(body);
    }
    
    public GridPoint getHead() {
        return new GridPoint(head);
    }

    public boolean getDead() {
        return dead;
    }

    public int getSnakeDir() {
        return snakeDir;
    }

    public Image getImageHead() {
        return IMG_HEAD;
    }

    public Image getImageBody() {
        return IMG_BODY;
    }

    public Image getImageTurn() {
        return IMG_TURN;
    }

    public Image getImageTail() {
        return IMG_TAIL;
    }
}
