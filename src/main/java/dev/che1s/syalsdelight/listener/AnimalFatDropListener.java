package dev.che1s.syalsdelight.listener;

import dev.che1s.syalsdelight.config.AnimalFatDropPolicy;
import dev.che1s.syalsdelight.util.AddonLang;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.bukkit.item.BukkitItemDefinition;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public final class AnimalFatDropListener implements Listener {
    public static final String ANIMAL_FAT_ID = "syals_delight:animal_fat";
    private static final Key ANIMAL_FAT_KEY = Key.of(ANIMAL_FAT_ID);

    private final AnimalFatDropPolicy policy;
    private final Logger logger;
    private final AtomicBoolean warnedMissingItem = new AtomicBoolean();

    public AnimalFatDropListener(AnimalFatDropPolicy policy, Logger logger) {
        this.policy = policy;
        this.logger = logger;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        int amount = policy.rollAmount(
                entity.getType().name(),
                ThreadLocalRandom.current().nextDouble()
        );
        if (amount <= 0) {
            return;
        }

        BukkitItemDefinition definition = CraftEngineItems.byId(ANIMAL_FAT_KEY);
        if (definition == null) {
            if (warnedMissingItem.compareAndSet(false, true)) {
                logger.warning(AddonLang.get(
                        "plugin.content-not-ready",
                        "CraftEngine 物品尚未就绪，动物脂肪掉落暂不执行。"
                ));
            }
            return;
        }

        ItemStack item = definition.buildBukkitItem();
        if (!isValidAnimalFat(item)) {
            logger.warning(AddonLang.get(
                    "plugin.animal-fat-output-invalid",
                    "无法构建 syals_delight:animal_fat，已跳过本次额外掉落。"
            ));
            return;
        }
        item.setAmount(amount);
        event.getDrops().add(item);
    }

    static boolean isValidAnimalFat(ItemStack item) {
        return item != null
                && item.getType() != Material.AIR
                && item.getType() != Material.BARRIER
                && item.getAmount() > 0
                && ANIMAL_FAT_KEY.equals(CraftEngineItems.getCustomItemId(item));
    }
}

