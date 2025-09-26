package vodmordia.modtabs.api.tabs_menu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerEntity;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.client.screens.NextTabsButton;
import vodmordia.modtabs.client.screens.TabButton;
import vodmordia.modtabs.client.tabs_menu.InventoryTab;
import vodmordia.modtabs.config.Config;

import java.util.*;
import java.util.function.Function;

import static vodmordia.modtabs.api.tabs_menu.TabBase.TAB_HEIGHT;
import static vodmordia.modtabs.api.tabs_menu.TabBase.TAB_WIDTH;

public class TabsMenu {
    private static final Map<Class<? extends Screen>, ScreenInfo> tabsScreens = new HashMap<>();
    private static final List<TabBase> allRegisteredTabs = new ArrayList<>();
    private static final List<ScreenRegistration> pendingScreenRegistrations = new ArrayList<>();
    private static int leftScreenPos;
    private static int topScreenPos;
    private static int startTabIndex;
    private static int currentTabsCount;
    private static List<TabBase> enabledTabs;
    private static boolean screenOpenedViaTab = false;
    private static Screen sourceScreen = null;
    private static int preservedStartTabIndex = 0;

    private TabsMenu() {
    }

    public static void addTabToScreen(TabBase newTab, Class<? extends Screen> screen, Function<PlayerEntity, Integer> screenWidth, Function<PlayerEntity, Integer> screenHeight, int priority) {
        if (tabsScreens.containsKey(screen)) {
            tabsScreens.get(screen).addTab(priority, newTab);
        } else {
            ScreenInfo screenInfo = new ScreenInfo(screenWidth, screenHeight, newTab, priority);
            tabsScreens.put(screen, screenInfo);
        }
    }

    public static void registerScreenWithAllTabs(Class<? extends Screen> screen, Function<PlayerEntity, Integer> screenWidth, Function<PlayerEntity, Integer> screenHeight) {
        pendingScreenRegistrations.add(new ScreenRegistration(screen, screenWidth, screenHeight));
    }

    public static void registerScreenWithAllTabs(Class<? extends Screen> screen, Function<PlayerEntity, Integer> screenWidth, Function<PlayerEntity, Integer> screenHeight, TabDisplayMode displayMode) {
        pendingScreenRegistrations.add(new ScreenRegistration(screen, screenWidth, screenHeight, displayMode));
    }

    public static void finalizePendingRegistrations() {
        for (ScreenRegistration registration : pendingScreenRegistrations) {
            if (!tabsScreens.containsKey(registration.screenClass)) {
                ScreenInfo screenInfo = new ScreenInfo(registration.screenWidth, registration.screenHeight, registration.displayMode);
                tabsScreens.put(registration.screenClass, screenInfo);
            }

            ScreenInfo screenInfo = tabsScreens.get(registration.screenClass);
            for (TabBase tab : allRegisteredTabs) {
                screenInfo.addTab(10, tab);
            }
        }
        pendingScreenRegistrations.clear();
    }

    public static void register(TabBase tabBase) {
        allRegisteredTabs.add(tabBase);
        tabBase.initTabOnScreens();
    }

    public static boolean hasTabsForScreen(Class<? extends Screen> screenClass) {
        return tabsScreens.containsKey(screenClass);
    }

    public static void markScreenOpenedViaTab(Screen sourceScreen) {
        TabsMenu.screenOpenedViaTab = true;
        TabsMenu.sourceScreen = sourceScreen;
        TabsMenu.preservedStartTabIndex = startTabIndex;
    }

    public static boolean wasScreenOpenedViaTab() {
        return screenOpenedViaTab;
    }

    public static Screen getSourceScreen() {
        return sourceScreen;
    }

    public static void clearTabScreenTracking() {
        screenOpenedViaTab = false;
        sourceScreen = null;
        preservedStartTabIndex = 0;
    }

    public static boolean hasCustomPositioning(Screen screen) {
        return false; // Simplified for Fabric
    }

    public static void updateButtonsPosition(Screen screen, int leftScreenPos, int topScreenPos) {
        if (TabsMenu.leftScreenPos != leftScreenPos || TabsMenu.topScreenPos != topScreenPos) {
            TabsMenu.leftScreenPos = leftScreenPos;
            TabsMenu.topScreenPos = topScreenPos;
            for (var button: screen.children()) {
                if (button instanceof TabButton tabButton) {
                    tabButton.updatePosition(TabsMenu.leftScreenPos, TabsMenu.topScreenPos);
                }
                if (button instanceof NextTabsButton tabButton) {
                    tabButton.updatePosition(TabsMenu.leftScreenPos, TabsMenu.topScreenPos);
                }
            }
        }
    }

