package com.comp2042.model;

import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Selects pickup identities proportionally to positive weights.
 * Each call performs an independent roll in enum order, without considering
 * board capacity or constructing a pickup. Repeated rolls do not guarantee
 * any fixed interval between particular pickup types.
 */
public final class WeightedPickupSelector {
    /** Generator supplied by the factory or another caller. */
    private final Random random;

    /**
     * Creates a selector using the supplied random source.
     *
     * @param random the generator used for weighted rolls
     * @throws NullPointerException if {@code random} is null
     */
    public WeightedPickupSelector(Random random) {
        this.random = Objects.requireNonNull(random);
    }

    /**
     * Selects a type with probability equal to its weight divided by the total.
     * Omitted types have zero probability. Supply non-null, positive weights
     * and do not modify the map during selection.
     *
     * @param weights the pickup types and their relative selection weights
     * @return the selected pickup type
     * @throws NullPointerException if {@code weights} is null
     * @throws IllegalArgumentException if a weight is not positive or none are supplied
     * @throws ArithmeticException if the total weight exceeds the integer range
     * @throws IllegalStateException if changed weights leave the roll unmatched
     */
    public PickupType select(Map<PickupType, Integer> weights) {
        int total = 0;
        for (PickupType type : PickupType.values()) {
            Integer weight = weights.get(type);
            if (weight == null) continue;
            if (weight <= 0) throw new IllegalArgumentException("Pickup weights must be positive");
            total = Math.addExact(total, weight);
        }
        if (total == 0) throw new IllegalArgumentException("At least one pickup weight is required");

        // Each type owns a consecutive interval whose length equals its weight.
        int roll = random.nextInt(total);
        // Enum order makes boundaries stable regardless of map iteration order.
        for (PickupType type : PickupType.values()) {
            roll -= weights.getOrDefault(type, 0);
            if (roll < 0) return type;
        }
        throw new IllegalStateException("Pickup weights changed during selection");
    }
}
