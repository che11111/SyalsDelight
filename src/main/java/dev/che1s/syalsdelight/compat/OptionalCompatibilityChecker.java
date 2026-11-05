package dev.che1s.syalsdelight.compat;

import dev.che1s.syalsdelight.util.AddonLang;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.plugin.java.JavaPlugin;

public final class OptionalCompatibilityChecker {
    private final JavaPlugin plugin;
    private final boolean warnMissing;

    public OptionalCompatibilityChecker(JavaPlugin plugin, boolean warnMissing) {
        this.plugin = plugin;
        this.warnMissing = warnMissing;
    }

    public CheckResult check() {
        int available = 0;
        int missing = 0;
        for (OptionalDependency dependency : OptionalDependency.ITEMS) {
            boolean present = CraftEngineItems.byId(Key.of(dependency.itemId())) != null;
            if (present) {
                available++;
            } else {
                missing++;
            }
            String message = AddonLang.get(
                    "plugin.compatibility-item",
                    "可选物品 {id}：{state}",
                    "id", dependency.itemId(),
                    "state", AddonLang.get(
                            present
                                    ? "plugin.compatibility-available"
                                    : "plugin.compatibility-missing",
                            present ? "可用" : "缺失"
                    )
            );
            if (present) {
                plugin.getLogger().info(message);
            } else if (warnMissing) {
                plugin.getLogger().warning(message);
            }
        }
        plugin.getLogger().info(AddonLang.get(
                "plugin.compatibility-summary",
                "可选兼容检查完成：{available} 个物品可用，{missing} 个物品缺失。",
                "available", available,
                "missing", missing
        ));
        return new CheckResult(available, missing);
    }

    public record CheckResult(int available, int missing) {
    }
}

