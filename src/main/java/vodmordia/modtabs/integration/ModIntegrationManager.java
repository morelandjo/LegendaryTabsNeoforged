package vodmordia.modtabs.integration;

import net.fabricmc.loader.api.FabricLoader;
import vodmordia.modtabs.ModTabs;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages integration with other mods
 */
public class ModIntegrationManager {
    private static final Map<ModIntegration, Boolean> loadedMods = new HashMap<>();

    /**
     * Detect which supported mods are loaded
     */
    public static void detectLoadedMods() {
        for (ModIntegration integration : ModIntegration.values()) {
            boolean isLoaded = FabricLoader.getInstance().isModLoaded(integration.getModId());
            loadedMods.put(integration, isLoaded);

            if (isLoaded) {
                ModTabs.LOGGER.info("Detected loaded mod: " + integration.getModId());
            }
        }
    }

    /**
     * Check if a specific mod is loaded
     */
    public static boolean isModLoaded(ModIntegration integration) {
        return loadedMods.getOrDefault(integration, false);
    }

    /**
     * Check if a mod by ID is loaded
     */
    public static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}