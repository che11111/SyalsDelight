package dev.che1s.syalsdelight.compat;

import com.huidu.farmersdelight.api.recipe.FarmersDelightRecipes;
import dev.che1s.syalsdelight.util.AddonLang;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class ContentReadinessVerifier {
    private static final List<String> ITEMS = List.of(
            "syals_delight:syals",
            "syals_delight:bag_o_chips",
            "syals_delight:kettle_chips",
            "syals_delight:naquadah_chips",
            "syals_delight:reduced_fat_chips",
            "syals_delight:vinegar_chips",
            "syals_delight:donkey_burger_chips",
            "syals_delight:smoker_ham_chips",
            "syals_delight:onion_chips",
            "syals_delight:spicy_chips",
            "syals_delight:tomato_chips",
            "syals_delight:astera_chips",
            "syals_delight:broccoli_flavored_slimming_chips",
            "syals_delight:pesto_flavored_chips",
            "syals_delight:steak_chimichurri_chips",
            "syals_delight:batch_fried_potato_slice",
            "syals_delight:fried_naquadah_potato_slice",
            "syals_delight:fried_potato_slice",
            "syals_delight:greasy_potato_slice",
            "syals_delight:hot_potato_slice",
            "syals_delight:low_fat_potato_slice",
            "syals_delight:potato_slice",
            "syals_delight:chips_bag",
            "syals_delight:syals_bag",
            "syals_delight:peeled_potato",
            "syals_delight:animal_fat",
            "syals_delight:oil_bottle",
            "syals_delight:zombie_oil_bottle",
            "syals_delight:underground_oil_bottle",
            "syals_delight:peeler"
    );

    private static final List<String> RECIPES = List.of(
            "syals_delight:fried_potato_slice",
            "syals_delight:greasy_potato_slice",
            "syals_delight:low_fat_potato_slice",
            "syals_delight:hot_potato_slice",
            "syals_delight:batch_fried_potato_slice",
            "syals_delight:oil_bottle",
            "syals_delight:fallback_fried_naquadah_potato_slice",
            "syals_delight:fallback_underground_oil_bottle",
            "syals_delight:fallback_zombie_oil_bottle"
    );

    private static final List<String> CUTTING_RECIPES = List.of(
            "syals_delight:potato_to_peeled_potato",
            "syals_delight:peeled_potato_to_potato_slices"
    );

    private final JavaPlugin plugin;

    public ContentReadinessVerifier(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public VerificationResult verify() {
        int availableItems = 0;
        for (String itemId : ITEMS) {
            if (CraftEngineItems.byId(Key.of(itemId)) != null) {
                availableItems++;
            } else {
                plugin.getLogger().warning(AddonLang.get(
                        "plugin.content-item-missing",
                        "CraftEngine 物品未加载：{id}",
                        "id", itemId
                ));
            }
        }

        List<String> cookingIds = FarmersDelightRecipes.cookingPotRecipeIds();
        List<String> cuttingIds = FarmersDelightRecipes.cuttingBoardRecipeIds();
        int availableRecipes = 0;
        for (String recipeId : RECIPES) {
            if (cookingIds.contains(recipeId)) {
                availableRecipes++;
            } else {
                plugin.getLogger().warning(AddonLang.get(
                        "plugin.content-recipe-missing",
                        "烹饪锅配方未加载：{id}",
                        "id", recipeId
                ));
            }
        }
        for (String recipeId : CUTTING_RECIPES) {
            if (cuttingIds.contains(recipeId)) {
                availableRecipes++;
            } else {
                plugin.getLogger().warning(AddonLang.get(
                        "plugin.content-recipe-missing",
                        "砧板配方未加载：{id}",
                        "id", recipeId
                ));
            }
        }

        int expectedRecipes = RECIPES.size() + CUTTING_RECIPES.size();
        plugin.getLogger().info(AddonLang.get(
                "plugin.content-readiness-summary",
                "内容就绪检查：{items}/{expectedItems} 个物品，{recipes}/{expectedRecipes} 个工作站配方。",
                "items", availableItems,
                "expectedItems", ITEMS.size(),
                "recipes", availableRecipes,
                "expectedRecipes", expectedRecipes
        ));
        return new VerificationResult(
                availableItems,
                ITEMS.size(),
                availableRecipes,
                expectedRecipes
        );
    }

    public record VerificationResult(
            int availableItems,
            int expectedItems,
            int availableRecipes,
            int expectedRecipes
    ) {
        public boolean complete() {
            return availableItems == expectedItems && availableRecipes == expectedRecipes;
        }
    }
}

