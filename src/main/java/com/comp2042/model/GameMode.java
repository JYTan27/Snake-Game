package com.comp2042.model;

import java.util.Set;

/** Independent rule configurations, shared by gameplay and menu descriptions. */
public enum GameMode {
    CLASSIC("Classic", "Collect diamonds at a steady pace. Borders are solid, so hitting an edge ends the game.",
            9.0, false, Set.of(PickupType.DIAMOND)),
    OBSTACLES("Obstacles", "Collect diamonds and blocks. Blocks add barriers until a quarter of the board is occupied. Borders and barriers are lethal.",
            9.0, false, Set.of(PickupType.DIAMOND, PickupType.BLOCKS)),
    HYPERSPEED("Hyperspeed", "Collect diamonds at a fixed fast pace. Borders are solid; plan your turns early.",
            16.0, false, Set.of(PickupType.DIAMOND)),
    POWER_UPS("Power-ups", "Collect diamonds, stars, rings, and bolts. Rings let you cross borders until the next pickup; bolts temporarily boost speed. No blocks appear.",
            9.0, false, Set.of(PickupType.DIAMOND, PickupType.STAR, PickupType.RING, PickupType.BOLT)),
    WRAPAROUND("Wraparound", "Collect diamonds and cross an edge to emerge on the opposite side. Your own body is still lethal.",
            9.0, true, Set.of(PickupType.DIAMOND));

    private final String displayName;
    private final String description;
    private final double speed;
    private final boolean noBorder;
    private final Set<PickupType> allowedPickups;

    GameMode(String displayName, String description, double speed, boolean noBorder, Set<PickupType> allowedPickups) {
        this.displayName = displayName;
        this.description = description;
        this.speed = speed;
        this.noBorder = noBorder;
        this.allowedPickups = Set.copyOf(allowedPickups);
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public double getSpeed() { return speed; }
    public boolean isNoBorder() { return noBorder; }
    public boolean allowsPickup(PickupType type) { return allowedPickups.contains(type); }
}
