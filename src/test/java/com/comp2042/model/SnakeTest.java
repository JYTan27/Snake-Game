package com.comp2042.model;

import com.comp2042.model.pickup.DiamondPickup;
import com.comp2042.model.pickup.Pickup;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnakeTest {

    private static final int WIDTH = 6;
    private static final int HEIGHT = 4;

    @ParameterizedTest
    @CsvSource({
        "LLL, L, 5, 2, 270",
        "URR, R, 0, 1, 90",
        "UU, U, 3, 3, 0",
        "D, D, 3, 0, 180"
    })
    void wrapsAtEachEdgeAndKeepsTailFacingTheDirectionOfTravel(
            String approach, char direction, int x, int y, int rotation) {
        Obstacle obstacles = new Obstacle(WIDTH, HEIGHT);
        Snake snake = new Snake(WIDTH, HEIGHT, true, obstacles);
        Pickup pickup = pickupAt(new GridPoint(0, 0), snake, obstacles);

        moveWithoutGrowing(snake, pickup, approach + direction);

        assertFalse(snake.getDead());
        assertEquals(new GridPoint(x, y), snake.getHead());
        assertEquals(1, snake.getBody().size());
        assertEquals(rotation, snake.getTailDir());
    }

    @ParameterizedTest
    @CsvSource({
        "LLL, L, 5, 2",
        "URR, R, 0, 1",
        "UU, U, 3, 3",
        "D, D, 3, 0"
    })
    void collidesWithAnObstacleAfterWrapping(String approach, char direction, int x, int y) {
        Obstacle obstacles = new Obstacle(WIDTH, HEIGHT) {
            @Override
            public List<GridPoint> getObstacles() {
                return List.of(new GridPoint(x, y));
            }
        };
        Snake snake = new Snake(WIDTH, HEIGHT, true, obstacles);
        Pickup pickup = pickupAt(new GridPoint(0, 0), snake, obstacles);
        moveWithoutGrowing(snake, pickup, approach);

        assertBlockedMovePreservesPose(snake, direction);
    }

    @Test
    void solidWallCollisionPreservesHeadBodyAndDirection() {
        Obstacle obstacles = new Obstacle(WIDTH, HEIGHT);
        Snake snake = new Snake(WIDTH, HEIGHT, false, obstacles);
        Pickup pickup = pickupAt(new GridPoint(0, 0), snake, obstacles);
        moveWithoutGrowing(snake, pickup, "UUL");

        assertBlockedMovePreservesPose(snake, 'U');
        assertBlockedMovePreservesPose(snake, 'D');
    }

    @Test
    void growingSnakeDiesOnContactWithItsOwnBody() {
        Obstacle obstacles = new Obstacle(WIDTH, HEIGHT);
        Snake snake = new Snake(WIDTH, HEIGHT, false, obstacles);
        collectAt(snake, obstacles, 'L', 2, 2);
        collectAt(snake, obstacles, 'U', 2, 1);
        collectAt(snake, obstacles, 'R', 3, 1);

        snake.move('D');

        assertTrue(snake.getDead());
    }

    @Test
    void snakeCanEnterTheTailCellAsTheTailMovesAway() {
        Obstacle obstacles = new Obstacle(WIDTH, HEIGHT);
        Snake snake = new Snake(WIDTH, HEIGHT, false, obstacles);
        collectAt(snake, obstacles, 'L', 2, 2);
        collectAt(snake, obstacles, 'U', 2, 1);
        collectAt(snake, obstacles, 'R', 3, 1);
        collectAt(snake, obstacles, 'R', 4, 1);
        Pickup pickup = pickupAt(new GridPoint(0, 0), snake, obstacles);

        moveWithoutGrowing(snake, pickup, "D");

        assertEquals(new GridPoint(4, 2), snake.getHead());
        assertEquals(5, snake.getBody().size());
        assertFalse(snake.hitSelf());
    }

    private static void assertBlockedMovePreservesPose(Snake snake, char direction) {
        GridPoint head = snake.getHead();
        List<GridPoint> body = snake.getBody().stream().map(GridPoint::new).toList();
        int rotation = snake.getSnakeDir();

        snake.move(direction);

        assertTrue(snake.getDead());
        assertEquals(head, snake.getHead());
        assertEquals(body, snake.getBody());
        assertEquals(rotation, snake.getSnakeDir());
    }

    private static void moveWithoutGrowing(Snake snake, Pickup pickup, String directions) {
        for (char direction : directions.toCharArray()) {
            snake.move(direction);
            assertFalse(snake.getDead(), "Expected a surviving move in direction " + direction);
            assertFalse(snake.atePickup(pickup), "No pickup is on the movement path");
        }
    }

    private static void collectAt(Snake snake, Obstacle obstacles, char direction, int x, int y) {
        Pickup pickup = pickupAt(new GridPoint(x, y), snake, obstacles);
        snake.move(direction);
        assertFalse(snake.getDead());
        assertTrue(snake.atePickup(pickup));
    }

    private static Pickup pickupAt(GridPoint cell, Snake snake, Obstacle obstacles) {
        return new DiamondPickup(WIDTH, HEIGHT, snake, obstacles) {
            @Override
            public GridPoint getPos() {
                return cell;
            }
        };
    }
}
