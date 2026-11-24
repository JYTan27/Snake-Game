package com.comp2042.controller;

import java.util.ArrayDeque;
import java.util.Deque;

import com.comp2042.model.Pickup;
import com.comp2042.model.DiamondPickup;
import com.comp2042.model.PickupFactory;
import com.comp2042.model.Game;
import com.comp2042.model.Obstacle;
import com.comp2042.model.Snake;
import com.comp2042.view.GameView;
import com.comp2042.service.audio.SoundEffect;
import com.comp2042.service.audio.SoundService;
import com.comp2042.model.stats.RunOutcome;
import com.comp2042.model.stats.RunSession;
import com.comp2042.service.leaderboard.LeaderboardService;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Coordinates keyboard input, timed movement, model updates, rendering, and audio
 * for one game scene. Uses internal arrow-key directions translated from WASD.
 * Starts or resumes on movement input and stops on pause, reset, death, victory,
 * or menu return. Event handling and animation callbacks run on the JavaFX application thread.
 */
public class GameController implements AutoCloseable {
    /** Maximum number of pending turns accepted between movement ticks. */
    private static final int MAX_BUFFERED_INPUTS = 2;

    /** Active snake, replaced when the game is reset. */
    private Snake snake;
    /** Active collectible, or {@code null} after placement exhaustion or victory. */
    private Pickup pickup;
    /** View initialised by scene construction before gameplay begins. */
    private GameView gameView;
    /** Shared game state, points, and mode configuration. */
    private Game game;
    /** Application stage used for fullscreen changes. */
    private Stage stage;
    /** Application-owned audio service; this controller stops but does not close it. */
    private final SoundService sounds;
    private final LeaderboardService leaderboards;
    private RunSession runSession;
    /** Length at the last settled board state, excluding a fatal move's temporary segment. */
    private int settledSnakeLength;
    /** Board obstacles cleared on reset. */
    private Obstacle obstacle;
    /** Navigation callback invoked after stopping gameplay and sound. */
    private final Runnable onReturnToMenu;

    /** Direction currently executed by movement ticks. */
    private KeyCode key = KeyCode.LEFT;
    /** Direction saved immediately before the most recently consumed turn. */
    private KeyCode lastKey = KeyCode.LEFT;
    /** Distinguishes a pause requested with P from initial, reset, or terminal stops. */
    private boolean pausedByPlayer;

    /** FIFO queue of up to two valid turns; additional input is ignored while full. */
    private final Deque<KeyCode> inputQueue = new ArrayDeque<>();

    /** Factory for mode-weighted replacement pickups. */
    private final PickupFactory pickupFactory = new PickupFactory();

    /**
     * Returns the current movement direction expressed as an arrow key.
     *
     * @return the current movement direction expressed as an arrow key
     */
    public KeyCode getCurrentKey() { return key; }
    /**
     * Returns the direction saved before the most recently consumed turn.
     *
     * @return the direction saved before the most recently consumed turn
     */
    public KeyCode getLastKey() { return lastKey; }

    /**
     * Connects the game entities, view, stage, and shared sound service.
     * Configures animation at the mode's base speed without starting playback.
     *
     * @param snake the initial snake
     * @param pickup the initial active pickup
     * @param game the game state and mode settings
     * @param gameView the view whose canvas and labels are initialised before use
     * @param obstacle the board's obstacle collection
     * @param stage the application stage
     * @param sounds the application-owned sound service
     * @param leaderboards application-owned result recording service
     * @param onReturnToMenu the callback used to navigate back to the menu
     * @throws NullPointerException if either shared service is null
     */
    public GameController(Snake snake, Pickup pickup, Game game, GameView gameView, Obstacle obstacle, Stage stage, SoundService sounds, LeaderboardService leaderboards, Runnable onReturnToMenu) {
        this.snake = snake;
        this.pickup = pickup;
        this.gameView = gameView;
        this.game = game;
        this.stage = stage;
        this.onReturnToMenu = onReturnToMenu;
        this.obstacle = obstacle;
        this.sounds = java.util.Objects.requireNonNull(sounds);
        this.leaderboards = java.util.Objects.requireNonNull(leaderboards);
        this.runSession = new RunSession(game.getMode());
        this.settledSnakeLength = snake.getBody().size() + 1;

        animation.setCycleCount(Animation.INDEFINITE);
        animation.setRate(game.getSpeed());
    }

