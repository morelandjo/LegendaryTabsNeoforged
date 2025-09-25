package vodmordia.modtabs.api.tabs_menu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerEntity;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.config.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central registry and management for tabs system
 */
public class TabsMenu {
    private static final List<TabBase> registeredTabs = new ArrayList<>();
    private static final Map<Class<? extends Screen>, List<TabBase>> screenTabs = new HashMap<>();
    private static final List<Runnable> pendingRegistrations = new ArrayList<>();

    // Screen tracking for tab navigation
    private static boolean screenOpenedViaTab = false;
    private static Class<? extends Screen> lastTabScreen = null;

    // Mouse tracking for hover effects
    private static int lastMouseX = 0;
    private static int lastMouseY = 0;

    /**
     * Register a tab
     */
    public static void register(TabBase tab) {
        registeredTabs.add(tab);
        tab.initTabOnScreens();
        ModTabs.LOGGER.debug("Registered tab: " + tab.getClass().getSimpleName());
    }

    /**
     * Get all registered tabs
     */
    public static List<TabBase> getRegisteredTabs() {
        return new ArrayList<>(registeredTabs);
    }

    /**
     * Get tabs for a specific screen
     */
    public static List<TabBase> getTabsForScreen(Class<? extends Screen> screenClass) {
        return screenTabs.getOrDefault(screenClass, new ArrayList<>());
    }

    /**
     * Check if a screen has any tabs
     */
    public static boolean hasTabsForScreen(Class<? extends Screen> screenClass) {
        List<TabBase> tabs = getTabsForScreen(screenClass);
        return !tabs.isEmpty();
    }

    /**
     * Get enabled tabs for a screen and player
     */
    public static List<TabBase> getEnabledTabsForScreen(Class<? extends Screen> screenClass, PlayerEntity player) {
        List<TabBase> allTabs = getTabsForScreen(screenClass);
        List<TabBase> enabledTabs = new ArrayList<>();

        for (TabBase tab : allTabs) {
            try {
                if (tab.isEnabled(player)) {
                    enabledTabs.add(tab);
                }
            } catch (Exception e) {
                ModTabs.LOGGER.warn("Error checking if tab is enabled: " + tab.getClass().getSimpleName() + " - " + e.getMessage());
            }
        }

        return enabledTabs;
    }

    /**
     * Register a screen to have tabs
     */
    public static void registerScreenForTabs(Class<? extends Screen> screenClass, TabBase tab) {
        screenTabs.computeIfAbsent(screenClass, k -> new ArrayList<>()).add(tab);
        ModTabs.LOGGER.debug("Registered tab " + tab.getClass().getSimpleName() + " for screen " + screenClass.getSimpleName());
    }

    /**
     * Add pending registration to be processed later
     */
    public static void addPendingRegistration(Runnable registration) {
        pendingRegistrations.add(registration);
    }

    /**
     * Process all pending registrations
     */
    public static void finalizePendingRegistrations() {
        ModTabs.LOGGER.debug("Processing " + pendingRegistrations.size() + " pending registrations");

        for (Runnable registration : pendingRegistrations) {
            try {
                registration.run();
            } catch (Exception e) {
                ModTabs.LOGGER.error("Error processing pending registration: " + e.getMessage());
            }
        }

        pendingRegistrations.clear();
    }

    /**
     * Initialize screen buttons (called from mixin)
     */
    public static void initScreenButtons(Screen screen) {
        if (screen == null) return;

        MinecraftClient minecraft = MinecraftClient.getInstance();
        if (minecraft.player == null) return;

        List<TabBase> enabledTabs = getEnabledTabsForScreen(screen.getClass(), minecraft.player);
        if (enabledTabs.isEmpty()) return;

        if (Config.Baked.enableDebugLogging) {
            ModTabs.LOGGER.info("Initializing {} tab buttons for screen: {}",
                enabledTabs.size(), screen.getClass().getSimpleName());
        }

        // Create and add TabButton widgets to screen
        createTabButtons(screen, minecraft.player, enabledTabs);
    }

    /**
     * Update mouse position for tuck mode and hover effects
     */
    public static void onMouseMove(int mouseX, int mouseY, Screen screen) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;