    public static void onMouseMove(int mouseX, int mouseY, Screen screen) {
        // Mouse move handling - simplified for now
    }

    /**
     * Initialize screen buttons (called from Fabric screen event)
     */
    public static void initScreenButtons(Screen screen) {
        ModTabs.LOGGER.info("initScreenButtons called for screen class: {}", screen.getClass().getName());
        ModTabs.LOGGER.info("Available registered screen classes: {}", tabsScreens.keySet().stream().map(Class::getSimpleName).toList());

        if (!tabsScreens.containsKey(screen.getClass())) {
            ModTabs.LOGGER.warn("Screen class {} not found in registered screens, returning early", screen.getClass().getSimpleName());
            return;
        }

        if (MinecraftClient.getInstance().player == null)
            return;

        ScreenInfo screenInfo = tabsScreens.get(screen.getClass());

        // Calculate tab position based on positioning mode (simplified for Fabric port)
        leftScreenPos = (screen.width - screenInfo.width.apply(MinecraftClient.getInstance().player)) / 2;
        topScreenPos = (screen.height - screenInfo.height.apply(MinecraftClient.getInstance().player)) / 2;

        // Simple bounds check
        if (topScreenPos - TAB_HEIGHT < 0) {
            return;
        }

        startTabIndex = screenOpenedViaTab ? preservedStartTabIndex : 0;
        currentTabsCount = 0;
        enabledTabs = new ArrayList<>();

        // Collect enabled tabs
        for (List<TabBase> tabBases: screenInfo.tabs.values()) {
            enabledTabs.addAll(tabBases.stream()
                    .filter(tabBase -> tabBase.isEnabled(MinecraftClient.getInstance().player))
                    .toList());
        }

        // Sort tabs by override order first, then alphabetically
        enabledTabs.sort((tab1, tab2) -> {
            int order1 = tab1.getOverrideOrder();
            int order2 = tab2.getOverrideOrder();

            if (order1 == 0 && order2 == 0) {
                return tab1.getClass().getSimpleName().compareTo(tab2.getClass().getSimpleName());
            }
            if (order1 == 0) return 1;
            if (order2 == 0) return -1;
            if (order1 == order2) {
                return tab1.getClass().getSimpleName().compareTo(tab2.getClass().getSimpleName());
            }
            return Integer.compare(order1, order2);
        });

        // Handle sticky inventory tab - separate inventory tab from other tabs
        TabBase inventoryTab = null;
        List<TabBase> nonInventoryTabs = new ArrayList<>();

        if (Config.Baked.stickyInventoryTab) {
            for (TabBase tab : enabledTabs) {
                if (tab instanceof InventoryTab) {
                    inventoryTab = tab;
                } else {
                    nonInventoryTabs.add(tab);
                }
            }
        } else {
            nonInventoryTabs = enabledTabs;
        }

        int remainingWidth;
        try {
            remainingWidth = screenInfo.width.apply(MinecraftClient.getInstance().player);
        } catch (Exception e) {
            ModTabs.LOGGER.error("Width calculation failed: " + e.getMessage());
            return;
        }

        // First pass: determine how many tabs can fit
        currentTabsCount = 0;
        int tempWidth = remainingWidth;

        // If sticky inventory tab is enabled and present, reserve space for it
        if (Config.Baked.stickyInventoryTab && inventoryTab != null) {
            if (tempWidth > TAB_WIDTH) {
                tempWidth -= TAB_WIDTH + 1;
                currentTabsCount++; // Count the inventory tab
            }
        }

        // Count remaining space for non-inventory tabs
        List<TabBase> tabsToCheck = Config.Baked.stickyInventoryTab ? nonInventoryTabs : enabledTabs;
        for (TabBase tabBase: tabsToCheck) {
            if (tempWidth > TAB_WIDTH) {
                tempWidth -= TAB_WIDTH + 1;
                currentTabsCount++;
            } else {
                break;
            }
        }

        // Clear existing tab buttons to prevent duplicates
        screen.children().removeIf(child -> child instanceof TabButton || child instanceof NextTabsButton);

        try {
            java.lang.reflect.Field drawablesField = Screen.class.getDeclaredField("drawables");
            drawablesField.setAccessible(true);
            Object drawables = drawablesField.get(screen);
            if (drawables instanceof java.util.List) {
                ((java.util.List<?>) drawables).removeIf(drawable ->
                    drawable instanceof TabButton || drawable instanceof NextTabsButton);
            }
        } catch (Exception e) {
            // Ignore reflection errors
        }

        // Second pass: create buttons for the correct range starting from startTabIndex
        int buttonPosition = 0;

        // If sticky inventory tab is enabled and present, always render it first
        if (Config.Baked.stickyInventoryTab && inventoryTab != null && currentTabsCount > 0) {
            TabButton inventoryButton = new TabButton(inventoryTab, MinecraftClient.getInstance().player, screen, buttonPosition, leftScreenPos, topScreenPos, screenInfo.displayMode);
            addButtonToScreen(screen, inventoryButton);
            buttonPosition++;
        }

        // Render other tabs based on pagination
        List<TabBase> tabsToRender = Config.Baked.stickyInventoryTab ? nonInventoryTabs : enabledTabs;
        int availableSlots = Config.Baked.stickyInventoryTab && inventoryTab != null ? currentTabsCount - 1 : currentTabsCount;

        for (int tabIndex = 0; tabIndex < tabsToRender.size() && buttonPosition < currentTabsCount; tabIndex++) {
            TabBase tabBase = tabsToRender.get(tabIndex);
            int tabIndexToShow = tabIndex - startTabIndex;

            if (tabIndexToShow >= 0 && tabIndexToShow < availableSlots) {
                TabButton newButton = new TabButton(tabBase, MinecraftClient.getInstance().player, screen, buttonPosition, leftScreenPos, topScreenPos, screenInfo.displayMode);
                addButtonToScreen(screen, newButton);
                buttonPosition++;
            }
        }

        // Determine if next button is needed based on sticky inventory tab mode
        int totalTabsToPage = Config.Baked.stickyInventoryTab ? nonInventoryTabs.size() : enabledTabs.size();
        int maxVisibleTabs = Config.Baked.stickyInventoryTab && inventoryTab != null ? currentTabsCount - 1 : currentTabsCount;

        if (totalTabsToPage > maxVisibleTabs) {
            NextTabsButton nextButton = new NextTabsButton(currentTabsCount, leftScreenPos, topScreenPos, screenInfo.displayMode,
                    button -> nextTabButtons(screen));
            addButtonToScreen(screen, nextButton);
        }
    }

