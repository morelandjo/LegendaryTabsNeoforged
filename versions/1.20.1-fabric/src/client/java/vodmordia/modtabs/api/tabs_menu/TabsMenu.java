package vodmordia.modtabs.api.tabs_menu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerEntity;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.client.animation.TabBarAnimationManager;
import vodmordia.modtabs.client.screens.NextTabsButton;
import vodmordia.modtabs.client.screens.TabButton;
import vodmordia.modtabs.client.tabs_menu.InventoryTab;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.config.ModTabsConfig;
import vodmordia.modtabs.config.TabDisplayVisibility;

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

    // Animation and hover detection
    private static TabBarAnimationManager animationManager = null;
    private static final int HOVER_PADDING = 20; // Pixels of extra hover area around tabs
    private static boolean isInTuckMode = false;
    private static TabDisplayMode currentDisplayMode = TabDisplayMode.NORMAL;

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

    public static void registerScreenWithAllTabs(Class<? extends Screen> screen, Function<PlayerEntity, Integer> screenWidth, Function<PlayerEntity, Integer> screenHeight, TabDisplayMode displayMode, TabPositioning positioning) {
        pendingScreenRegistrations.add(new ScreenRegistration(screen, screenWidth, screenHeight, displayMode, positioning, null, null, 0));
    }

    public static void registerScreenWithAllTabs(Class<? extends Screen> screen, Function<PlayerEntity, Integer> screenWidth, Function<PlayerEntity, Integer> screenHeight, TabDisplayMode displayMode, TabPositioning positioning, int screenEdgeOffset) {
        pendingScreenRegistrations.add(new ScreenRegistration(screen, screenWidth, screenHeight, displayMode, positioning, null, null, screenEdgeOffset));
    }

    public static void forceRegisterScreenWithAllTabs(Class<? extends Screen> screen, Function<PlayerEntity, Integer> screenWidth, Function<PlayerEntity, Integer> screenHeight, TabDisplayMode displayMode, TabPositioning positioning) {
        // Force registration - remove any existing registration for this screen class first
        pendingScreenRegistrations.removeIf(reg -> reg.screenClass.equals(screen));
        // Then add our new registration
        pendingScreenRegistrations.add(new ScreenRegistration(screen, screenWidth, screenHeight, displayMode, positioning, null, null, 0));
    }

    public static void registerScreenWithCustomPosition(Class<? extends Screen> screen, Function<PlayerEntity, Integer> screenWidth, Function<PlayerEntity, Integer> screenHeight, Function<Screen, Integer> customTabX, Function<Screen, Integer> customTabY) {
        pendingScreenRegistrations.add(new ScreenRegistration(screen, screenWidth, screenHeight, TabDisplayMode.NORMAL, TabPositioning.CUSTOM, customTabX, customTabY, 0));
    }

    public static void registerScreenWithCustomPosition(Class<? extends Screen> screen, Function<PlayerEntity, Integer> screenWidth, Function<PlayerEntity, Integer> screenHeight, TabDisplayMode displayMode, Function<Screen, Integer> customTabX, Function<Screen, Integer> customTabY) {
        pendingScreenRegistrations.add(new ScreenRegistration(screen, screenWidth, screenHeight, displayMode, TabPositioning.CUSTOM, customTabX, customTabY, 0));
    }

    public static void finalizePendingRegistrations() {
        for (ScreenRegistration registration : pendingScreenRegistrations) {
            if (!tabsScreens.containsKey(registration.screenClass)) {
                ScreenInfo screenInfo = new ScreenInfo(registration.screenWidth, registration.screenHeight, registration.displayMode);
                screenInfo.positioning = registration.positioning;
                screenInfo.customTabX = registration.customTabX;
                screenInfo.customTabY = registration.customTabY;
                screenInfo.screenEdgeOffset = registration.screenEdgeOffset;
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

    private static TabDisplayVisibility getTabDisplayVisibilityForScreen(Screen screen) {
        String screenClassName = screen.getClass().getName();

        // Check each screen type and its corresponding display visibility setting
        if (screenClassName.contains("InventoryScreen")) {
            return ModTabsConfig.inventoryTabDisplayVisibility;
        } else if (screenClassName.contains("AdvancementsScreen")) {
            return ModTabsConfig.advancementsTabDisplayVisibility;
        } else if (screenClassName.contains("arsnouveau")) {
            return TabDisplayVisibility.YES; // Mod not available in Fabric
        } else if (screenClassName.contains("backpacked")) {
            return TabDisplayVisibility.YES; // Mod not available in Fabric
        } else if (screenClassName.contains("BodyHealthScreen")) {
            return TabDisplayVisibility.YES; // Mod not available in Fabric
        } else if (screenClassName.contains("cobblemon")) {
            return TabDisplayVisibility.YES; // Mod not available in Fabric
        } else if (screenClassName.contains("FoodStatsScreen") || screenClassName.contains("diet")) {
            return TabDisplayVisibility.YES; // Mod not available in Fabric
        } else if (screenClassName.contains("ftbquests")) {
            return ModTabsConfig.ftbQuestsTabDisplayVisibility;
        } else if (screenClassName.contains("ftbteams")) {
            return ModTabsConfig.ftbTeamsTabDisplayVisibility;
        } else if (screenClassName.contains("journeymap")) {
            return TabDisplayVisibility.YES; // Mod not available in Fabric
        } else if (screenClassName.contains("draconicevolution")) {
            return TabDisplayVisibility.YES; // Mod not available in Fabric
        } else if (screenClassName.contains("mapatlases")) {
            return ModTabsConfig.mapAtlasesTabDisplayVisibility;
        } else if (screenClassName.contains("xaero")) {
            return ModTabsConfig.xaerosMapTabDisplayVisibility;
        } else if (screenClassName.contains("puffish") || screenClassName.contains("skillsmod")) {
            return ModTabsConfig.pufferfishSkillsTabDisplayVisibility;
        } else if (screenClassName.contains("PassiveSkillScreen")) {
            return TabDisplayVisibility.YES; // Mod not available in Fabric
        } else if (screenClassName.contains("sophisticatedbackpacks")) {
            return ModTabsConfig.sophisticatedBackpacksTabDisplayVisibility;
        } else if (screenClassName.contains("travelersbackpack")) {
            return ModTabsConfig.travelersBackpackTabDisplayVisibility;
        }

        // For unknown screens, default to showing the tab bar
        return TabDisplayVisibility.YES;
    }

    public static void onMouseMove(int mouseX, int mouseY, Screen screen) {
        if (animationManager != null && isInTuckMode) {
            boolean inHoverZone = isMouseInHoverZone(mouseX, mouseY, screen);
            boolean wasInHoverState = animationManager.isInHoverState();

            if (inHoverZone && !wasInHoverState) {
                animationManager.onMouseEnterHoverZone();
            } else if (!inHoverZone && wasInHoverState) {
                animationManager.onMouseExitHoverZone();
            }
        }
    }

    private static boolean isMouseInHoverZone(int mouseX, int mouseY, Screen screen) {
        if (!tabsScreens.containsKey(screen.getClass())) {
            return false;
        }

        ScreenInfo screenInfo = tabsScreens.get(screen.getClass());

        // Calculate tab area bounds with padding
        int tabAreaLeft = leftScreenPos - HOVER_PADDING;
        int tabAreaRight = leftScreenPos + (currentTabsCount * (TAB_WIDTH + 1)) + HOVER_PADDING;

        int tabAreaTop, tabAreaBottom;
        if (screenInfo.displayMode == TabDisplayMode.INVERTED) {
            tabAreaTop = topScreenPos - HOVER_PADDING;
            tabAreaBottom = topScreenPos + TAB_HEIGHT + HOVER_PADDING;
        } else {
            tabAreaTop = topScreenPos - TAB_HEIGHT - HOVER_PADDING;
            tabAreaBottom = topScreenPos + HOVER_PADDING;
        }

        return mouseX >= tabAreaLeft && mouseX <= tabAreaRight &&
               mouseY >= tabAreaTop && mouseY <= tabAreaBottom;
    }

    public static int getAnimatedYOffset() {
        if (animationManager != null && isInTuckMode) {
            return animationManager.getYOffset(TAB_HEIGHT, currentDisplayMode);
        }
        return 0;
    }

    public static boolean hasCustomPositioning(Screen screen) {
        ScreenInfo screenInfo = tabsScreens.get(screen.getClass());
        if (screenInfo == null) {
            return false;
        }
        return screenInfo.positioning == TabPositioning.CUSTOM &&
               screenInfo.customTabX != null && screenInfo.customTabY != null;
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


    /**
     * Initialize screen buttons (called from Fabric screen event)
     */
    public static void initScreenButtons(Screen screen) {
        ModTabs.LOGGER.info("initScreenButtons called for screen: {}", screen.getClass().getSimpleName());

        if (!tabsScreens.containsKey(screen.getClass())) {
            // Screen doesn't have tabs registered, return silently
            ModTabs.LOGGER.info("Screen {} not registered for tabs, returning", screen.getClass().getSimpleName());
            return;
        }

        ModTabs.LOGGER.info("Screen {} is registered for tabs", screen.getClass().getSimpleName());

        if (MinecraftClient.getInstance().player == null) {
            ModTabs.LOGGER.warn("Player is null, cannot initialize tabs for screen: {}", screen.getClass().getSimpleName());
            return;
        }

        ScreenInfo screenInfo = tabsScreens.get(screen.getClass());

        // Store current display mode for animation
        currentDisplayMode = screenInfo.displayMode;

        // Calculate tab position based on positioning mode
        switch (screenInfo.positioning) {
            case GUI_RELATIVE:
                // Original behavior - position relative to GUI center
                int guiWidth = screenInfo.width.apply(MinecraftClient.getInstance().player);
                int guiHeight = screenInfo.height.apply(MinecraftClient.getInstance().player);
                leftScreenPos = (screen.width - guiWidth) / 2;
                topScreenPos = (screen.height - guiHeight) / 2;
                break;
            case SCREEN_TOP:
                // Position at top of screen with offset
                int guiWidthTop = screenInfo.width.apply(MinecraftClient.getInstance().player);
                int screenWidth = screen.width;
                leftScreenPos = (screenWidth - guiWidthTop) / 2;

                // For inverted tabs, position at absolute top (y=0), otherwise use offset
                topScreenPos = screenInfo.displayMode == TabDisplayMode.INVERTED ? 0 : screenInfo.screenEdgeOffset;
                break;
            case SCREEN_BOTTOM:
                // Position at bottom of screen with offset
                leftScreenPos = (screen.width - screenInfo.width.apply(MinecraftClient.getInstance().player)) / 2;
                // For NORMAL display mode, TabButton will subtract TAB_HEIGHT again, so we need to add it back
                // For INVERTED display mode, TabButton will use the position as-is
                if (screenInfo.displayMode == TabDisplayMode.NORMAL) {
                    topScreenPos = screen.height - screenInfo.screenEdgeOffset;
                } else {
                    topScreenPos = screen.height - TAB_HEIGHT - screenInfo.screenEdgeOffset;
                }
                break;
            case CUSTOM:
                // Custom positioning - use the provided functions
                leftScreenPos = screenInfo.customTabX != null ? screenInfo.customTabX.apply(screen) : 0;
                topScreenPos = screenInfo.customTabY != null ? screenInfo.customTabY.apply(screen) : 0;
                break;
        }

        // Simple bounds check - but skip for INVERTED tabs at SCREEN_TOP which are allowed to be at Y=0
        if (screenInfo.displayMode == TabDisplayMode.NORMAL && topScreenPos - TAB_HEIGHT < 0) {
            ModTabs.LOGGER.warn("Insufficient vertical space for tabs (topScreenPos={}, TAB_HEIGHT={}), returning early", topScreenPos, TAB_HEIGHT);
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
     * Add a button to screen by manually adding to lists using reflection
     * This works in both dev and production environments
     */
    private static void addButtonToScreen(Screen screen, net.minecraft.client.gui.Element button) {
        try {
            ModTabs.LOGGER.info("Adding button {} to screen {}", button.getClass().getSimpleName(), screen.getClass().getSimpleName());

            // Add to children list
            @SuppressWarnings("unchecked")
            java.util.List<net.minecraft.client.gui.Element> children =
                (java.util.List<net.minecraft.client.gui.Element>) screen.children();
            children.add(button);
            ModTabs.LOGGER.info("Added to children list, size now: {}", children.size());

            // Track which list we've added to for drawables, so we don't add to the same list for selectables
            Object foundDrawablesList = null;

            // Add to drawables list if button is drawable
            if (button instanceof net.minecraft.client.gui.Drawable drawable) {
                boolean foundDrawables = false;
                ModTabs.LOGGER.info("Button is Drawable, searching for drawables list...");
                // Find the drawables field - it's a List<Drawable>
                // Skip the children field since we already added to it
                for (java.lang.reflect.Field field : Screen.class.getDeclaredFields()) {
                    if (java.util.List.class.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        Object list = field.get(screen);
                        // Skip if this is the same list as children
                        if (list == children) {
                            ModTabs.LOGGER.info("Skipping field {} (same as children)", field.getName());
                            continue;
                        }
                        if (list instanceof java.util.List) {
                            try {
                                @SuppressWarnings("unchecked")
                                java.util.List<net.minecraft.client.gui.Drawable> drawablesList =
                                    (java.util.List<net.minecraft.client.gui.Drawable>) list;
                                // Try to add - if it succeeds, we found the drawables list
                                drawablesList.add(drawable);
                                ModTabs.LOGGER.info("Added to drawables list (field: {}), size now: {}", field.getName(), drawablesList.size());
                                foundDrawables = true;
                                foundDrawablesList = list; // Track this list so we can skip it for selectables
                                break;
                            } catch (ClassCastException e) {
                                // Not the right list, continue
                                ModTabs.LOGGER.info("Field {} not a Drawable list", field.getName());
                            }
                        }
                    }
                }
                if (!foundDrawables) {
                    ModTabs.LOGGER.warn("Could not find drawables list!");
                }
            }

            // Add to selectables list if button is selectable
            if (button instanceof net.minecraft.client.gui.Selectable selectable) {
                boolean foundSelectables = false;
                ModTabs.LOGGER.info("Button is Selectable, searching for selectables list...");
                // Find the selectables field - it's a List<Selectable>
                // Skip the children field and the drawables list we already added to
                for (java.lang.reflect.Field field : Screen.class.getDeclaredFields()) {
                    if (java.util.List.class.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        Object list = field.get(screen);
                        // Skip if this is the same list as children or drawables
                        if (list == children) {
                            ModTabs.LOGGER.info("Skipping field {} (same as children)", field.getName());
                            continue;
                        }
                        if (list == foundDrawablesList) {
                            ModTabs.LOGGER.info("Skipping field {} (same as drawables list)", field.getName());
                            continue;
                        }
                        if (list instanceof java.util.List) {
                            try {
                                @SuppressWarnings("unchecked")
                                java.util.List<net.minecraft.client.gui.Selectable> selectablesList =
                                    (java.util.List<net.minecraft.client.gui.Selectable>) list;
                                // Try to add - if it succeeds, we found the selectables list
                                selectablesList.add(selectable);
                                ModTabs.LOGGER.info("Added to selectables list (field: {}), size now: {}", field.getName(), selectablesList.size());
                                foundSelectables = true;
                                break;
                            } catch (ClassCastException e) {
                                // Not the right list, continue
                                ModTabs.LOGGER.info("Field {} not a Selectable list", field.getName());
                            }
                        }
                    }
                }
                if (!foundSelectables) {
                    ModTabs.LOGGER.warn("Could not find selectables list!");
                }
            }
        } catch (Exception e) {
            ModTabs.LOGGER.error("Failed to add tab button to screen: {}", e.getMessage(), e);
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
        public TabPositioning positioning;
        public Function<Screen, Integer> customTabX;
        public Function<Screen, Integer> customTabY;
        public int screenEdgeOffset;

        public ScreenInfo(Function<PlayerEntity, Integer> width, Function<PlayerEntity, Integer> height, TabBase newTab, int priority) {
            this.width = width;
            this.height = height;
            this.tabs = new TreeMap<>();
            this.displayMode = TabDisplayMode.NORMAL;
            this.positioning = TabPositioning.GUI_RELATIVE;
            this.screenEdgeOffset = 0;
            this.addTab(priority, newTab);
        }

        public ScreenInfo(Function<PlayerEntity, Integer> width, Function<PlayerEntity, Integer> height) {
            this.width = width;
            this.height = height;
            this.tabs = new TreeMap<>();
            this.displayMode = TabDisplayMode.NORMAL;
            this.positioning = TabPositioning.GUI_RELATIVE;
            this.screenEdgeOffset = 0;
        }

        public ScreenInfo(Function<PlayerEntity, Integer> width, Function<PlayerEntity, Integer> height, TabDisplayMode displayMode) {
            this.width = width;
            this.height = height;
            this.tabs = new TreeMap<>();
            this.displayMode = displayMode;
            this.positioning = TabPositioning.GUI_RELATIVE;
            this.screenEdgeOffset = 0;
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
        public final TabPositioning positioning;
        public final Function<Screen, Integer> customTabX;
        public final Function<Screen, Integer> customTabY;
        public final int screenEdgeOffset;

        public ScreenRegistration(Class<? extends Screen> screenClass, Function<PlayerEntity, Integer> screenWidth, Function<PlayerEntity, Integer> screenHeight) {
            this(screenClass, screenWidth, screenHeight, TabDisplayMode.NORMAL, TabPositioning.GUI_RELATIVE, null, null, 0);
        }

        public ScreenRegistration(Class<? extends Screen> screenClass, Function<PlayerEntity, Integer> screenWidth, Function<PlayerEntity, Integer> screenHeight, TabDisplayMode displayMode) {
            this(screenClass, screenWidth, screenHeight, displayMode, TabPositioning.GUI_RELATIVE, null, null, 0);
        }

        public ScreenRegistration(Class<? extends Screen> screenClass, Function<PlayerEntity, Integer> screenWidth, Function<PlayerEntity, Integer> screenHeight, TabDisplayMode displayMode, TabPositioning positioning, Function<Screen, Integer> customTabX, Function<Screen, Integer> customTabY, int screenEdgeOffset) {
            this.screenClass = screenClass;
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
            this.displayMode = displayMode;
            this.positioning = positioning;
            this.customTabX = customTabX;
            this.customTabY = customTabY;
            this.screenEdgeOffset = screenEdgeOffset;
        }
    }

    /**
     * Add a pending registration for delayed tab registration
     */
    public static void addPendingRegistration(Runnable registration) {
        // For backward compatibility - just run immediately
        registration.run();
    }

    /**
     * Register a screen for tabs
     */
    public static void registerScreenForTabs(Class<? extends Screen> screenClass, TabBase tab) {

        // Create default screen info if not exists
        if (!tabsScreens.containsKey(screenClass)) {
            ScreenInfo screenInfo = new ScreenInfo(
                (player) -> 176, // STANDARD_WIDTH
                (player) -> 166, // STANDARD_HEIGHT
                TabDisplayMode.NORMAL
            );
            tabsScreens.put(screenClass, screenInfo);
        }

        // Add tab to screen with default priority 0
        ScreenInfo screenInfo = tabsScreens.get(screenClass);
        screenInfo.addTab(0, tab);
    }
}