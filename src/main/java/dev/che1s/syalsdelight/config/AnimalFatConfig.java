package dev.che1s.syalsdelight.config;

import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record AnimalFatConfig(AnimalFatDropPolicy policy, Set<String> invalidEntities) {
    public static AnimalFatConfig from(ConfigurationSection section) {
        if (section == null) {
            return new AnimalFatConfig(AnimalFatDropPolicy.sourceDefaults(), Set.of());
        }
        boolean enabled = section.getBoolean("enabled", true);
        int amount = section.getInt("amount", 1);
        double chance = section.getDouble("chance", 1.0);
        List<String> entities = section.getStringList("entities");
        if (entities.isEmpty()) {
            entities = List.of(
                    "COW",
                    "PIG",
                    "SHEEP",
                    "CHICKEN",
                    "RABBIT",
                    "GOAT",
                    "MOOSHROOM"
            );
        }

        Set<String> accepted = new LinkedHashSet<>();
        Set<String> invalid = new LinkedHashSet<>();
        for (String entity : entities) {
            if (entity == null || entity.isBlank()) {
                continue;
            }
            String normalized = entity.trim().toUpperCase(java.util.Locale.ROOT);
            if (isSupportedEntityName(normalized)) {
                accepted.add(normalized);
            } else {
                invalid.add(entity);
            }
        }
        return new AnimalFatConfig(
                new AnimalFatDropPolicy(enabled, amount, chance, accepted),
                Set.copyOf(invalid)
        );
    }

    private static boolean isSupportedEntityName(String entityName) {
        try {
            org.bukkit.entity.EntityType.valueOf(entityName);
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }
}

