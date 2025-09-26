package vodmordia.modtabs.client.events;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;
import vodmordia.modtabs.client.screens.TabButton;
import vodmordia.modtabs.client.screens.NextTabsButton;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

/**
 * Fabric event handlers for screen events
 */
public class FabricScreenEvents {

    /**
     * Called when a screen is initialized (equivalent to ScreenEvent.Init.Post)
     */
    public static void onScreenInit(Screen screen) {
        ModTabs.LOGGER.info("Screen init called for: {}", screen.getClass().getName());

        // Initialize tab buttons for this screen
        ModTabs.LOGGER.info("Calling TabsMenu.initScreenButtons for screen: {}", screen.getClass().getSimpleName());
        TabsMenu.initScreenButtons(screen);

        // Hide L2 tabs after screen initialization
        if (ModIntegrationManager.isModLoaded(ModIntegration.L2_LIBRARY) ||
            ModIntegrationManager.isModLoaded(ModIntegration.L2_HOSTILITY) ||
            ModIntegrationManager.isModLoaded(ModIntegration.L2_ARTIFACTS) ||
            ModIntegrationManager.isModLoaded(ModIntegration.MODULAR_GOLEMS)) {
            // But don't hide tabs when we're in the Modular Golems tracker screens (they need sub-tabs)
            boolean isModularGolemsTrackerScreen = false;
            try {
                Class<?> golemInfoScreenClass = Class.forName("dev.xkmc.modulargolems.content.client.tracker.GolemInfoScreen");
                isModularGolemsTrackerScreen = golemInfoScreenClass.isInstance(screen);
            } catch (ClassNotFoundException e) {
                // Class not found, not a Modular Golems screen
            }

            if (!isModularGolemsTrackerScreen) {
                hideL2Tabs(screen);
            }
        }
    }

    /**
     * Called when a screen is about to close
     */
    public static void onScreenClose(Screen screen) {
        // Clear tab screen tracking when screen closes naturally
        // (not when switching to another tab screen)
        if (!TabsMenu.wasScreenOpenedViaTab()) {
            TabsMenu.clearTabScreenTracking();
        }
    }

    /**
     * Handle key press events (equivalent to ScreenEvent.KeyPressed.Pre)
     */
    public static boolean onKeyPressed(Screen screen, int keyCode, int scanCode, int modifiers) {
        if (TabsMenu.wasScreenOpenedViaTab()) {
            MinecraftClient minecraft = MinecraftClient.getInstance();

            // Check if inventory key was pressed
            if (minecraft.options.inventoryKey.matchesKey(keyCode, scanCode)) {
                if (minecraft.player != null) {
                    try {
                        // Close any open container properly to prevent duplication
                        if (minecraft.player.currentScreenHandler != null &&
                            !minecraft.player.currentScreenHandler.getClass().getSimpleName().equals("PlayerScreenHandler")) {
                            minecraft.player.closeHandledScreen();
                        }
                    } catch (Exception e) {
                        // Ignore close errors
                    }

                    // Open inventory and clear tab tracking
                    TabsMenu.clearTabScreenTracking();
                    minecraft.setScreen(new InventoryScreen(minecraft.player));
                    return true; // Cancel the event
                }
            }
        }
        return false; // Don't cancel the event
    }

    /**
     * Handle mouse click events (equivalent to ScreenEvent.MouseButtonPressed.Pre)
     */
    public static boolean onMouseClicked(Screen screen, double mouseX, double mouseY, int button) {
        // Special handling for screens that might block tab button clicks
        String screenClassName = screen.getClass().getName();

        if (screenClassName.contains("puffish.skillsmod") ||
            screenClassName.contains("ftblibrary") ||
            screenClassName.contains("xaero.map")) {

            // Check if the click is on any tab button and forward the click
            for (var child : screen.children()) {
                if (child instanceof TabButton tabButton) {
                    if (tabButton.isMouseOver(mouseX, mouseY)) {
                        tabButton.onPress();
                        return true; // Cancel the original click
                    }
                }
                if (child instanceof NextTabsButton nextTabsButton) {
                    if (nextTabsButton.isMouseOver(mouseX, mouseY)) {
                        nextTabsButton.onPress();
                        return true; // Cancel the original click
                    }
                }
            }
        }
        return false; // Don't cancel the event
    }

