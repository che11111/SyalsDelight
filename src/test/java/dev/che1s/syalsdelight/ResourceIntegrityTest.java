package dev.che1s.syalsdelight;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceIntegrityTest {
    private static final Path RESOURCES = Path.of("src/main/resources");
    private static final Path PACK = RESOURCES.resolve(
            "craftengine/syals_delight"
    );
    private static final Path ASSETS = PACK.resolve(
            "resourcepack/assets/syals_delight"
    );

    private static final Set<String> EXPECTED_ITEMS_WITHOUT_DUPLICATE = Set.of(
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
    void allItemsHaveModelsTexturesAndAlignedLanguages() throws Exception {
        Map<String, Object> root = loadYaml(
                PACK.resolve("configuration/items.yml")
        );
        Map<String, Map<String, Object>> items = section(root, "items");
        assertEquals(30, items.size());
        assertEquals(
                EXPECTED_ITEMS_WITHOUT_DUPLICATE,
                items.keySet().stream()
                        .map(id -> id.substring("syals_delight:".length()))
                        .collect(java.util.stream.Collectors.toSet())
        );

        Map<String, Map<String, Object>> templates = section(root, "templates");
        Set<String> expectedTextures = new HashSet<>();
        for (Map.Entry<String, Map<String, Object>> entry : items.entrySet()) {
            String id = entry.getKey();
            String path = id.substring("syals_delight:".length());
            Map<String, Object> definition = entry.getValue();
            String modelId = modelId(definition, templates, id, path);
            Path modelFile = modelPath(modelId);
            assertTrue(
                    Files.isRegularFile(modelFile),
                    () -> "缺少模型：" + id + " -> " + modelId + " -> " + modelFile
            );

            Map<String, Object> model = loadJson(modelFile);
            Map<String, Object> textures = map(model.get("textures"));
            Object layer0 = textures.get("layer0");
            assertNotNull(layer0, () -> "模型缺少 layer0：" + id);
            String textureId = layer0.toString();
            Path textureFile = texturePath(textureId);
            assertTrue(Files.isRegularFile(textureFile), () -> "缺少纹理：" + id);
            expectedTextures.add(textureFile.getFileName().toString());
        }

        try (Stream<Path> files = Files.list(ASSETS.resolve("textures/item"))) {
            Set<String> actual = files
                    .filter(path -> path.getFileName().toString().endsWith(".png"))
                    .map(path -> path.getFileName().toString())
                    .collect(java.util.stream.Collectors.toSet());
            assertEquals(expectedTextures, actual);
        }
        assertTrue(Files.isRegularFile(
                ASSETS.resolve("textures/item/naquadah_chips.png.mcmeta")
        ));
        assertFalse(Files.exists(ASSETS.resolve("textures/item.zip")));
    }

    @Test
    void languageKeysMatchAcrossJsonAndCraftEngineTranslations() throws Exception {
        Map<String, Object> zhJson = loadJson(ASSETS.resolve("lang/zh_cn.json"));
        Map<String, Object> enJson = loadJson(ASSETS.resolve("lang/en_us.json"));
        assertEquals(zhJson.keySet(), enJson.keySet());
        assertEquals(34, zhJson.size());

        Map<String, Object> translationsRoot = loadYaml(
                PACK.resolve("configuration/translations.yml")
        );
        Map<String, Object> en = map(translationsRoot.get("translations")).get("en")
                instanceof Map<?, ?> value
                ? castMap(value)
                : Map.of();
        Map<String, Object> zh = map(translationsRoot.get("translations")).get("zh_cn")
                instanceof Map<?, ?> value
                ? castMap(value)
                : Map.of();
        assertEquals(en.keySet(), zh.keySet());

        Map<String, Object> items = loadYaml(
                PACK.resolve("configuration/items.yml")
        );
        Map<String, Map<String, Object>> definitions = section(items, "items");
        for (String itemId : definitions.keySet()) {
            String name = itemId.substring("syals_delight:".length());
            String key = "item.syals_delight." + name;
            assertTrue(zhJson.containsKey(key), () -> "中文缺少：" + key);
            assertTrue(enJson.containsKey(key), () -> "英文缺少：" + key);
            assertEquals(zhJson.get(key), zh.get(key));
            assertEquals(enJson.get(key), en.get(key));
        }
        assertEquals(zhJson.keySet(), zh.keySet());
        assertEquals(enJson.keySet(), en.keySet());
    }

    @Test
    void recipesOnlyReferenceResolvableExactIds() throws Exception {
        Set<String> itemIds = loadYaml(PACK.resolve("configuration/items.yml"))
                .get("items") instanceof Map<?, ?> raw
                ? castMap(raw).keySet()
                : Set.of();
        Set<String> itemTags = new LinkedHashSet<>();
        for (Path recipeFile : recipeFiles()) {
            Map<String, Object> root = loadYaml(recipeFile);
            collectIds(root, itemIds, itemTags);
        }

        assertTrue(itemTags.contains("syals_delight:food_oil"));
        assertEquals(29, recipeCount());
    }

    @Test
    void foodValuesMatchConvertedSourceValues() throws Exception {
        Map<String, Map<String, Object>> items = section(
                loadYaml(PACK.resolve("configuration/items.yml")),
                "items"
        );
        Map<String, double[]> expected = Map.ofEntries(
                Map.entry("syals", new double[]{6, 111.6}),
                Map.entry("bag_o_chips", new double[]{6, 111.6}),
                Map.entry("kettle_chips", new double[]{6, 111.6}),
                Map.entry("naquadah_chips", new double[]{189, 25099.2}),
                Map.entry("reduced_fat_chips", new double[]{4, 68.8}),
                Map.entry("vinegar_chips", new double[]{6, 118.8}),
                Map.entry("donkey_burger_chips", new double[]{6, 111.6}),
                Map.entry("smoker_ham_chips", new double[]{6, 111.6}),
                Map.entry("onion_chips", new double[]{6, 111.6}),
                Map.entry("spicy_chips", new double[]{6, 111.6}),
                Map.entry("tomato_chips", new double[]{6, 111.6}),
                Map.entry("astera_chips", new double[]{6, 111.6}),
                Map.entry("broccoli_flavored_slimming_chips", new double[]{6, 111.6}),
                Map.entry("pesto_flavored_chips", new double[]{6, 111.6}),
                Map.entry("steak_chimichurri_chips", new double[]{6, 111.6}),
                Map.entry("batch_fried_potato_slice", new double[]{2, 8.0}),
                Map.entry("fried_naquadah_potato_slice", new double[]{2, 8.0}),
                Map.entry("fried_potato_slice", new double[]{2, 8.0}),
                Map.entry("greasy_potato_slice", new double[]{2, 8.0}),
                Map.entry("hot_potato_slice", new double[]{2, 8.0}),
                Map.entry("low_fat_potato_slice", new double[]{2, 8.0}),
                Map.entry("potato_slice", new double[]{2, 8.0})
        );
        for (Map.Entry<String, double[]> entry : expected.entrySet()) {
            Map<String, Object> data = map(
                    items.get("syals_delight:" + entry.getKey()).get("data")
            );
            Map<String, Object> food = map(data.get("food"));
            assertEquals(entry.getValue()[0], ((Number) food.get("nutrition")).doubleValue());
            assertEquals(entry.getValue()[1], ((Number) food.get("saturation")).doubleValue());
        }
    }

    @Test
    void oilsReturnGlassBottlesAndPeelerIsNotAKnife() throws Exception {
        Map<String, Map<String, Object>> items = section(
                loadYaml(PACK.resolve("configuration/items.yml")),
                "items"
        );
        for (String oil : Set.of(
                "syals_delight:oil_bottle",
                "syals_delight:zombie_oil_bottle",
                "syals_delight:underground_oil_bottle"
        )) {
            Map<String, Object> settings = map(items.get(oil).get("settings"));
            Map<String, Object> remainder = map(settings.get("craft-remainder"));
            assertEquals("fixed", remainder.get("type"));
            assertEquals("minecraft:glass_bottle", remainder.get("item"));
            assertEquals(
                    List.of("syals_delight:food_oil"),
                    settings.get("tags")
            );
        }

        Map<String, Object> settings = map(
                items.get("syals_delight:peeler").get("settings")
        );
        assertFalse(settings.containsKey("tags"));
        Map<String, Object> data = map(
                items.get("syals_delight:peeler").get("data")
        );
        assertEquals(131, data.get("max-damage"));
    }

    @Test
    void readmeImagesExistAndNaquadahUsesAnimatedGif() throws Exception {
        String readme = Files.readString(Path.of("README.md"), StandardCharsets.UTF_8);
        Matcher matcher = Pattern.compile("src=\"(img/[^\"]+)\"").matcher(readme);
        int imageCount = 0;
        while (matcher.find()) {
            Path image = Path.of(matcher.group(1));
            assertTrue(Files.isRegularFile(image), () -> "README 图片不存在：" + image);
            imageCount++;
        }
        assertTrue(imageCount >= 31);
        assertTrue(readme.contains("https://img.shields.io/badge/CraftEngine-26.9.1%2B_API-5865F2"));
        assertTrue(readme.contains("https://github.com/Xiao-MoMi/craft-engine"));
        assertTrue(readme.contains("img/items/naquadah_chips.gif"));
        assertFalse(readme.contains(
                "img/items/naquadah_chips.png\" width=\"48\""
        ));

        for (String document : List.of("README.md", "README-zh.md")) {
            String text = Files.readString(Path.of(document), StandardCharsets.UTF_8);
            int header = text.contains("| Item | How to obtain |")
                    ? text.indexOf("| Item | How to obtain |")
                    : text.indexOf("| 物品 | 获取方式 |");
            String table = text.substring(header, text.indexOf("\n\n", header));
            List<String> itemRows = table.lines()
                    .filter(line -> line.startsWith("| <img"))
                    .toList();
            assertEquals(15, itemRows.size());
            assertTrue(itemRows.stream().allMatch(
                    row -> row.split("<img", -1).length - 1 == 2
            ));
        }
    }

    @Test
    void licensingDeclaresMitUpstreamAndAgplPlugin() throws Exception {
        String license = Files.readString(Path.of("LICENSE"), StandardCharsets.UTF_8);
        assertTrue(license.contains("upstream Syal's Delight mod is licensed under the MIT License"));
        assertTrue(license.contains("AGPL-3.0-only"));
        assertTrue(license.contains("GNU AFFERO GENERAL PUBLIC LICENSE"));
        assertFalse(Files.exists(Path.of("THIRD_PARTY_NOTICES.md")));
    }

    static Path pack() {
        return PACK;
    }

    static List<Path> recipeFiles() throws Exception {
        try (Stream<Path> paths = Files.walk(PACK.resolve("configuration"))) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".yml"))
                    .filter(path -> path.getFileName().toString().contains("recipe"))
                    .sorted()
                    .toList();
        }
    }

    static Map<String, Object> loadYaml(Path path) throws Exception {
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return castMap(new Yaml(new SafeConstructor(options)).load(reader));
        }
    }

    static Map<String, Object> loadJson(Path path) throws Exception {
        return loadYaml(path);
    }

    static Map<String, Map<String, Object>> section(
            Map<String, Object> root,
            String key
    ) {
        Map<String, Object> raw = map(root.get(key));
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        raw.forEach((name, value) -> result.put(name, map(value)));
        return result;
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> castMap(Map<?, ?> value) {
        return (Map<String, Object>) value;
    }

    private static String modelId(
            Map<String, Object> definition,
            Map<String, Map<String, Object>> templates,
            String id,
            String path
    ) {
        Object direct = definition.get("model");
        if (direct instanceof String value) {
            return value;
        }
        Object templateName = definition.get("template");
        assertNotNull(templateName, () -> "物品没有模型或模板：" + id);
        Map<String, Object> template = templates.get(templateName.toString());
        assertNotNull(template, () -> "模板不存在：" + templateName);
        return template.get("model").toString()
                .replace("${__NAMESPACE__}", "syals_delight")
                .replace("${__ID__}", path);
    }

    private static Path modelPath(String modelId) {
        String path = modelId.substring("syals_delight:".length());
        return ASSETS.resolve("models/" + path + ".json");
    }

    private static Path texturePath(String textureId) {
        String path = textureId.substring("syals_delight:".length());
        return ASSETS.resolve("textures/" + path + ".png");
    }

    private static void collectIds(
            Object node,
            Set<String> itemIds,
            Set<String> itemTags
    ) {
        if (node instanceof Map<?, ?> rawMap) {
            for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                if (entry.getKey().toString().equals("type")) {
                    continue;
                }
                collectIds(entry.getValue(), itemIds, itemTags);
            }
            return;
        }
        if (node instanceof List<?> list) {
            for (Object value : list) {
                collectIds(value, itemIds, itemTags);
            }
            return;
        }
        if (!(node instanceof String value)) {
            return;
        }
        if (value.startsWith("#farmersdelight:")
                || value.startsWith("#minecraft:")) {
            return;
        }
        if (value.startsWith("#syals_delight:")) {
            itemTags.add(value.substring(1));
            return;
        }
        if (!value.contains(":") || value.startsWith("minecraft:")
                || value.startsWith("farmersdelight:")) {
            return;
        }
        assertTrue(
                itemIds.contains(value),
                () -> "配方引用了不存在的物品：" + value
        );
    }

    private static int recipeCount() throws Exception {
        int count = 0;
        for (Path file : recipeFiles()) {
            Map<String, Object> root = loadYaml(file);
            for (String key : List.of("recipes", "cooking_recipes", "cutting_recipes")) {
                if (root.get(key) instanceof Map<?, ?> section) {
                    count += section.size();
                }
            }
        }
        return count;
    }
}

