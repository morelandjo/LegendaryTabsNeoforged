package vodmordia.modtabs;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;
import vodmordia.modtabs.client.tabs_menu.*;
import vodmordia.modtabs.client.tabs_menu.AdvancementsTab;
import vodmordia.modtabs.client.tabs_menu.CustomJsonTab;
import vodmordia.modtabs.client.keybinds.ModKeybinds;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.config.ModTabsConfig;
import vodmordia.modtabs.integration.ModIntegrationManager;
import vodmordia.modtabs.utils.CustomTabLoader;
import vodmordia.modtabs.config.CustomTabDefinition;
import eu.midnightdust.lib.config.MidnightConfig;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

@Mod(ModTabs.MOD_ID)
public class ModTabs
{
    public static final String MOD_ID = "modtabs";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static Path configPath = FMLPaths.CONFIGDIR.get();
    public static Path modConfigPath = Paths.get(configPath.toAbsolutePath().toString(), "modtabs");

    private static boolean customTabsLoaded = false;


    public ModTabs(IEventBus modEventBus, ModContainer modContainer)
    {
        IEventBus neoForgeBus = NeoForge.EVENT_BUS;

        // MidnightConfig.init is deferred to FMLCommonSetupEvent (enqueueWork) to avoid
        // a ConcurrentModificationException race with MidnightLib's own constructor —
        // NeoForge dispatches @Mod constructors in parallel, and MidnightConfig mutates
        // shared static LinkedHashMaps without synchronization.
        modEventBus.addListener((net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent event) ->
            event.enqueueWork(() -> MidnightConfig.init(MOD_ID, ModTabsConfig.class))
        );

        // Initialize mod integration manager
        ModIntegrationManager.detectLoadedMods();

        // Register network packets
        modEventBus.addListener(this::registerNetworking);

        // Event handlers removed - now using proper Patchouli synchronization
    }

    private void registerNetworking(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent event) {
        // optional() so a client-only install can still connect to vanilla / mod-less servers.
        // Without it, NeoForge marks the channels as required on both ends and rejects the handshake.
        var registrar = event.registrar(MOD_ID).versioned("1.0").optional();

        registrar.playToServer(
            vodmordia.modtabs.network.TomeConvertPayload.TYPE,
            vodmordia.modtabs.network.TomeConvertPayload.STREAM_CODEC,
            vodmordia.modtabs.network.TomeConvertHandler::handle
        );

        registrar.playToServer(
            vodmordia.modtabs.network.OpenReliableBackpackPayload.TYPE,
            vodmordia.modtabs.network.OpenReliableBackpackPayload.STREAM_CODEC,
            vodmordia.modtabs.network.OpenReliableBackpackHandler::handle
        );
        registrar.playToServer(
            vodmordia.modtabs.network.OpenQuarkBackpackPayload.TYPE,
            vodmordia.modtabs.network.OpenQuarkBackpackPayload.STREAM_CODEC,
            vodmordia.modtabs.network.OpenQuarkBackpackHandler::handle
        );

        LOGGER.info("Registered ModTabs network packets");
    }

    // Old config event handlers removed - MidnightConfig handles reloading automatically

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(ModKeybinds.TAB_CYCLE);
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            Config.Baked.bakeClient();

