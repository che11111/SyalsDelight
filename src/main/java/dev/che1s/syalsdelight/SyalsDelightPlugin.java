package dev.che1s.syalsdelight;

import com.huidu.farmersdelight.api.FarmersDelightApi;
import dev.che1s.syalsdelight.compat.ContentReadinessVerifier;
import dev.che1s.syalsdelight.compat.OptionalCompatibilityChecker;
import dev.che1s.syalsdelight.config.AnimalFatConfig;
import dev.che1s.syalsdelight.listener.AnimalFatDropListener;
import dev.che1s.syalsdelight.listener.CompatibilityWarmupListener;
import dev.che1s.syalsdelight.listener.HandInteractionDeduplicator;
import dev.che1s.syalsdelight.listener.InteractionLifecycleListener;
import dev.che1s.syalsdelight.util.AddonLang;
import dev.che1s.syalsdelight.util.AddonResourceInstaller;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Files;

public final class SyalsDelightPlugin extends JavaPlugin {
    public static final String NAMESPACE = "syals_delight";
    private static final int REGISTERED_ITEMS = 30;
    private static final int REGISTERED_RECIPES = 29;
    private static final int BSTATS_PLUGIN_ID = 35133;
    private static final String RELOAD_GUARD = "syalsdelight.enabled.in.this.jvm";

    private final HandInteractionDeduplicator interactionDeduplicator =
            new HandInteractionDeduplicator();
    private Metrics metrics;

    @Override
    public void onLoad() {
        saveDefaultConfig();
        AddonLang.init(this);
        AddonResourceInstaller.InstallResult result = AddonResourceInstaller.install(
                this,
                NAMESPACE,
                getPluginMeta().getVersion(),
                getConfig().getBoolean("craftengine-resources.auto-completion", true),
                getConfig().getBoolean("craftengine-resources.update-on-version-change", true)
        );
        if (result.changed()) {
            getLogger().info(AddonLang.get(
                    "plugin.resources-installed",
                    "已安装 {count} 个 CraftEngine 资源，并清理 {removed} 个旧文件（{namespace} {version}）。",
                    "count", result.copied(),
                    "removed", result.removed(),
                    "namespace", NAMESPACE,
                    "version", getPluginMeta().getVersion()
            ));
        }
    }

    @Override
    public void onEnable() {
        if (System.getProperty(RELOAD_GUARD) != null) {
            getLogger().severe(AddonLang.get(
                    "plugin.unsafe-reload",
                    "请勿使用 /reload 重载 SyalsDelight，请完整重启服务器。"
            ));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        System.setProperty(RELOAD_GUARD, "1");

        FarmersDelightApi api = FarmersDelightApi.get();
        if (api == null || !api.isAvailable()) {
            getLogger().warning(AddonLang.get(
                    "plugin.farmersdelight-unavailable",
                    "未检测到可用的 FarmersDelight，SyalsDelight 已禁用。"
            ));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        reloadConfig();
        AddonLang.reload();
        seedExternalGui();
        metrics = new Metrics(this, BSTATS_PLUGIN_ID);

        AnimalFatConfig animalFat = AnimalFatConfig.from(
                getConfig().getConfigurationSection("animal-fat-drop")
        );
        if (!animalFat.invalidEntities().isEmpty()) {
            for (String entity : animalFat.invalidEntities()) {
                getLogger().warning(AddonLang.get(
                        "plugin.animal-fat-config-warning",
                        "动物脂肪掉落配置包含无效实体类型：{entity}",
                        "entity", entity
                ));
            }
        }

        getServer().getPluginManager().registerEvents(
                new AnimalFatDropListener(animalFat.policy(), getLogger()),
                this
        );
        getServer().getPluginManager().registerEvents(
                new InteractionLifecycleListener(interactionDeduplicator),
                this
        );

        OptionalCompatibilityChecker checker = null;
        if (getConfig().getBoolean("compatibility.check-on-warmup", true)) {
            checker = new OptionalCompatibilityChecker(
                    this,
                    getConfig().getBoolean("compatibility.warn-missing-optional-items", true)
            );
        }
        getServer().getPluginManager().registerEvents(
                new CompatibilityWarmupListener(
                        checker,
                        new ContentReadinessVerifier(this)
                ),
                this
        );

        getLogger().info(AddonLang.get(
                "plugin.enabled-summary",
                "SyalsDelight 已加载：{items} 个物品，{recipes} 个配方。",
                "items", REGISTERED_ITEMS,
                "recipes", REGISTERED_RECIPES
        ));
    }

    @Override
    public void onDisable() {
        if (metrics != null) {
            metrics.shutdown();
            metrics = null;
        }
        getLogger().info(AddonLang.get("plugin.disabled", "SyalsDelight 已停用。"));
    }

    private void seedExternalGui() {
        if (!Files.isRegularFile(getDataFolder().toPath().resolve("gui.yml"))) {
            try {
                saveResource("gui.yml", false);
            } catch (IllegalArgumentException ignored) {
                // 内置文件缺失时配方页面仍由 FarmersDelight 公共分类提供。
            }
        }
    }
}