    /**
     * Movement loop with a one-second keyframe scaled by the current moves-per-second
     * rate. Each tick consumes at most one turn, moves, resolves death or collection,
     * attempts replacement placement when needed, and refreshes the view.
     */
    private final Timeline animation = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
        if (snake.getDead() || game.gameWon()) {
            return;
        }

        // Consume next buffered turn at the start of this logic tick
        if (!inputQueue.isEmpty()) {
            lastKey = key;
            key = inputQueue.pollFirst();
            // Record the corner at the old head cell before movement changes its position.
            gameView.turnDir(snake, lastKey, key);
        }

        settledSnakeLength = snake.getBody().size() + 1;
        if (key.equals(KeyCode.LEFT)) {
            snake.move('L');
        } else if (key.equals(KeyCode.RIGHT)) {
            snake.move('R');
        } else if (key.equals(KeyCode.UP)) {
            snake.move('U');
        } else if (key.equals(KeyCode.DOWN)) {
            snake.move('D');
        }

        if (snake.getDead()) {
            // A fatal move must not resolve pickup growth or trigger collection audio.
            finishDeath();
            return;
        }

        if (snake.atePickup(pickup)) {
            settledSnakeLength = snake.getBody().size() + 1;
            collectPickup();
            spawnPickup();
            if (pickup == null) {
                finishGame();
                return;
            }
        }

        settledSnakeLength = snake.getBody().size() + 1;
        gameView.drawGrid(pickup, snake, obstacle, gameView.gc);

