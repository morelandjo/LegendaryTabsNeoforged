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
 * Fabric event handlers to replace NeoForge event system
 */
public class FabricScreenEvents {

    /**
     * Called when a screen is initialized (equivalent to ScreenEvent.Init.Post)
     */
    public static void onScreenInit(Screen screen) {
        // Initialize tab buttons for this screen
        TabsMenu.initScreenButtons(screen);

        // Handle special screen positioning if needed
        if (TabsMenu.hasCustomPositioning(screen)) {
            // For screens with custom positioning, update button positions
            TabsMenu.updateButtonsPosition(screen, 0, 0);
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
    public static void hideL2Tabs(Screen screen) {
        // Hide L2 Hostility tabs when they conflict with our tabs
        String screenName = screen.getClass().getName();

        if (screenName.contains("l2hostility")) {
            try {
                // Find and hide any L2 tab widgets
                for (var child : screen.children()) {
                    String childName = child.getClass().getName();
                    if (childName.contains("l2hostility") && childName.contains("tab")) {
                        // Try to make the L2 tab invisible using reflection
                        try {
                            java.lang.reflect.Field visibleField = child.getClass().getField("visible");
                            visibleField.setAccessible(true);
                            visibleField.set(child, false);
                        } catch (Exception visibleEx) {
                            // If visible field doesn't exist, try setVisible method
                            try {
                                java.lang.reflect.Method setVisibleMethod = child.getClass().getMethod("setVisible", boolean.class);
                                setVisibleMethod.invoke(child, false);
                            } catch (Exception methodEx) {
                                // Ignore if we can't hide the widget
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore errors when trying to hide L2 tabs
                ModTabs.LOGGER.debug("Could not hide L2 tabs: " + e.getMessage());
            }
        }
    }
}