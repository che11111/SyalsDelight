package dev.che1s.syalsdelight.config;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class AnimalFatDropPolicy {
    private final boolean enabled;
    private final int amount;
    private final double chance;
    private final Set<String> entityTypes;

    public AnimalFatDropPolicy(
            boolean enabled,
            int amount,
            double chance,
            Set<String> entityTypes
    ) {
        this.enabled = enabled;
        this.amount = Math.max(0, amount);
        this.chance = Math.max(0.0, Math.min(1.0, chance));
        this.entityTypes = entityTypes.stream()
                .map(value -> value.trim().toUpperCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    public static AnimalFatDropPolicy sourceDefaults() {
        return new AnimalFatDropPolicy(
                true,
                1,
                1.0,
                Set.of("COW", "PIG", "SHEEP", "CHICKEN", "RABBIT", "GOAT", "MOOSHROOM")
        );
    }

    public boolean matches(String entityType) {
        return enabled
                && amount > 0
                && entityType != null
                && entityTypes.contains(entityType.toUpperCase(Locale.ROOT));
    }

    public int rollAmount(String entityType, double randomValue) {
        if (!matches(entityType) || chance <= 0.0) {
            return 0;
        }
        return randomValue < chance ? amount : 0;
    }

    public boolean enabled() {
        return enabled;
    }

    public int amount() {
        return amount;
    }

    public double chance() {
        return chance;
    }

    public Set<String> entityTypes() {
        return entityTypes;
    }
}

