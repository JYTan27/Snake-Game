package com.comp2042.model;

import java.util.Map;

/** Independent rule configurations, shared by gameplay and menu descriptions. */
public enum GameMode {
    CLASSIC("Classic", "Collect diamonds at a steady pace. Borders are solid, so hitting an edge ends the game.",
            9.0, false, Map.of(PickupType.DIAMOND, 1)),
    OBSTACLES("Obstacles", "Collect diamonds and blocks. Blocks add barriers until a quarter of the board is occupied. Borders and barriers are lethal.",
            9.0, false, Map.of(PickupType.DIAMOND, 4, PickupType.BLOCKS, 1)),
    HYPERSPEED("Hyperspeed", "Collect diamonds at a fixed fast pace. Borders are solid; plan your turns early.",
            16.0, false, Map.of(PickupType.DIAMOND, 1)),
    POWER_UPS("Power-ups", "Collect diamonds, stars, rings, and bolts. Rings let you cross borders until the next pickup; bolts temporarily boost speed. No blocks appear.",
            9.0, false, Map.of(PickupType.DIAMOND, 9, PickupType.STAR, 1, PickupType.RING, 1, PickupType.BOLT, 1)),
    WRAPAROUND("Wraparound", "Collect diamonds and cross an edge to emerge on the opposite side. Your own body is still lethal.",
            9.0, true, Map.of(PickupType.DIAMOND, 1));

    private final String displayName;
    private final String description;
    private final double speed;
    private final boolean noBorder;
    private final Map<PickupType, Integer> pickupWeights;

    GameMode(String displayName, String description, double speed, boolean noBorder, Map<PickupType, Integer> pickupWeights) {
        this.displayName = displayName;
        this.description = description;
        this.speed = speed;
        this.noBorder = noBorder;
        this.pickupWeights = Map.copyOf(pickupWeights);
        if (!this.pickupWeights.containsKey(PickupType.DIAMOND)
                || this.pickupWeights.values().stream().anyMatch(weight -> weight <= 0)) {
            throw new IllegalArgumentException("Modes require a diamond entry and positive pickup weights");
        }
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public double getSpeed() { return speed; }
    public boolean isNoBorder() { return noBorder; }
    public Map<PickupType, Integer> getPickupWeights() { return pickupWeights; }
}