    /**
     * Add a button to screen using Fabric's approach
     */
    private static void addButtonToScreen(Screen screen, net.minecraft.client.gui.Element button) {
        try {
            // Use reflection to access the protected addDrawableChild method
            java.lang.reflect.Method addDrawableChildMethod =
                Screen.class.getDeclaredMethod("addDrawableChild", net.minecraft.client.gui.Element.class);
            addDrawableChildMethod.setAccessible(true);
            addDrawableChildMethod.invoke(screen, button);
        } catch (Exception e) {
            // Fallback: manually add to both children and drawables lists
            try {
                @SuppressWarnings("unchecked")
                java.util.List<net.minecraft.client.gui.Element> children =
                    (java.util.List<net.minecraft.client.gui.Element>) screen.children();
                children.add(button);

                // Also add to drawables list
                java.lang.reflect.Field drawablesField = Screen.class.getDeclaredField("drawables");
                drawablesField.setAccessible(true);
                Object drawables = drawablesField.get(screen);
                if (drawables instanceof java.util.List && button instanceof net.minecraft.client.gui.Drawable) {
                    @SuppressWarnings("unchecked")
                    java.util.List<net.minecraft.client.gui.Drawable> drawablesList = (java.util.List<net.minecraft.client.gui.Drawable>) drawables;
                    drawablesList.add((net.minecraft.client.gui.Drawable) button);
                }
            } catch (Exception e2) {
                ModTabs.LOGGER.warn("Failed to add tab button to screen: " + e2.getMessage());
            }
        }
    }

