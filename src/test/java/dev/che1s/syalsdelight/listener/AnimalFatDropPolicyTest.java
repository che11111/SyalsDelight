package dev.che1s.syalsdelight.listener;

import dev.che1s.syalsdelight.config.AnimalFatDropPolicy;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnimalFatDropPolicyTest {
    @Test
    void sourceDefaultsMatchAllSevenEntitiesAtOneHundredPercentForOneItem() {
        AnimalFatDropPolicy policy = AnimalFatDropPolicy.sourceDefaults();
        for (String entity : Set.of(
                "COW", "PIG", "SHEEP", "CHICKEN", "RABBIT", "GOAT", "MOOSHROOM"
        )) {
            assertTrue(policy.matches(entity));
            assertEquals(1, policy.rollAmount(entity, 0.999999));
        }
        assertFalse(policy.matches("HORSE"));
        assertEquals(0, policy.rollAmount("HORSE", 0.0));
    }

    @Test
    void probabilityAndAmountAreConfigurableWithoutChangingDefaults() {
        AnimalFatDropPolicy policy = new AnimalFatDropPolicy(
                true,
                2,
                0.25,
                Set.of("cow", "pig")
        );
        assertEquals(2, policy.rollAmount("COW", 0.20));
        assertEquals(0, policy.rollAmount("COW", 0.25));
        assertEquals(0, policy.rollAmount("SHEEP", 0.0));
        assertEquals(1, AnimalFatDropPolicy.sourceDefaults().amount());
        assertEquals(1.0, AnimalFatDropPolicy.sourceDefaults().chance());
    }

    @Test
    void disabledPolicyNeverDrops() {
        AnimalFatDropPolicy policy = new AnimalFatDropPolicy(
                false,
                1,
                1.0,
                Set.of("COW")
        );
        assertFalse(policy.matches("COW"));
        assertEquals(0, policy.rollAmount("COW", 0.0));
    }
}

