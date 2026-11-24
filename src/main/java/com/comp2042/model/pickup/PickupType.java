package com.comp2042.model.pickup;

/**
 * Pickup identities shared by mode weights, factory construction, and sound mapping.
 * Diamonds provide the default reward; stars increase points, bolts increase speed,
 * rings enable wrapping, and blocks request obstacles. Concrete {@link Pickup}
 * subclasses implement these behaviours; enum values do not store positions or sprites.
 * Declaration order also defines the weighted selector's stable roll intervals.
 */
public enum PickupType {
    /** Standard pickup with the default point reward. */
    DIAMOND,
    /** Pickup with an increased display-point reward. */
    STAR,
    /** Pickup that adds a temporary movement-speed bonus. */
    BOLT,
    /** Pickup that temporarily enables border wrapping. */
    RING,
    /** Pickup that requests additional obstacles. */
    BLOCKS
}