    public static void nextTabButtons(Screen screen) {
        // Handle sticky inventory tab logic
        if (Config.Baked.stickyInventoryTab) {
            // Find inventory and non-inventory tabs
            TabBase inventoryTab = null;
            List<TabBase> nonInventoryTabs = new ArrayList<>();

            for (TabBase tab : enabledTabs) {
                if (tab instanceof InventoryTab) {
                    inventoryTab = tab;
                } else {
                    nonInventoryTabs.add(tab);
                }
            }

            // Calculate pagination for non-inventory tabs only
            int availableSlots = inventoryTab != null ? currentTabsCount - 1 : currentTabsCount;

            if (startTabIndex + availableSlots >= nonInventoryTabs.size())
                startTabIndex = 0;
            else
                startTabIndex += availableSlots + Math.min(nonInventoryTabs.size() - availableSlots * 2 - startTabIndex, 0);

            // Update buttons: skip first button if inventory tab is sticky
            var tabButtons = screen.children().stream().filter(button -> button instanceof TabButton).toList();
            int buttonStartIndex = inventoryTab != null ? 1 : 0;
            int currentTabIndex = 0;

            for (TabBase tabBase: nonInventoryTabs) {
                int tabIndexToUpdate = currentTabIndex - startTabIndex;
                if (tabIndexToUpdate >= availableSlots)
                    break;

                if (tabIndexToUpdate >= 0 && buttonStartIndex + tabIndexToUpdate < tabButtons.size())
                    ((TabButton) tabButtons.get(buttonStartIndex + tabIndexToUpdate)).setTabBase(tabBase);

                currentTabIndex++;
            }
        } else {
            // Original logic for non-sticky mode
            if (startTabIndex + currentTabsCount >= enabledTabs.size())
                startTabIndex = 0;
            else
                startTabIndex += currentTabsCount + Math.min(enabledTabs.size() - currentTabsCount * 2 - startTabIndex, 0);

            var tabButtons = screen.children().stream().filter(button -> button instanceof TabButton).toList();
            int currentTabIndex = 0;
            for (TabBase tabBase: enabledTabs) {
                int tabIndexToUpdate = currentTabIndex - startTabIndex;
                if (tabIndexToUpdate >= currentTabsCount)
                    break;

                if (tabIndexToUpdate >= 0 && tabIndexToUpdate < tabButtons.size())
                    ((TabButton) tabButtons.get(tabIndexToUpdate)).setTabBase(tabBase);

                currentTabIndex++;
            }
        }
    }

    /**
     * Cycle to the next tab when the tab cycle keybind is pressed
     */
    public static void cycleToNextTab(Screen currentScreen) {
        if (currentScreen == null) {
            return;
        }

        if (!tabsScreens.containsKey(currentScreen.getClass())) {
            return; // No tabs registered for this screen
        }

        if (enabledTabs == null || enabledTabs.isEmpty()) {
            return; // No enabled tabs to cycle through
        }

        // Find the currently active tab (the one that matches the current screen)
        TabBase currentTab = null;
        int currentTabIndex = -1;

        for (int i = 0; i < enabledTabs.size(); i++) {
            TabBase tab = enabledTabs.get(i);
            if (tab.isCurrentlyUsed(currentScreen)) {
                currentTab = tab;
                currentTabIndex = i;
                break;
            }
        }

        // If no current tab was found, default to cycling from the first tab
        if (currentTab == null) {
            currentTabIndex = -1; // This will make next index 0
        }

        // Calculate the next tab index
        int nextTabIndex = (currentTabIndex + 1) % enabledTabs.size();
        TabBase nextTab = enabledTabs.get(nextTabIndex);

        // Calculate which page the next tab should be on and update startTabIndex
        if (Config.Baked.stickyInventoryTab) {
            // Handle sticky inventory tab pagination
            TabBase inventoryTab = null;
            List<TabBase> nonInventoryTabs = new ArrayList<>();

            for (TabBase tab : enabledTabs) {
                if (tab instanceof InventoryTab) {
                    inventoryTab = tab;
                } else {
                    nonInventoryTabs.add(tab);
                }
            }

            // If the next tab is the inventory tab, no pagination needed
            if (!(nextTab instanceof InventoryTab)) {
                // Find the index of the next tab in the non-inventory tabs list
                int nextNonInventoryIndex = nonInventoryTabs.indexOf(nextTab);
                if (nextNonInventoryIndex != -1) {
                    // Calculate available slots (minus inventory tab slot if present)
                    int availableSlots = inventoryTab != null ? currentTabsCount - 1 : currentTabsCount;

                    // Calculate which page this tab should be on
                    int targetPage = nextNonInventoryIndex / availableSlots;
                    startTabIndex = targetPage * availableSlots;
                }
            } else {
                // If cycling to inventory tab, reset to first page
                startTabIndex = 0;
            }
        } else {
            // Calculate which page the next tab should be on for non-sticky mode
            int targetPage = nextTabIndex / currentTabsCount;
            startTabIndex = targetPage * currentTabsCount;
        }

        // Mark screen opened via tab to preserve the updated pagination
        markScreenOpenedViaTab(currentScreen);

        // Open the next tab
        nextTab.openTargetScreen(MinecraftClient.getInstance().player);
    }

