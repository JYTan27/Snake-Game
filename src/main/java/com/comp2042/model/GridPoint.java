package com.comp2042.model;

import java.util.Objects;

/**
 * A mutable integer coordinate on the game grid.
 * Columns increase to the right and rows increase downward. Coordinates are
 * not restricted to board bounds. Equality and hashing use the current values;
 * avoid changing a point while it is used as a hash-based collection key.
 */
public class GridPoint {

    /** Horizontal grid coordinate in cells. */
    private int x;
    /** Vertical grid coordinate in cells. */
    private int y;

    /**
     * Creates a point at the supplied grid coordinates.
     *
     * @param x the column coordinate
     * @param y the row coordinate
     */
    public GridPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Creates an independent point with the same coordinates as another point.
     *
     * @param other the point whose coordinate values are copied
     * @throws NullPointerException if {@code other} is null
     */
    public GridPoint(GridPoint other) {
        this.x = other.x;
        this.y = other.y;
    }

    /**
     * Returns the current column coordinate.
     *
     * @return the current column coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the current row coordinate.
     *
     * @return the current row coordinate
     */
    public int getY() {
        return y;
    }

    /**
     * Replaces both coordinate values without checking board bounds.
     *
     * @param x the new column coordinate
     * @param y the new row coordinate
     */
    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Replaces this point's coordinates with values copied from another point.
     *
     * @param other the source of the new coordinates
     * @throws NullPointerException if {@code other} is null
     */
    public void setLocation(GridPoint other) {
        this.x = other.x;
        this.y = other.y;
    }

    /**
     * Compares grid coordinate values with another point.
     *
     * @param o the object to compare, which may be {@code null}
     * @return {@code true} if the object is a grid point with matching coordinates
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // Bind the other point only after confirming its type for coordinate comparison.
        if (!(o instanceof GridPoint that)) return false;
        return x == that.x && y == that.y;
    }

    /**
     * Returns a hash based on the current coordinate values.
     *
     * @return a hash based on the current coordinate values
     */
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    /**
     * Returns a diagnostic description containing both coordinate values.
     *
     * @return a diagnostic description containing both coordinate values
     */
    @Override
    public String toString() {
        return "GridPoint[x=" + x + ", y=" + y + "]";
    }
}
