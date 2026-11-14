package com.comp2042.view;

import com.comp2042.controller.MainMenuController;
import com.comp2042.model.Coin;
import com.comp2042.model.Game;
import com.comp2042.model.GridPoint;
import com.comp2042.model.Obstacle;
import com.comp2042.model.Snake;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class GameView {

    private static final Color BACKGROUND_COLOR = Color.web("feffe4");

    public Canvas gridCanvas;
    public GraphicsContext gc;

    public Label gameOverLabel;
    public Label gameWonLabel;
    public Label scoreLabel;
    private boolean gameOverVis = false;
    private boolean gameWonVis  = false;

    private final Game game;
    private double initX;
    private double initY;
    private double fullScale = 1;
    private boolean fullsize = false;
    private final int[][] turnArray =
            new int[MainMenuController.getxValue()][MainMenuController.getyValue()];

    public GameView(Game game) {
        this.game = game;
    }

    public void makeScene(Scene scene, Snake snek, Coin coin) {
        gridCanvas = new Canvas(scene.getWidth(), scene.getHeight());
        initX = scene.getWidth();
        initY = scene.getHeight();
        gc = gridCanvas.getGraphicsContext2D();
        drawGrid(coin, snek, null, gc, false, 0);

        String css = getClass().getResource("/com/comp2042/view/styling.css").toExternalForm();

        gameOverLabel = new Label("GAME OVER");
        gameOverLabel.setId("gameOverLabel");
        gameOverLabel.getStylesheets().add(css);
        gameOverLabel.setVisible(false);

        gameWonLabel = new Label("YOU WON!");
        gameWonLabel.setId("gameWonLabel");
        gameWonLabel.getStylesheets().add(css);
        gameWonLabel.setVisible(false);

        scoreLabel = new Label("Score: " + game.getDisplayScore());
        scoreLabel.setId("scoreLabel");
        scoreLabel.getStylesheets().add(css);
        scoreLabel.setFont(new Font("Arial", 20));
        scoreLabel.setVisible(true);
    }

    public void scaleToFullscreen(boolean fullScreen, Stage primaryStage, Obstacle obstacle, Snake snek, Coin coin) {
        primaryStage.setFullScreen(fullScreen);

        double xScaling = Screen.getPrimary().getVisualBounds().getHeight() / initY;
        double yScaling = Screen.getPrimary().getVisualBounds().getWidth()  / initX;

        if (fullScreen && !fullsize) {
            fullScale = Math.min(xScaling, yScaling);
            gridCanvas.setWidth(gridCanvas.getWidth()   * fullScale);
            gridCanvas.setHeight(gridCanvas.getHeight() * fullScale);
            fullsize = true;
        } else if (!fullScreen) {
            fullScale = 1;
            gridCanvas.setWidth(initX);
            gridCanvas.setHeight(initY);
            fullsize = false;
        }

        gameOverLabel.setFont(new Font("Arial", 25 * fullScale));
        gameWonLabel.setFont(new Font("Arial", 25 * fullScale));
        drawGrid(coin, snek, obstacle, gc, false, 0);
    }

    public void drawGrid(Coin coin, Snake snek, Obstacle obstacle,
                         GraphicsContext gc, boolean a, int b) {
        gc.setFill(BACKGROUND_COLOR);
        gc.fillRect(0, 0, gridCanvas.getWidth(), gridCanvas.getHeight());

        double cellSize = game.getMultiplier() * fullScale; // Bug #9: single scale factor

        // Draw coin
        if (coin != null) {
            gc.drawImage(
                coin.getImage(),
                coin.getPos().getX() * cellSize,
                coin.getPos().getY() * cellSize,
                cellSize - 3 * fullScale,
                cellSize - 3 * fullScale
            );
        }

        // Draw body segments
        int bodyIndex = 0;
        for (GridPoint point : snek.getBody()) {
            drawBody(snek, point, bodyIndex, cellSize);
            bodyIndex++;
        }

        // Draw head
        drawHead(snek, cellSize);

        // Draw obstacles
        if (obstacle != null) {
            gc.setFill(obstacle.getColor());
            for (GridPoint point : obstacle.getObstacles()) {
                gc.fillRect(point.getX() * cellSize, point.getY() * cellSize, cellSize, cellSize);
            }
        }
    }

    private void drawHead(Snake snek, double cellSize) {
        Image img = snek.getImageHead();
        double x   = snek.getHead().getX() * cellSize;
        double y   = snek.getHead().getY() * cellSize;
        double cx  = x + cellSize / 2;
        double cy  = y + cellSize / 2;

        gc.save();
        gc.translate(cx, cy);
        gc.rotate(snek.getSnakeDir());
        gc.drawImage(img, -cellSize / 2, -cellSize / 2, cellSize, cellSize);
        gc.restore();
    }

    private void drawBody(Snake snek, GridPoint point, int index, double cellSize) {
        Image img;
        double rotateDeg;

        if (index == 0) {
            img       = snek.getImageTail();
            rotateDeg = tailRotation(snek);
        } else if (index > 0 &&
                   turnArray[snek.getBody().get(index).getX()]
                             [snek.getBody().get(index).getY()] != 0) {
            img       = snek.getImageTurn();
            rotateDeg = 90.0 * turnArray[snek.getBody().get(index).getX()]
                                         [snek.getBody().get(index).getY()];
        } else {
            img       = snek.getImageBody();
            rotateDeg = snek.getBodyDir(index);
        }

        // Clear stale turn entry when the tail segment leaves a turn cell
        if (turnArray[snek.getBody().get(0).getX()]
                      [snek.getBody().get(0).getY()] != 0) {
            turnArray[snek.getBody().get(0).getX()]
                      [snek.getBody().get(0).getY()] = 0;
        }

        double x  = point.getX() * cellSize;
        double y  = point.getY() * cellSize;
        double cx = x + cellSize / 2;
        double cy = y + cellSize / 2;

        gc.save();
        gc.translate(cx, cy);
        gc.rotate(rotateDeg);
        gc.drawImage(img, -cellSize / 2, -cellSize / 2, cellSize, cellSize);
        gc.restore();
    }

    private double tailRotation(Snake snek) {
        return snek.getTailDir();
    }

    public void turnDir(Snake snek, KeyCode lastKey, KeyCode currentKey) {
        if ((lastKey == KeyCode.LEFT  && currentKey == KeyCode.UP)   ||
            (lastKey == KeyCode.DOWN  && currentKey == KeyCode.RIGHT)) {
            turnArray[snek.getHead().getX()][snek.getHead().getY()] = 2;
        } else if ((lastKey == KeyCode.LEFT  && currentKey == KeyCode.DOWN) ||
                   (lastKey == KeyCode.UP    && currentKey == KeyCode.RIGHT)) {
            turnArray[snek.getHead().getX()][snek.getHead().getY()] = 3;
        } else if ((lastKey == KeyCode.RIGHT && currentKey == KeyCode.UP)   ||
                   (lastKey == KeyCode.DOWN  && currentKey == KeyCode.LEFT)) {
            turnArray[snek.getHead().getX()][snek.getHead().getY()] = 1;
        } else if ((lastKey == KeyCode.UP    && currentKey == KeyCode.LEFT)  ||
                   (lastKey == KeyCode.RIGHT && currentKey == KeyCode.DOWN)) {
            turnArray[snek.getHead().getX()][snek.getHead().getY()] = 4;
        } else {
            turnArray[snek.getHead().getX()][snek.getHead().getY()] = 0;
        }
    }

    public void resetTurnArray() {
        for (int[] row : turnArray) {
            java.util.Arrays.fill(row, 0);
        }
    }

    public void gameOver(Snake snek) {
        if (snek.getDead() && !gameOverVis) {
            gameOverVis = true;
            gameOverLabel.setVisible(true);
        } else if (!snek.getDead() && gameOverVis) {
            gameOverLabel.setVisible(false);
            gameOverVis = false;
        }
    }

    public void gameWon() {
        if (game.gameWon() && !gameWonVis) {
            gameWonVis = true;
            gameWonLabel.setVisible(true);
        } else if (!game.gameWon() && gameWonVis) {
            gameWonVis = false;
            gameWonLabel.setVisible(false);
        }
    }

    public void updateScoreLabel() {
        scoreLabel.setText("Score: " + game.getDisplayScore());
    }
}
