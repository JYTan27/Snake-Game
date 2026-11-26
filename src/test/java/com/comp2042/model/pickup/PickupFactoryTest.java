package com.comp2042.model.pickup;

import com.comp2042.model.Game;
import com.comp2042.model.GameMode;
import com.comp2042.model.GridPoint;
import com.comp2042.model.Obstacle;
import com.comp2042.model.Snake;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PickupFactoryTest {

    @ParameterizedTest
    @MethodSource("modePickups")
    void spawnsOnlyTheCollectiblesAvailableInEachMode(GameMode mode, Set<PickupType> expected) {
        Game game = new Game(20, 20, mode);
        Obstacle obstacles = new Obstacle(20, 20);
        Snake snake = new Snake(20, 20, game.isNoBorder(), obstacles);
        obstacles.newObstacles(snake, new DiamondPickup(20, 20, snake, obstacles), 1);
        Set<PickupType> spawned = EnumSet.noneOf(PickupType.class);
        int totalWeight = mode.getPickupWeights().values().stream().mapToInt(Integer::intValue).sum();

        for (int roll = 0; roll < totalWeight; roll++) {
            Pickup pickup = factoryForRoll(roll).createPickup(game, snake, obstacles).orElseThrow();
            spawned.add(pickup.getType());
            assertFalse(snake.getHead().equals(pickup.getPos()));
            assertFalse(snake.getBody().contains(pickup.getPos()));
            assertFalse(obstacles.getObstacles().contains(pickup.getPos()));
        }

        assertEquals(expected, spawned);
    }

    private static Stream<Arguments> modePickups() {
        return Stream.of(
                Arguments.of(GameMode.CLASSIC, Set.of(PickupType.DIAMOND)),
                Arguments.of(GameMode.OBSTACLES, Set.of(PickupType.DIAMOND, PickupType.BLOCKS)),
                Arguments.of(GameMode.HYPERSPEED, Set.of(PickupType.DIAMOND)),
                Arguments.of(GameMode.POWER_UPS,
                        Set.of(PickupType.DIAMOND, PickupType.STAR, PickupType.RING, PickupType.BOLT)),
                Arguments.of(GameMode.WRAPAROUND, Set.of(PickupType.DIAMOND))
        );
    }

    @ParameterizedTest
    @CsvSource({"20, 20, 20", "11, 11, 6"})
    void replacesBlocksAtThePlacementCapAndAllowsThemAgainAfterRestart(int width, int height, int cap) {
        Game game = new Game(width, height, GameMode.OBSTACLES);
        Obstacle obstacles = new Obstacle(width, height);
        Snake snake = new Snake(width, height, false, obstacles);
        Pickup initial = new DiamondPickup(width, height, snake, obstacles);
        PickupFactory factory = factoryForBlockRoll();
        obstacles.newObstacles(snake, initial, cap - 1);

        Pickup blocks = factory.createPickup(game, snake, obstacles).orElseThrow();
        assertEquals(PickupType.BLOCKS, blocks.getType());
        game.collectPickup(blocks, snake, obstacles);

        assertEquals(cap, obstacles.getObstacles().size());
        assertFalse(obstacles.canAddObstacles());
        assertEquals(PickupType.DIAMOND, factory.createPickup(game, snake, obstacles).orElseThrow().getType());

        obstacles.reset();

        assertTrue(obstacles.getObstacles().isEmpty());
        assertEquals(PickupType.BLOCKS, factory.createPickup(game, snake, obstacles).orElseThrow().getType());
    }

    @Test
    void boardsWithNoObstacleCapacityNeverSpawnBlocks() {
        Game game = new Game(4, 4, GameMode.OBSTACLES);
        Obstacle obstacles = new Obstacle(4, 4);
        Snake snake = new Snake(4, 4, false, obstacles);

        assertFalse(obstacles.canAddObstacles());
        assertEquals(PickupType.DIAMOND,
                factoryForBlockRoll().createPickup(game, snake, obstacles).orElseThrow().getType());
    }

    @Test
    void obstaclePlacementUsesOnlyDistinctFreeCellsWithinTheBoard() {
        int width = 8;
        int height = 15;
        Obstacle obstacles = new Obstacle(width, height);
        Snake snake = new Snake(width, height, false, obstacles);
        Pickup pickup = new DiamondPickup(width, height, snake, obstacles);
        obstacles.newObstacles(snake, pickup, 2);

        List<GridPoint> firstBatch = List.copyOf(obstacles.getObstacles());
        obstacles.newObstacles(snake, pickup, 100);
        List<GridPoint> placed = obstacles.getObstacles();

        assertEquals(2, firstBatch.size());
        assertEquals(6, placed.size());
        assertTrue(placed.containsAll(firstBatch));
        assertEquals(placed.size(), new HashSet<>(placed).size());
        assertTrue(placed.stream().allMatch(cell ->
                cell.getX() >= 0 && cell.getX() < width && cell.getY() >= 0 && cell.getY() < height));
        assertFalse(placed.contains(snake.getHead()));
        assertFalse(placed.contains(pickup.getPos()));
        assertTrue(snake.getBody().stream().noneMatch(placed::contains));
    }

    @Test
    @Timeout(value = 5, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void collectingTheLastFreeCellGrowsTheSnakeAndExhaustsPickupPlacement() {
        Game game = new Game(3, 1, GameMode.CLASSIC);
        Obstacle obstacles = new Obstacle(3, 1);
        Snake snake = new Snake(3, 1, false, obstacles);
        Pickup pickup = new DiamondPickup(3, 1, snake, obstacles);
        assertEquals(new GridPoint(0, 0), pickup.getPos());

        snake.move('L');
        assertFalse(snake.getDead());
        assertTrue(snake.atePickup(pickup));
        game.collectPickup(pickup, snake, obstacles);

        assertEquals(3, snake.getBody().size() + 1);
        assertEquals(pickup.getPoints(), game.getDisplayScore());
        assertTrue(factoryForRoll(0).createPickup(game, snake, obstacles).isEmpty());
    }

    private static PickupFactory factoryForBlockRoll() {
        return new PickupFactory(new Random() {
            @Override
            public int nextInt(int bound) {
                return bound - 1;
            }
        });
    }

    private static PickupFactory factoryForRoll(int roll) {
        return new PickupFactory(new Random() {
            @Override
            public int nextInt(int bound) {
                return roll;
            }
        });
    }
}
