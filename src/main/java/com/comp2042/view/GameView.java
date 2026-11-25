package com.comp2042.view;

import com.comp2042.controller.MainMenuController;
import com.comp2042.model.pickup.Pickup;
import com.comp2042.model.Game;
import com.comp2042.model.GridPoint;
import com.comp2042.model.Obstacle;
import com.comp2042.model.Snake;
import com.comp2042.view.board.BoardRenderer;
import com.comp2042.view.board.BoardTheme;
import com.comp2042.view.board.CheckerboardBoardRenderer;

import java.util.List;
import java.util.Objects;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;
import javafx.stage.Screen;

/**
 * Renders the board, snake, pickups, obstacles, score, and end-game messages.
 * Maintains sprite turn history and scales the canvas for fullscreen display.
 * Call {@link #initialize(Snake, Pickup)} before using methods that draw
 * on the canvas or update labels. UI updates must run on the JavaFX application thread.
 */
public class GameView {

    /** Canvas containing the board and gameplay sprites, created by {@code initialize}. */
    public Canvas gridCanvas;
    /** Drawing context for {@link #gridCanvas}, initialised by {@code initialize}. */
    public GraphicsContext gc;

    /** Message whose visibility reflects the snake's death state. */
    public Label gameOverLabel;
    /** Message whose visibility reflects the game's recorded completion state. */
    public Label gameWonLabel;
    /** Label displaying the current points total. */
    public Label scoreLabel;

    /** Game state and board dimensions used for rendering. */
    private final Game game;
    /** Strategy that draws the board background. */
    private final BoardRenderer boardRenderer;
    /** Colour palette supplied to the board renderer. */
    private final BoardTheme boardTheme;
    /** Original canvas width in pixels, used when restoring windowed display. */
    private final double baseWidth;
    /** Original canvas height in pixels, used when restoring windowed display. */
    private final double baseHeight;
    /** Scale applied to the base cell size; {@code 1} represents windowed display. */
    private double fullScale = 1;
    /**
     * Corner sprite rotations indexed by grid column and row. Zero means no
     * recorded corner; values one through four represent multiples of 90 degrees.
     * Dimensions follow the board size configured by the main menu.
     */
    private final int[][] turnArray = new int[MainMenuController.getxValue()][MainMenuController.getyValue()];

    /**
     * Creates a view using the checkerboard pattern and Soft Cream palette.
     *
     * @param game the game whose state and dimensions will be displayed
     */
    public GameView(Game game) {
        this(game, new CheckerboardBoardRenderer(), BoardTheme.SOFT_CREAM);
    }

    /**
     * Creates a view with the supplied board appearance. Canvas and label
     * creation is deferred until {@code initialize} is called.
     *
     * @param game the game whose state and dimensions will be displayed
     * @param boardRenderer the board background renderer
     * @param boardTheme the palette used by the board renderer
     * @throws NullPointerException if {@code boardRenderer} or {@code boardTheme} is null
     */
    public GameView(Game game, BoardRenderer boardRenderer, BoardTheme boardTheme) {
        this.game = game;
        this.boardRenderer = Objects.requireNonNull(boardRenderer);
        this.boardTheme = Objects.requireNonNull(boardTheme);
        baseWidth = game.getWidth() * game.getMultiplier();
        baseHeight = game.getHeight() * game.getMultiplier();
    }

