package vodmordia.modtabs.config;

import vodmordia.modtabs.api.tabs_menu.TabConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Dynamic configuration system for tabs that can be extended without code changes
 */
public class DynamicTabConfig {
    private static final Map<String, TabConfiguration> tabConfigs = new HashMap<>();

    // Register default configurations
    static {
        registerTab("inventoryTab", true, 0);
        registerTab("advancementsTab", true, 0);
        registerTab("arsElixirumTab", true, 0);
        registerTab("arsNouveauTab", true, 0);
        registerTab("backpackedTab", true, 0);
        registerTab("bodyDamageTab", true, 0);
        registerTab("cobblemonTab", true, 0);
        registerTab("cosmeticArmorTab", true, 0);
        registerTab("dietTab", true, 0);
        registerTab("ftbQuestsTab", true, 0);
        registerTab("ftbTeamsTab", true, 0);
        registerTab("journeyMapTab", true, 0);
        registerTab("l2ArtifactsTab", true, 0);
        registerTab("l2AttributesTab", true, 0);
        registerTab("l2HostilityTab", true, 0);
        registerTab("mapAtlasesTab", true, 0);
        registerTab("modularGolemsTab", true, 0);
        registerTab("passiveSkillTreeTab", true, 0);
        registerTab("pufferfishSkillsTab", true, 0);
        registerTab("reskillableTab", true, 0);
        registerTab("sophisticatedBackpacksTab", true, 0);
        registerTab("travelersBackpackTab", true, 0);
        registerTab("xaerosMapTab", true, 0);
    }

    /**
     * Register a tab configuration
     */
    public static void registerTab(String configKey, boolean enabled, int order) {
        tabConfigs.put(configKey, new TabConfiguration(enabled, order));
    }

    /**
     * Get configuration for a tab
     */
    public static TabConfiguration getTabConfig(String configKey) {
        return tabConfigs.getOrDefault(configKey, TabConfiguration.defaultConfig());
    }

    /**
     * Update a tab configuration
     */
    public static void updateTabConfig(String configKey, boolean enabled, int order) {
        tabConfigs.put(configKey, new TabConfiguration(enabled, order));
    }

    /**
     * Check if a tab is enabled
     */
    public static boolean isTabEnabled(String configKey) {
        return getTabConfig(configKey).enabled();
    }

    /**
     * Get the order for a tab
     */
    public static int getTabOrder(String configKey) {
        return getTabConfig(configKey).order();
    }

    /**
     * Get all registered tab configurations
     */
    public static Map<String, TabConfiguration> getAllConfigs() {
        return Map.copyOf(tabConfigs);
    }

    /**
     * Sync with the legacy ModTabsConfig system
     * This method reads the old config values and updates the dynamic system
     */
    public static void syncWithLegacyConfig() {
        // This would read from ModTabsConfig and update the dynamic system
        // For now, we'll keep using the legacy system for compatibility
        updateTabConfig("inventoryTab", ModTabsConfig.inventoryTabEnabled, ModTabsConfig.inventoryTabOrder);
        updateTabConfig("advancementsTab", ModTabsConfig.advancementsTabEnabled, ModTabsConfig.advancementsTabOrder);
        updateTabConfig("arsElixirumTab", ModTabsConfig.arsElixirumTabEnabled, ModTabsConfig.arsElixirumTabOrder);
        updateTabConfig("arsNouveauTab", ModTabsConfig.arsNouveauTabEnabled, ModTabsConfig.arsNouveauTabOrder);
        updateTabConfig("backpackedTab", ModTabsConfig.backpackedTabEnabled, ModTabsConfig.backpackedTabOrder);
        updateTabConfig("bodyDamageTab", ModTabsConfig.bodyDamageTabEnabled, ModTabsConfig.bodyDamageTabOrder);
        updateTabConfig("cobblemonTab", ModTabsConfig.cobblemonTabEnabled, ModTabsConfig.cobblemonTabOrder);
        updateTabConfig("cosmeticArmorTab", ModTabsConfig.cosmeticArmorTabEnabled, ModTabsConfig.cosmeticArmorTabOrder);
        updateTabConfig("dietTab", ModTabsConfig.dietTabEnabled, ModTabsConfig.dietTabOrder);
        updateTabConfig("ftbQuestsTab", ModTabsConfig.ftbQuestsTabEnabled, ModTabsConfig.ftbQuestsTabOrder);
        updateTabConfig("ftbTeamsTab", ModTabsConfig.ftbTeamsTabEnabled, ModTabsConfig.ftbTeamsTabOrder);
        updateTabConfig("journeyMapTab", ModTabsConfig.journeyMapTabEnabled, ModTabsConfig.journeyMapTabOrder);
        updateTabConfig("l2ArtifactsTab", ModTabsConfig.l2ArtifactsTabEnabled, ModTabsConfig.l2ArtifactsTabOrder);
        updateTabConfig("l2AttributesTab", ModTabsConfig.l2AttributesTabEnabled, ModTabsConfig.l2AttributesTabOrder);
        updateTabConfig("l2HostilityTab", ModTabsConfig.l2HostilityTabEnabled, ModTabsConfig.l2HostilityTabOrder);
        updateTabConfig("mapAtlasesTab", ModTabsConfig.mapAtlasesTabEnabled, ModTabsConfig.mapAtlasesTabOrder);
        updateTabConfig("modularGolemsTab", ModTabsConfig.modularGolemsTabEnabled, ModTabsConfig.modularGolemsTabOrder);
        updateTabConfig("passiveSkillTreeTab", ModTabsConfig.passiveSkillTreeTabEnabled, ModTabsConfig.passiveSkillTreeTabOrder);
        updateTabConfig("pufferfishSkillsTab", ModTabsConfig.pufferfishSkillsTabEnabled, ModTabsConfig.pufferfishSkillsTabOrder);
        updateTabConfig("reskillableReimaginedTab", ModTabsConfig.reskillableReimaginedTabEnabled, ModTabsConfig.reskillableReimaginedTabOrder);
        updateTabConfig("sophisticatedBackpacksTab", ModTabsConfig.sophisticatedBackpacksTabEnabled, ModTabsConfig.sophisticatedBackpacksTabOrder);
        updateTabConfig("travelersBackpackTab", ModTabsConfig.travelersBackpackTabEnabled, ModTabsConfig.travelersBackpackTabOrder);
        updateTabConfig("xaerosMapTab", ModTabsConfig.xaerosMapTabEnabled, ModTabsConfig.xaerosMapTabOrder);
    }
}