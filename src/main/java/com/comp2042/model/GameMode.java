package com.comp2042.model;

import com.comp2042.model.pickup.PickupType;

import java.util.Map;

/**
 * Immutable mode presets shared by gameplay, the menu, and the help screen.
 * Each defines base speed, default border wrapping, and relative pickup weights.
 * All modes use full-board completion as their objective.
 */
public enum GameMode {
    /** Diamond-only play at standard speed with solid borders. */
    CLASSIC("Classic", "Collect diamonds at a steady pace. Borders are solid, so hitting an edge ends the game.",
            9.0, false, Map.of(PickupType.DIAMOND, 1)),
    /** Standard-speed play where blocks add obstacles up to the board's cap. */
    OBSTACLES("Obstacles", "Collect diamonds and blocks. Blocks add barriers until a quarter of the board is occupied. Borders and barriers are lethal.",
            9.0, false, Map.of(PickupType.DIAMOND, 4, PickupType.BLOCKS, 1)),
    /** Diamond-only play at a higher fixed base speed with solid borders. */
    HYPERSPEED("Hyperspeed", "Collect diamonds at a fixed fast pace. Borders are solid; plan your turns early.",
            16.0, false, Map.of(PickupType.DIAMOND, 1)),
    /** Standard-speed play with stars, temporary wrapping, and speed bonuses. */
    POWER_UPS("Power-ups", "Collect diamonds, stars, rings, and bolts. Rings let you cross borders until the next pickup; bolts temporarily boost speed. No blocks appear.",
            9.0, false, Map.of(PickupType.DIAMOND, 9, PickupType.STAR, 1, PickupType.RING, 1, PickupType.BOLT, 1)),
    /** Diamond-only play with permanent border wrapping at standard speed. */
    WRAPAROUND("Wraparound", "Collect diamonds and cross an edge to emerge on the opposite side. Your own body is still lethal.",
            9.0, true, Map.of(PickupType.DIAMOND, 1));

    /** Mode name displayed in the menu and help screen. */
    private final String displayName;
    /** Description reused by the help screen. */
    private final String description;
    /** Base movement rate in grid cells per second. */
    private final double speed;
    /** Whether crossing a border wraps by default. */
    private final boolean noBorder;
    /** Immutable positive weights defining which pickup types can be selected. */
    private final Map<PickupType, Integer> pickupWeights;

    /**
     * Defines a mode and copies its pickup weights into an immutable map.
     *
     * @param displayName the mode's visible name
     * @param description the help-screen explanation
     * @param speed the base movement speed in moves per second
     * @param noBorder whether border wrapping is enabled by default
     * @param pickupWeights relative selection weights, including a diamond entry
     * @throws NullPointerException if the map, a key, or a value is null
     * @throws IllegalArgumentException if diamonds are absent or a weight is not positive
     */
    GameMode(String displayName, String description, double speed, boolean noBorder, Map<PickupType, Integer> pickupWeights) {
        this.displayName = displayName;
        this.description = description;
        this.speed = speed;
        this.noBorder = noBorder;
        // Keep callers from changing selection rules after the preset is created.
        this.pickupWeights = Map.copyOf(pickupWeights);
        if (!this.pickupWeights.containsKey(PickupType.DIAMOND)
                || this.pickupWeights.values().stream().anyMatch(weight -> weight <= 0)) {
            throw new IllegalArgumentException("Modes require a diamond entry and positive pickup weights");
        }
    }

    /**
     * Returns the name shown in the mode selector.
     *
     * @return the name shown in the mode selector
     */
    public String getDisplayName() { return displayName; }
    /**
     * Returns the description displayed on the help screen.
     *
     * @return the description displayed on the help screen
     */
    public String getDescription() { return description; }
    /**
     * Returns the base movement speed in moves per second.
     *
     * @return the base movement speed in moves per second
     */
    public double getSpeed() { return speed; }
    /**
     * Reports whether this mode enables border wrapping by default.
     *
     * @return whether this mode enables border wrapping by default
     */
    public boolean isNoBorder() { return noBorder; }
    /**
     * Returns relative pickup weights. A type's selection probability is its
     * weight divided by the sum of weights; omitted types are not selected.
     * The factory may replace a selected blocks pickup when the obstacle cap is reached.
     *
     * @return the immutable map of pickup types to positive weights
     */
    public Map<PickupType, Integer> getPickupWeights() { return pickupWeights; }
}
