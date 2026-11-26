package com.comp2042.model.pickup;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WeightedPickupSelectorTest {

    @ParameterizedTest
    @CsvSource({"0, DIAMOND", "1, DIAMOND", "2, STAR", "4, STAR", "5, BLOCKS"})
    void assignsRollsByWeightAndEnumOrder(int roll, PickupType expected) {
        Map<PickupType, Integer> weights = new LinkedHashMap<>();
        weights.put(PickupType.BLOCKS, 1);
        weights.put(PickupType.STAR, 3);
        weights.put(PickupType.DIAMOND, 2);
        WeightedPickupSelector selector = new WeightedPickupSelector(new Random() {
            @Override
            public int nextInt(int bound) {
                assertEquals(6, bound);
                return roll;
            }
        });

        assertEquals(expected, selector.select(weights));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void rejectsNonPositiveWeights(int weight) {
        WeightedPickupSelector selector = new WeightedPickupSelector(new Random(0));

        assertThrows(IllegalArgumentException.class,
                () -> selector.select(Map.of(PickupType.DIAMOND, 1, PickupType.BLOCKS, weight)));
    }

    @Test
    void rejectsAnEmptyWeightTable() {
        WeightedPickupSelector selector = new WeightedPickupSelector(new Random(0));

        assertThrows(IllegalArgumentException.class, () -> selector.select(Map.of()));
    }
}
