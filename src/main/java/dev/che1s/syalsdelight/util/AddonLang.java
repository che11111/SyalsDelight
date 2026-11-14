package dev.che1s.syalsdelight.util;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AddonLang {
    private static final Map<String, String> MESSAGES = new ConcurrentHashMap<>();
    private static final Map<String, String> FALLBACK_MESSAGES = new ConcurrentHashMap<>();
    private static JavaPlugin plugin;
    private static String loadedLanguage = LanguageResolver.FALLBACK;

    private AddonLang() {
    }

    public static void init(JavaPlugin owner) {
        plugin = owner;
        reload();
    }

    public static void reload() {
        MESSAGES.clear();
        FALLBACK_MESSAGES.clear();
        List<Diagnostic> diagnostics = new ArrayList<>();
        String configured = plugin.getConfig().getString("language", LanguageResolver.AUTO);
        LanguageResolver.Selection selection =
                LanguageResolver.select(configured, Locale.getDefault());
        String locale = selection.language();
        if (selection.fallback()) {
            diagnostics.add(new Diagnostic(
                    "plugin.language.invalid-code",
                    "无法从服务器语言“{server}”识别配置“{code}”，已改用 en_us。",
                    "code", configured == null ? "auto" : configured,
                    "server", String.valueOf(selection.systemLocale())
            ));
        }

        for (String languageCode : LanguageResolver.SUPPORTED) {
            ensureExternalLanguage(languageCode, diagnostics);
        }
        loadLanguage(LanguageResolver.FALLBACK, FALLBACK_MESSAGES);
        if (!loadLanguage(locale)) {
            diagnostics.add(new Diagnostic(
                    "plugin.language.load-failed",
                    "无法加载语言“{code}”，已改用 en_us。",
                    "code", locale
            ));
            locale = LanguageResolver.FALLBACK;
        }
        loadedLanguage = locale;
        for (Diagnostic diagnostic : diagnostics) {
            plugin.getLogger().warning(get(
                    diagnostic.key(),
                    diagnostic.fallback(),
                    diagnostic.values()
            ));
        }
    }

    public static String get(String key, String fallback, Object... values) {
        String message = MESSAGES.getOrDefault(
                key,
                FALLBACK_MESSAGES.getOrDefault(key, fallback)
        );
        for (int index = 0; index + 1 < values.length; index += 2) {
            message = message.replace(
                    "{" + values[index] + "}",
                    String.valueOf(values[index + 1])
            );
        }
        return message;
    }

    public static String language() {
        return loadedLanguage;
    }

    private static boolean loadLanguage(String languageCode) {
        return loadLanguage(languageCode, MESSAGES);
    }

    private static boolean loadLanguage(String languageCode, Map<String, String> messages) {
        Path external = plugin.getDataFolder().toPath()
                .resolve("lang")
                .resolve(languageCode + ".yml");
        try {
            Reader reader;
            if (Files.isRegularFile(external)) {
                reader = Files.newBufferedReader(external, StandardCharsets.UTF_8);
            } else {
                var stream = plugin.getClass().getClassLoader()
                        .getResourceAsStream("lang/" + languageCode + ".yml");
                if (stream == null) {
                    return false;
                }
                reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            }
            try (reader) {
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(reader);
                for (String key : yaml.getKeys(true)) {
                    if (yaml.isString(key)) {
                        messages.put(key, yaml.getString(key));
                    }
                }
            }
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static void ensureExternalLanguage(
            String languageCode,
            List<Diagnostic> diagnostics
    ) {
        Path target = plugin.getDataFolder().toPath()
                .resolve("lang")
                .resolve(languageCode + ".yml");
        YamlConfiguration packaged = loadPackaged(languageCode);
        if (packaged == null) {
            diagnostics.add(new Diagnostic(
                    "plugin.language.bundled-missing",
                    "内置语言文件缺失：{code}",
                    "code", languageCode
            ));
            return;
        }

        try {
            Files.createDirectories(target.getParent());
            YamlConfiguration external = Files.isRegularFile(target)
                    ? YamlConfiguration.loadConfiguration(target.toFile())
                    : new YamlConfiguration();
            boolean changed = !Files.isRegularFile(target);
            for (String key : packaged.getKeys(true)) {
                if (packaged.isString(key) && !external.isSet(key)) {
                    external.set(key, packaged.getString(key));
                    changed = true;
                }
            }
            if (changed) {
                external.save(target.toFile());
            }
        } catch (Exception exception) {
            diagnostics.add(new Diagnostic(
                    "plugin.language.save-failed",
                    "无法准备语言文件“{file}”：{error}",
                    "file", target,
                    "error", exception.getMessage()
            ));
        }
    }

    private static YamlConfiguration loadPackaged(String languageCode) {
        String path = "lang/" + languageCode + ".yml";
        try (var stream = plugin.getClass().getClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                return null;
            }
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return YamlConfiguration.loadConfiguration(reader);
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    private record Diagnostic(String key, String fallback, Object... values) {
    }
}

