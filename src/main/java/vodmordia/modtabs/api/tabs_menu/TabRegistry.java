package vodmordia.modtabs.api.tabs_menu;

import vodmordia.modtabs.config.ModTabsConfig;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Centralized registry for tab configurations and metadata
 */
public class TabRegistry {
    private static final Map<Class<? extends TabBase>, TabConfiguration> tabConfigs = new ConcurrentHashMap<>();
    private static final Map<String, TabConfiguration> configKeyMap = new ConcurrentHashMap<>();

    /**
     * Register a tab class with its configuration
     */
    public static void registerTab(Class<? extends TabBase> tabClass, TabConfiguration config) {
        tabConfigs.put(tabClass, config);

        // Also register by config key if the tab has a @TabConfig annotation
        TabConfig annotation = tabClass.getAnnotation(TabConfig.class);
        if (annotation != null) {
            configKeyMap.put(annotation.configKey(), config);
        }
    }

    /**
     * Get configuration for a tab class
     */
    public static TabConfiguration getConfiguration(Class<? extends TabBase> tabClass) {
        return tabConfigs.getOrDefault(tabClass, TabConfiguration.defaultConfig());
    }

    /**
     * Get configuration by config key
     */
    public static TabConfiguration getConfiguration(String configKey) {
        return configKeyMap.getOrDefault(configKey, TabConfiguration.defaultConfig());
    }

    /**
     * Get the order for a tab instance using annotation-based lookup
     */
    public static int getTabOrder(TabBase tab) {
        Class<? extends TabBase> tabClass = tab.getClass();

        // First try a previously-registered configuration. Note that getConfiguration
        // returns defaultConfig() (order=0) for unregistered classes, which would
        // short-circuit before the annotation/config-field path — check containsKey
        // explicitly so the live ModTabsConfig field wins for unregistered tabs.
        if (tabConfigs.containsKey(tabClass)) {
            return tabConfigs.get(tabClass).order();
        }

        // Fallback to annotation: read the live <configKey>Order field on ModTabsConfig
        // so the global-settings modal's reorder writes are picked up at sort time.
        TabConfig annotation = tabClass.getAnnotation(TabConfig.class);
        if (annotation != null) {
            return getConfigValueByKey(annotation.configKey() + "Order", annotation.defaultOrder());
        }

        return 0;
    }

    /**
     * Get a config value by key with fallback
     */
    private static int getConfigValueByKey(String key, int defaultValue) {
        try {
            // Use reflection to get the config value from ModTabsConfig
            var field = ModTabsConfig.class.getDeclaredField(key);
            if (field.getType() == int.class) {
                field.setAccessible(true);
                return field.getInt(null);
            }
        } catch (Exception e) {
            // Field doesn't exist or can't be accessed, return default
        }
        return defaultValue;
    }

    /**
     * Initialize the registry with default configurations from annotations
     */
    public static void initializeDefaults() {
        // This will be called during mod initialization to set up default configurations
        // based on @TabConfig annotations
    }
}