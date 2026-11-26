package com.comp2042.model;

import com.comp2042.model.pickup.BoltPickup;
import com.comp2042.model.pickup.DiamondPickup;
import com.comp2042.model.pickup.Pickup;
import com.comp2042.model.pickup.RingPickup;
import com.comp2042.model.pickup.StarPickup;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameTest {

    private static final int WIDTH = 6;
    private static final int HEIGHT = 4;
    private final Obstacle obstacles = new Obstacle(WIDTH, HEIGHT);

    @Test
    void boltSpeedDoesNotStackAndExpiresOnTheNextPickup() {
        Game game = new Game(WIDTH, HEIGHT, GameMode.POWER_UPS);
        Snake snake = new Snake(WIDTH, HEIGHT, game.isNoBorder(), obstacles);
        Pickup bolt = new BoltPickup(WIDTH, HEIGHT, snake, obstacles);
        Pickup star = new StarPickup(WIDTH, HEIGHT, snake, obstacles);
        double boostedSpeed = game.getSpeed() + bolt.getSpeedBonus();

        assertTrue(boostedSpeed > game.getSpeed());
        assertEquals(boostedSpeed, game.collectPickup(bolt, snake, obstacles));
        assertEquals(boostedSpeed, game.collectPickup(bolt, snake, obstacles));
        assertEquals(game.getSpeed(), game.collectPickup(star, snake, obstacles));
        assertTrue(star.getPoints() > new DiamondPickup(WIDTH, HEIGHT, snake, obstacles).getPoints());
        assertEquals(2 * bolt.getPoints() + star.getPoints(), game.getDisplayScore());
    }

    @Test
    void ringEnablesWrappingUntilAnotherPickupIsCollected() {
        Game game = new Game(WIDTH, HEIGHT, GameMode.POWER_UPS);
        Snake snake = new Snake(WIDTH, HEIGHT, game.isNoBorder(), obstacles);
        Pickup ring = new RingPickup(WIDTH, HEIGHT, snake, obstacles);
        game.collectPickup(ring, snake, obstacles);

        crossLeftEdge(snake, ring);

        Pickup diamond = new DiamondPickup(WIDTH, HEIGHT, snake, obstacles);
        game.collectPickup(diamond, snake, obstacles);
        snake.move('U');
        assertFalse(snake.getDead());
        snake.atePickup(diamond);

        snake.move('R');

        assertTrue(snake.getDead(), "A diamond should restore the mode's solid borders");
        assertEquals(new GridPoint(WIDTH - 1, HEIGHT / 2 - 1), snake.getHead());
    }

    @Test
    void diamondRetainsWraparoundModesDefaultWrapping() {
        Game game = new Game(WIDTH, HEIGHT, GameMode.WRAPAROUND);
        Snake snake = new Snake(WIDTH, HEIGHT, game.isNoBorder(), obstacles);
        Pickup diamond = new DiamondPickup(WIDTH, HEIGHT, snake, obstacles);
        game.collectPickup(diamond, snake, obstacles);

        crossLeftEdge(snake, diamond);
    }

    @Test
    void pointsAloneDoNotWinAndResetRetainsModeRules() {
        Game game = new Game(WIDTH, HEIGHT, GameMode.HYPERSPEED);
        game.addPoints(1_000);

        assertFalse(game.gameWon());
        game.markWon();
        assertTrue(game.gameWon());

        game.resetScore();

        assertFalse(game.gameWon());
        assertEquals(0, game.getDisplayScore());
        assertEquals(GameMode.HYPERSPEED, game.getMode());
        assertTrue(game.getSpeed() > GameMode.CLASSIC.getSpeed());
    }

    private static void crossLeftEdge(Snake snake, Pickup pickup) {
        for (int step = 0; step <= WIDTH / 2; step++) {
            snake.move('L');
            assertFalse(snake.getDead());
            snake.atePickup(pickup);
        }
        assertEquals(new GridPoint(WIDTH - 1, HEIGHT / 2), snake.getHead());
    }
}
