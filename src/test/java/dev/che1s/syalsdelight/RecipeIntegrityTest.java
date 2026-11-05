package dev.che1s.syalsdelight;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecipeIntegrityTest {
    private static final Set<String> EXPECTED_ITEMS = Set.of(
            "syals", "bag_o_chips", "kettle_chips", "naquadah_chips",
            "reduced_fat_chips", "vinegar_chips", "donkey_burger_chips",
            "smoker_ham_chips", "onion_chips", "spicy_chips", "tomato_chips",
            "astera_chips", "broccoli_flavored_slimming_chips",
            "pesto_flavored_chips", "steak_chimichurri_chips",
            "batch_fried_potato_slice", "fried_naquadah_potato_slice",
            "fried_potato_slice", "greasy_potato_slice", "hot_potato_slice",
            "low_fat_potato_slice", "potato_slice", "chips_bag", "syals_bag",
            "peeled_potato", "animal_fat", "oil_bottle", "zombie_oil_bottle",
            "underground_oil_bottle", "peeler"
    );

    @Test
    void everyItemHasAnAcquisitionRoute() throws Exception {
        Set<String> available = new HashSet<>();
        available.add("syals_delight:animal_fat");
        List<Recipe> recipes = loadRecipes();

        boolean changed;
        do {
            changed = false;
            for (Recipe recipe : recipes) {
                if (available.contains(recipe.result())) {
                    continue;
                }
                if (recipe.inputs().stream().allMatch(input -> isAvailable(input, available))) {
                    available.add(recipe.result());
                    changed = true;
                }
            }
        } while (changed);

        for (String item : EXPECTED_ITEMS) {
            assertTrue(
                    available.contains("syals_delight:" + item),
                    () -> "没有获取路线：" + item
            );
        }
    }

    @Test
    void recipesHaveExpectedStationCountsAndNoOptionalNamespaceReferences() throws Exception {
        List<Recipe> recipes = loadRecipes();
        assertEquals(29, recipes.size());
        assertEquals(9, recipes.stream().filter(recipe -> recipe.station().equals("cooking_recipes")).count());
        assertEquals(2, recipes.stream().filter(recipe -> recipe.station().equals("cutting_recipes")).count());
        assertEquals(18, recipes.stream().filter(recipe -> recipe.station().equals("recipes")).count());

        Set<String> allowedNamespaces = Set.of(
                "syals_delight", "minecraft", "farmersdelight"
        );
        for (Recipe recipe : recipes) {
            for (String input : recipe.inputs()) {
                if (input.startsWith("#")) {
                    assertTrue(
                            input.contains("syals_delight:")
                                    || input.contains("farmersdelight:"),
                            () -> "未知标签：" + input
                    );
                    continue;
                }
                if (!input.contains(":")) {
                    continue;
                }
                String namespace = input.substring(0, input.indexOf(':'));
                assertTrue(
                        allowedNamespaces.contains(namespace),
                        () -> "读取到了可选外部命名空间：" + input
                );
            }
        }
    }

    private static boolean isAvailable(String input, Set<String> available) {
        if (input.startsWith("#syals_delight:food_oil")) {
            return available.contains("syals_delight:oil_bottle")
                    || available.contains("syals_delight:zombie_oil_bottle")
                    || available.contains("syals_delight:underground_oil_bottle");
        }
        if (input.startsWith("#")) {
            return true;
        }
        if (!input.startsWith("syals_delight:")) {
            return true;
        }
        return available.contains(input);
    }

    private static List<Recipe> loadRecipes() throws Exception {
        java.util.ArrayList<Recipe> recipes = new java.util.ArrayList<>();
        for (Path file : ResourceIntegrityTest.recipeFiles()) {
            Map<String, Object> root = ResourceIntegrityTest.loadYaml(file);
            collect(root, "recipes", recipes);
            collect(root, "cooking_recipes", recipes);
            collect(root, "cutting_recipes", recipes);
        }
        return List.copyOf(recipes);
    }

    private static void collect(
            Map<String, Object> root,
            String station,
            List<Recipe> recipes
    ) {
        if (!(root.get(station) instanceof Map<?, ?> raw)) {
            return;
        }
        for (Map.Entry<?, ?> entry : raw.entrySet()) {
            Map<String, Object> recipe = ResourceIntegrityTest.map(entry.getValue());
            String result;
            if (station.equals("recipes")) {
                result = ResourceIntegrityTest.map(recipe.get("result")).get("id").toString();
            } else if (station.equals("cooking_recipes")) {
                result = recipe.get("result").toString();
            } else {
                List<?> results = (List<?>) recipe.get("results");
                result = ResourceIntegrityTest.map(results.getFirst()).get("item").toString();
            }
            recipes.add(new Recipe(
                    entry.getKey().toString(),
                    station,
                    result,
                    inputs(recipe, station)
            ));
        }
    }

    private static List<String> inputs(Map<String, Object> recipe, String station) {
        if (station.equals("cutting_recipes")) {
            return List.of(
                    recipe.get("input").toString(),
                    recipe.get("tool").toString()
            );
        }
        Object ingredients = recipe.get("ingredients");
        if (ingredients instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }
        if (ingredients instanceof Map<?, ?> map) {
            return map.values().stream().map(Object::toString).toList();
        }
        return List.of();
    }

    private record Recipe(
            String id,
            String station,
            String result,
            List<String> inputs
    ) {
    }
}