    /**
     * Creates the canvas and labels and draws the initial board without obstacles.
     * Uses the game's board dimensions for the initial canvas size.
     * The caller adds the created canvas and labels to the gameplay layout.
     *
     * @param snake the snake to draw in its initial pose
     * @param pickup the initial pickup, or {@code null} to omit it
     */
    public void initialize(Snake snake, Pickup pickup) {
        gridCanvas = new Canvas(baseWidth, baseHeight);
        gc = gridCanvas.getGraphicsContext2D();
        drawGrid(pickup, snake, null, gc);

        // Resolve the packaged stylesheet for the labels drawn over the board.
        String css = Objects.requireNonNull(getClass().getResource("/com/comp2042/view/styling.css")).toExternalForm();

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

    /**
     * Adjusts the canvas and end-message sizes and redraws the board.
     * Requires an initialised canvas and labels.
     *
     * @param fullScreen whether to enter fullscreen or restore windowed display
     * @param obstacle the obstacles to draw, or {@code null} to omit them
     * @param snake the snake in its current pose
     * @param pickup the current pickup, or {@code null} after completion
     */
    public void scaleToFullscreen(boolean fullScreen, Obstacle obstacle, Snake snake, Pickup pickup) {
        fullScale = 1;
        if (fullScreen) {
            // Use the primary screen's usable area to calculate the fullscreen scale.
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            // Use the smaller ratio to fit both dimensions without stretching cells.
            fullScale = Math.min(bounds.getWidth() / baseWidth, bounds.getHeight() / baseHeight);
        }
        gridCanvas.setWidth(baseWidth * fullScale);
        gridCanvas.setHeight(baseHeight * fullScale);

        gameOverLabel.setFont(new Font("Arial", 25 * fullScale));
        gameWonLabel.setFont(new Font("Arial", 25 * fullScale));
        drawGrid(pickup, snake, obstacle, gc);
    }

    /**
     * Draws the background, pickup, snake, and obstacles at the current scale.
     * Reuses one body-list copy and clears the tail cell's recorded corner.
     * Requires the canvas and drawing context created by {@code initialize}.
     *
     * @param pickup the pickup to draw, or {@code null} to omit it
     * @param snake the snake in its current pose
     * @param obstacle the obstacles to draw, or {@code null} to omit them
     * @param gc the canvas drawing context; must be this view's {@link #gc}
     */
    public void drawGrid(Pickup pickup, Snake snake, Obstacle obstacle, GraphicsContext gc) {
        double cellSize = game.getMultiplier() * fullScale; // Apply display scaling once to the base cell size.
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

        // Use one snapshot of the body for both turn history and sprite rendering.
        List<GridPoint> body = snake.getBody();
        if (!body.isEmpty()) {
            GridPoint tail = body.getFirst();
            // The tail uses its own orientation, so its former corner can be forgotten.
            turnArray[tail.getX()][tail.getY()] = 0;
        }
        // Render each body segment by its position in the tail-to-head list.
        for (int bodyIndex = 0; bodyIndex < body.size(); bodyIndex++) {
            drawBody(snake, body, bodyIndex, cellSize);
        }

        // Draw head
        drawHead(snake, cellSize);

        if (obstacle != null) {
            gc.setFill(obstacle.getColor());
            // Draw each obstacle in its own grid cell at the current scale.
            for (GridPoint point : obstacle.getObstacles()) {
                gc.fillRect(point.getX() * cellSize, point.getY() * cellSize, cellSize, cellSize);
            }
        }
    }

    /**
     * Draws the head rotated to the snake's current direction.
     *
     * @param snake the snake supplying the head position, image, and rotation
     * @param cellSize the scaled width and height of a grid cell in pixels
     */
    private void drawHead(Snake snake, double cellSize) {
        // Use the head sprite's cell origin and centre to rotate it in place.
        Image img = snake.getImageHead();
        double x   = snake.getHead().getX() * cellSize;
        double y   = snake.getHead().getY() * cellSize;
        double cx  = x + cellSize / 2;
        double cy  = y + cellSize / 2;

        // Rotate around the cell centre and restore the transform for later sprites.
        gc.save();
        gc.translate(cx, cy);
        gc.rotate(snake.getSnakeDir());
        gc.drawImage(img, -cellSize / 2, -cellSize / 2, cellSize, cellSize);
        gc.restore();
    }

    /**
     * Draws one segment using a tail, corner, or straight body sprite.
     *
     * @param snake the snake supplying sprite images and orientation calculations
     * @param body the body-list copy shared by this redraw, ordered from tail to neck
     * @param index the valid index of the segment to draw; zero identifies the tail
     * @param cellSize the scaled width and height of a grid cell in pixels
     */
    private void drawBody(Snake snake, List<GridPoint> body, int index, double cellSize) {
        // Look up the current segment and any remembered corner at its cell.
        GridPoint point = body.get(index);
        int turn = turnArray[point.getX()][point.getY()];
        // Choose a sprite and angle together according to this segment's role.
        Image img;
        double rotateDeg;

        if (index == 0) {
            // Tail direction accounts for adjacency across wrapped board edges.
            img       = snake.getImageTail();
            rotateDeg = snake.getTailDir();
        } else if (turn != 0) {
            // Recorded turns select a corner sprite rotated in quarter turns.
            img       = snake.getImageTurn();
            rotateDeg = 90.0 * turn;
        } else {
            // Straight segments align with the neighbouring segment toward the tail.
            img       = snake.getImageBody();
            rotateDeg = snake.getBodyDir(point, body.get(index - 1));
        }

        // Use the cell origin for drawing and its centre as the rotation pivot.
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

    /**
     * Records a corner rotation at the head's current cell before the next move.
     * Direction pairs without a recognised perpendicular turn clear that cell's entry.
     *
     * @param snake the snake whose current head position identifies the turn cell
     * @param lastKey the direction before the turn, expressed as an arrow key
     * @param currentKey the direction after the turn, expressed as an arrow key
     */
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

    /** Clears all recorded corners when starting a fresh game. */
    public void resetTurnArray() {
        // Reset the recorded turn values one column at a time.
        for (int[] row : turnArray) {
            java.util.Arrays.fill(row, 0);
        }
    }

    /**
     * Shows or hides the game-over message using the model's death state.
     * Requires labels to have been created by {@code initialize}.
     *
     * @param snake the snake whose death state determines visibility
     */
    public void gameOver(Snake snake) {
        gameOverLabel.setVisible(snake.getDead());
    }

    /**
     * Shows or hides the win message using the game's recorded completion state.
     * Requires labels to have been created by {@code initialize}.
     */
    public void gameWon() {
        gameWonLabel.setVisible(game.gameWon());
    }

    /** Updates the initialised score label from the game's current display points. */
    public void updateScoreLabel() {
        scoreLabel.setText("Score: " + game.getDisplayScore());
    }
}