    /**
     * Handle screen render pre events (equivalent to ScreenEvent.Render.Pre)
     */
    public static void onScreenRenderPre(Screen screen, DrawContext guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Update mouse position for tuck mode hover detection
        TabsMenu.onMouseMove(mouseX, mouseY, screen);

        if (screen instanceof HandledScreen<?> containerScreen) {
            if (!TabsMenu.hasCustomPositioning(screen)) {
                try {
                    // Use reflection to access protected fields
                    java.lang.reflect.Field xField = HandledScreen.class.getDeclaredField("x");
                    java.lang.reflect.Field yField = HandledScreen.class.getDeclaredField("y");
                    xField.setAccessible(true);
                    yField.setAccessible(true);
                    int x = xField.getInt(containerScreen);
                    int y = yField.getInt(containerScreen);
                    TabsMenu.updateButtonsPosition(screen, x, y);
                } catch (Exception e) {
                    // Fallback to default positioning if reflection fails
                    TabsMenu.updateButtonsPosition(screen, 0, 0);
                }
            }
        }
    }

    /**
     * Handle screen render post events (equivalent to ScreenEvent.Render.Post)
     */
    public static void onScreenRenderPost(Screen screen, DrawContext guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Update mouse position for hover effects
        TabsMenu.onMouseMove(mouseX, mouseY, screen);

        // Special rendering for screens that don't call super.render() properly
        String screenClassName = screen.getClass().getName();

        if (screenClassName.contains("puffish.skillsmod") ||
            screenClassName.contains("ftblibrary") ||
            screenClassName.contains("xaero.map")) {

            // Manually render tab buttons on top of these problematic screens
            for (var child : screen.children()) {
                if (child instanceof TabButton tabButton) {
                    tabButton.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
                }
                if (child instanceof NextTabsButton nextTabsButton) {
                    nextTabsButton.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
                }
            }
        }
    }

    /**
     * Hide L2 mod tabs from the screen
     */
    private static void hideL2Tabs(Screen screen) {
        if (ModIntegrationManager.isModLoaded(ModIntegration.L2_LIBRARY) ||
            ModIntegrationManager.isModLoaded(ModIntegration.L2_HOSTILITY) ||
            ModIntegrationManager.isModLoaded(ModIntegration.L2_ARTIFACTS) ||
            ModIntegrationManager.isModLoaded(ModIntegration.MODULAR_GOLEMS)) {
            try {
                // Remove L2's tab buttons from the screen
                screen.children().removeIf(widget -> {
                    String className = widget.getClass().getName();
                    boolean isL2Tab = className.contains("l2tabs") ||
                                     className.contains("l2library") ||
                                     className.contains("l2hostility") ||
                                     className.contains("l2artifacts") ||
                                     className.contains("modulargolems");
                    boolean isTab = className.toLowerCase().contains("tab");
                    return isL2Tab && isTab;
                });

                // Also try to remove from renderables using reflection
                try {
                    java.lang.reflect.Field renderablesField = Screen.class.getDeclaredField("renderables");
                    renderablesField.setAccessible(true);
                    Object renderables = renderablesField.get(screen);
                    if (renderables instanceof java.util.List) {
                        ((java.util.List<?>) renderables).removeIf(renderable -> {
                            String className = renderable.getClass().getName();
                            boolean isL2Tab = className.contains("l2tabs") ||
                                             className.contains("l2library") ||
                                             className.contains("l2hostility") ||
                                             className.contains("l2artifacts") ||
                                             className.contains("modulargolems");
                            boolean isTab = className.toLowerCase().contains("tab");
                            return isL2Tab && isTab;
                        });
                    }
                } catch (Exception e) {
                    // Ignore if we can't access renderables field
                }

            } catch (Exception e) {
                // Ignore errors when trying to hide L2 tabs
            }
        }
    }
}