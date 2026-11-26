package com.comp2042.service.audio;

import com.comp2042.model.pickup.PickupType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SoundEffectTest {

    @ParameterizedTest
    @CsvSource({"DIAMOND, DIAMOND", "STAR, STAR", "RING, RING", "BOLT, BOLT", "BLOCKS, BLOCKS"})
    void selectsTheMatchingCollectionSound(PickupType pickup, SoundEffect expected) {
        assertEquals(expected, SoundEffect.forPickup(pickup));
    }

    @ParameterizedTest
    @EnumSource(SoundEffect.class)
    void eachEffectResolvesToABundledResource(SoundEffect effect) {
        assertNotNull(SoundEffect.class.getResource(effect.getResourcePath()), effect.getResourcePath());
    }
}
