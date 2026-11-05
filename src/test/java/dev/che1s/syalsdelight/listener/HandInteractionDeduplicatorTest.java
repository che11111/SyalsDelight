package dev.che1s.syalsdelight.listener;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HandInteractionDeduplicatorTest {
    @Test
    void sameClickFromSecondHandIsSuppressed() {
        HandInteractionDeduplicator deduplicator = new HandInteractionDeduplicator();
        UUID player = UUID.randomUUID();

        assertFalse(deduplicator.isDuplicate(
                player, "syals_delight:cutting_board", 10, 64, -3, 500L
        ));
        assertTrue(deduplicator.isDuplicate(
                player, "syals_delight:cutting_board", 10, 64, -3, 500L
        ));
    }

    @Test
    void exactBlockPositionAndTickMustAllMatch() {
        HandInteractionDeduplicator deduplicator = new HandInteractionDeduplicator();
        UUID player = UUID.randomUUID();
        assertFalse(deduplicator.isDuplicate(player, "syals_delight:a", 1, 2, 3, 10));
        assertFalse(deduplicator.isDuplicate(player, "syals_delight:a", 1, 2, 3, 11));
        assertFalse(deduplicator.isDuplicate(player, "syals_delight:a", 1, 2, 4, 11));
        assertFalse(deduplicator.isDuplicate(player, "syals_delight:b", 1, 2, 4, 11));
    }

    @Test
    void playerQuitCleanupRemovesCachedState() {
        HandInteractionDeduplicator deduplicator = new HandInteractionDeduplicator();
        UUID player = UUID.randomUUID();
        deduplicator.isDuplicate(player, "syals_delight:a", 1, 2, 3, 10);
        assertEquals(1, deduplicator.cachedPlayers());

        deduplicator.forget(player);
        assertEquals(0, deduplicator.cachedPlayers());
        assertFalse(deduplicator.isDuplicate(player, "syals_delight:a", 1, 2, 3, 10));
    }
}

