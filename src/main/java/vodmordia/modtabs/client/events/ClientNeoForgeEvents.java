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
import vodmordia.modtabs.api.tabs_menu.GlobalSettingsPanel;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;
import vodmordia.modtabs.client.screens.LayoutEditorButtons;
import vodmordia.modtabs.client.screens.NextTabsButton;
import vodmordia.modtabs.client.screens.TabButton;
import vodmordia.modtabs.client.keybinds.ModKeybinds;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;
import vodmordia.modtabs.utils.ScreenClasses;

@Mod.EventBusSubscriber(modid = ModTabs.MOD_ID, value = Dist.CLIENT)
public class ClientNeoForgeEvents {

    @SubscribeEvent
    public static void preRenderScreen(ScreenEvent.Render.Pre event) {
        event.getScreen();
        Screen screen = event.getScreen();

        // Update mouse position for tuck mode hover detection
        TabsMenu.onMouseMove(event.getMouseX(), event.getMouseY(), screen);

        if (screen instanceof AbstractContainerScreen<?> containerScreen) {
            TabsMenu.updateButtonsPosition(screen, containerScreen.getGuiLeft(), containerScreen.getGuiTop());
        }

        // FTB Chunks LargeMapScreen reads `grabbed` directly each frame in drawBackground —
        // canceling our ScreenEvent presses isn't enough if the field gets set before we enter
        // edit mode (or by any path we don't intercept). Force it to 0 while editing so the
        // map can never pan under the editor panel.
        if (TabsMenu.isEditing(screen)) {
            neutralizeFtbMapDrag(screen);
        }
    }

    private static java.lang.reflect.Field ftbWrappedGuiField;
    private static java.lang.reflect.Field ftbLargeMapGrabbedField;
    private static boolean ftbReflectFailed;

