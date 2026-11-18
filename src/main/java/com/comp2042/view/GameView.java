package com.comp2042.view;

import com.comp2042.controller.MainMenuController;
import com.comp2042.model.Pickup;
import com.comp2042.model.Game;
import com.comp2042.model.GridPoint;
import com.comp2042.model.Obstacle;
import com.comp2042.model.Snake;
import com.comp2042.view.board.BoardRenderer;
import com.comp2042.view.board.BoardTheme;
import com.comp2042.view.board.CheckerboardBoardRenderer;

import java.util.List;
import java.util.Objects;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class GameView {

    public Canvas gridCanvas;
    public GraphicsContext gc;

    public Label gameOverLabel;
    public Label gameWonLabel;
    public Label scoreLabel;

    private final Game game;
    private final BoardRenderer boardRenderer;
    private final BoardTheme boardTheme;
    private double initX;
    private double initY;
    private double fullScale = 1;
    private boolean fullsize = false;
    private final int[][] turnArray =
            new int[MainMenuController.getxValue()][MainMenuController.getyValue()];

    public GameView(Game game) {
        this(game, new CheckerboardBoardRenderer(), BoardTheme.SOFT_CREAM);
    }

    public GameView(Game game, BoardRenderer boardRenderer, BoardTheme boardTheme) {
        this.game = game;
        this.boardRenderer = Objects.requireNonNull(boardRenderer);
        this.boardTheme = Objects.requireNonNull(boardTheme);
    }

    public void makeScene(Scene scene, Snake snake, Pickup pickup) {
        gridCanvas = new Canvas(scene.getWidth(), scene.getHeight());
        initX = scene.getWidth();
        initY = scene.getHeight();
        gc = gridCanvas.getGraphicsContext2D();
        drawGrid(pickup, snake, null, gc);

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

    public void scaleToFullscreen(boolean fullScreen, Stage primaryStage, Obstacle obstacle, Snake snake, Pickup pickup) {
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
        drawGrid(pickup, snake, obstacle, gc);
    }

    public void drawGrid(Pickup pickup, Snake snake, Obstacle obstacle, GraphicsContext gc) {
        double cellSize = game.getMultiplier() * fullScale; // Bug #9: single scale factor
        boardRenderer.draw(gc, game.getWidth(), game.getHeight(), cellSize, boardTheme);

        // Centre the pickup sprite; keep pixel-art sampling local to this draw.
        if (pickup != null) {
            double pickupSize = cellSize - 3 * fullScale;
            double inset = (cellSize - pickupSize) / 2;
            gc.save();
            gc.setImageSmoothing(false);
            gc.drawImage(
                pickup.getImage(),
                pickup.getPos().getX() * cellSize + inset,
                pickup.getPos().getY() * cellSize + inset,
                pickupSize,
                pickupSize
            );
            gc.restore();
        }

        // Reuse one body snapshot for the entire redraw.
        List<GridPoint> body = snake.getBody();
        if (!body.isEmpty()) {
            GridPoint tail = body.getFirst();
            turnArray[tail.getX()][tail.getY()] = 0;
        }
        for (int bodyIndex = 0; bodyIndex < body.size(); bodyIndex++) {
            drawBody(snake, body, bodyIndex, cellSize);
        }

        // Draw head
        drawHead(snake, cellSize);

        // Draw obstacles
        if (obstacle != null) {
            gc.setFill(obstacle.getColor());
            for (GridPoint point : obstacle.getObstacles()) {
                gc.fillRect(point.getX() * cellSize, point.getY() * cellSize, cellSize, cellSize);
            }
        }
    }

    private void drawHead(Snake snake, double cellSize) {
        Image img = snake.getImageHead();
        double x   = snake.getHead().getX() * cellSize;
        double y   = snake.getHead().getY() * cellSize;
        double cx  = x + cellSize / 2;
        double cy  = y + cellSize / 2;

        gc.save();
        gc.translate(cx, cy);
        gc.rotate(snake.getSnakeDir());
        gc.drawImage(img, -cellSize / 2, -cellSize / 2, cellSize, cellSize);
        gc.restore();
    }

    private void drawBody(Snake snake, List<GridPoint> body, int index, double cellSize) {
        GridPoint point = body.get(index);
        int turn = turnArray[point.getX()][point.getY()];
        Image img;
        double rotateDeg;

        if (index == 0) {
            img       = snake.getImageTail();
            rotateDeg = snake.getTailDir();
        } else if (turn != 0) {
            img       = snake.getImageTurn();
            rotateDeg = 90.0 * turn;
        } else {
            img       = snake.getImageBody();
            rotateDeg = snake.getBodyDir(point, body.get(index - 1));
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

    public void turnDir(Snake snake, KeyCode lastKey, KeyCode currentKey) {
        if ((lastKey == KeyCode.LEFT  && currentKey == KeyCode.UP)   ||
            (lastKey == KeyCode.DOWN  && currentKey == KeyCode.RIGHT)) {
            turnArray[snake.getHead().getX()][snake.getHead().getY()] = 2;
        } else if ((lastKey == KeyCode.LEFT  && currentKey == KeyCode.DOWN) ||
                   (lastKey == KeyCode.UP    && currentKey == KeyCode.RIGHT)) {
            turnArray[snake.getHead().getX()][snake.getHead().getY()] = 3;
        } else if ((lastKey == KeyCode.RIGHT && currentKey == KeyCode.UP)   ||
                   (lastKey == KeyCode.DOWN  && currentKey == KeyCode.LEFT)) {
            turnArray[snake.getHead().getX()][snake.getHead().getY()] = 1;
        } else if ((lastKey == KeyCode.UP    && currentKey == KeyCode.LEFT)  ||
                   (lastKey == KeyCode.RIGHT && currentKey == KeyCode.DOWN)) {
            turnArray[snake.getHead().getX()][snake.getHead().getY()] = 4;
        } else {
            turnArray[snake.getHead().getX()][snake.getHead().getY()] = 0;
        }
    }

    public void resetTurnArray() {
        for (int[] row : turnArray) {
            java.util.Arrays.fill(row, 0);
        }
    }

    public void gameOver(Snake snake) {
        gameOverLabel.setVisible(snake.getDead());
    }

    public void gameWon() {
        gameWonLabel.setVisible(game.gameWon());
    }

    public void updateScoreLabel() {
        scoreLabel.setText("Score: " + game.getDisplayScore());
    }
}
