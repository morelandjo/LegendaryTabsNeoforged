package vodmordia.modtabs.integration;

import net.neoforged.fml.ModList;
import vodmordia.modtabs.ModTabs;

import java.util.EnumSet;
import java.util.Set;

/**
 * Centralized manager for mod integrations
 */
public class ModIntegrationManager {
    private static final Set<ModIntegration> loadedMods = EnumSet.noneOf(ModIntegration.class);

    /**
     * Initialize mod detection - call during mod construction
     */
    public static void detectLoadedMods() {
        loadedMods.clear();

        for (ModIntegration mod : ModIntegration.values()) {
            if (ModList.get().isLoaded(mod.getModId())) {
                loadedMods.add(mod);
            }
        }
    }

    /**
     * Check if a specific mod is loaded
     */
    public static boolean isModLoaded(ModIntegration mod) {
        return loadedMods.contains(mod);
    }

    /**
     * Get all loaded mods
     */
    public static Set<ModIntegration> getLoadedMods() {
        return Set.copyOf(loadedMods);
    }

}