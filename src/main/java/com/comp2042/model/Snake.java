package com.comp2042.model;

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

import javafx.scene.image.Image;

public class Snake {
    private static final Image IMG_HEAD =
            new Image(Snake.class.getResourceAsStream("/com/comp2042/images/snake/snakehead.png"));
    private static final Image IMG_BODY =
            new Image(Snake.class.getResourceAsStream("/com/comp2042/images/snake/snakebody.png"));
    private static final Image IMG_TURN =
            new Image(Snake.class.getResourceAsStream("/com/comp2042/images/snake/snaketurnleft.png"));
    private static final Image IMG_TAIL =
            new Image(Snake.class.getResourceAsStream("/com/comp2042/images/snake/snaketail.png"));

    private boolean dead = false;
    private final Point head = new Point();

    private final Deque<Point> body = new ArrayDeque<>();

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
        this.body.addLast(new Point(gameWidth / 2 + 1, gameHeight / 2));
    }

    public void move(char dir) {
        body.addLast(new Point(head));

        switch (dir) {
            case 'U' -> attemptMove(DIR_UP,    0, -1);
            case 'D' -> attemptMove(DIR_DOWN,  0,  1);
            case 'L' -> attemptMove(DIR_LEFT, -1,  0);
            case 'R' -> attemptMove(DIR_RIGHT, 1,  0);
        }

        if (body.size() > 4 && hitSelf() && !body.peekFirst().equals(head)) {
            dead = true;
        }
    }

    private void attemptMove(int dirAngle, int dx, int dy) {
        snakeDir = dirAngle;

        Point target = new Point(head.x + dx, head.y + dy);

        if (isOutOfBounds(target)) {
            if (borderWrap) {
                head.setLocation(wrapCoordinate(target));
            } else {
                dead = true;
            }
        } else if (obstacle.getObstacles().contains(target)) {
            dead = true;
        } else {
            head.setLocation(target);
        }
    }

    private Point wrapCoordinate(Point p) {
        int wx = ((p.x % gameWidth)  + gameWidth)  % gameWidth;
        int wy = ((p.y % gameHeight) + gameHeight) % gameHeight;
        return new Point(wx, wy);
    }

    public boolean ateCoin(Coin coin) {
        boolean ate = head.equals(coin.getPos()); // Bug #15: single evaluation
        if (!ate) {
            body.pollFirst(); // Bug #14: O(1) tail removal via Deque
        }
        return ate;
    }

    private boolean isOutOfBounds(Point p) {
        return p.x < 0 || p.y < 0 || p.x >= gameWidth || p.y >= gameHeight;
    }

    public boolean hitSelf() {
        return body.contains(head);
    }

    public int getBodyDir(int bodyCount) {
        if (bodyCount == 0) {
            return 0;
        }
        ArrayList<Point> bodyList = new ArrayList<>(body);
        if (bodyList.get(bodyCount).getY() == bodyList.get(bodyCount - 1).getY()) {
            return DIR_LEFT;
        } else {
            return DIR_UP;
        }
    }

    public void ignoreBorders(boolean wrap) {
        this.borderWrap = wrap;
    }

    public ArrayList<Point> getBody() {
        return new ArrayList<>(body);
    }
    
    public Point getHead() {
        return new Point(head);
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