        // Update hover states for tab buttons
        if (screen != null) {
            for (var child : screen.children()) {
                if (child instanceof vodmordia.modtabs.client.screens.TabButton tabButton) {
                    // Hover state is automatically handled by the widget's isMouseOver method
                    // No need to manually set hover state
                } else if (child instanceof vodmordia.modtabs.client.screens.NextTabsButton nextTabsButton) {
                    // Hover state is automatically handled by the widget's isMouseOver method
                    // No need to manually set hover state
                }
            }
        }
    }

    /**
     * Check if screen has custom positioning
     */
    public static boolean hasCustomPositioning(Screen screen) {
        if (screen == null) return false;

        // Some screens handle their own positioning
        String screenName = screen.getClass().getName();
        return screenName.contains("InventoryScreen") ||
               screenName.contains("CreativeInventoryScreen") ||
               screenName.contains("SurvivalInventoryScreen");
    }

    /**
     * Update button positions for screens that need it
     */
    public static void updateButtonsPosition(Screen screen, int x, int y) {
        if (screen == null) return;

        // Update positions of all tab buttons for this screen
        if (screen != null) {
            int tabIndex = 0;
            int maxTabsPerColumn = 8;
            int tabSpacing = TabBase.TAB_HEIGHT + 2;

            for (var child : screen.children()) {
                if (child instanceof vodmordia.modtabs.client.screens.TabButton tabButton) {
                    // Calculate new position based on index
                    int currentColumn = tabIndex / maxTabsPerColumn;
                    int currentRow = tabIndex % maxTabsPerColumn;

                    int newX = x + 10 + (currentColumn * (TabBase.TAB_WIDTH + 5));
                    int newY = y + 50 + (currentRow * tabSpacing);

                    // Set position using ButtonWidget's setX/setY methods
                    tabButton.setX(newX);
                    tabButton.setY(newY);
                    tabIndex++;
                } else if (child instanceof vodmordia.modtabs.client.screens.NextTabsButton nextButton) {
                    // Position next button after the last column
                    int currentColumn = (tabIndex / maxTabsPerColumn) + 1;
                    int nextButtonX = x + 10 + (currentColumn * (TabBase.TAB_WIDTH + 5));
                    int nextButtonY = y + 50;

                    // Set position using ButtonWidget's setX/setY methods
                    nextButton.setX(nextButtonX);
                    nextButton.setY(nextButtonY);
                }
            }
        }

        if (Config.Baked.enableDebugLogging) {
            ModTabs.LOGGER.debug("Updated tab button positions for {}: ({}, {})",
                screen.getClass().getSimpleName(), x, y);
        }
    }

    /**
     * Check if the current screen was opened via a tab click
     */
    public static boolean wasScreenOpenedViaTab() {
        return screenOpenedViaTab;
    }

    /**
     * Clear tab screen tracking
     */
    public static void clearTabScreenTracking() {
        screenOpenedViaTab = false;
        lastTabScreen = null;
    }

    /**
     * Mark that a screen is being opened via tab
     */
    public static void markScreenOpenedViaTab(Class<? extends Screen> screenClass) {
        screenOpenedViaTab = true;
        lastTabScreen = screenClass;
    }

    /**
     * Open a target screen from a tab
     */
    public static void openTabScreen(TabBase tab, PlayerEntity player) {
        try {
            markScreenOpenedViaTab(null); // We'll determine this when the screen opens
            tab.openTargetScreen(player);
        } catch (Exception e) {
            ModTabs.LOGGER.error("Failed to open screen for tab {}: {}",
                tab.getClass().getSimpleName(), e.getMessage());
            clearTabScreenTracking();
        }
    }

    /**
     * Create and add tab buttons to a screen
     */
    private static void createTabButtons(Screen screen, PlayerEntity player, List<TabBase> enabledTabs) {
        if (enabledTabs.isEmpty()) return;

        try {
            // Calculate tab positions
            int startX = 10; // Left side of screen
            int startY = 50; // Below screen title
            int tabSpacing = TabBase.TAB_HEIGHT + 2;

            // Maximum tabs per column
            int maxTabsPerColumn = 8;
            int currentColumn = 0;
            int currentRow = 0;

            for (int i = 0; i < enabledTabs.size(); i++) {
                TabBase tab = enabledTabs.get(i);

                if (currentRow >= maxTabsPerColumn) {
                    currentColumn++;
                    currentRow = 0;
                }

                int tabX = startX + (currentColumn * (TabBase.TAB_WIDTH + 5));
                int tabY = startY + (currentRow * tabSpacing);

                // Create TabButton
                vodmordia.modtabs.client.screens.TabButton tabButton =
                    new vodmordia.modtabs.client.screens.TabButton(tab, player, tabX, tabY);

                // Add to screen using reflection for access
                try {
                    // Use reflection to access protected addDrawableChild method
                    java.lang.reflect.Method addDrawableChildMethod =
                        net.minecraft.client.gui.screen.Screen.class.getDeclaredMethod("addDrawableChild",
                            net.minecraft.client.gui.Element.class);
                    addDrawableChildMethod.setAccessible(true);
                    addDrawableChildMethod.invoke(screen, tabButton);
                } catch (Exception e) {
                    // Fallback: cast and add directly to children list
                    try {
                        @SuppressWarnings("unchecked")
                        java.util.List<net.minecraft.client.gui.Element> children =
                            (java.util.List<net.minecraft.client.gui.Element>) screen.children();
                        children.add(tabButton);
                    } catch (Exception e2) {
                        // Final fallback - just log the error
                        if (Config.Baked.enableDebugLogging) {
                            ModTabs.LOGGER.warn("Failed to add tab button to screen: " + e2.getMessage());
                        }
                    }
                }

                currentRow++;
            }

            // Add NextTabsButton if there are too many tabs
            if (enabledTabs.size() > maxTabsPerColumn) {
                int nextButtonX = startX + ((currentColumn + 1) * (TabBase.TAB_WIDTH + 5));
                int nextButtonY = startY;

                vodmordia.modtabs.client.screens.NextTabsButton nextButton =
                    new vodmordia.modtabs.client.screens.NextTabsButton(
                        nextButtonX, nextButtonY, enabledTabs, maxTabsPerColumn,
                        () -> refreshTabButtons(screen, player, enabledTabs));

                try {
                    // Use reflection to access protected addDrawableChild method
                    java.lang.reflect.Method addDrawableChildMethod =
                        net.minecraft.client.gui.screen.Screen.class.getDeclaredMethod("addDrawableChild",
                            net.minecraft.client.gui.Element.class);
                    addDrawableChildMethod.setAccessible(true);
                    addDrawableChildMethod.invoke(screen, nextButton);
                } catch (Exception e) {
                    // Ignore if we can't add the next button
                }
            }

        } catch (Exception e) {
            if (Config.Baked.enableDebugLogging) {
                ModTabs.LOGGER.error("Error creating tab buttons: " + e.getMessage());
            }
        }
    }

    /**
     * Refresh tab buttons (for pagination)
     */
    private static void refreshTabButtons(Screen screen, PlayerEntity player, List<TabBase> enabledTabs) {
        // Remove existing tab buttons and recreate them
        // This is a simple implementation - could be optimized
        try {
            // Clear existing widgets
            screen.children().removeIf(widget ->
                widget instanceof vodmordia.modtabs.client.screens.TabButton ||
                widget instanceof vodmordia.modtabs.client.screens.NextTabsButton);

            // Try to access drawables list via reflection for cleanup
            try {
                java.lang.reflect.Field drawablesField = net.minecraft.client.gui.screen.Screen.class.getDeclaredField("drawables");
                drawablesField.setAccessible(true);
                Object drawables = drawablesField.get(screen);
                if (drawables instanceof java.util.List) {
                    ((java.util.List<?>) drawables).removeIf(drawable ->
                        drawable instanceof vodmordia.modtabs.client.screens.TabButton ||
                        drawable instanceof vodmordia.modtabs.client.screens.NextTabsButton);
                }
            } catch (Exception e) {
                // Ignore reflection errors
            }

            // Recreate buttons
            createTabButtons(screen, player, enabledTabs);
        } catch (Exception e) {
            if (Config.Baked.enableDebugLogging) {
                ModTabs.LOGGER.error("Error refreshing tab buttons: " + e.getMessage());
            }
        }
    }

    /**
     * Clear all registrations (for testing)
     */
    public static void clear() {
        registeredTabs.clear();
        screenTabs.clear();
        pendingRegistrations.clear();
    }
}