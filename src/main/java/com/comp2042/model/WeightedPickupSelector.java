package com.comp2042.model;

import java.util.Map;
import java.util.Objects;
import java.util.Random;

/** Selects proportionally to weights, independently of board state and construction. */
public final class WeightedPickupSelector {
    private final Random random;

    public WeightedPickupSelector(Random random) {
        this.random = Objects.requireNonNull(random);
    }

    public PickupType select(Map<PickupType, Integer> weights) {
        int total = 0;
        for (PickupType type : PickupType.values()) {
            Integer weight = weights.get(type);
            if (weight == null) continue;
            if (weight <= 0) throw new IllegalArgumentException("Pickup weights must be positive");
            total = Math.addExact(total, weight);
        }
        if (total == 0) throw new IllegalArgumentException("At least one pickup weight is required");

        int roll = random.nextInt(total);
        // Enum order makes boundaries stable regardless of map iteration order.
        for (PickupType type : PickupType.values()) {
            roll -= weights.getOrDefault(type, 0);
            if (roll < 0) return type;
        }
        throw new IllegalStateException("Pickup weights changed during selection");
    }
}
