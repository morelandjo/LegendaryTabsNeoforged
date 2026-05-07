package vodmordia.modtabs.client.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;
import vodmordia.modtabs.client.screens.LayoutEditorButtons;
import vodmordia.modtabs.client.screens.NextTabsButton;
import vodmordia.modtabs.client.screens.TabButton;
import vodmordia.modtabs.client.keybinds.ModKeybinds;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

@Mod.EventBusSubscriber(modid = ModTabs.MOD_ID, value = Dist.CLIENT)
public class ClientNeoForgeEvents {

    @SubscribeEvent
    public static void preRenderScreen(ScreenEvent.Render.Pre event) {
        event.getScreen();
        Screen screen = event.getScreen();

        // Update mouse position for tuck mode hover detection
        TabsMenu.onMouseMove(event.getMouseX(), event.getMouseY(), screen);

        if (screen instanceof AbstractContainerScreen<?> containerScreen) {
            if (!TabsMenu.hasCustomPositioning(screen)) {
                TabsMenu.updateButtonsPosition(screen, containerScreen.getGuiLeft(), containerScreen.getGuiTop());
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMousePressedPre(ScreenEvent.MouseButtonPressed.Pre event) {
        Screen screen = event.getScreen();
        if (!TabsMenu.isEditing(screen)) return;

        double mx = event.getMouseX();
        double my = event.getMouseY();

        if (TabsMenu.isGlobalSettingsOpen()) {
            if (event.getButton() == 0) {
                TabsMenu.handleGlobalSettingsMouseDown(screen, mx, my);
            }
            event.setCanceled(true);
            return;
        }
        if (event.getButton() == 0 && TabsMenu.isMouseOnPanelHandle(screen, mx, my)) {
            TabsMenu.togglePanelCollapsed();
            event.setCanceled(true);
            return;
        }
        if (isClickOnEditorWidget(screen, mx, my)) {
            return;
        }
        if (event.getButton() == 0) {
            TabsMenu.onMousePressed(screen, mx, my);
        }
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseDragged(ScreenEvent.MouseDragged.Pre event) {
        Screen screen = event.getScreen();
        if (!TabsMenu.isEditing(screen)) return;
        if (TabsMenu.isGlobalSettingsOpen()) {
            if (event.getMouseButton() == 0) {
                TabsMenu.handleGlobalSettingsMouseDrag(screen, event.getMouseX(), event.getMouseY());
            }
            event.setCanceled(true);
            return;
        }
        if (event.getMouseButton() == 0) {
            TabsMenu.onMouseDragged(screen, event.getMouseX(), event.getMouseY());
        }
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseReleasedEditor(ScreenEvent.MouseButtonReleased.Pre event) {
        Screen screen = event.getScreen();
        if (!TabsMenu.isEditing(screen)) return;
        if (TabsMenu.isGlobalSettingsOpen()) {
            TabsMenu.handleGlobalSettingsMouseUp(screen, event.getMouseX(), event.getMouseY());
            event.setCanceled(true);
            return;
        }
        TabsMenu.onMouseReleased(screen);
        if (isClickOnEditorWidget(screen, event.getMouseX(), event.getMouseY())) {
            return;
        }
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCharTyped(ScreenEvent.CharacterTyped.Pre event) {
        if (!TabsMenu.isEditing(event.getScreen())) return;
        if (TabsMenu.isGlobalSettingsOpen()
                && TabsMenu.handleGlobalSettingsCharTyped(event.getCodePoint())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseScrolled(ScreenEvent.MouseScrolled.Pre event) {
        if (!TabsMenu.isEditing(event.getScreen())) return;
        if (TabsMenu.isGlobalSettingsOpen()) {
            TabsMenu.handleGlobalSettingsMouseScroll(event.getScreen(),
                    event.getMouseX(), event.getMouseY(), event.getScrollDelta());
        }
        event.setCanceled(true);
    }

    private static boolean isClickOnEditorWidget(Screen screen, double mx, double my) {
        for (var child : screen.children()) {
            if (child instanceof LayoutEditorButtons.EditOnly btn && btn.isMouseOver(mx, my)) {
                return true;
            }
            if (child instanceof LayoutEditorButtons.CustomIconEditBox eb && eb.isMouseOver(mx, my)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasFocusedEditorEditBox(Screen screen) {
        for (var child : screen.children()) {
            if (child instanceof LayoutEditorButtons.CustomIconEditBox eb && eb.isFocused()) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void renderEditModeOverlay(ScreenEvent.Render.Post event) {
        if (TabsMenu.isEditing(event.getScreen())) {
            TabsMenu.renderEditModeOverlay(event.getGuiGraphics(), event.getScreen(), event.getMouseX(), event.getMouseY());
        }
    }

    @SubscribeEvent
    public static void screenInitPost(ScreenEvent.Init.Post event) {
        event.getScreen();
        TabsMenu.initScreenButtons(event);

        // Hide L2 tabs after screen initialization
        if (ModIntegrationManager.isModLoaded(ModIntegration.L2_LIBRARY) ||
            ModIntegrationManager.isModLoaded(ModIntegration.L2_HOSTILITY) ||
            ModIntegrationManager.isModLoaded(ModIntegration.L2_ARTIFACTS) ||
            ModIntegrationManager.isModLoaded(ModIntegration.MODULAR_GOLEMS)) {
            // But don't hide tabs when we're in the Modular Golems tracker screens (they need sub-tabs)
            boolean isModularGolemsTrackerScreen = false;
            try {
                Class<?> golemInfoScreenClass = Class.forName("dev.xkmc.modulargolems.content.client.tracker.GolemInfoScreen");
                isModularGolemsTrackerScreen = golemInfoScreenClass.isInstance(event.getScreen());
            } catch (ClassNotFoundException e) {
                // Class not found, not a Modular Golems screen
            }

            if (!isModularGolemsTrackerScreen) {
                hideL2Tabs(event.getScreen());
            }
        }
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();

        // Layout editor: Esc cancels edit mode; all other keys are swallowed so
        // hotkeys (e.g. inventory keybind, slot number keys) can't fire — UNLESS the
        // custom-icon EditBox is focused, in which case backspace/arrows/delete/etc.
        // need to reach it for normal text-field editing.
        if (TabsMenu.isEditing(event.getScreen())) {
            if (event.getKeyCode() == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
                if (TabsMenu.isGlobalSettingsOpen()) {
                    TabsMenu.closeGlobalSettings(false);
                } else {
                    TabsMenu.exitEditMode();
                }
                event.setCanceled(true);
                return;
            }
            if (TabsMenu.isGlobalSettingsOpen()
                    && TabsMenu.handleGlobalSettingsKey(event.getKeyCode())) {
                event.setCanceled(true);
                return;
            }
            if (hasFocusedEditorEditBox(event.getScreen())) {
                return; // let vanilla deliver the key to the EditBox
            }
            event.setCanceled(true);
            return;
        }

        // Shift+Z to enter the layout editor on the current screen.
        if (event.getKeyCode() == org.lwjgl.glfw.GLFW.GLFW_KEY_Z && Screen.hasShiftDown()) {
            Screen currentScreen = event.getScreen();
            if (currentScreen != null && TabsMenu.hasTabsForScreen(currentScreen.getClass())
                    && !TabsMenu.isEditing(currentScreen)) {
                TabsMenu.enterEditMode(currentScreen);
                event.setCanceled(true);
                return;
            }
        }

        // Handle tab cycling keybind
        if (ModKeybinds.TAB_CYCLE.matches(event.getKeyCode(), event.getScanCode()) && Screen.hasShiftDown()) {
            Screen currentScreen = event.getScreen();
            if (currentScreen != null && TabsMenu.hasTabsForScreen(currentScreen.getClass())) {
                TabsMenu.cycleToNextTab(currentScreen);
                event.setCanceled(true);
                return;
            }
        }

        // Only handle inventory keybind if screen was opened via tab AND current screen has tab integration
        if (TabsMenu.wasScreenOpenedViaTab() && TabsMenu.hasTabsForScreen(event.getScreen().getClass())) {
            if (minecraft.options.keyInventory.matches(event.getKeyCode(), event.getScanCode())) {
                if (minecraft.player != null && minecraft.gameMode != null) {
                    // Before switching to inventory, properly close any open container (like backpack)
                    // This fixes the duplication issue when switching from backpack to inventory via keybind
                    try {
                        if (minecraft.player.containerMenu != null && !minecraft.player.containerMenu.getClass().getSimpleName().equals("InventoryMenu")) {
                            minecraft.player.closeContainer();
                        }
                    } catch (Exception e) {
                        // Silently ignore close container errors
                    }

                    // Open inventory and clear tab tracking so normal E key behavior works
                    TabsMenu.clearTabScreenTracking();
                    minecraft.setScreen(new InventoryScreen(minecraft.player));
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (!TabsMenu.wasScreenOpenedViaTab()) {
            TabsMenu.clearTabScreenTracking();
        }
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Pre event) {
        // Hide L2Library tabs when L2 mods are loaded and we're managing tabs
        if (ModIntegrationManager.isModLoaded(ModIntegration.L2_LIBRARY) ||
            ModIntegrationManager.isModLoaded(ModIntegration.L2_HOSTILITY) ||
            ModIntegrationManager.isModLoaded(ModIntegration.L2_ARTIFACTS) ||
            ModIntegrationManager.isModLoaded(ModIntegration.MODULAR_GOLEMS)) {
            Screen screen = event.getScreen();
            if (screen instanceof AbstractContainerScreen<?>) {
                // Cancel L2's tab rendering by removing their tab widgets
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
    }

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
                
                screen.renderables.removeIf(renderable -> {
                    String className = renderable.getClass().getName();
                    boolean isL2Tab = className.contains("l2tabs") ||
                                     className.contains("l2library") ||
                                     className.contains("l2hostility") ||
                                     className.contains("l2artifacts") ||
                                     className.contains("modulargolems");
                    boolean isTab = className.toLowerCase().contains("tab");
                    return isL2Tab && isTab;
                });
                
            } catch (Exception e) {
                
            }
        }
    }

    @SubscribeEvent
    public static void onScreenRenderPost(ScreenEvent.Render.Post event) {
        // Update mouse position for tuck mode hover detection (for screens that need special handling)
        TabsMenu.onMouseMove(event.getMouseX(), event.getMouseY(), event.getScreen());

        // Special handling for screens that don't call super.render() properly
        String screenClassName = event.getScreen().getClass().getName();

        if (screenClassName.equals("net.puffish.skillsmod.client.gui.SkillsScreen") ||
            screenClassName.equals("dev.ftb.mods.ftblibrary.ui.ScreenWrapper") ||
            screenClassName.equals("xaero.map.gui.GuiMap") ||
            screenClassName.equals("pepjebs.mapatlases.client.screen.AtlasOverviewScreen") ||
            screenClassName.equals("betteradvancements.common.gui.BetterAdvancementsScreen")) {

            // Find and render all TabButton and NextTabsButton widgets for this screen - this renders AFTER the screen content including blur
            for (var child : event.getScreen().children()) {
                if (child instanceof TabButton tabButton) {
                    // Render the tab button on top of everything the screen just rendered
                    tabButton.renderWidget(event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getPartialTick());
                }
                if (child instanceof NextTabsButton nextTabsButton) {
                    // Render the next tabs button on top of everything the screen just rendered
                    nextTabsButton.renderWidget(event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getPartialTick());
                }
            }
        }
    }

    /**
     * Screens whose own {@code mouseClicked} doesn't call {@code super.mouseClicked} —
     * the standard child-iteration path won't reach our TabButton, so the click /
     * release handlers below forward events directly.
     */
    private static boolean isScreenWithCustomClickRouting(String screenClassName) {
        return screenClassName.equals("net.puffish.skillsmod.client.gui.SkillsScreen")
            || screenClassName.equals("dev.ftb.mods.ftblibrary.ui.ScreenWrapper")
            || screenClassName.equals("xaero.map.gui.GuiMap")
            || screenClassName.equals("pepjebs.mapatlases.client.screen.AtlasOverviewScreen")
            || screenClassName.equals("betteradvancements.common.gui.BetterAdvancementsScreen");
    }

    @SubscribeEvent
    public static void onScreenMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!isScreenWithCustomClickRouting(event.getScreen().getClass().getName())) return;

        // Forward to mouseClicked (NOT onPress) so the long-press timer in TabButton.mouseClicked
        // gets armed. onPress would skip pressStartMs and the gesture would behave as an
        // instant click, breaking the long-press-to-edit gesture on these screens.
        for (var child : event.getScreen().children()) {
            if (child instanceof TabButton tabButton) {
                if (tabButton.isMouseOver(event.getMouseX(), event.getMouseY())) {
                    tabButton.mouseClicked(event.getMouseX(), event.getMouseY(), event.getButton());
                    event.setCanceled(true);
                    return;
                }
            }
            if (child instanceof NextTabsButton nextTabsButton) {
                if (nextTabsButton.isMouseOver(event.getMouseX(), event.getMouseY())) {
                    nextTabsButton.mouseClicked(event.getMouseX(), event.getMouseY(), event.getButton());
                    event.setCanceled(true);
                    return;
                }
            }
        }
    }

    /**
     * Companion to {@link #onScreenMouseClick}: forwards releases on these special screens
     * so {@code TabButton.mouseReleased} runs the short-click open-target logic and clears
     * {@code pressStartMs}. Without this, a quick click would never open the target screen
     * because the timer is set on press but never read on release.
     */
    @SubscribeEvent
    public static void onScreenMouseReleasedSpecial(ScreenEvent.MouseButtonReleased.Pre event) {
        Screen screen = event.getScreen();
        if (TabsMenu.isEditing(screen)) return; // edit-mode handler takes over
        if (!isScreenWithCustomClickRouting(screen.getClass().getName())) return;

        for (var child : screen.children()) {
            if (child instanceof TabButton tabButton) {
                tabButton.mouseReleased(event.getMouseX(), event.getMouseY(), event.getButton());
            }
            if (child instanceof NextTabsButton nextTabsButton) {
                nextTabsButton.mouseReleased(event.getMouseX(), event.getMouseY(), event.getButton());
            }
        }
    }
}
