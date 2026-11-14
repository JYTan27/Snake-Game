package com.comp2042.controller;

import java.util.ArrayDeque;
import java.util.Deque;

import com.comp2042.model.Coin;
import com.comp2042.model.CoinFactory;
import com.comp2042.model.Game;
import com.comp2042.model.Obstacle;
import com.comp2042.model.Snake;
import com.comp2042.view.GameView;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GameController {
    private static final int MAX_BUFFERED_INPUTS = 2;

    private Snake snake;
    private Coin coin;
    private GameView gameView;
    private Game game;
    private Stage stage;
    private final String coinSoundPath = getClass().getResource("/com/comp2042/audio/coinSound.wav").toExternalForm();
    private final AudioClip coinSoundEffect = new AudioClip(coinSoundPath);
    private Obstacle obstacle;
    private final Runnable onReturnToMenu;

    private KeyCode key = KeyCode.LEFT;
    private KeyCode lastKey = KeyCode.LEFT;

    // FIFO input queue so rapid turns are never dropped
    private final Deque<KeyCode> inputQueue = new ArrayDeque<>();

    private final CoinFactory coinFactory = new CoinFactory();

    public KeyCode getCurrentKey() { return key; }
    public KeyCode getLastKey() { return lastKey; }

    public GameController(Snake snake, Coin coin, Game game, GameView gameView, Obstacle obstacle, Stage stage, Runnable onReturnToMenu) {
        this.snake = snake;
        this.coin = coin;
        this.gameView = gameView;
        this.game = game;
        this.stage = stage;
        this.onReturnToMenu = onReturnToMenu;
        this.obstacle = obstacle;

        animation.setCycleCount(Animation.INDEFINITE);
        animation.setRate(game.getSpeed());
    }

    private final Timeline animation = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
        if (snake.getDead() || game.gameWon()) {
            return;
        }

        // Consume next buffered turn at the start of this logic tick
        if (!inputQueue.isEmpty()) {
            lastKey = key;
            key = inputQueue.pollFirst();
            gameView.turnDir(snake, lastKey, key);
        }

        if (key.equals(KeyCode.LEFT)) {
            snake.move('L');
        } else if (key.equals(KeyCode.RIGHT)) {
            snake.move('R');
        } else if (key.equals(KeyCode.UP)) {
            snake.move('U');
        } else if (key.equals(KeyCode.DOWN)) {
            snake.move('D');
        }

        if (!snake.getDead() && snake.ateCoin(coin)) {
            powerCoin();
            game.incScore();
            coinSoundEffect.play();
            if (game.gameWon()) {
                finishGame();
                return;
            }
            newCoin();
            if (coin == null) {
                finishGame();
                return;
            }
        }

        if (!snake.getDead() || snake.hitSelf()) {
            gameView.drawGrid(coin, snake, obstacle, gameView.gc, false, 0);
        }

        gameView.gameWon();
        gameView.gameOver(snake);
        gameView.updateScoreLabel();
    }));

    public void handle(KeyEvent event) {
        KeyCode code = event.getCode();

        if (code.isArrowKey() && !game.gameWon()) {
            animation.play();

            // Enqueue valid direction if buffer has room
            if (inputQueue.size() < MAX_BUFFERED_INPUTS) {
                KeyCode reference = inputQueue.isEmpty() ? key : inputQueue.peekLast();
                if (isValidNextDirection(code, reference)) {
                    inputQueue.addLast(code);
                }
            }
        }

        if (code.equals(KeyCode.P)) {
            animation.stop();
        }

        if (code.equals(KeyCode.TAB)) {
            reset();
            gameView.scoreLabel.setText("Score: " + game.getDisplayScore());
        }

        if (code.equals(KeyCode.F)) {
            setFullScreen(!stage.isFullScreen());
        }

        if (code.equals(KeyCode.ESCAPE)) {
            animation.stop();
            onReturnToMenu.run();
        }
    }

    /** Applies the stage mode and matching board/label scale together. */
    public void setFullScreen(boolean fullScreen) {
        gameView.scaleToFullscreen(fullScreen, stage, obstacle, snake, coin);
        String endLabelStyle = fullScreen ? "-fx-font-size: 50;" : "-fx-font-size: 15;";
        gameView.gameOverLabel.setStyle(endLabelStyle);
        gameView.gameWonLabel.setStyle(endLabelStyle);
        gameView.scoreLabel.setStyle(fullScreen ? "-fx-font-size: 35;" : "-fx-font-size: 15;");
    }

    /**
     * Ensures an input key is a valid perpendicular turn relative to the reference direction.
     * Prevents reversing into own body and duplicate keys.
     */
    private boolean isValidNextDirection(KeyCode newKey, KeyCode referenceKey) {
        if (!newKey.isArrowKey()) return false;
        if (newKey.equals(referenceKey)) return false;
        if (newKey.equals(KeyCode.LEFT)  && referenceKey.equals(KeyCode.RIGHT)) return false;
        if (newKey.equals(KeyCode.RIGHT) && referenceKey.equals(KeyCode.LEFT))  return false;
        if (newKey.equals(KeyCode.DOWN)  && referenceKey.equals(KeyCode.UP))    return false;
        if (newKey.equals(KeyCode.UP)    && referenceKey.equals(KeyCode.DOWN))  return false;
        return true;
    }

    private void reset() {
        animation.jumpTo(Duration.ZERO);
        animation.stop();

        inputQueue.clear();

        obstacle.reset();
        game.resetScore();
        snake = new Snake(game.getWidth(), game.getHeight(), game.isNoBorder(), obstacle);
        coin  = new Coin(game.getWidth(), game.getHeight(), snake, obstacle);

        animation.setRate(game.getSpeed());
        key = KeyCode.LEFT;
        lastKey = KeyCode.LEFT;

        gameView.resetTurnArray();

        gameView.gameWon();
        gameView.gameOver(snake);
        gameView.updateScoreLabel();

        gameView.drawGrid(coin, snake, obstacle, gameView.gc, false, 0);
    }

    private void powerCoin() {
        animation.setRate(game.getSpeed() + coin.getSpeedBonus());
        snake.ignoreBorders(game.isNoBorder());
        coin.applyEffect(game, snake, obstacle);
        game.addPoints(coin.getPoints());
    }

    private void newCoin() {
        coin = coinFactory.createCoin(game, snake, obstacle).orElse(null);
    }

    private void finishGame() {
        game.markWon();
        animation.stop();
        inputQueue.clear();
        coin = null;
        gameView.drawGrid(coin, snake, obstacle, gameView.gc, false, 0);
        gameView.updateScoreLabel();
        gameView.gameWon();
    }
}
