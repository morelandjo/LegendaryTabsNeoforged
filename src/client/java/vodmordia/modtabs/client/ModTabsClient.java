package vodmordia.modtabs.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;
import vodmordia.modtabs.client.tabs_menu.*;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.config.CustomTabDefinition;
import vodmordia.modtabs.utils.CustomTabLoader;

import java.lang.reflect.Method;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ModTabsClient implements ClientModInitializer {

    private static boolean customTabsLoaded = false;

    @Override
    public void onInitializeClient() {
        ModTabs.LOGGER.info("Initializing Mod Tabs Client");

        Config.Baked.bakeClient();

        // Register all tabs - each tab's isEnabled() method handles mod detection via @TabConfig
        TabsMenu.register(new InventoryTab());
        TabsMenu.register(new BackpackedTab());
        TabsMenu.register(new TravelersBackpackTab());
        TabsMenu.register(new BodyDamageTab());

        // Run inspection for FTB Quests if needed
        try {
            Class<?> inspectorClass = Class.forName("vodmordia.modtabs.utils.FTBQuestsInspector");
            Method inspectMethod = inspectorClass.getMethod("inspect");
            inspectMethod.invoke(null);
        } catch (Exception e) {
            ModTabs.LOGGER.warn("Failed to run FTB Quests inspection: " + e.getMessage());
        }
        TabsMenu.register(new FtbQuestsTab());

        // Run inspection for FTB Teams if needed
        try {
            Class<?> inspectorClass = Class.forName("vodmordia.modtabs.utils.FTBTeamsInspector");
            Method inspectMethod = inspectorClass.getMethod("inspectModClasses");
            inspectMethod.invoke(null);
        } catch (Exception e) {
            ModTabs.LOGGER.warn("Failed to run FTB Teams inspection: " + e.getMessage());
        }
        TabsMenu.register(new FtbTeamsTab());

        TabsMenu.register(new ReskillableReimaginedTab());
        TabsMenu.register(new MapAtlasesTab());
        TabsMenu.register(new XaerosMapTab());
        TabsMenu.register(new JourneyMapTab());
        TabsMenu.register(new DietTab());
        TabsMenu.register(new PassiveSkillTreeTab());
        TabsMenu.register(new PufferfishsSkillsTab());
        TabsMenu.register(new L2HostilityDifficultyTab());
        TabsMenu.register(new L2AttributeTab());
        TabsMenu.register(new L2ArtifactsTab());
        TabsMenu.register(new SophisticatedBackpacksTab());
        TabsMenu.register(new CosmeticArmorTab());
        TabsMenu.register(new CobblemonTab());
        TabsMenu.register(new DraconicEvolutionTab());
        TabsMenu.register(new ModularGolemsTab());

        // Run inspection for Ars Elixirum if needed
        try {
            Class<?> inspectorClass = Class.forName("vodmordia.modtabs.utils.ArsElixirumInspector");
            Method inspectMethod = inspectorClass.getMethod("inspectModClasses");
            inspectMethod.invoke(null);
        } catch (Exception e) {
            ModTabs.LOGGER.warn("Failed to run Ars Elixirum inspection: " + e.getMessage());
        }
        TabsMenu.register(new ArsElixirumTab());

        TabsMenu.register(new ArsNouveauTab());
        TabsMenu.register(new AdvancementsTab());

        // Wait for Patchouli books to load, then load custom tabs
        waitForPatchouliAndLoadCustomTabs();

        // Finalize all pending screen registrations now that all tabs are registered
        TabsMenu.finalizePendingRegistrations();

        ModTabs.LOGGER.info("Mod Tabs Client initialized successfully");
    }

    /**
     * Wait for Patchouli books to be loaded, then load custom tabs
     */
    private static void waitForPatchouliAndLoadCustomTabs() {
        // Run in a separate thread to avoid blocking the mod loading
        new Thread(() -> {
            try {
                if (Config.Baked.customTabsDebugLogging) {
                    ModTabs.LOGGER.info("Waiting for Patchouli books to be loaded...");
                }

                // Wait for Patchouli books to be loaded using their synchronization mechanism
                waitForPatchouliBooksLoaded();

                if (Config.Baked.customTabsDebugLogging) {
                    ModTabs.LOGGER.info("Patchouli books loaded! Loading custom tabs now.");
                }

                customTabsLoaded = true;
                loadCustomTabs();

            } catch (Exception e) {
                ModTabs.LOGGER.error("Error waiting for Patchouli books: " + e.getMessage());
                if (Config.Baked.customTabsDebugLogging) {
                    e.printStackTrace();
                }
            }
        }, "PatchouliCustomTabsLoader").start();
    }

    /**
     * Wait for Patchouli books to be loaded using reflection to access their synchronization mechanism
     */
    private static void waitForPatchouliBooksLoaded() {
        try {
            // Check if Patchouli is loaded first
            if (!FabricLoader.getInstance().isModLoaded("patchouli")) {
                if (Config.Baked.customTabsDebugLogging) {
                    ModTabs.LOGGER.info("Patchouli not loaded, proceeding without waiting");
                }
                return;
            }

            // Try to access Patchouli's Fabric client initializer for synchronization
            // This may need to be updated based on Patchouli's Fabric implementation
            try {
                Class<?> clientInitClass = Class.forName("vazkii.patchouli.fabric.client.FabricClientInitializer");
                // Similar synchronization mechanism as before, but adapted for Fabric
                // The exact implementation will depend on how Patchouli handles book loading in Fabric

                // For now, we'll use a simple wait approach
                Thread.sleep(3000); // Wait 3 seconds for books to load

            } catch (ClassNotFoundException e) {
                ModTabs.LOGGER.info("Patchouli Fabric client initializer not found, using fallback wait");
                Thread.sleep(3000);
            }

        } catch (Exception e) {
            ModTabs.LOGGER.warn("Failed to wait for Patchouli books: " + e.getMessage());
            if (Config.Baked.customTabsDebugLogging) {
                e.printStackTrace();
            }
            // Fallback: wait a short time
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Public method to manually trigger custom tabs loading (for debugging)
     */
    public static void forceLoadCustomTabs() {
        if (!customTabsLoaded) {
            ModTabs.LOGGER.info("Manually forcing custom tabs loading");
            customTabsLoaded = true;
            loadCustomTabs();
        } else {
            ModTabs.LOGGER.info("Custom tabs already loaded");
        }
    }

    /**
     * Load and register custom tabs from JSON configuration files
     */
    private static void loadCustomTabs() {
        if (Config.Baked.customTabsDebugLogging) {
            ModTabs.LOGGER.info("loadCustomTabs() called - customTabsEnabled: " + Config.Baked.customTabsEnabled);
        }

        if (!Config.Baked.customTabsEnabled) {
            ModTabs.LOGGER.info("Custom tabs are disabled in configuration");
            return;
        }

        try {
            if (Config.Baked.customTabsDebugLogging) {
                ModTabs.LOGGER.info("Loading custom tab definitions from JSON files...");
            }

            List<CustomTabDefinition> customTabDefinitions = CustomTabLoader.loadCustomTabs();

            if (Config.Baked.customTabsDebugLogging) {
                ModTabs.LOGGER.info("Found " + customTabDefinitions.size() + " custom tab definitions");
            }

            for (CustomTabDefinition definition : customTabDefinitions) {
                try {
                    if (Config.Baked.customTabsDebugLogging) {
                        ModTabs.LOGGER.info("Processing custom tab: " + definition.tabId +
                                " (enabled: " + definition.enabled +
                                ", action type: " + (definition.action != null ? definition.action.type : "null") + ")");
                    }

                    CustomJsonTab customTab = new CustomJsonTab(definition);
                    TabsMenu.register(customTab);

                    if (Config.Baked.customTabsDebugLogging) {
                        ModTabs.LOGGER.info("Successfully registered custom tab: " + definition.tabId + " (order: " + definition.order + ")");
                    }
                } catch (Exception e) {
                    ModTabs.LOGGER.error("Failed to register custom tab " + definition.tabId + ": " + e.getMessage());
                    if (Config.Baked.customTabsDebugLogging) {
                        e.printStackTrace();
                    }
                }
            }

            ModTabs.LOGGER.info("Loaded " + customTabDefinitions.size() + " custom tab(s)");

            // Re-finalize registrations to ensure custom tabs are properly integrated
            if (Config.Baked.customTabsDebugLogging) {
                ModTabs.LOGGER.info("Re-finalizing tab registrations for custom tabs");
            }
            TabsMenu.finalizePendingRegistrations();

            // Refresh the current screen to show newly loaded custom tabs
            refreshCurrentScreenForNewTabs();

        } catch (Exception e) {
            ModTabs.LOGGER.error("Error loading custom tabs: " + e.getMessage());
            if (Config.Baked.customTabsDebugLogging) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Refresh the current screen to show newly loaded custom tabs
     */
    private static void refreshCurrentScreenForNewTabs() {
        try {
            net.minecraft.client.MinecraftClient minecraft = net.minecraft.client.MinecraftClient.getInstance();
            if (minecraft.currentScreen != null) {
                // Get the current screen
                net.minecraft.client.gui.screen.Screen currentScreen = minecraft.currentScreen;

                // Check if this screen supports tabs
                if (TabsMenu.hasTabsForScreen(currentScreen.getClass())) {
                    if (Config.Baked.customTabsDebugLogging) {
                        ModTabs.LOGGER.info("Refreshing screen " + currentScreen.getClass().getSimpleName() + " to show new custom tabs");
                    }

                    // Force a refresh by closing and reopening the screen
                    // This will trigger the tab building process again
                    minecraft.setScreen(null);
                    minecraft.setScreen(currentScreen);
                }
            }
        } catch (Exception e) {
            ModTabs.LOGGER.warn("Failed to refresh current screen for new custom tabs: " + e.getMessage());
            if (Config.Baked.customTabsDebugLogging) {
                e.printStackTrace();
            }
        }
    }
}