        gameView.gameWon();
        gameView.gameOver(snake);
        gameView.updateScoreLabel();
    }));

    /**
     * Handles WASD movement/start/resume, P to pause, Tab to restart, F to toggle
     * fullscreen, and Escape to return to the menu. Movement is disabled after
     * death or victory; other shortcuts remain available. Pause/resume sounds
     * occur only when entering or leaving a player-requested pause.
     *
     * @param event the key-pressed event from the game scene
     */
    public void handle(KeyEvent event) {
        KeyCode code = event.getCode();
        KeyCode direction = switch (code) {
            case W -> KeyCode.UP;
            case A -> KeyCode.LEFT;
            case S -> KeyCode.DOWN;
            case D -> KeyCode.RIGHT;
            default -> null;
        };

        if (direction != null && !game.gameWon() && !snake.getDead()) {
            runSession.startOrResume();
            if (pausedByPlayer) {
                pausedByPlayer = false;
                sounds.play(SoundEffect.UNPAUSE);
            }
            animation.play();

            // Enqueue valid direction if buffer has room
            if (inputQueue.size() < MAX_BUFFERED_INPUTS) {
                // Validate against the last queued turn so rapid sequences stay consistent.
                KeyCode reference = inputQueue.isEmpty() ? key : inputQueue.peekLast();
                if (isValidNextDirection(direction, reference)) {
                    inputQueue.addLast(direction);
                }
            }
        }

        if (code.equals(KeyCode.P) && animation.getStatus() == Animation.Status.RUNNING) {
            runSession.pause();
            animation.stop();
            pausedByPlayer = true;
            sounds.play(SoundEffect.PAUSE);
        }

        if (code.equals(KeyCode.TAB)) {
            reset();
        }

        if (code.equals(KeyCode.F)) {
            setFullScreen(!stage.isFullScreen());
        }

        if (code.equals(KeyCode.ESCAPE)) {
            close();
            onReturnToMenu.run();
        }
    }

    /**
     * Applies fullscreen state, redraws the board, and adjusts status-label styles.
     * Requires the view's canvas and labels to have been initialised.
     *
     * @param fullScreen whether to enter fullscreen or restore windowed display
     */
    public void setFullScreen(boolean fullScreen) {
        gameView.scaleToFullscreen(fullScreen, stage, obstacle, snake, pickup);
        String endLabelStyle = fullScreen ? "-fx-font-size: 50;" : "-fx-font-size: 15;";
        gameView.gameOverLabel.setStyle(endLabelStyle);
        gameView.gameWonLabel.setStyle(endLabelStyle);
        gameView.scoreLabel.setStyle(fullScreen ? "-fx-font-size: 35;" : "-fx-font-size: 15;");
    }

    /**
     * Ensures an input key is a valid perpendicular turn relative to the reference direction.
     * Prevents reversing into own body and duplicate keys.
     * The caller supplies arrow-key directions produced by the WASD mapping.
     *
     * @param newKey the proposed internal direction
     * @param referenceKey the current direction or last queued direction
     * @return {@code true} if the proposed direction is neither identical nor opposite
     */
    private boolean isValidNextDirection(KeyCode newKey, KeyCode referenceKey) {
        if (newKey.equals(referenceKey)) return false;
        if (newKey.equals(KeyCode.LEFT)  && referenceKey.equals(KeyCode.RIGHT)) return false;
        if (newKey.equals(KeyCode.RIGHT) && referenceKey.equals(KeyCode.LEFT))  return false;
        if (newKey.equals(KeyCode.DOWN)  && referenceKey.equals(KeyCode.UP))    return false;
        if (newKey.equals(KeyCode.UP)    && referenceKey.equals(KeyCode.DOWN))  return false;
        return true;
    }

    /**
     * Stops audio and animation and restores a fresh, paused game in the same mode.
     * Clears input, obstacles, points, completion, and sprite turn history; recreates
     * the snake and initial diamond; then restores base speed, leftward direction,
     * and the displayed labels and board. Plays restart audio after reset completes;
     * beginning movement afterward is not treated as resuming a player pause.
     */
    private void reset() {
        recordRun(RunOutcome.ABANDONED);
        pausedByPlayer = false;
        sounds.stopAll();
        animation.jumpTo(Duration.ZERO);
        animation.stop();

        inputQueue.clear();

        obstacle.reset();
        game.resetScore();
        snake = new Snake(game.getWidth(), game.getHeight(), game.isNoBorder(), obstacle);
        pickup  = new DiamondPickup(game.getWidth(), game.getHeight(), snake, obstacle);
        runSession = new RunSession(game.getMode());
        settledSnakeLength = snake.getBody().size() + 1;

        animation.setRate(game.getSpeed());
        key = KeyCode.LEFT;
        lastKey = KeyCode.LEFT;

        gameView.resetTurnArray();

        gameView.gameWon();
        gameView.gameOver(snake);
        gameView.updateScoreLabel();

        gameView.drawGrid(pickup, snake, obstacle, gameView.gc);
        sounds.play(SoundEffect.RESTART);
    }

    /** Delegates collection to the model, applies its returned speed, and plays the pickup sound. */
    private void collectPickup() {
        animation.setRate(game.collectPickup(pickup, snake, obstacle));
        sounds.play(SoundEffect.forPickup(pickup.getType()));
    }

    /** Replaces the active pickup, using {@code null} to signal that no free cell remains. */
    private void spawnPickup() {
        pickup = pickupFactory.createPickup(game, snake, obstacle).orElse(null);
    }

    /**
     * Records full-board completion once, stops gameplay, clears the pickup and
     * queued turns, refreshes the final display, and replaces other audio with the win sound.
     */
    private void finishGame() {
        if (game.gameWon()) return;
        recordRun(RunOutcome.WIN);
        pausedByPlayer = false;
        game.markWon();
        animation.stop();
        inputQueue.clear();
        pickup = null;
        gameView.drawGrid(pickup, snake, obstacle, gameView.gc);
        gameView.updateScoreLabel();
        gameView.gameWon();
        sounds.stopAll();
        sounds.play(SoundEffect.WIN);
    }

    /**
     * Stops movement after a fatal collision, clears queued turns, displays the
     * final pose and score, and replaces other audio with the death sound.
     */
    private void finishDeath() {
        recordRun(RunOutcome.LOSS);
        pausedByPlayer = false;
        animation.stop();
        inputQueue.clear();
        gameView.drawGrid(pickup, snake, obstacle, gameView.gc);
        gameView.gameOver(snake);
        gameView.updateScoreLabel();
        sounds.stopAll();
        sounds.play(SoundEffect.DEATH);
    }

    /** Finalises only started, unfinished runs; repeat lifecycle callbacks are safe. */
    private void recordRun(RunOutcome outcome) {
        runSession.finish(outcome, game.getDisplayScore(), settledSnakeLength).ifPresent(leaderboards::recordResult);
    }

    /** Records an abandoned run and stops gameplay during navigation or shutdown. */
    @Override
    public void close() {
        recordRun(RunOutcome.ABANDONED);
        pausedByPlayer = false;
        animation.stop();
        inputQueue.clear();
        sounds.stopAll();
    }
}