    public static class ScreenInfo {
        public Function<PlayerEntity, Integer> width;
        public Function<PlayerEntity, Integer> height;
        public Map<Integer, List<TabBase>> tabs;
        public TabDisplayMode displayMode;

        public ScreenInfo(Function<PlayerEntity, Integer> width, Function<PlayerEntity, Integer> height, TabBase newTab, int priority) {
            this.width = width;
            this.height = height;
            this.tabs = new TreeMap<>();
            this.displayMode = TabDisplayMode.NORMAL;
            this.addTab(priority, newTab);
        }

        public ScreenInfo(Function<PlayerEntity, Integer> width, Function<PlayerEntity, Integer> height) {
            this.width = width;
            this.height = height;
            this.tabs = new TreeMap<>();
            this.displayMode = TabDisplayMode.NORMAL;
        }

        public ScreenInfo(Function<PlayerEntity, Integer> width, Function<PlayerEntity, Integer> height, TabDisplayMode displayMode) {
            this.width = width;
            this.height = height;
            this.tabs = new TreeMap<>();
            this.displayMode = displayMode;
        }

        public void addTab(int priority, TabBase newTab) {
            if (this.tabs.containsKey(priority)) {
                List<TabBase> existingTabs = this.tabs.get(priority);
                // Check for duplicate tabs of the same class
                boolean alreadyExists = existingTabs.stream()
                    .anyMatch(tab -> tab.getClass().equals(newTab.getClass()));
                if (!alreadyExists) {
                    existingTabs.add(newTab);
                }
            } else {
                ArrayList<TabBase> newTabsForPriority = new ArrayList<>();
                newTabsForPriority.add(newTab);
                this.tabs.put(priority, newTabsForPriority);
            }
        }
    }

    private static class ScreenRegistration {
        public final Class<? extends Screen> screenClass;
        public final Function<PlayerEntity, Integer> screenWidth;
        public final Function<PlayerEntity, Integer> screenHeight;
        public final TabDisplayMode displayMode;

        public ScreenRegistration(Class<? extends Screen> screenClass, Function<PlayerEntity, Integer> screenWidth, Function<PlayerEntity, Integer> screenHeight) {
            this.screenClass = screenClass;
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
            this.displayMode = TabDisplayMode.NORMAL;
        }

        public ScreenRegistration(Class<? extends Screen> screenClass, Function<PlayerEntity, Integer> screenWidth, Function<PlayerEntity, Integer> screenHeight, TabDisplayMode displayMode) {
            this.screenClass = screenClass;
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
            this.displayMode = displayMode;
        }
    }

    /**
     * Get the animated Y offset for tuck mode (placeholder for now)
     */
    public static int getAnimatedYOffset() {
        // TODO: Implement actual tuck mode animation logic
        return 0;
    }

    /**
     * Add a pending registration for delayed tab registration
     */
    public static void addPendingRegistration(Runnable registration) {
        // TODO: Implement pending registration system
        registration.run(); // For now, just run immediately
    }

    /**
     * Register a screen for tabs
     */
    public static void registerScreenForTabs(Class<? extends Screen> screenClass, TabBase tab) {
        ModTabs.LOGGER.info("Registering tab {} for screen {}", tab.getClass().getSimpleName(), screenClass.getSimpleName());

        // Create default screen info if not exists
        if (!tabsScreens.containsKey(screenClass)) {
            ScreenInfo screenInfo = new ScreenInfo(
                (player) -> 176, // STANDARD_WIDTH
                (player) -> 166, // STANDARD_HEIGHT
                TabDisplayMode.NORMAL
            );
            tabsScreens.put(screenClass, screenInfo);
            ModTabs.LOGGER.info("Created default screen registration for {}", screenClass.getSimpleName());
        }

        // Add tab to screen with default priority 0
        ScreenInfo screenInfo = tabsScreens.get(screenClass);
        screenInfo.addTab(0, tab);

        ModTabs.LOGGER.info("Successfully registered tab {} for screen {}, total priority levels: {}",
            tab.getClass().getSimpleName(),
            screenClass.getSimpleName(),
            screenInfo.tabs.size());
    }
}