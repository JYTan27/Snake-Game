package com.comp2042.model;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Obstacle {
    private static final Random RANDOM = new Random();

    private Color color = Color.BLACK;
    private ArrayList<GridPoint> obstacles = new ArrayList<>();
    private int boardWidth;
    private int boardHeight;

    public Obstacle(int boardWidth, int boardHeight) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
    }

    public void newObstacles(Snake snek, Coin coin, int amount) {
        if (obstacles.size() > boardWidth * boardHeight / 4) {
            return;
        }

        List<GridPoint> occupied = new ArrayList<>();
        occupied.add(snek.getHead());
        occupied.addAll(snek.getBody());
        occupied.add(coin.getPos());
        occupied.addAll(obstacles);

        List<GridPoint> freeCells = new ArrayList<>();
        for (int cx = 0; cx < boardWidth; cx++) {
            for (int cy = 0; cy < boardHeight; cy++) {
                GridPoint candidate = new GridPoint(cx, cy);
                if (!occupied.contains(candidate)) {
                    freeCells.add(candidate);
                }
            }
        }

        for (int i = 0; i < amount && !freeCells.isEmpty(); i++) {
            int idx = RANDOM.nextInt(freeCells.size());
            obstacles.add(freeCells.remove(idx));
        }
    }

    public Color getColor() {
        return color;
    }

    public List<GridPoint> getObstacles() {
        return Collections.unmodifiableList(obstacles);
    }

    public void reset() {
        obstacles.clear();
    }
}