            // Initialize icons directory for custom tab icons
            vodmordia.modtabs.utils.DynamicTextureLoader.getIconsDirectory();

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
                LOGGER.warn("Failed to run FTB Quests inspection: " + e.getMessage());
            }
            TabsMenu.register(new FtbQuestsTab());

            // Run inspection for FTB Teams if needed
            try {
                Class<?> inspectorClass = Class.forName("vodmordia.modtabs.utils.FTBTeamsInspector");
                Method inspectMethod = inspectorClass.getMethod("inspectModClasses");
                inspectMethod.invoke(null);
            } catch (Exception e) {
                LOGGER.warn("Failed to run FTB Teams inspection: " + e.getMessage());
            }
            TabsMenu.register(new FtbTeamsTab());
            TabsMenu.register(new FtbChunksTab());

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
                LOGGER.warn("Failed to run Ars Elixirum inspection: " + e.getMessage());
            }
            TabsMenu.register(new ArsElixirumTab());

            TabsMenu.register(new ArsNouveauTab());
            TabsMenu.register(new AdvancementsTab());
            TabsMenu.register(new BrassworksMissionsTab());
            TabsMenu.register(new EccentricTomeTab());
            TabsMenu.register(new RpgCraftingTab());
            TabsMenu.register(new MotpTab());
            TabsMenu.register(new BiologyDictionaryTab());
            TabsMenu.register(new ReliableBackpackTab());
            TabsMenu.register(new QuarkBackpackTab());
            TabsMenu.register(new WildexTab());
            TabsMenu.register(new ApothicAttributesTab());
            TabsMenu.register(new AetherTab());
            TabsMenu.register(new CuriosTab());
            TabsMenu.register(new EpicFightTab());
            TabsMenu.register(new EpicSkillsTab());
            TabsMenu.register(new CompletionistsIndexTab());
            TabsMenu.register(new SdmShopTab());
            TabsMenu.register(new JobsPlusTab());
            TabsMenu.register(new QuestLogTab());
            TabsMenu.register(new MapwrightTab());
            TabsMenu.register(new FieldGuideTab());
            TabsMenu.register(new ModonomiconTab());

            // One transient tab per nearby container block (chests, barrels, modded
            // inventories, …). Discovered fresh on every screen-init.
            TabsMenu.registerDynamicProvider(new NearbyContainersProvider());

            // Mount the tab row on the vanilla container screens too so the bar follows the
            // player when they click a nearby-chest tab. Without this, ContainerScreen has no
            // tabs registered and the bar disappears once a chest is open — defeating the
            // point of being able to hop between nearby chests via tab clicks.
            // ContainerScreen covers chests / barrels / ender chests / trapped chests (they
            // all share ChestMenu); the others are separate vanilla screen classes.
            vodmordia.modtabs.api.tabs_menu.ScreenRegistry.registerStandardScreens(
                    net.minecraft.client.gui.screens.inventory.ContainerScreen.class,
                    net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen.class,
                    net.minecraft.client.gui.screens.inventory.DispenserScreen.class,
                    net.minecraft.client.gui.screens.inventory.HopperScreen.class
            );

            // Wait for Patchouli books to load, then load custom tabs
            waitForPatchouliAndLoadCustomTabs();

            // Finalize all pending screen registrations now that all tabs are registered
            TabsMenu.finalizePendingRegistrations();
        }

        /**
         * Wait for Patchouli books to be loaded, then load custom tabs
         */
        private static void waitForPatchouliAndLoadCustomTabs() {
            // Run in a separate thread to avoid blocking the mod loading
            new Thread(() -> {
                try {
                    if (Config.Baked.customTabsDebugLogging) {
                        LOGGER.info("Waiting for Patchouli books to be loaded...");
                    }

                    // Wait for Patchouli books to be loaded using their synchronization mechanism
                    waitForPatchouliBooksLoaded();

                    if (Config.Baked.customTabsDebugLogging) {
                        LOGGER.info("Patchouli books loaded! Loading custom tabs now.");
                    }

                    customTabsLoaded = true;
                    loadCustomTabs();

                } catch (Exception e) {
                    LOGGER.error("Error waiting for Patchouli books: " + e.getMessage());
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
                if (!ModList.get().isLoaded("patchouli")) {
                    if (Config.Baked.customTabsDebugLogging) {
                        LOGGER.info("Patchouli not loaded, proceeding without waiting");
                    }
                    return;
                }

                // Access Patchouli's NeoForgeClientInitializer using reflection
                Class<?> clientInitClass = Class.forName("vazkii.patchouli.neoforge.client.NeoForgeClientInitializer");

                // Get the BOOK_LOAD_LOCK and BOOK_LOAD_CONDITION
                java.lang.reflect.Field lockField = clientInitClass.getDeclaredField("BOOK_LOAD_LOCK");
                lockField.setAccessible(true);
                java.util.concurrent.locks.Lock bookLoadLock = (java.util.concurrent.locks.Lock) lockField.get(null);

                java.lang.reflect.Field conditionField = clientInitClass.getDeclaredField("BOOK_LOAD_CONDITION");
                conditionField.setAccessible(true);
                java.util.concurrent.locks.Condition bookLoadCondition = (java.util.concurrent.locks.Condition) conditionField.get(null);

                java.lang.reflect.Field booksLoadedField = clientInitClass.getDeclaredField("booksLoaded");
                booksLoadedField.setAccessible(true);

                // Wait for books to be loaded using Patchouli's synchronization mechanism.
                // Use a bounded timeout so a Patchouli internal change (renamed field, missed signal)
                // can never hang this loader thread forever.
                final long timeoutNanos = java.util.concurrent.TimeUnit.SECONDS.toNanos(30);
                long remaining = timeoutNanos;
                bookLoadLock.lock();
                try {
                    while (!(Boolean) booksLoadedField.get(null)) {
                        if (remaining <= 0) {
                            LOGGER.warn("Timed out after 30s waiting for Patchouli books to load; proceeding anyway");
                            break;
                        }
                        if (Config.Baked.customTabsDebugLogging) {
                            LOGGER.info("Books not yet loaded, waiting...");
                        }
                        remaining = bookLoadCondition.awaitNanos(remaining);
                    }
                    if (Config.Baked.customTabsDebugLogging) {
                        LOGGER.info("Patchouli signaled that books are loaded!");
                    }
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    LOGGER.warn("Interrupted while waiting for Patchouli books; proceeding anyway");
                } finally {
                    bookLoadLock.unlock();
                }

            } catch (ClassNotFoundException e) {
                LOGGER.info("Patchouli NeoForgeClientInitializer not found, proceeding without waiting");
            } catch (Exception e) {
                LOGGER.warn("Failed to wait for Patchouli books using synchronization mechanism: " + e.getMessage());
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
                LOGGER.info("Manually forcing custom tabs loading");
                customTabsLoaded = true;
                loadCustomTabs();
            } else {
                LOGGER.info("Custom tabs already loaded");
            }
        }


        /**
         * Load and register custom tabs from JSON configuration files
         */
        private static void loadCustomTabs() {
            if (Config.Baked.customTabsDebugLogging) {
                LOGGER.info("loadCustomTabs() called - customTabsEnabled: " + Config.Baked.customTabsEnabled);
            }

            if (!Config.Baked.customTabsEnabled) {
                LOGGER.info("Custom tabs are disabled in configuration");
                return;
            }

            try {
                if (Config.Baked.customTabsDebugLogging) {
                    LOGGER.info("Loading custom tab definitions from JSON files...");
                }

                java.util.List<CustomTabDefinition> customTabDefinitions = CustomTabLoader.loadCustomTabs();

                if (Config.Baked.customTabsDebugLogging) {
                    LOGGER.info("Found " + customTabDefinitions.size() + " custom tab definitions");
                }

                for (CustomTabDefinition definition : customTabDefinitions) {
                    try {
                        if (Config.Baked.customTabsDebugLogging) {
                            LOGGER.info("Processing custom tab: " + definition.tabId +
                                      " (enabled: " + definition.enabled +
                                      ", action type: " + (definition.action != null ? definition.action.type : "null") + ")");
                        }

                        CustomJsonTab customTab = new CustomJsonTab(definition);
                        TabsMenu.register(customTab);

                        if (Config.Baked.customTabsDebugLogging) {
                            LOGGER.info("Successfully registered custom tab: " + definition.tabId + " (order: " + definition.order + ")");
                        }
                    } catch (Exception e) {
                        LOGGER.error("Failed to register custom tab " + definition.tabId + ": " + e.getMessage());
                        if (Config.Baked.customTabsDebugLogging) {
                            e.printStackTrace();
                        }
                    }
                }

                LOGGER.info("Loaded " + customTabDefinitions.size() + " custom tab(s)");

                // Re-finalize registrations to ensure custom tabs are properly integrated
                if (Config.Baked.customTabsDebugLogging) {
                    LOGGER.info("Re-finalizing tab registrations for custom tabs");
                }
                TabsMenu.finalizePendingRegistrations();

                // Refresh the current screen to show newly loaded custom tabs
                refreshCurrentScreenForNewTabs();

            } catch (Exception e) {
                LOGGER.error("Error loading custom tabs: " + e.getMessage());
                if (Config.Baked.customTabsDebugLogging) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Refresh the current screen to show newly loaded custom tabs.
         *
         * This is invoked from the {@code PatchouliCustomTabsLoader} worker thread, but
         * {@link net.minecraft.client.Minecraft#setScreen} must run on the client thread —
         * mutating the screen off-thread can race with rendering and crash. Schedule the
         * refresh via {@code Minecraft.execute} so it lands on the right thread.
         */
        private static void refreshCurrentScreenForNewTabs() {
            net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
            minecraft.execute(() -> {
                try {
                    net.minecraft.client.gui.screens.Screen currentScreen = minecraft.screen;
                    if (currentScreen == null) {
                        return;
                    }
                    if (!vodmordia.modtabs.api.tabs_menu.TabsMenu.hasTabsForScreen(currentScreen.getClass())) {
                        return;
                    }
                    if (Config.Baked.customTabsDebugLogging) {
                        LOGGER.info("Refreshing screen " + currentScreen.getClass().getSimpleName() + " to show new custom tabs");
                    }
                    // Force a refresh by closing and reopening — re-runs the tab build pass.
                    minecraft.setScreen(null);
                    minecraft.setScreen(currentScreen);
                } catch (Exception e) {
                    LOGGER.warn("Failed to refresh current screen for new custom tabs: " + e.getMessage());
                    if (Config.Baked.customTabsDebugLogging) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}