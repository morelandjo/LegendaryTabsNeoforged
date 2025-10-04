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
        // Initialize tab buttons for this screen
        TabsMenu.initScreenButtons(screen);

        // Register keyboard events for all screens
        net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents.allowKeyPress(screen).register((screen1, key, scancode, modifiers) -> {
            // Return false to cancel the event, true to allow it
            return !onKeyPressed(screen1, key, scancode, modifiers);
        });

        // Register render events for all screens
        net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.beforeRender(screen).register((screen1, drawContext, mouseX, mouseY, tickDelta) -> {
            onScreenRenderPre(screen1, drawContext, mouseX, mouseY, tickDelta);
        });

        // Register screen remove event to handle close
        net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.remove(screen).register(screen1 -> {
            onScreenClose(screen1);
        });

        // Register afterRender event for screens that need tabs rendered on top
        String screenClassName = screen.getClass().getName();

        // Check for AdvancementsScreen by class type (works in both dev and production)
        boolean isAdvancementsScreen = false;
        try {
            isAdvancementsScreen = screen instanceof net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
        } catch (Exception e) {
            // Ignore if class not found
        }

        boolean needsLateRendering = screenClassName.contains("puffish.skillsmod") ||
            screenClassName.contains("ftblibrary") ||
            screenClassName.contains("xaero.map") ||
            isAdvancementsScreen ||
            screenClassName.contains("mapatlases");

        if (needsLateRendering && TabsMenu.hasTabsForScreen(screen.getClass())) {
            net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.afterRender(screen).register((screen1, drawContext, mouseX, mouseY, tickDelta) -> {
                // Render tabs at the very end, after all screen content including backgrounds
                renderTabsAfterScreen(screen1, drawContext, mouseX, mouseY, tickDelta);
            });

            // Register mouse click handler for these screens to ensure tab clicks work
            net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents.beforeMouseClick(screen).register((screen1, mouseX, mouseY, button) -> {
                // Check if click is on a tab button
                for (var child : screen1.children()) {
                    if (child instanceof TabButton tabButton) {
                        if (tabButton.isMouseOver(mouseX, mouseY)) {
                            vodmordia.modtabs.ModTabs.LOGGER.info("Tab button clicked at ({}, {})", mouseX, mouseY);
                            tabButton.onPress();
                            return; // Consume the click event
                        }
                    }
                    if (child instanceof NextTabsButton nextTabsButton) {
                        if (nextTabsButton.isMouseOver(mouseX, mouseY)) {
                            vodmordia.modtabs.ModTabs.LOGGER.info("Next button clicked at ({}, {})", mouseX, mouseY);
                            nextTabsButton.onPress();
                            return; // Consume the click event
                        }
                    }
                }
            });
        } else {
            // For other screens, use regular afterRender for tab rendering
            net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.afterRender(screen).register((screen1, drawContext, mouseX, mouseY, tickDelta) -> {
                onScreenRenderPost(screen1, drawContext, mouseX, mouseY, tickDelta);
            });
        }

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
     * Handle screen render pre events (equivalent to ScreenEvent.Render.Pre)
     */
    public static void onScreenRenderPre(Screen screen, DrawContext guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Update mouse position for tuck mode hover detection
        TabsMenu.onMouseMove(mouseX, mouseY, screen);

        // For HandledScreen instances with GUI_RELATIVE positioning,
        // the position is already calculated correctly in initScreenButtons
        // DO NOT update positions here as it causes incorrect positioning in production
        // Only update for custom positioning modes
        if (screen instanceof HandledScreen<?> containerScreen) {
            // Skip position updates - positions are set correctly in initScreenButtons
            // This was causing tabs to be positioned incorrectly in production
        }
    }

    /**
     * Handle screen render post events (equivalent to ScreenEvent.Render.Post)
     */
    private static int renderCallCount = 0;

    public static void onScreenRenderPost(Screen screen, DrawContext guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Update mouse position for hover effects
        TabsMenu.onMouseMove(mouseX, mouseY, screen);

        String screenClassName = screen.getClass().getName();

        // Check if this screen has tabs registered
        boolean hasTabs = TabsMenu.hasTabsForScreen(screen.getClass());
        if (!hasTabs) {
            return;
        }

        // For certain screens that don't call super.render() or render tabs behind content,
        // we need to manually render tabs on top
        // NOTE: Screens that use afterRender (puffish, ftblibrary, AdvancementsScreen, mapatlases)
        // are now handled by the afterRender event registered in onScreenInit, so we only
        // manually render for xaero.map here
        boolean needsManualRendering = screenClassName.contains("xaero.map");

        if (needsManualRendering) {
            // Push matrix and translate Z to render on top of backgrounds
            guiGraphics.getMatrices().push();
            guiGraphics.getMatrices().translate(0, 0, 400); // High Z value to render on top

            // Manually render tab buttons on top of screen content
            for (var child : screen.children()) {
                if (child instanceof TabButton tabButton) {
                    tabButton.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
                }
                if (child instanceof NextTabsButton nextTabsButton) {
                    nextTabsButton.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
                }
            }

            guiGraphics.getMatrices().pop();
        }
    }

    /**
     * Render tabs after all screen content (called from ScreenEvents.afterRender)
     */
    private static void renderTabsAfterScreen(Screen screen, DrawContext drawContext, int mouseX, int mouseY, float tickDelta) {
        // Push matrix and translate Z to render on top of backgrounds
        drawContext.getMatrices().push();
        drawContext.getMatrices().translate(0, 0, 400); // High Z value to render on top

        // Manually render tab buttons on top
        for (var child : screen.children()) {
            if (child instanceof TabButton tabButton) {
                tabButton.renderWidget(drawContext, mouseX, mouseY, tickDelta);
            }
            if (child instanceof NextTabsButton nextTabsButton) {
                nextTabsButton.renderWidget(drawContext, mouseX, mouseY, tickDelta);
            }
        }

        drawContext.getMatrices().pop();
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