    private static void neutralizeFtbMapDrag(Screen screen) {
        if (ftbReflectFailed) return;
        if (!"dev.ftb.mods.ftblibrary.ui.ScreenWrapper".equals(screen.getClass().getName())) return;
        try {
            if (ftbWrappedGuiField == null) {
                ftbWrappedGuiField = screen.getClass().getDeclaredField("wrappedGui");
                ftbWrappedGuiField.setAccessible(true);
            }
            Object wrapped = ftbWrappedGuiField.get(screen);
            if (wrapped == null) return;
            if (!"dev.ftb.mods.ftbchunks.client.gui.LargeMapScreen".equals(wrapped.getClass().getName())) return;
            if (ftbLargeMapGrabbedField == null) {
                ftbLargeMapGrabbedField = wrapped.getClass().getDeclaredField("grabbed");
                ftbLargeMapGrabbedField.setAccessible(true);
            }
            ftbLargeMapGrabbedField.setInt(wrapped, 0);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            ftbReflectFailed = true;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMousePressedPre(ScreenEvent.MouseButtonPressed.Pre event) {
        Screen screen = event.getScreen();
        if (!TabsMenu.isEditing(screen)) return;

        double mx = event.getMouseX();
        double my = event.getMouseY();

        if (GlobalSettingsPanel.isOpen()) {
            if (event.getButton() == 0) {
                GlobalSettingsPanel.handleMouseDown(screen, mx, my);
            }
            event.setCanceled(true);
            return;
        }
        // Custom-icon dropdown: dispatch popup item clicks here so they aren't consumed by
        // earlier-registered widgets (e.g. tabs under the popup). Also closes on outside click.
        if (LayoutEditorButtons.CustomIconDropdown.handleClickPre(screen, mx, my, event.getButton())) {
            event.setCanceled(true);
            return;
        }
        if (event.getButton() == 0 && TabsMenu.isMouseOnPanelHandle(screen, mx, my)) {
            TabsMenu.togglePanelCollapsed();
            event.setCanceled(true);
            return;
        }
        // Editor buttons / EditBox: dispatch the click manually and cancel. We can't rely on
        // vanilla's children iteration because FTB Library's ScreenWrapper.mouseClicked
        // forwards to wrappedGui FIRST — its map/quest panel always consumes, so super
        // (the children iteration that would reach our editor widgets) never runs.
        if (dispatchClickToEditorWidget(screen, mx, my, event.getButton())) {
            event.setCanceled(true);
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
        if (GlobalSettingsPanel.isOpen()) {
            if (event.getMouseButton() == 0) {
                GlobalSettingsPanel.handleMouseDrag(screen, event.getMouseX(), event.getMouseY());
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
        if (GlobalSettingsPanel.isOpen()) {
            GlobalSettingsPanel.handleMouseUp(screen, event.getMouseX(), event.getMouseY());
            event.setCanceled(true);
            return;
        }
        TabsMenu.onMouseReleased(screen);
        // Dispatch release manually for the same reason as mouseClicked above.
        dispatchReleaseToEditorWidget(screen, event.getMouseX(), event.getMouseY(), event.getButton());
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCharTyped(ScreenEvent.CharacterTyped.Pre event) {
        if (!TabsMenu.isEditing(event.getScreen())) return;
        if (GlobalSettingsPanel.isOpen()
                && GlobalSettingsPanel.handleCharTyped(event.getCodePoint())) {
            event.setCanceled(true);
            return;
        }
        // Deliver chars to a focused editor EditBox manually and cancel — same reason as
        // mouseClicked: FTB Library's ScreenWrapper.charTyped forwards to wrappedGui first,
        // so vanilla's children iteration never runs and our EditBoxes never see typed chars.
        // Cancellation also keeps Shift+Z from leaking 'Z' into the host screen's text fields
        // (e.g. Eccentric Tome's search bar).
        if (dispatchCharTypedToEditorEditBox(event.getScreen(), event.getCodePoint(), event.getModifiers())) {
            event.setCanceled(true);
            return;
        }
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseScrolled(ScreenEvent.MouseScrolled.Pre event) {
        if (!TabsMenu.isEditing(event.getScreen())) return;
        if (GlobalSettingsPanel.isOpen()) {
            GlobalSettingsPanel.handleMouseScroll(event.getScreen(),
                    event.getMouseX(), event.getMouseY(), event.getScrollDelta());
        }
        event.setCanceled(true);
    }

    private static boolean isEditorWidget(Object child) {
        return child instanceof LayoutEditorButtons.EditOnly
            || child instanceof LayoutEditorButtons.IconScaleEditBox
            || child instanceof LayoutEditorButtons.IconNudgeEditBox
            || child instanceof LayoutEditorButtons.MaxTabsPerPageEditBox;
    }

    private static boolean isEditorEditBox(Object child) {
        return child instanceof LayoutEditorButtons.IconScaleEditBox
            || child instanceof LayoutEditorButtons.IconNudgeEditBox
            || child instanceof LayoutEditorButtons.MaxTabsPerPageEditBox;
    }

    /**
     * Dispatch a click to the first editor widget at the cursor and update screen focus.
     * Returns true if a widget consumed the click.
     */
    private static boolean dispatchClickToEditorWidget(Screen screen, double mx, double my, int button) {
        // Snapshot children: dispatching a click can swap focus on a MaxTabsPerPageEditBox,
        // whose setFocused(false) override triggers reinitCurrentScreen() — that rebuilds
        // screen.children mid-iteration and throws ConcurrentModificationException if we
        // walked the live list directly.
        java.util.List<net.minecraft.client.gui.components.events.GuiEventListener> snapshot =
                new java.util.ArrayList<>(screen.children());
        net.minecraft.client.gui.components.events.GuiEventListener focused = null;
        boolean dispatched = false;
        for (var child : snapshot) {
            if (!isEditorWidget(child)) continue;
            if (child instanceof net.minecraft.client.gui.components.AbstractWidget w && w.isMouseOver(mx, my)) {
                if (w.mouseClicked(mx, my, button)) {
                    focused = w;
                    dispatched = true;
                    break;
                }
            }
        }
        // Defocus any other editor EditBox so only the clicked one (if any) keeps focus —
        // otherwise typing routes to whichever was focused last instead of what the user
        // just clicked. Use the same snapshot for the same CME-avoidance reason.
        for (var child : snapshot) {
            if (isEditorEditBox(child) && child != focused
                    && child instanceof net.minecraft.client.gui.components.EditBox eb) {
                eb.setFocused(false);
            }
        }
        if (focused != null) {
            screen.setFocused(focused);
        }
        return dispatched;
    }

    private static void dispatchReleaseToEditorWidget(Screen screen, double mx, double my, int button) {
        // Snapshot for the same reason as dispatchClickToEditorWidget.
        java.util.List<net.minecraft.client.gui.components.events.GuiEventListener> snapshot =
                new java.util.ArrayList<>(screen.children());
        for (var child : snapshot) {
            if (!isEditorWidget(child)) continue;
            if (child instanceof net.minecraft.client.gui.components.AbstractWidget w) {
                w.mouseReleased(mx, my, button);
            }
        }
    }

    private static boolean dispatchCharTypedToEditorEditBox(Screen screen, char codePoint, int modifiers) {
        for (var child : screen.children()) {
            if (!isEditorEditBox(child)) continue;
            if (child instanceof net.minecraft.client.gui.components.EditBox eb && eb.isFocused()) {
                return eb.charTyped(codePoint, modifiers);
            }
        }
        return false;
    }

    private static boolean dispatchKeyPressedToEditorEditBox(Screen screen, int keyCode, int scanCode, int modifiers) {
        for (var child : screen.children()) {
            if (!isEditorEditBox(child)) continue;
            if (child instanceof net.minecraft.client.gui.components.EditBox eb && eb.isFocused()) {
                return eb.keyPressed(keyCode, scanCode, modifiers);
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

        // Runic Skills' tab strip is suppressed via vodmordia.modtabs.mixin.DrawTabsMixin
        // (cancels DrawTabs.render at HEAD). The mixin is registered in modtabs.mixins.json
        // and is a no-op when Runic Skills isn't loaded.
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
                if (GlobalSettingsPanel.isOpen()) {
                    GlobalSettingsPanel.close(false);
                } else {
                    TabsMenu.exitEditMode();
                }
                event.setCanceled(true);
                return;
            }
            if (GlobalSettingsPanel.isOpen()
                    && GlobalSettingsPanel.handleKey(event.getKeyCode())) {
                event.setCanceled(true);
                return;
            }
            // EditBox-focused keystrokes (digits, backspace, delete, arrows) must reach the
            // EditBox rather than nudging the bar. Dispatch manually + cancel — see
            // dispatchClickToEditorWidget for the FTB ScreenWrapper rationale.
            if (dispatchKeyPressedToEditorEditBox(event.getScreen(), event.getKeyCode(),
                    event.getScanCode(), event.getModifiers())) {
                event.setCanceled(true);
                return;
            }
            int step = Screen.hasShiftDown() ? 8 : 1;
            switch (event.getKeyCode()) {
                case org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT  -> { TabsMenu.nudgeBar(-step, 0); event.setCanceled(true); return; }
                case org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT -> { TabsMenu.nudgeBar(+step, 0); event.setCanceled(true); return; }
                case org.lwjgl.glfw.GLFW.GLFW_KEY_UP    -> { TabsMenu.nudgeBar(0, -step); event.setCanceled(true); return; }
                case org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN  -> { TabsMenu.nudgeBar(0, +step); event.setCanceled(true); return; }
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
            screenClassName.equals("betteradvancements.common.gui.BetterAdvancementsScreen") ||
            screenClassName.equals(ScreenClasses.EPIC_FIGHT_SKILL_EDIT_SCREEN) ||
            screenClassName.equals(ScreenClasses.EPIC_SKILLS_SKILL_TREE_SCREEN) ||
            screenClassName.equals(ScreenClasses.MINE_AND_SLASH_TALENTS_SCREEN)) {

            // These screens don't iterate renderables in their render method, so we
            // manually render TabButton/NextTabsButton at Z=0. Editor widgets are
            // handled by renderEditModeOverlay's Z=400/Z=700 passes — rendering them
            // here at Z=0 would put them BEHIND the panel BG drawn at Z=600.
            for (var child : event.getScreen().children()) {
                if (child instanceof TabButton tabButton) {
                    tabButton.renderWidget(event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getPartialTick());
                }
                if (child instanceof NextTabsButton nextTabsButton) {
                    nextTabsButton.renderWidget(event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getPartialTick());
                }
            }
        }
    }

    /**
     * Give our tab buttons first dibs on every screen, not just the custom-routing ones.
     * Without this, host screens with many widgets (e.g. MOTP's PlayerStatsGUIScreen has 13
     * ImageButtons) iterate their own buttons first; if any has raw clicked() bounds that
     * overlap our tab area (common at small scale, since vanilla uses unscaled width=32px),
     * the wrong target screen opens. Forwarding only when isMouseOver matches our scaled
     * bounds preserves slot interaction on AbstractContainerScreens — we don't cancel
     * unless the cursor is actually on a tab.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onScreenMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {
        if (TabsMenu.isEditing(event.getScreen())) return; // edit-mode handler runs at HIGHEST
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
     * Companion to {@link #onScreenMouseClick}: dispatches releases to whichever tab button
     * has {@code pressStartMs > 0} (set in {@code mouseClicked}). Runs at HIGH priority for
     * every screen so the short-click open-target path fires regardless of the host screen
     * doing custom mouse routing or eating the release through some focused widget.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onScreenMouseReleasedSpecial(ScreenEvent.MouseButtonReleased.Pre event) {
        Screen screen = event.getScreen();
        if (TabsMenu.isEditing(screen)) return; // edit-mode handler takes over
        // Only forward (and cancel) when a tab actually owns this release — i.e. it had a
        // pending press from mouseClicked. Without that gate we'd cancel every left-button
        // release on every screen with tabs, since AbstractWidget.mouseReleased returns true
        // for any left release regardless of state — that breaks slot drag-drop in the vanilla
        // inventory. NextTabsButton doesn't track press state (it fires onPress synchronously
        // from mouseClicked) so it never needs forwarding here.
        // Iterate a snapshot: tabButton.mouseReleased -> openTargetScreen -> Minecraft.setScreen,
        // which can synchronously mutate the live children list (mod mixins on Screen lifecycle)
        // and trigger ConcurrentModificationException on the next iterator step.
        boolean dispatched = false;
        for (var child : new java.util.ArrayList<>(screen.children())) {
            if (child instanceof TabButton tabButton && tabButton.hasPendingPress()) {
                tabButton.mouseReleased(event.getMouseX(), event.getMouseY(), event.getButton());
                dispatched = true;
                break;
            }
        }
        if (dispatched) event.setCanceled(true);
    }
}
