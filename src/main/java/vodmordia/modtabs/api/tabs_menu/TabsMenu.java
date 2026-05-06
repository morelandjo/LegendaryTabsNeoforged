package vodmordia.modtabs.api.tabs_menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.ScreenEvent;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.client.screens.NextTabsButton;
import vodmordia.modtabs.client.screens.TabButton;
import vodmordia.modtabs.client.tabs_menu.InventoryTab;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.config.ModTabsConfig;
import vodmordia.modtabs.config.TabDisplayVisibility;
import vodmordia.modtabs.client.animation.TabBarAnimationManager;
import vodmordia.modtabs.utils.ScreenClasses;

import java.util.*;
import java.util.function.Function;

import static vodmordia.modtabs.api.tabs_menu.TabBase.TAB_HEIGHT;
import static vodmordia.modtabs.api.tabs_menu.TabBase.TAB_WIDTH;
import static vodmordia.modtabs.api.tabs_menu.TabBase.TAB_HEIGHT_VERTICAL;
import static vodmordia.modtabs.api.tabs_menu.TabBase.TAB_WIDTH_VERTICAL;

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

    private TabsMenu() {
    }

    private static TabDisplayVisibility getTabDisplayVisibilityForScreen(Screen screen) {
        String screenClassName = screen.getClass().getName();

        // Check each screen type and its corresponding display visibility setting.
        // Class-name strings come from ScreenClasses so a mod rename only edits one constant.
        switch (screenClassName) {
            case ScreenClasses.VANILLA_INVENTORY:
                return ModTabsConfig.inventoryTabDisplayVisibility;
            case ScreenClasses.VANILLA_ADVANCEMENTS:
            case ScreenClasses.BETTER_ADVANCEMENTS:
                return ModTabsConfig.advancementsTabDisplayVisibility;
            case ScreenClasses.ARS_NOUVEAU_SPELLBOOK_GUI_LEGACY1:
            case ScreenClasses.ARS_NOUVEAU_SPELLBOOK_GUI_LEGACY2:
                return ModTabsConfig.arsNouveauTabDisplayVisibility;
            case ScreenClasses.BACKPACKED_FLYWHEEL_TRANSFORM:
            case ScreenClasses.BACKPACKED_SCREEN_ALT:
            case ScreenClasses.BACKPACKED_SCREEN:
                return ModTabsConfig.backpackedTabDisplayVisibility;
            case ScreenClasses.LSO_BODY_HEALTH:
                return ModTabsConfig.bodyDamageTabDisplayVisibility;
            case ScreenClasses.COBBLEMON_PARTY_LEGACY:
            case ScreenClasses.COBBLEMON_PARTY:
                return ModTabsConfig.cobblemonTabDisplayVisibility;
            case ScreenClasses.APPLESKIN_FOOD_STATS:
                return ModTabsConfig.dietTabDisplayVisibility;
            case ScreenClasses.FTB_LIBRARY_WRAPPER:
                // This could be FTB Quests or FTB Teams - use FTB Quests setting as default
                return ModTabsConfig.ftbQuestsTabDisplayVisibility;
            case ScreenClasses.JOURNEYMAP_FULLSCREEN:
                return ModTabsConfig.journeyMapTabDisplayVisibility;
            case ScreenClasses.SCGUNS_ATTACHMENT:
            case ScreenClasses.DRACONIC_EVOLUTION_GUI:
                return ModTabsConfig.draconicEvolutionTabDisplayVisibility;
            case ScreenClasses.MAP_ATLASES_ACCESS_UTILS:
                return ModTabsConfig.mapAtlasesTabDisplayVisibility;
            case ScreenClasses.XAEROS_MAP:
                return ModTabsConfig.xaerosMapTabDisplayVisibility;
            case ScreenClasses.PUFFERFISH_SKILLS_ALT1:
            case ScreenClasses.PUFFERFISH_SKILLS_ALT2:
                return ModTabsConfig.pufferfishSkillsTabDisplayVisibility;
            case ScreenClasses.SCGUNS_PASSIVE_SKILL:
                return ModTabsConfig.passiveSkillTreeTabDisplayVisibility;
            case ScreenClasses.BRASSWORKS_MISSIONS_UI:
                return ModTabsConfig.brassworksMissionsTabDisplayVisibility;
            case ScreenClasses.BIOLOGY_DICTIONARY_HOME_SCREEN:
            case ScreenClasses.BIOLOGY_DICTIONARY_ABOUT_SCREEN:
            case ScreenClasses.BIOLOGY_DICTIONARY_CONFIG_SCREEN:
            case ScreenClasses.BIOLOGY_DICTIONARY_ENTITY_OVERVIEW_SCREEN:
            case ScreenClasses.BIOLOGY_DICTIONARY_ENTITY_DETAIL_SCREEN:
                return ModTabsConfig.biologyDictionaryTabDisplayVisibility;
            case ScreenClasses.VANILLA_CONTAINER:
                // Check if it's a sophisticated backpack screen
                if (screenClassName.contains("sophisticatedbackpacks")) {
                    return ModTabsConfig.sophisticatedBackpacksTabDisplayVisibility;
                }
                // Check if it's a travelers backpack screen
                if (screenClassName.contains("travelersbackpack")) {
                    return ModTabsConfig.travelersBackpackTabDisplayVisibility;
                }
                break;
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

        if (screenInfo.positioning != null && screenInfo.positioning.isVertical()) {
            int tabAreaTop = topScreenPos - HOVER_PADDING;
            int tabAreaBottom = topScreenPos + (currentTabsCount * (TAB_HEIGHT_VERTICAL + 1)) + HOVER_PADDING;
            int tabAreaLeft = leftScreenPos - HOVER_PADDING;
            int tabAreaRight = leftScreenPos + TAB_WIDTH_VERTICAL + HOVER_PADDING;
            return mouseX >= tabAreaLeft && mouseX <= tabAreaRight &&
                   mouseY >= tabAreaTop && mouseY <= tabAreaBottom;
        }


        // Horizontal layout
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

    private static TabDisplayMode currentDisplayMode = TabDisplayMode.NORMAL;
    private static TabPositioning currentPositioning = TabPositioning.GUI_RELATIVE;

    public static int getAnimatedYOffset() {
        if (animationManager != null && isInTuckMode && !currentPositioning.isVertical()) {
            return animationManager.getYOffset(TAB_HEIGHT, currentDisplayMode);
        }
        return 0;
    }

    public static int getAnimatedXOffset() {
        if (animationManager != null && isInTuckMode && currentPositioning.isVertical()) {
            return animationManager.getXOffset(TAB_WIDTH_VERTICAL, currentPositioning);
        }
        return 0;
    }

    public static boolean isCurrentVertical() {
        return currentPositioning != null && currentPositioning.isVertical();
    }

    public static boolean hasCustomPositioning(Screen screen) {
        ScreenInfo screenInfo = tabsScreens.get(screen.getClass());
        if (screenInfo == null) {
            return false;
        }
        return screenInfo.positioning != TabPositioning.GUI_RELATIVE;
    }

    public static void updateButtonsPosition(Screen screen, int guiLeft, int guiTop) {
        // Translate the GUI's actual position into our tab anchor.
        // Horizontal GUI_RELATIVE: anchor matches the GUI's top-left.
        // Vertical GUI_RELATIVE_RIGHT: anchor sits at the GUI's right edge (guiLeft + guiWidth).
        int newLeft = guiLeft;
        int newTop = guiTop;
        if (currentPositioning == TabPositioning.GUI_RELATIVE_RIGHT) {
            ScreenInfo info = tabsScreens.get(screen.getClass());
            if (info != null) {
                int guiW = info.width.apply(Minecraft.getInstance().player);
                newLeft = guiLeft + guiW;
            }
        }

        if (TabsMenu.leftScreenPos != newLeft || TabsMenu.topScreenPos != newTop) {
            TabsMenu.leftScreenPos = newLeft;
            TabsMenu.topScreenPos = newTop;
            for (GuiEventListener button: screen.children()) {
                if (button instanceof TabButton tabButton) {
                    tabButton.updatePosition(TabsMenu.leftScreenPos, TabsMenu.topScreenPos);
                }
                if (button instanceof NextTabsButton tabButton) {
                    tabButton.updatePosition(TabsMenu.leftScreenPos, TabsMenu.topScreenPos);
                }
            }
        }
    }

    public static void addTabToScreen(TabBase newTab, Class<? extends Screen> screen, Function<Player, Integer> screenWidth, Function<Player, Integer> screenHeight, int priority) {
        if (tabsScreens.containsKey(screen)) {
            tabsScreens.get(screen).addTab(priority, newTab);
        } else {
            ScreenInfo screenInfo = new ScreenInfo(screenWidth, screenHeight, newTab, priority);
            tabsScreens.put(screen, screenInfo);
        }
    }

    public static void registerScreenWithAllTabs(Class<? extends Screen> screen, Function<Player, Integer> screenWidth, Function<Player, Integer> screenHeight) {
        // Store the registration for later processing
        pendingScreenRegistrations.add(new ScreenRegistration(screen, screenWidth, screenHeight));
    }

    public static void registerScreenWithAllTabs(Class<? extends Screen> screen, Function<Player, Integer> screenWidth, Function<Player, Integer> screenHeight, TabDisplayMode displayMode) {
        // Store the registration for later processing
        pendingScreenRegistrations.add(new ScreenRegistration(screen, screenWidth, screenHeight, displayMode));
    }

    public static void registerScreenWithAllTabs(Class<? extends Screen> screen, Function<Player, Integer> screenWidth, Function<Player, Integer> screenHeight, TabDisplayMode displayMode, TabPositioning positioning) {
        // Store the registration for later processing
        pendingScreenRegistrations.add(new ScreenRegistration(screen, screenWidth, screenHeight, displayMode, positioning, null, null, 0));
    }

    public static void registerScreenWithAllTabs(Class<? extends Screen> screen, Function<Player, Integer> screenWidth, Function<Player, Integer> screenHeight, TabDisplayMode displayMode, TabPositioning positioning, int screenEdgeOffset) {
        // Store the registration for later processing
        pendingScreenRegistrations.add(new ScreenRegistration(screen, screenWidth, screenHeight, displayMode, positioning, null, null, screenEdgeOffset));
    }

    public static void forceRegisterScreenWithAllTabs(Class<? extends Screen> screen, Function<Player, Integer> screenWidth, Function<Player, Integer> screenHeight, TabDisplayMode displayMode, TabPositioning positioning) {
        // Force registration - remove any existing registration for this screen class first
        pendingScreenRegistrations.removeIf(reg -> reg.screenClass.equals(screen));
        // Then add our new registration
        pendingScreenRegistrations.add(new ScreenRegistration(screen, screenWidth, screenHeight, displayMode, positioning, null, null, 0));
    }

    public static void registerScreenWithCustomPosition(Class<? extends Screen> screen, Function<Player, Integer> screenWidth, Function<Player, Integer> screenHeight, Function<Screen, Integer> customTabX, Function<Screen, Integer> customTabY) {
        // Store the registration for later processing
        pendingScreenRegistrations.add(new ScreenRegistration(screen, screenWidth, screenHeight, TabDisplayMode.NORMAL, TabPositioning.CUSTOM, customTabX, customTabY, 0));
    }

    public static void registerScreenWithCustomPosition(Class<? extends Screen> screen, Function<Player, Integer> screenWidth, Function<Player, Integer> screenHeight, TabDisplayMode displayMode, Function<Screen, Integer> customTabX, Function<Screen, Integer> customTabY) {
        // Store the registration for later processing
        pendingScreenRegistrations.add(new ScreenRegistration(screen, screenWidth, screenHeight, displayMode, TabPositioning.CUSTOM, customTabX, customTabY, 0));
    }

    public static void finalizePendingRegistrations() {
        // Process all pending screen registrations now that all tabs are registered
        for (ScreenRegistration registration : pendingScreenRegistrations) {
            if (!tabsScreens.containsKey(registration.screenClass)) {
                ScreenInfo screenInfo = new ScreenInfo(registration.screenWidth, registration.screenHeight, registration.displayMode);
                screenInfo.positioning = registration.positioning;
                screenInfo.customTabX = registration.customTabX;
                screenInfo.customTabY = registration.customTabY;
                screenInfo.screenEdgeOffset = registration.screenEdgeOffset;
                tabsScreens.put(registration.screenClass, screenInfo);
            }

            // Add all registered tabs to this screen with default priority 10
            ScreenInfo screenInfo = tabsScreens.get(registration.screenClass);
            for (TabBase tab : allRegisteredTabs) {
                screenInfo.addTab(10, tab);
            }
        }
        pendingScreenRegistrations.clear();
    }

    public static void initScreenButtons(ScreenEvent.Init.Post event) {

        if (tabsScreens.containsKey(event.getScreen().getClass())) {

            // Check tab display visibility for this screen type
            TabDisplayVisibility visibility = getTabDisplayVisibilityForScreen(event.getScreen());
            if (visibility == TabDisplayVisibility.NO) {
                return; // Don't display any tabs for this screen
            }

            // Initialize tuck mode if needed
            isInTuckMode = (visibility == TabDisplayVisibility.TUCK);
            if (isInTuckMode) {
                animationManager = new TabBarAnimationManager();
            } else {
                animationManager = null;
            }

            // Clear existing tab buttons to prevent duplicates
            var existingTabButtons = event.getScreen().children().stream()
                .filter(widget -> widget instanceof TabButton || widget instanceof NextTabsButton)
                .toList();
            existingTabButtons.forEach(event.getScreen().children()::remove);

            if (Minecraft.getInstance().player == null)
                return;

            ScreenInfo screenInfo = tabsScreens.get(event.getScreen().getClass());

            // Apply user placement override only to GUI_RELATIVE registrations.
            // Tabs hardcoded as SCREEN_TOP, SCREEN_BOTTOM, SCREEN_RIGHT, or CUSTOM
            // are left untouched.
            TabPositioning effectivePositioning = screenInfo.positioning;
            TabDisplayMode effectiveDisplayMode = screenInfo.displayMode;
            if (screenInfo.positioning == TabPositioning.GUI_RELATIVE && Config.Baked.standardTabPlacement != null) {
                switch (Config.Baked.standardTabPlacement) {
                    case TOP:
                        effectiveDisplayMode = TabDisplayMode.NORMAL;
                        break;
                    case BOTTOM:
                        effectiveDisplayMode = TabDisplayMode.INVERTED;
                        break;
                    case RIGHT:
                        effectivePositioning = TabPositioning.GUI_RELATIVE_RIGHT;
                        effectiveDisplayMode = TabDisplayMode.NORMAL;
                        break;
                }
            }
            // Store current display mode and positioning for animation/render
            currentDisplayMode = effectiveDisplayMode;
            currentPositioning = effectivePositioning;

            // Calculate tab position based on positioning mode
            switch (effectivePositioning) {
                case GUI_RELATIVE:
                    // Original behavior - position relative to GUI center
                    TabsMenu.leftScreenPos = (event.getScreen().width - screenInfo.width.apply(Minecraft.getInstance().player)) / 2;
                    TabsMenu.topScreenPos = (event.getScreen().height - screenInfo.height.apply(Minecraft.getInstance().player)) / 2;
                    break;
                case SCREEN_TOP:
                    // Position at top of screen with offset
                    int guiWidth = screenInfo.width.apply(Minecraft.getInstance().player);
                    int screenWidth = event.getScreen().width;
                    TabsMenu.leftScreenPos = (screenWidth - guiWidth) / 2;

                    // For inverted tabs, position at absolute top (y=0), otherwise use offset
                    TabsMenu.topScreenPos = effectiveDisplayMode == TabDisplayMode.INVERTED ? 0 : screenInfo.screenEdgeOffset;
                    break;
                case SCREEN_BOTTOM:
                    // Position at bottom of screen with offset
                    TabsMenu.leftScreenPos = (event.getScreen().width - screenInfo.width.apply(Minecraft.getInstance().player)) / 2;
                    // For NORMAL display mode, TabButton will subtract TAB_HEIGHT again, so we need to add it back
                    // For INVERTED display mode, TabButton will use the position as-is
                    if (effectiveDisplayMode == TabDisplayMode.NORMAL) {
                        TabsMenu.topScreenPos = event.getScreen().height - screenInfo.screenEdgeOffset;
                    } else {
                        TabsMenu.topScreenPos = event.getScreen().height - TAB_HEIGHT - screenInfo.screenEdgeOffset;
                    }
                    break;
                case SCREEN_RIGHT: {
                    int guiHeightR = screenInfo.height.apply(Minecraft.getInstance().player);
                    TabsMenu.leftScreenPos = event.getScreen().width - TAB_WIDTH_VERTICAL - screenInfo.screenEdgeOffset;
                    TabsMenu.topScreenPos = (event.getScreen().height - guiHeightR) / 2;
                    break;
                }
                case GUI_RELATIVE_RIGHT: {
                    int guiW = screenInfo.width.apply(Minecraft.getInstance().player);
                    int guiH = screenInfo.height.apply(Minecraft.getInstance().player);
                    int guiLeft = (event.getScreen().width - guiW) / 2;
                    TabsMenu.leftScreenPos = guiLeft + guiW;
                    TabsMenu.topScreenPos = (event.getScreen().height - guiH) / 2;
                    break;
                }
                case CUSTOM:
                    // Use custom position functions
                    if (screenInfo.customTabX != null && screenInfo.customTabY != null) {
                        TabsMenu.leftScreenPos = screenInfo.customTabX.apply(event.getScreen());
                        TabsMenu.topScreenPos = screenInfo.customTabY.apply(event.getScreen());
                    } else {
                        // Fallback to GUI_RELATIVE if custom functions not provided
                        TabsMenu.leftScreenPos = (event.getScreen().width - screenInfo.width.apply(Minecraft.getInstance().player)) / 2;
                        TabsMenu.topScreenPos = (event.getScreen().height - screenInfo.height.apply(Minecraft.getInstance().player)) / 2;
                    }
                    break;
            }

            // Only check bounds for GUI_RELATIVE positioning
            if (effectivePositioning == TabPositioning.GUI_RELATIVE && TabsMenu.topScreenPos - TAB_HEIGHT < 0) {
                ModTabs.LOGGER.warn("TabsMenu: EARLY RETURN - Tab position would be off-screen");
                return;
            }

            startTabIndex = screenOpenedViaTab ? preservedStartTabIndex : 0;
            // Don't clear tracking immediately - let the keybind handler do it after use
            currentTabsCount = 0;
            enabledTabs = new ArrayList<>();
            for (List<TabBase> tabBases: screenInfo.tabs.values()) {
                enabledTabs.addAll(tabBases.stream()
                        .filter(tabBase -> tabBase.isEnabled(Minecraft.getInstance().player))
                        .toList());
            }

            // Sort tabs by override order first, then alphabetically by class name
            enabledTabs.sort((tab1, tab2) -> {
                int order1 = tab1.getOverrideOrder();
                int order2 = tab2.getOverrideOrder();


                // If both have order 0 (no override), sort alphabetically
                if (order1 == 0 && order2 == 0) {
                    return tab1.getClass().getSimpleName().compareTo(tab2.getClass().getSimpleName());
                }

                // If one has order 0, it goes after the one with a set order
                if (order1 == 0) return 1;
                if (order2 == 0) return -1;

                // If both have the same non-zero order, sort alphabetically
                if (order1 == order2) {
                    return tab1.getClass().getSimpleName().compareTo(tab2.getClass().getSimpleName());
                }

                // Otherwise sort by order
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
                // No need to sort nonInventoryTabs again since enabledTabs is already sorted
            } else {
                nonInventoryTabs = enabledTabs;
            }


            boolean vertical = effectivePositioning != null && effectivePositioning.isVertical();
            int axisSize = vertical ? TAB_HEIGHT_VERTICAL : TAB_WIDTH;

            int remainingAxis;
            try {
                remainingAxis = vertical
                    ? screenInfo.height.apply(Minecraft.getInstance().player)
                    : screenInfo.width.apply(Minecraft.getInstance().player);
            } catch (Exception e) {
                ModTabs.LOGGER.error("TabsMenu: Axis size calculation failed: " + e.getMessage());
                return;
            }


            // First pass: determine how many tabs can fit
            currentTabsCount = 0;
            int tempAxis = remainingAxis;

            // If sticky inventory tab is enabled and present, reserve space for it
            if (Config.Baked.stickyInventoryTab && inventoryTab != null) {
                if (tempAxis > axisSize) {
                    tempAxis -= axisSize + 1;
                    currentTabsCount++; // Count the inventory tab
                }
            }

            // Count remaining space for non-inventory tabs
            List<TabBase> tabsToCheck = Config.Baked.stickyInventoryTab ? nonInventoryTabs : enabledTabs;
            for (TabBase tabBase: tabsToCheck) {
                if (tempAxis > axisSize) {
                    tempAxis -= axisSize + 1;
                    currentTabsCount++;
                } else {
                    break;
                }
            }


            // Clear existing tab buttons to prevent duplicates and conflicts
            int removedChildren = 0;

            // Count and log what we're removing
            for (var child : event.getScreen().children()) {
                if (child instanceof TabButton) {
                    TabButton tabButton = (TabButton) child;
                    removedChildren++;
                }
            }

            // Count renderables separately
            int removedRenderables = (int) event.getScreen().renderables.stream()
                .filter(renderable -> renderable instanceof TabButton)
                .count();

            event.getScreen().children().removeIf(child -> child instanceof TabButton);
            event.getScreen().renderables.removeIf(renderable -> renderable instanceof TabButton);


            // Second pass: create buttons for the correct range starting from startTabIndex
            int buttonPosition = 0;

            // If sticky inventory tab is enabled and present, always render it first
            if (Config.Baked.stickyInventoryTab && inventoryTab != null && currentTabsCount > 0) {
                TabButton inventoryButton = new TabButton(inventoryTab, Minecraft.getInstance().player, event.getScreen(), buttonPosition, TabsMenu.leftScreenPos, TabsMenu.topScreenPos, effectiveDisplayMode, effectivePositioning);
                event.addListener(inventoryButton);
                buttonPosition++;
            }

            // Render other tabs based on pagination
            List<TabBase> tabsToRender = Config.Baked.stickyInventoryTab ? nonInventoryTabs : enabledTabs;
            int availableSlots = Config.Baked.stickyInventoryTab && inventoryTab != null ? currentTabsCount - 1 : currentTabsCount;

            for (int tabIndex = 0; tabIndex < tabsToRender.size() && buttonPosition < currentTabsCount; tabIndex++) {
                TabBase tabBase = tabsToRender.get(tabIndex);
                int tabIndexToShow = tabIndex - startTabIndex;

                if (tabIndexToShow >= 0 && tabIndexToShow < availableSlots) {
                    TabButton newButton = new TabButton(tabBase, Minecraft.getInstance().player, event.getScreen(), buttonPosition, TabsMenu.leftScreenPos, TabsMenu.topScreenPos, effectiveDisplayMode, effectivePositioning);
                    event.addListener(newButton);
                    buttonPosition++;
                }
            }

            // Determine if next button is needed based on sticky inventory tab mode
            int totalTabsToPage = Config.Baked.stickyInventoryTab ? nonInventoryTabs.size() : enabledTabs.size();
            int maxVisibleTabs = Config.Baked.stickyInventoryTab && inventoryTab != null ? currentTabsCount - 1 : currentTabsCount;

            if (totalTabsToPage > maxVisibleTabs) {
                // Use the same positioning logic as TabButton - pass topScreenPos directly and let NextTabsButton handle display mode
                event.addListener(new NextTabsButton(currentTabsCount, TabsMenu.leftScreenPos, TabsMenu.topScreenPos, effectiveDisplayMode, effectivePositioning,
                        button -> nextTabButtons(event.getScreen())));
            }
        }
    }

    public static void nextTabButtons(Screen screen) {
        List<? extends GuiEventListener> tabButtons = screen.children().stream().filter(button -> button instanceof TabButton).toList();

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

            int currentTabIndex = 0;
            for (TabBase tabBase: enabledTabs) {
                int tabIndexToUpdate = currentTabIndex - startTabIndex;
                if (tabIndexToUpdate >= currentTabsCount)
                    break;

                if (tabIndexToUpdate >= 0)
                    ((TabButton) tabButtons.get(tabIndexToUpdate)).setTabBase(tabBase);

                currentTabIndex++;
            }
        }
    }

    public static void register(TabBase tabBase) {
        allRegisteredTabs.add(tabBase);
        tabBase.initTabOnScreens();
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

    /**
     * Check if a screen class has tabs registered for it
     */
    public static boolean hasTabsForScreen(Class<? extends Screen> screenClass) {
        return tabsScreens.containsKey(screenClass);
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
        nextTab.openTargetScreen(Minecraft.getInstance().player);
    }

    public static class ScreenInfo {
        public Function<Player, Integer> width;
        public Function<Player, Integer> height;
        public Map<Integer, List<TabBase>> tabs;
        public TabDisplayMode displayMode;
        public TabPositioning positioning;
        public Function<Screen, Integer> customTabX;
        public Function<Screen, Integer> customTabY;
        public int screenEdgeOffset;
        public ScreenInfo(Function<Player, Integer> width, Function<Player, Integer> height, TabBase newTab, int priority) {
            this.width = width;
            this.height = height;
            this.tabs = new TreeMap<>();
            this.displayMode = TabDisplayMode.NORMAL; // Default to normal
            this.positioning = TabPositioning.GUI_RELATIVE; // Default to GUI relative
            this.customTabX = null;
            this.customTabY = null;
            this.screenEdgeOffset = 0; // Default offset from screen edges
            this.addTab(priority, newTab);
        }

        public ScreenInfo(Function<Player, Integer> width, Function<Player, Integer> height) {
            this.width = width;
            this.height = height;
            this.tabs = new TreeMap<>();
            this.displayMode = TabDisplayMode.NORMAL; // Default to normal
            this.positioning = TabPositioning.GUI_RELATIVE; // Default to GUI relative
            this.customTabX = null;
            this.customTabY = null;
            this.screenEdgeOffset = 0; // Default offset from screen edges
        }

        public ScreenInfo(Function<Player, Integer> width, Function<Player, Integer> height, TabDisplayMode displayMode) {
            this.width = width;
            this.height = height;
            this.tabs = new TreeMap<>();
            this.displayMode = displayMode;
            this.positioning = TabPositioning.GUI_RELATIVE; // Default to GUI relative
            this.customTabX = null;
            this.customTabY = null;
            this.screenEdgeOffset = 0; // Default offset from screen edges
        }

        public void addTab(int priority, TabBase newTab) {
            if (this.tabs.containsKey(priority)) {
                List<TabBase> existingTabs = this.tabs.get(priority);
                // Dedup by instance, not by class — multiple CustomJsonTab instances
                // (one per JSON definition) all share the same class but are distinct tabs.
                // A class-based check would silently drop every custom tab after the first.
                if (!existingTabs.contains(newTab)) {
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
        public final Function<Player, Integer> screenWidth;
        public final Function<Player, Integer> screenHeight;
        public final TabDisplayMode displayMode;
        public final TabPositioning positioning;
        public final Function<Screen, Integer> customTabX;
        public final Function<Screen, Integer> customTabY;
        public final int screenEdgeOffset;

        public ScreenRegistration(Class<? extends Screen> screenClass, Function<Player, Integer> screenWidth, Function<Player, Integer> screenHeight) {
            this.screenClass = screenClass;
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
            this.displayMode = TabDisplayMode.NORMAL;
            this.positioning = TabPositioning.GUI_RELATIVE;
            this.customTabX = null;
            this.customTabY = null;
            this.screenEdgeOffset = 0;
        }

        public ScreenRegistration(Class<? extends Screen> screenClass, Function<Player, Integer> screenWidth, Function<Player, Integer> screenHeight, TabDisplayMode displayMode) {
            this.screenClass = screenClass;
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
            this.displayMode = displayMode;
            this.positioning = TabPositioning.GUI_RELATIVE;
            this.customTabX = null;
            this.customTabY = null;
            this.screenEdgeOffset = 0;
        }

        public ScreenRegistration(Class<? extends Screen> screenClass, Function<Player, Integer> screenWidth, Function<Player, Integer> screenHeight, TabDisplayMode displayMode, TabPositioning positioning, Function<Screen, Integer> customTabX, Function<Screen, Integer> customTabY, int screenEdgeOffset) {
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
}
