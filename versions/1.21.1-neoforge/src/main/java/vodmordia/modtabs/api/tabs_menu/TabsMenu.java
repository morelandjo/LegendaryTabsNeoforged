package vodmordia.modtabs.api.tabs_menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.ScreenEvent;
import vodmordia.modtabs.client.screens.NextTabsButton;
import vodmordia.modtabs.client.screens.TabButton;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.config.ModTabsConfig;
import vodmordia.modtabs.config.TabDisplayVisibility;
import vodmordia.modtabs.client.animation.TabBarAnimationManager;
import vodmordia.modtabs.layout.ScreenLayout;
import vodmordia.modtabs.layout.ScreenLayoutStore;
import vodmordia.modtabs.utils.ScreenClasses;

import java.util.*;
import java.util.function.Function;

import static vodmordia.modtabs.api.tabs_menu.TabBase.TAB_HEIGHT;
import static vodmordia.modtabs.api.tabs_menu.TabBase.TAB_WIDTH;

public class TabsMenu {
    private static final Map<Class<? extends Screen>, ScreenInfo> tabsScreens = new HashMap<>();
    private static final List<TabBase> allRegisteredTabs = new ArrayList<>();
    private static final List<ScreenRegistration> pendingScreenRegistrations = new ArrayList<>();
    private static final List<DynamicTabProvider> dynamicTabProviders = new ArrayList<>();
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
    private static boolean isInTuckMode = false;

    // Set to true while AbstractContainerScreenMixin is replaying our tab widgets BEFORE
    // the GUI panel draws, so they appear behind it. TabButton/NextTabsButton check this
    // (with edit-mode) to render once at the right pass and skip the other.
    public static boolean renderingBehindPanel = false;

    /**
     * Container screens that completely override {@code render} and never call
     * {@code super.render} (e.g. Sophisticated Backpacks' {@code StorageScreenBase}
     * replays its own version manually). Our {@code AbstractContainerScreen.render}
     * mixin never fires for these, so {@code renderingBehindPanel} stays false and
     * the behind-panel pass is lost. For screens in this set, tabs render in the
     * normal renderables iteration (on top of the GUI panel) instead.
     */
    private static final java.util.Set<String> RENDER_TABS_ON_TOP_FQNS = java.util.Set.of(
            "net.p3pp3rf1y.sophisticatedbackpacks.client.gui.BackpackScreen"
    );

    public static boolean rendersTabsOnTop(Screen screen) {
        return screen != null && RENDER_TABS_ON_TOP_FQNS.contains(screen.getClass().getName());
    }

    /**
     * Renders the screen's TabButtons / NextTabsButton manually, before the GUI panel
     * draws. Called from {@link vodmordia.modtabs.mixin.AbstractContainerScreenMixin}
     * between the dim and the panel image. Skipped while editing — edit mode wants tabs
     * on top so the user can grab handles.
     */
    public static void renderTabsBehindPanel(Screen screen, GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        if (isEditing(screen)) return;
        if (!tabsScreens.containsKey(screen.getClass())) return;
        renderingBehindPanel = true;
        try {
            for (var r : screen.renderables) {
                if (r instanceof TabButton || r instanceof NextTabsButton) {
                    r.render(gui, mouseX, mouseY, partialTick);
                }
            }
            // ItemRenderer (used for icons like Apothic Attributes' sword) submits to
            // GuiGraphics's deferred item buffer, which flushes only at the end of the
            // frame — by then the panel has already drawn, leaving the icon on top of
            // it. Flush now so the icons rasterize at this point in the render order.
            gui.flush();
        } finally {
            renderingBehindPanel = false;
        }
    }

    // Layout editor (Phase 1: drag, Phase 2: scale + spacing)
    private static Class<? extends Screen> editingScreenClass = null;
    private static int dragOffsetX = 0;
    private static int dragOffsetY = 0;
    private static float tempScale = 1.0f;
    private static int tempSpacingDelta = 0;
    private static float tempRotation = 0.0f;
    private static int tempNextOffsetX = 0;
    private static int tempNextOffsetY = 0;
    private static float tempNextRotation = 0.0f;
    private static int tempIconRotation = 0;
    private static int dragAnchorMouseX = 0;
    private static int dragAnchorMouseY = 0;
    private static int dragStartOffsetX = 0;
    private static int dragStartOffsetY = 0;
    private static float dragStartTempScale = 1.0f;
    private static int dragStartTempSpacingDelta = 0;
    private static float dragStartTempRotation = 0.0f;
    private static int dragStartTempNextOffsetX = 0;
    private static int dragStartTempNextOffsetY = 0;
    private static float dragStartTempNextRotation = 0.0f;
    private static double dragStartCenterDistance = 0;
    private static double dragStartCenterX = 0;
    private static double dragStartCenterY = 0;
    private static double dragStartAngleRad = 0;

    public enum DragMode { NONE, BAR, SCALE, SPACING_LOW, SPACING_HIGH, ROTATION, NEXT_TRANSLATE, NEXT_ROTATE }
    private static DragMode dragMode = DragMode.NONE;

    private TabsMenu() {
    }

    public static boolean isEditing(Screen screen) {
        return screen != null && editingScreenClass != null && editingScreenClass.equals(screen.getClass());
    }

    public static void enterEditMode(Screen screen) {
        // Modpack lock: when the modpack maker sets allowEditing=false in the config file,
        // every entry point into the layout editor (Shift+Z, tab long-press, the Edit button
        // in LayoutEditorButtons) becomes a no-op. Gating here once covers all three.
        if (!Config.Baked.allowEditing) return;
        editingScreenClass = screen.getClass();
        dragOffsetX = 0;
        dragOffsetY = 0;
        tempScale = 1.0f;
        tempSpacingDelta = 0;
        tempRotation = 0.0f;
        tempNextOffsetX = 0;
        tempNextOffsetY = 0;
        tempNextRotation = 0.0f;
        tempIconRotation = 0;
        dragMode = DragMode.NONE;
        panelCollapsed = true;
        GlobalSettingsPanel.close(false);
        setTabTooltipsSuppressed(screen, true);
    }

    public static void exitEditMode() {
        Screen current = Minecraft.getInstance().screen;
        if (current != null) {
            setTabTooltipsSuppressed(current, false);
        }
        vodmordia.modtabs.client.screens.LayoutEditorButtons.CustomIconDropdown.closeOpen();
        editingScreenClass = null;
        dragOffsetX = 0;
        dragOffsetY = 0;
        tempScale = 1.0f;
        tempSpacingDelta = 0;
        tempRotation = 0.0f;
        tempNextOffsetX = 0;
        tempNextOffsetY = 0;
        tempNextRotation = 0.0f;
        tempIconRotation = 0;
        dragMode = DragMode.NONE;
    }

    private static void setTabTooltipsSuppressed(Screen screen, boolean suppressed) {
        for (GuiEventListener child : screen.children()) {
            if (child instanceof TabButton tb) {
                tb.setTooltipSuppressed(suppressed);
            }
        }
    }

    public static List<TabBase> getEnabledTabs() {
        return enabledTabs;
    }

    public static int getDragOffsetX() {
        return dragOffsetX;
    }

    public static int getDragOffsetY() {
        return dragOffsetY;
    }

    /**
     * Effective scale for the current screen — saved scale × in-progress drag scale during edit.
     * Reads from the current screen's stored layout.
     */
    public static float currentEffectiveScale() {
        Screen s = Minecraft.getInstance().screen;
        if (s == null) return 1.0f;
        ScreenLayout layout = ScreenLayoutStore.get(s.getClass());
        return layout.scale * (isEditing(s) ? tempScale : 1.0f);
    }

    public static int currentNextOffsetX() {
        Screen s = Minecraft.getInstance().screen;
        if (s == null) return 0;
        ScreenLayout layout = ScreenLayoutStore.get(s.getClass());
        return layout.nextButtonOffsetX + (isEditing(s) ? tempNextOffsetX : 0);
    }

    public static int currentNextOffsetY() {
        Screen s = Minecraft.getInstance().screen;
        if (s == null) return 0;
        ScreenLayout layout = ScreenLayoutStore.get(s.getClass());
        return layout.nextButtonOffsetY + (isEditing(s) ? tempNextOffsetY : 0);
    }

    public static float currentNextEffectiveRotation() {
        Screen s = Minecraft.getInstance().screen;
        if (s == null) return 0f;
        ScreenLayout layout = ScreenLayoutStore.get(s.getClass());
        return layout.nextButtonRotation + (isEditing(s) ? tempNextRotation : 0f);
    }

    /** Effective icon rotation in degrees (0/90/180/270), saved + temp wrapped to 0-359. */
    public static int currentIconRotation() {
        if (previewRendering) return 0;
        Screen s = Minecraft.getInstance().screen;
        if (s == null) return 0;
        ScreenLayout layout = ScreenLayoutStore.get(s.getClass());
        int total = layout.iconRotation + (isEditing(s) ? tempIconRotation : 0);
        return ((total % 360) + 360) % 360;
    }

    /** While true, TabRenderer skips the background and forces horizontal orientation /
     *  zero icon rotation, so the panel preview shows just the icon at its natural pose. */
    public static boolean previewRendering = false;

    /** Invoked by the cycle button: bump tempIconRotation by 90° (cycles 0→90→180→270→0). */
    public static void cycleIconRotation() {
        tempIconRotation = (tempIconRotation + 90) % 360;
    }

    /** Reads the saved tuck direction for the currently-open screen. */
    public static vodmordia.modtabs.layout.TuckDirection currentTuckDirection() {
        Screen s = Minecraft.getInstance().screen;
        if (s == null) return vodmordia.modtabs.layout.TuckDirection.DOWN;
        ScreenLayout layout = ScreenLayoutStore.get(s.getClass());
        return layout.tuckDirection != null ? layout.tuckDirection : vodmordia.modtabs.layout.TuckDirection.DOWN;
    }

    /** Cycle button writes the new direction directly to the saved layout JSON. */
    public static void cycleTuckDirection() {
        Screen s = Minecraft.getInstance().screen;
        if (s == null) return;
        ScreenLayout layout = ScreenLayoutStore.get(s.getClass());
        if (layout.tuckDirection == null) layout.tuckDirection = vodmordia.modtabs.layout.TuckDirection.DOWN;
        layout.tuckDirection = layout.tuckDirection.next();
        ScreenLayoutStore.save(s.getClass().getName(), layout);
    }

    /** Per-page cap for the current screen's saved layout (0 = unlimited). */
    public static int currentMaxTabsPerPage() {
        Screen s = Minecraft.getInstance().screen;
        if (s == null) return 0;
        return ScreenLayoutStore.get(s.getClass()).maxTabsPerPage;
    }

    /**
     * Persists the per-page cap. Does NOT re-init the screen — that would tear down the
     * focused EditBox on every keystroke. The widget triggers re-init itself when it
     * loses focus, so the bar repaginates once the user is done typing.
     */
    public static void setMaxTabsPerPage(int value) {
        Screen s = Minecraft.getInstance().screen;
        if (s == null) return;
        ScreenLayout layout = ScreenLayoutStore.get(s.getClass());
        layout.maxTabsPerPage = Math.max(0, value);
        ScreenLayoutStore.save(s.getClass().getName(), layout);
    }

    /** Reapply the saved layout to the current screen — used after deferred edits. */
    private static java.lang.reflect.Field screenInitializedField;
    private static boolean screenInitializedFieldFailed;

    /**
     * Force a full screen re-init (children cleared, init() callback re-run, ScreenEvent.Init
     * fired). Vanilla's {@code Screen.init(mc, w, h)} short-circuits on {@code initialized=true}
     * and only calls {@code repositionElements()}, so a plain {@code mc.setScreen(mc.screen)}
     * doesn't rebuild our tab buttons. We reset {@code initialized} via reflection first so the
     * full event-firing path runs.
     */
    public static void reinitCurrentScreen() {
        Minecraft mc = Minecraft.getInstance();
        Screen screen = mc.screen;
        if (screen == null) return;
        if (!screenInitializedFieldFailed) {
            try {
                if (screenInitializedField == null) {
                    screenInitializedField = Screen.class.getDeclaredField("initialized");
                    screenInitializedField.setAccessible(true);
                }
                screenInitializedField.setBoolean(screen, false);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                screenInitializedFieldFailed = true;
            }
        }
        mc.setScreen(screen);
    }

    /** Tab order for the current screen's saved layout (defaults to LEFT_TO_RIGHT). */
    public static vodmordia.modtabs.layout.TabOrder currentTabOrder() {
        Screen s = Minecraft.getInstance().screen;
        if (s == null) return vodmordia.modtabs.layout.TabOrder.LEFT_TO_RIGHT;
        ScreenLayout layout = ScreenLayoutStore.get(s.getClass());
        return layout.tabOrder != null ? layout.tabOrder : vodmordia.modtabs.layout.TabOrder.LEFT_TO_RIGHT;
    }

    /** Cycle button writes the new tab order directly to the saved layout JSON. */
    public static void cycleTabOrder() {
        Screen s = Minecraft.getInstance().screen;
        if (s == null) return;
        ScreenLayout layout = ScreenLayoutStore.get(s.getClass());
        if (layout.tabOrder == null) layout.tabOrder = vodmordia.modtabs.layout.TabOrder.LEFT_TO_RIGHT;
        layout.tabOrder = layout.tabOrder.next();
        ScreenLayoutStore.save(s.getClass().getName(), layout);
        // Re-init so initScreenButtons rebuilds the bar with the new order.
        reinitCurrentScreen();
    }

    /** Anchor for the current screen's saved layout (defaults to GUI_RELATIVE). */
    public static vodmordia.modtabs.layout.Anchor currentAnchor() {
        Screen s = Minecraft.getInstance().screen;
        if (s == null) return vodmordia.modtabs.layout.Anchor.GUI_RELATIVE;
        ScreenLayout layout = ScreenLayoutStore.get(s.getClass());
        return layout.anchor != null ? layout.anchor : vodmordia.modtabs.layout.Anchor.GUI_RELATIVE;
    }

    /**
     * Toggle the per-screen anchor and re-baseline {@code offsetX/Y} so the bar stays at
     * the same absolute screen position. Without re-baselining, flipping
     * GUI_RELATIVE → SCREEN_ABSOLUTE would teleport the bar by the GUI's top-left coords.
     * Saves immediately and re-fires {@code initScreenButtons} via {@code setScreen}.
     */
    public static void cycleAnchor() {
        Screen s = Minecraft.getInstance().screen;
        if (s == null) return;
        ScreenLayout layout = ScreenLayoutStore.get(s.getClass());
        vodmordia.modtabs.layout.Anchor oldAnchor = (layout.anchor != null)
                ? layout.anchor
                : vodmordia.modtabs.layout.Anchor.GUI_RELATIVE;
        vodmordia.modtabs.layout.Anchor newAnchor = oldAnchor.next();
        // Bar's current absolute position = saved leftScreenPos (already includes
        // layout.offsetX). We pin the bar there and recompute offsets in the new frame.
        int absX = TabsMenu.leftScreenPos;
        int absY = TabsMenu.topScreenPos;
        int[] guiBase = computeGuiBase(s);
        int newOffsetX, newOffsetY;
        if (newAnchor == vodmordia.modtabs.layout.Anchor.SCREEN_ABSOLUTE) {
            newOffsetX = absX;
            newOffsetY = absY;
        } else {
            newOffsetX = absX - guiBase[0];
            newOffsetY = absY - guiBase[1];
        }
        ScreenLayout updated = layout.copy();
        updated.anchor = newAnchor;
        updated.offsetX = newOffsetX;
        updated.offsetY = newOffsetY;
        ScreenLayoutStore.save(s.getClass().getName(), updated);
        // Re-init the screen so initScreenButtons re-runs against the new anchor.
        reinitCurrentScreen();
    }

    /**
     * Outlines the active anchor frame as a 1-px yellow rect. GUI_RELATIVE traces the
     * registered GUI dimensions (centered on the screen); SCREEN_ABSOLUTE traces the
     * screen edges. Yellow is used (rather than the panel's green) so the anchor
     * outline reads as distinct from the bar's selection / handle accents.
     */
    private static void drawAnchorOutline(GuiGraphics gui, Screen screen) {
        ScreenLayout layout = ScreenLayoutStore.get(screen.getClass());
        int color = 0xFFFFD700;
        int x0, y0, x1, y1;
        if (layout.anchor == vodmordia.modtabs.layout.Anchor.SCREEN_ABSOLUTE) {
            x0 = 0;
            y0 = 0;
            x1 = screen.width;
            y1 = screen.height;
        } else {
            int[] base = computeGuiBase(screen);
            ScreenInfo info = tabsScreens.get(screen.getClass());
            int guiW = (info != null) ? info.width.apply(Minecraft.getInstance().player) : 176;
            int guiH = (info != null) ? info.height.apply(Minecraft.getInstance().player) : 166;
            x0 = base[0];
            y0 = base[1];
            x1 = x0 + guiW;
            y1 = y0 + guiH;
        }
        // 4 thin edges (top, bottom, left, right). Inset SCREEN_ABSOLUTE by 1px so the
        // right/bottom edges aren't drawn off-screen.
        boolean inset = layout.anchor == vodmordia.modtabs.layout.Anchor.SCREEN_ABSOLUTE;
        if (inset) { x1 -= 1; y1 -= 1; }
        gui.fill(x0, y0, x1, y0 + 1, color);
        gui.fill(x0, y1, x1 + 1, y1 + 1, color);
        gui.fill(x0, y0, x0 + 1, y1, color);
        gui.fill(x1, y0, x1 + 1, y1, color);
    }

    /** Top-left of the host GUI box on the current screen, for the registered dimensions. */
    private static int[] computeGuiBase(Screen screen) {
        ScreenInfo info = tabsScreens.get(screen.getClass());
        if (info == null) return new int[]{0, 0};
        try {
            int guiW = info.width.apply(Minecraft.getInstance().player);
            int guiH = info.height.apply(Minecraft.getInstance().player);
            return new int[]{(screen.width - guiW) / 2, (screen.height - guiH) / 2};
        } catch (Exception ignored) {
            return new int[]{0, 0};
        }
    }

    /** Effective rotation in degrees (saved + drag delta). 0 = unrotated. */
    public static float currentEffectiveRotation() {
        Screen s = Minecraft.getInstance().screen;
        if (s == null) return 0f;
        ScreenLayout layout = ScreenLayoutStore.get(s.getClass());
        return layout.rotation + (isEditing(s) ? tempRotation : 0f);
    }

    /** Center point (screen coords) of the unrotated bar — the pivot for rotation. */
    public static double[] barCenter() {
        int[] b = computeTabBarBounds();
        return new double[]{(b[0] + b[2]) / 2.0, (b[1] + b[3]) / 2.0};
    }

    /** Rotate (x, y) around (cx, cy) by `degrees` clockwise (screen Y is down). */
    public static double[] rotatePoint(double x, double y, double cx, double cy, double degrees) {
        double rad = Math.toRadians(degrees);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        double dx = x - cx;
        double dy = y - cy;
        return new double[]{
            cx + dx * cos - dy * sin,
            cy + dx * sin + dy * cos
        };
    }

    /** Inverse of {@link #rotatePoint}: undoes a CW rotation by `degrees`. */
    public static double[] inverseRotatePoint(double x, double y, double cx, double cy, double degrees) {
        return rotatePoint(x, y, cx, cy, -degrees);
    }

    /** Unit vector along the bar's primary axis in screen coords (accounts for rotation). */
    private static double[] primaryAxisUnit() {
        double rad = Math.toRadians(currentEffectiveRotation());
        // Unrotated horizontal primary axis is (1, 0) → rotated CW = (cos, sin).
        return new double[]{Math.cos(rad), Math.sin(rad)};
    }

    /** Effective gap between adjacent tabs along the primary axis (saved + drag delta). */
    public static int currentEffectiveTabSpacing() {
        Screen s = Minecraft.getInstance().screen;
        if (s == null) return 1;
        ScreenLayout layout = ScreenLayoutStore.get(s.getClass());
        return layout.tabSpacing + (isEditing(s) ? tempSpacingDelta : 0);
    }

    /** Stride from one tab's primary-axis anchor to the next. */
    public static int primaryAxisStep() {
        return Math.round(TAB_WIDTH * currentEffectiveScale()) + currentEffectiveTabSpacing();
    }

    /** Scaled tab width (full pixel width including the scale multiplier). */
    public static int effectiveTabWidth() {
        return Math.round(TAB_WIDTH * currentEffectiveScale());
    }

    /** Scaled tab height. */
    public static int effectiveTabHeight() {
        return Math.round(TAB_HEIGHT * currentEffectiveScale());
    }

    public static void saveEdit(Screen screen) {
        if (screen == null) return;
        String fqn = screen.getClass().getName();
        ScreenLayout existing = ScreenLayoutStore.get(fqn);
        ScreenLayout updated = new ScreenLayout(
            existing.offsetX + dragOffsetX,
            existing.offsetY + dragOffsetY,
            existing.scale * tempScale,
            existing.tabSpacing + tempSpacingDelta,
            existing.rotation + tempRotation,
            existing.nextButtonOffsetX + tempNextOffsetX,
            existing.nextButtonOffsetY + tempNextOffsetY,
            existing.nextButtonRotation + tempNextRotation,
            (((existing.iconRotation + tempIconRotation) % 360) + 360) % 360);
        // Preserve tuckDirection, anchor, tabOrder, and maxTabsPerPage from existing —
        // saveEdit's 9-arg ScreenLayout ctor doesn't set them, and clobbering each one
        // caused observable bugs (tuck reset to DOWN, absolute offsets reinterpreted as
        // GUI-relative, R→L bars reverting, per-screen pagination cap lost on save).
        updated.tuckDirection = existing.tuckDirection;
        updated.anchor = existing.anchor;
        updated.tabOrder = existing.tabOrder;
        updated.maxTabsPerPage = existing.maxTabsPerPage;
        ScreenLayoutStore.save(fqn, updated);
        // Re-anchor the bar position to the saved offsets directly. We can't rely on the
        // setScreen(same) call below to fire ScreenEvent.Init.Post — vanilla Screen.init
        // short-circuits on `initialized=true` and only calls repositionElements, skipping
        // our handler. AbstractContainerScreen masks this via per-frame updateButtonsPosition,
        // but plain Screen subclasses (AdvancementsScreen, Xaero, etc.) would otherwise keep
        // the stale leftScreenPos and the bar would snap back to its pre-edit position once
        // dragOffsetX resets.
        ScreenInfo info = tabsScreens.get(screen.getClass());
        if (info != null) {
            if (updated.anchor == vodmordia.modtabs.layout.Anchor.SCREEN_ABSOLUTE) {
                leftScreenPos = updated.offsetX;
                topScreenPos = updated.offsetY;
            } else {
                Player player = Minecraft.getInstance().player;
                int guiLeft = (screen.width - info.width.apply(player)) / 2;
                int guiTop = (screen.height - info.height.apply(player)) / 2;
                leftScreenPos = guiLeft + updated.offsetX;
                topScreenPos = guiTop + updated.offsetY;
            }
        }
        exitEditMode();
        // Re-init the screen so non-container screens (Xaero map, advancements,
        // skill screens, etc.) pick up the new position. AbstractContainerScreen
        // would already see it via per-frame updateButtonsPosition, but doing the
        // re-init for everything keeps behavior consistent.
        reinitCurrentScreen();
    }

    public static void resetEdit(Screen screen) {
        if (screen == null) return;
        ScreenLayoutStore.reset(screen.getClass().getName());
        exitEditMode();
    }

    public static void onMousePressed(Screen screen, double mouseX, double mouseY) {
        if (!isEditing(screen)) return;

        dragAnchorMouseX = (int) mouseX;
        dragAnchorMouseY = (int) mouseY;
        dragStartOffsetX = dragOffsetX;
        dragStartOffsetY = dragOffsetY;
        dragStartTempScale = tempScale;
        dragStartTempSpacingDelta = tempSpacingDelta;
        dragStartTempRotation = tempRotation;
        dragStartTempNextOffsetX = tempNextOffsetX;
        dragStartTempNextOffsetY = tempNextOffsetY;
        dragStartTempNextRotation = tempNextRotation;

        int handle = handleHitTest(mouseX, mouseY);
        if (handle >= 0 && handle <= 3) {
            dragMode = DragMode.SCALE;
            double[] center = barCenter();
            dragStartCenterX = center[0];
            dragStartCenterY = center[1];
            dragStartCenterDistance = Math.hypot(mouseX - dragStartCenterX, mouseY - dragStartCenterY);
            if (dragStartCenterDistance < 1.0) dragStartCenterDistance = 1.0;
        } else if (handle == 4) {
            dragMode = DragMode.SPACING_LOW;
        } else if (handle == 5) {
            dragMode = DragMode.SPACING_HIGH;
        } else if (handle == 6) {
            dragMode = DragMode.ROTATION;
            double[] center = barCenter();
            dragStartCenterX = center[0];
            dragStartCenterY = center[1];
            dragStartAngleRad = Math.atan2(mouseY - dragStartCenterY, mouseX - dragStartCenterX);
        } else if (handle == 7) {
            // Next-button rotation handle. Pivot is the next-button's CURRENT screen-space center
            // (post-bar-rotation, post-translate) so the user spins around it intuitively.
            dragMode = DragMode.NEXT_ROTATE;
            double[] center = nextButtonScreenCenter();
            dragStartCenterX = center[0];
            dragStartCenterY = center[1];
            dragStartAngleRad = Math.atan2(mouseY - dragStartCenterY, mouseX - dragStartCenterX);
        } else if (isMouseOnNextButton(mouseX, mouseY)) {
            dragMode = DragMode.NEXT_TRANSLATE;
        } else if (isMouseOnTabBar(mouseX, mouseY)) {
            dragMode = DragMode.BAR;
        } else {
            dragMode = DragMode.NONE;
        }
    }

    public static void onMouseDragged(Screen screen, double mouseX, double mouseY) {
        if (!isEditing(screen) || dragMode == DragMode.NONE) return;
        switch (dragMode) {
            case BAR -> {
                dragOffsetX = dragStartOffsetX + (int) (mouseX - dragAnchorMouseX);
                dragOffsetY = dragStartOffsetY + (int) (mouseY - dragAnchorMouseY);
                applyEdgeSnap(screen);
            }
            case SCALE -> {
                double now = Math.hypot(mouseX - dragStartCenterX, mouseY - dragStartCenterY);
                tempScale = dragStartTempScale * (float) (now / dragStartCenterDistance);
            }
            case SPACING_HIGH -> {
                // Project mouse delta onto the bar's *current* primary axis so dragging
                // along the rotated bar widens it regardless of screen orientation.
                double[] axis = primaryAxisUnit();
                double mdx = mouseX - dragAnchorMouseX;
                double mdy = mouseY - dragAnchorMouseY;
                double projected = mdx * axis[0] + mdy * axis[1];
                int divisor = Math.max(1, currentTabsCount - 1);
                tempSpacingDelta = dragStartTempSpacingDelta + (int) (projected / divisor);
            }
            case SPACING_LOW -> {
                double[] axis = primaryAxisUnit();
                double mdx = mouseX - dragAnchorMouseX;
                double mdy = mouseY - dragAnchorMouseY;
                double projected = mdx * axis[0] + mdy * axis[1];
                int divisor = Math.max(1, currentTabsCount - 1);
                tempSpacingDelta = dragStartTempSpacingDelta + (int) (-projected / divisor);
                // Shift the bar by the projected component along the axis so the dragged end
                // follows the cursor along the rotated bar.
                dragOffsetX = dragStartOffsetX + (int) (projected * axis[0]);
                dragOffsetY = dragStartOffsetY + (int) (projected * axis[1]);
            }
            case ROTATION -> {
                double now = Math.atan2(mouseY - dragStartCenterY, mouseX - dragStartCenterX);
                double deltaDeg = Math.toDegrees(now - dragStartAngleRad);
                tempRotation = dragStartTempRotation + (float) deltaDeg;

                Screen s = Minecraft.getInstance().screen;
                if (s != null) {
                    float saved = ScreenLayoutStore.get(s.getClass()).rotation;
                    float effective = saved + tempRotation;
                    float wrapped = ((effective % 360f) + 360f) % 360f;
                    float nearest = Math.round(wrapped / 90f) * 90f;
                    if (Math.abs(wrapped - nearest) <= ROTATION_SNAP_DEGREES) {
                        float snappedEffective = effective - wrapped + nearest;
                        tempRotation = snappedEffective - saved;
                    }
                }
            }
            case NEXT_TRANSLATE -> {
                tempNextOffsetX = dragStartTempNextOffsetX + (int) (mouseX - dragAnchorMouseX);
                tempNextOffsetY = dragStartTempNextOffsetY + (int) (mouseY - dragAnchorMouseY);
            }
            case NEXT_ROTATE -> {
                double now = Math.atan2(mouseY - dragStartCenterY, mouseX - dragStartCenterX);
                double deltaDeg = Math.toDegrees(now - dragStartAngleRad);
                tempNextRotation = dragStartTempNextRotation + (float) deltaDeg;

                // Same cardinal snap as the bar rotation, applied to next-button's TOTAL rotation.
                Screen s = Minecraft.getInstance().screen;
                if (s != null) {
                    ScreenLayout layout = ScreenLayoutStore.get(s.getClass());
                    float effective = layout.nextButtonRotation + tempNextRotation;
                    float wrapped = ((effective % 360f) + 360f) % 360f;
                    float nearest = Math.round(wrapped / 90f) * 90f;
                    if (Math.abs(wrapped - nearest) <= ROTATION_SNAP_DEGREES) {
                        float snappedEffective = effective - wrapped + nearest;
                        tempNextRotation = snappedEffective - layout.nextButtonRotation;
                    }
                }
            }
            default -> {}
        }
    }

    public static void onMouseReleased(Screen screen) {
        if (!isEditing(screen)) return;
        dragMode = DragMode.NONE;
    }

    public static void nudgeBar(int dx, int dy) {
        dragOffsetX += dx;
        dragOffsetY += dy;
    }

    /**
     * Hit-test the eight handle regions: 0-3 = corners (TL, TR, BL, BR), 4 = primary-axis low
     * midpoint, 5 = primary-axis high midpoint, -1 = miss. Cross-axis midpoints intentionally
     * have no handles in Phase 2.
     */
    private static int handleHitTest(double mouseX, double mouseY) {
        int[] b = computeTabBarBounds();
        // Inverse-rotate mouse so we can hit-test against unrotated handle positions.
        double[] m = inverseRotateMouseToBarFrame(mouseX, mouseY, b);
        double mx = m[0];
        double my = m[1];
        int cornerExtent = 9;
        int midHit = 6;
        int rotHit = 6;
        int fl = b[0] - 1, ft = b[1] - 1, fr = b[2] + 1, fb = b[3] + 1;
        if (insideCornerHit(mx, my, fl, ft, -1, -1, cornerExtent)) return 0;
        if (insideCornerHit(mx, my, fr, ft,  1, -1, cornerExtent)) return 1;
        if (insideCornerHit(mx, my, fl, fb, -1,  1, cornerExtent)) return 2;
        if (insideCornerHit(mx, my, fr, fb,  1,  1, cornerExtent)) return 3;
        int midX = (fl + fr) / 2;
        int midY = (ft + fb) / 2;
        if (within(mx, my, fl, midY, midHit)) return 4;
        if (within(mx, my, fr, midY, midHit)) return 5;
        // Rotation handle: positioned 14px above the top-edge midpoint in the unrotated frame.
        if (within(mx, my, midX, ft - ROTATION_HANDLE_OFFSET, rotHit)) return 6;
        // Next-button rotation handle — only when a next button actually exists. Without
        // this guard, screens with a single tab would let a click near (0,0) hit handle 7
        // because nextButtonScreenCenter falls back to (0,0).
        Screen current = Minecraft.getInstance().screen;
        boolean hasNextButton = current != null
                && current.children().stream().anyMatch(c -> c instanceof NextTabsButton);
        if (hasNextButton) {
            double[] nbCenter = nextButtonScreenCenter();
            float totalNextRot = currentEffectiveRotation() + currentNextEffectiveRotation();
            double rad = Math.toRadians(totalNextRot - 90); // -90 = "up" before rotation
            double hx = nbCenter[0] + Math.cos(rad) * (ROTATION_HANDLE_OFFSET + 6);
            double hy = nbCenter[1] + Math.sin(rad) * (ROTATION_HANDLE_OFFSET + 6);
            if (Math.hypot(mouseX - hx, mouseY - hy) <= rotHit) return 7;
        }
        return -1;
    }

    private static final int ROTATION_HANDLE_OFFSET = 14;
    private static final float ROTATION_SNAP_DEGREES = 5f;

    private static boolean within(double mouseX, double mouseY, int cx, int cy, int half) {
        return mouseX >= cx - half && mouseX < cx + half && mouseY >= cy - half && mouseY < cy + half;
    }

    private static boolean insideCornerHit(double mouseX, double mouseY, int cx, int cy, int dx, int dy, int extent) {
        int loX = dx < 0 ? cx - extent : cx - 2;
        int hiX = dx < 0 ? cx + 2       : cx + extent;
        int loY = dy < 0 ? cy - extent : cy - 2;
        int hiY = dy < 0 ? cy + 2       : cy + extent;
        return mouseX >= loX && mouseX < hiX && mouseY >= loY && mouseY < hiY;
    }

    /**
     * Bounds of the rendered tab bar (without hover padding) — used to decide
     * whether a mouse press in edit mode should start a drag.
     */
    /**
     * Render the edit-mode visual overlay: dim the screen everywhere except the tab
     * bar's bounding rect, draw a green frame just outside the bar, and place a small
     * triangle at each side's midpoint as a visual indicator. Called from a
     * Render.Post hook so it draws on top of the screen content but works around
     * the tab bar (which has already been drawn during the screen's normal render
     * pass and is therefore visible through the "hole" in the dim overlay).
     */
    public static void renderEditModeOverlay(GuiGraphics gui, Screen screen, int mouseX, int mouseY) {
        if (!isEditing(screen)) {
            return;
        }
        int sw = screen.width;
        int sh = screen.height;
        int dim = 0xC0000000;

        // Pass 1: dim + tabs at Z=400. ItemRenderer pushes its own +150 internally for
        // 3D-rendered item icons, so item icons land at Z≈550 — fine, they just need to
        // be above the player model (which renders below 400).
        gui.pose().pushPose();
        gui.pose().translate(0, 0, 400);

        gui.fill(0, 0, sw, sh, dim);

        // Anchor outline: a thin green rect around the host GUI box (when GUI_RELATIVE)
        // or the screen edges (when SCREEN_ABSOLUTE). Lives at Z=400 so it's drawn over
        // the dim but under the panel BG (Z=600), making the chosen reference frame
        // obvious as the user toggles the anchor button.
        drawAnchorOutline(gui, screen);

        for (var child : screen.children()) {
            if (child instanceof net.minecraft.client.gui.components.AbstractWidget w) {
                if (child instanceof vodmordia.modtabs.client.screens.TabButton
                        || child instanceof vodmordia.modtabs.client.screens.NextTabsButton
                        || child instanceof vodmordia.modtabs.client.screens.LayoutEditorButtons.EditOnly
                        || child instanceof vodmordia.modtabs.client.screens.LayoutEditorButtons.IconScaleEditBox
                        || child instanceof vodmordia.modtabs.client.screens.LayoutEditorButtons.IconNudgeEditBox
                        || child instanceof vodmordia.modtabs.client.screens.LayoutEditorButtons.MaxTabsPerPageEditBox) {
                    w.render(gui, mouseX, mouseY, 0f);
                }
            }
        }

        gui.pose().popPose();

        // Pass 2: decorations (frame, handles, panel) at Z=600 so they sit ABOVE any
        // 3D-rendered item icons that landed at Z≈550 in pass 1. Without this split, an
        // ItemStack-icon tab (e.g. Apothic Attributes' sword) would draw on top of the panel.
        gui.pose().pushPose();
        gui.pose().translate(0, 0, 600);

        // Frame + handles render inside a pose-rotated block so they spin with the bar.
        int[] bounds = computeTabBarBounds();
        int tabLeft = bounds[0], tabTop = bounds[1], tabRight = bounds[2], tabBottom = bounds[3];
        double cx = (tabLeft + tabRight) / 2.0;
        double cy = (tabTop + tabBottom) / 2.0;
        float rotation = currentEffectiveRotation();
        int hovered = handleHitTest(mouseX, mouseY);

        gui.pose().pushPose();
        if (rotation != 0f) {
            gui.pose().translate(cx, cy, 0);
            gui.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(rotation));
            gui.pose().translate(-cx, -cy, 0);
        }

        // Highlight the tab whose configKey matches the screen we're editing — subtle
        // transparent green fill so the user can tell which tab their per-screen icon
        // / visibility / custom-icon settings will apply to.
        TabBase editedTab = findTabForConfigKey(getConfigKeyForScreen(screen));
        if (editedTab != null) {
            int offX = getAnimatedXOffset() + getDragOffsetX();
            int offY = getAnimatedYOffset() + getDragOffsetY();
            int tabSw = effectiveTabWidth();
            int tabSh = effectiveTabHeight();
            for (var child : screen.children()) {
                if (child instanceof TabButton tb && tb.tabBase == editedTab) {
                    int ax = tb.getX() + offX;
                    int ay = tb.getY() + offY;
                    gui.fill(ax, ay, ax + tabSw, ay + tabSh, 0x8044FF66);
                }
            }
        }

        int green = 0xFF44FF66;
        int inset = 1;
        int fl = tabLeft - inset, ft = tabTop - inset, fr = tabRight + inset, fb = tabBottom + inset;
        int t = 1;
        gui.fill(fl, ft, fr, ft + t, green);
        gui.fill(fl, fb - t, fr, fb, green);
        gui.fill(fl, ft, fl + t, fb, green);
        gui.fill(fr - t, ft, fr, fb, green);

        int midX = (fl + fr) / 2;
        int midY = (ft + fb) / 2;
        drawCornerHandle(gui, fl, ft, 0, hovered == 0);
        drawCornerHandle(gui, fr, ft, 1, hovered == 1);
        drawCornerHandle(gui, fl, fb, 2, hovered == 2);
        drawCornerHandle(gui, fr, fb, 3, hovered == 3);
        drawInwardTriangle(gui, fl - 4, midY, 2, hovered == 4);
        drawInwardTriangle(gui, fr + 4, midY, 3, hovered == 5);

        // Rotation handle — small filled circle above the top-edge midpoint, with a
        // thin tether line back to the frame. Lives in the rotated pose so it follows the bar.
        int rotHandleX = midX;
        int rotHandleY = ft - ROTATION_HANDLE_OFFSET;
        gui.fill(midX, rotHandleY + 1, midX + 1, ft, green);
        drawRotationHandle(gui, rotHandleX, rotHandleY, hovered == 6);

        gui.pose().popPose();

        // Next-button rotation handle — only when a next button actually exists on the
        // screen. Without this guard, screens with a single tab (no overflow chevron)
        // would render the handle at (0,0) since nextButtonScreenCenter falls back to
        // a zero default — that produced a stray purple disc in the upper-left corner.
        boolean hasNextButton = screen.children().stream().anyMatch(c -> c instanceof NextTabsButton);
        if (hasNextButton) {
            double[] nbCenter = nextButtonScreenCenter();
            float totalNextRot = currentEffectiveRotation() + currentNextEffectiveRotation();
            double rad = Math.toRadians(totalNextRot - 90);
            int hx = (int) Math.round(nbCenter[0] + Math.cos(rad) * (ROTATION_HANDLE_OFFSET + 6));
            int hy = (int) Math.round(nbCenter[1] + Math.sin(rad) * (ROTATION_HANDLE_OFFSET + 6));
            drawLineBetween(gui, (int) Math.round(nbCenter[0]), (int) Math.round(nbCenter[1]), hx, hy, 0xFFB36BFF);
            drawNextRotationHandle(gui, hx, hy, hovered == 7);
        }

        // Floating options panel (top-left of screen). Drawn AFTER everything else so it
        // sits on top, including over rotated tabs that might overlap.
        drawOptionsPanel(gui, screen);

        gui.pose().popPose();

        // Pass 3: panel controls at Z=700 — must be ABOVE the panel BG (Z=600) or they'd
        // be obscured by the rectangle drawn in drawOptionsPanel.
        gui.pose().pushPose();
        gui.pose().translate(0, 0, 700);
        for (var child : screen.children()) {
            if (child instanceof net.minecraft.client.gui.components.AbstractWidget w) {
                if (child instanceof vodmordia.modtabs.client.screens.LayoutEditorButtons.IconRotationCycle
                        || child instanceof vodmordia.modtabs.client.screens.LayoutEditorButtons.VisibilityCycle
                        || child instanceof vodmordia.modtabs.client.screens.LayoutEditorButtons.TuckDirectionCycle
                        || child instanceof vodmordia.modtabs.client.screens.LayoutEditorButtons.CustomIconDropdown
                        || child instanceof vodmordia.modtabs.client.screens.LayoutEditorButtons.CustomIconRefresh
                        || child instanceof vodmordia.modtabs.client.screens.LayoutEditorButtons.CustomIconFolder
                        || child instanceof vodmordia.modtabs.client.screens.LayoutEditorButtons.IconScaleEditBox
                        || child instanceof vodmordia.modtabs.client.screens.LayoutEditorButtons.IconNudgeEditBox
                        || child instanceof vodmordia.modtabs.client.screens.LayoutEditorButtons.AnchorCycle
                        || child instanceof vodmordia.modtabs.client.screens.LayoutEditorButtons.TabOrderCycle
                        || child instanceof vodmordia.modtabs.client.screens.LayoutEditorButtons.MaxTabsPerPageEditBox) {
                    w.render(gui, mouseX, mouseY, 0f);
                }
            }
        }
        gui.pose().popPose();

        // Pass 4: global settings modal (Z=900) — sits above everything else, including
        // the panel and its widgets, so its scrim and controls capture all input.
        if (GlobalSettingsPanel.isOpen()) {
            gui.pose().pushPose();
            gui.pose().translate(0, 0, 900);
            GlobalSettingsPanel.render(gui, screen, mouseX, mouseY);
            gui.pose().popPose();
        }
    }

    /** Public so the LayoutEditorButtons module can position controls inside it. */
    public static final int PANEL_W = 230;
    public static final int PANEL_H = 224;
    public static final int PANEL_TITLE_H = 14;
    public static final int PANEL_ROW_H = 16;
    public static final int PANEL_LABEL_W = 78;
    public static final int PANEL_PAD = 6;
    /** Reserved space at the bottom of the panel for the preview row. */
    public static final int PANEL_PREVIEW_H = 28;
    /** Width of the side handle that's always visible (drawn off the right edge of the panel). */
    public static final int PANEL_HANDLE_W = 12;
    public static final int PANEL_HANDLE_H = 40;
    /** Expanded panel left margin from screen edge. */
    private static final int PANEL_MARGIN_X = 8;

    /** When collapsed, the panel slides off-screen left and only the handle peeks back in. */
    private static boolean panelCollapsed = false;

    public static int currentPanelX(Screen screen) {
        if (panelCollapsed) return -PANEL_W; // handle ends up at x=0
        return PANEL_MARGIN_X;
    }

    public static int currentPanelY(Screen screen) {
        Screen s = (screen != null) ? screen : Minecraft.getInstance().screen;
        int sh = (s != null) ? s.height : 240;
        return Math.max(8, (sh - PANEL_H) / 2);
    }

    public static int panelHandleX(Screen screen) {
        return currentPanelX(screen) + PANEL_W;
    }

    public static int panelHandleY(Screen screen) {
        return currentPanelY(screen) + (PANEL_H - PANEL_HANDLE_H) / 2;
    }

    public static boolean isMouseOnPanelHandle(Screen screen, double mx, double my) {
        int hx = panelHandleX(screen);
        int hy = panelHandleY(screen);
        return mx >= hx && mx <= hx + PANEL_HANDLE_W
            && my >= hy && my <= hy + PANEL_HANDLE_H;
    }

    public static void togglePanelCollapsed() {
        panelCollapsed = !panelCollapsed;
    }

    public static boolean isPanelCollapsed() {
        return panelCollapsed;
    }

    private static void drawOptionsPanel(GuiGraphics gui, Screen screen) {
        int x = currentPanelX(screen), y = currentPanelY(screen), w = PANEL_W, h = PANEL_H;
        int bg = 0xE8101418;
        int border = 0xFF44FF66;
        gui.fill(x, y, x + w, y + h, bg);
        // 1-px border (skip the right edge — the handle covers it)
        gui.fill(x, y, x + w, y + 1, border);
        gui.fill(x, y + h - 1, x + w, y + h, border);
        gui.fill(x, y, x + 1, y + h, border);
        gui.fill(x + w - 1, y, x + w, y + h, border);
        // Title
        gui.drawString(Minecraft.getInstance().font, "Layout Options",
                x + PANEL_PAD, y + 4, 0xFFCCFFCC, false);
        gui.fill(x + 1, y + PANEL_TITLE_H, x + w - 1, y + PANEL_TITLE_H + 1, border);
        // Row labels
        int rowY = y + PANEL_TITLE_H + PANEL_PAD;
        gui.drawString(Minecraft.getInstance().font, "Icon rotation:", x + PANEL_PAD, rowY + 4, 0xFFCCCCCC, false);
        gui.drawString(Minecraft.getInstance().font, "Tab visibility:", x + PANEL_PAD, rowY + PANEL_ROW_H + 4, 0xFFCCCCCC, false);
        gui.drawString(Minecraft.getInstance().font, "Tuck direction:", x + PANEL_PAD, rowY + PANEL_ROW_H * 2 + 4, 0xFFCCCCCC, false);
        gui.drawString(Minecraft.getInstance().font, "Custom icon:", x + PANEL_PAD, rowY + PANEL_ROW_H * 3 + 4, 0xFFCCCCCC, false);

        // Preview row — render of the tab's icon at its natural pose (no rotation,
        // no vertical re-orientation) so the user can compare against tweaks.
        int previewRowY = rowY + PANEL_ROW_H * 4 + PANEL_PAD;
        gui.drawString(Minecraft.getInstance().font, "Preview:", x + PANEL_PAD, previewRowY + 8, 0xFFCCCCCC, false);
        TabBase previewTab = findTabForConfigKey(getConfigKeyForScreen(screen));
        if (previewTab != null) {
            int previewTabX = x + PANEL_PAD + PANEL_LABEL_W;
            int previewTabY = previewRowY;
            previewRendering = true;
            try {
                previewTab.render(gui, previewTabX, previewTabY, false);
            } finally {
                previewRendering = false;
            }
        }

        // Scale-factor row sits below the preview. The trailing "%" is drawn after the
        // editor's IconScaleEditBox; both anchor off PANEL_PAD + PANEL_LABEL_W like other rows.
        int scaleRowY = previewRowY + PANEL_PREVIEW_H + PANEL_PAD;
        gui.drawString(Minecraft.getInstance().font, "Scale factor:", x + PANEL_PAD, scaleRowY + 4, 0xFFCCCCCC, false);
        // The "%" suffix is drawn here (not as a widget) so it stays glued to whatever the
        // EditBox's right edge happens to be — addToScreen sizes the box to leave 12 px for it.
        gui.drawString(Minecraft.getInstance().font, "%",
                x + PANEL_W - PANEL_PAD - 8, scaleRowY + 4, 0xFFCCCCCC, false);

        // Icon-nudge row: four small inputs (U/D/L/R) for per-direction pixel offsets.
        // The cell letters are drawn here so the EditBoxes can stay simple text inputs;
        // addToScreen registers four IconNudgeEditBox widgets keyed off the same offsets.
        int nudgeRowY = scaleRowY + PANEL_ROW_H;
        gui.drawString(Minecraft.getInstance().font, "Icon nudge:", x + PANEL_PAD, nudgeRowY + 4, 0xFFCCCCCC, false);
        int relCellX = PANEL_PAD + PANEL_LABEL_W;
        int controlW = PANEL_W - PANEL_PAD - PANEL_LABEL_W - PANEL_PAD;
        int nudgeLetterW = 8;
        int nudgeBoxW = 22;
        int nudgeCellW = nudgeLetterW + nudgeBoxW;
        int nudgePitch = (controlW - nudgeCellW) / 3; // distance between cell starts; 4 cells
        String[] nudgeLetters = { "U", "D", "L", "R" };
        for (int i = 0; i < 4; i++) {
            int cellX = x + relCellX + i * nudgePitch;
            gui.drawString(Minecraft.getInstance().font, nudgeLetters[i], cellX, nudgeRowY + 4, 0xFFCCCCCC, false);
        }

        // Anchor row: GUI/SCREEN toggle. The corresponding outline (GUI box vs screen edges)
        // is drawn during pass 1 of renderEditModeOverlay so the user sees the chosen frame.
        int anchorRowY = nudgeRowY + PANEL_ROW_H;
        gui.drawString(Minecraft.getInstance().font, "Anchor:", x + PANEL_PAD, anchorRowY + 4, 0xFFCCCCCC, false);

        // Tab Order row: L→R vs R→L visual ordering of tabs in the bar.
        int tabOrderRowY = anchorRowY + PANEL_ROW_H;
        gui.drawString(Minecraft.getInstance().font, "Tab order:", x + PANEL_PAD, tabOrderRowY + 4, 0xFFCCCCCC, false);

        // Max tabs/page row: per-screen pagination cap. 0 = unlimited (single page).
        int maxTabsRowY = tabOrderRowY + PANEL_ROW_H;
        gui.drawString(Minecraft.getInstance().font, "Tabs/page:", x + PANEL_PAD, maxTabsRowY + 4, 0xFFCCCCCC, false);

        // Side handle (always visible — peeks at screen-left edge when panel is slid off).
        int hx = panelHandleX(screen);
        int hy = panelHandleY(screen);
        gui.fill(hx, hy, hx + PANEL_HANDLE_W, hy + PANEL_HANDLE_H, bg);
        gui.fill(hx, hy, hx + PANEL_HANDLE_W, hy + 1, border);
        gui.fill(hx, hy + PANEL_HANDLE_H - 1, hx + PANEL_HANDLE_W, hy + PANEL_HANDLE_H, border);
        gui.fill(hx + PANEL_HANDLE_W - 1, hy, hx + PANEL_HANDLE_W, hy + PANEL_HANDLE_H, border);
        // Arrow showing which direction the click will move the panel.
        String arrow = panelCollapsed ? ">" : "<";
        int aw = Minecraft.getInstance().font.width(arrow);
        gui.drawString(Minecraft.getInstance().font, arrow,
                hx + (PANEL_HANDLE_W - aw) / 2, hy + (PANEL_HANDLE_H - 8) / 2, 0xFFCCFFCC, false);
    }

    /** Bresenham-style line drawn as a stack of 1×1 fills. Crude but adequate for tethers. */
    private static void drawLineBetween(GuiGraphics gui, int x0, int y0, int x1, int y1, int color) {
        int dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
        int dy = -Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
        int err = dx + dy;
        while (true) {
            gui.fill(x0, y0, x0 + 1, y0 + 1, color);
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 >= dy) { err += dy; x0 += sx; }
            if (e2 <= dx) { err += dx; y0 += sy; }
        }
    }

    /** Purple disc — visually distinct from the yellow bar-rotation handle. */
    private static void drawNextRotationHandle(GuiGraphics gui, int cx, int cy, boolean hovered) {
        int color = hovered ? 0xFFD6A3FF : 0xFFB36BFF;
        int radius = 4;
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                if (dx * dx + dy * dy <= radius * radius) {
                    gui.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
                }
            }
        }
    }

    /** Small filled disc used as the rotation handle. Yellow → bright yellow on hover. */
    private static void drawRotationHandle(GuiGraphics gui, int cx, int cy, boolean hovered) {
        int color = hovered ? 0xFFFFEE66 : 0xFFFFCC22;
        int radius = 4;
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                if (dx * dx + dy * dy <= radius * radius) {
                    gui.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
                }
            }
        }
    }

    /**
     * Equilateral triangle with one vertex sitting exactly on the bar's corner; the
     * opposite edge (base) sits diagonally outward. Blue by default, brighter blue when
     * hovered. Corner index: 0=TL, 1=TR, 2=BL, 3=BR.
     */
    private static void drawCornerHandle(GuiGraphics gui, int cx, int cy, int corner, boolean hovered) {
        int color = hovered ? 0xFFA8DDFF : 0xFF44AAFF;
        int side = 8;
        double medianLen = side * Math.sqrt(3) / 2.0; // height of an equilateral triangle
        double halfBase = side / 2.0;

        // Outward-from-corner unit vector, normalized.
        double dx = (corner == 0 || corner == 2) ? -1 : 1;
        double dy = (corner == 0 || corner == 1) ? -1 : 1;
        double mag = Math.sqrt(dx * dx + dy * dy);
        dx /= mag;
        dy /= mag;

        // Base center is medianLen away along the outward diagonal.
        double bcx = cx + dx * medianLen;
        double bcy = cy + dy * medianLen;
        // Perpendicular to the diagonal (rotated 90° CCW: (-dy, dx)).
        double px = -dy;
        double py = dx;
        int v1x = cx;
        int v1y = cy;
        int v2x = (int) Math.round(bcx + px * halfBase);
        int v2y = (int) Math.round(bcy + py * halfBase);
        int v3x = (int) Math.round(bcx - px * halfBase);
        int v3y = (int) Math.round(bcy - py * halfBase);

        // Rasterize: scan the bounding box and fill pixels whose center is inside the triangle.
        int minX = Math.min(v1x, Math.min(v2x, v3x));
        int maxX = Math.max(v1x, Math.max(v2x, v3x));
        int minY = Math.min(v1y, Math.min(v2y, v3y));
        int maxY = Math.max(v1y, Math.max(v2y, v3y));
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (pointInTriangle(x + 0.5, y + 0.5, v1x, v1y, v2x, v2y, v3x, v3y)) {
                    gui.fill(x, y, x + 1, y + 1, color);
                }
            }
        }
    }

    private static boolean pointInTriangle(double px, double py, int ax, int ay, int bx, int by, int cx, int cy) {
        double s1 = (px - bx) * (ay - by) - (ax - bx) * (py - by);
        double s2 = (px - cx) * (by - cy) - (bx - cx) * (py - cy);
        double s3 = (px - ax) * (cy - ay) - (cx - ax) * (py - ay);
        boolean hasNeg = s1 < 0 || s2 < 0 || s3 < 0;
        boolean hasPos = s1 > 0 || s2 > 0 || s3 > 0;
        return !(hasNeg && hasPos);
    }

    /**
     * Filled triangle pointing in cardinal direction {@code dir}: 0=down, 1=up, 2=right, 3=left.
     * Drawn axis-aligned via {@code gui.fill} stacks; no matrix math needed.
     */
    private static void drawInwardTriangle(GuiGraphics gui, int cx, int cy, int dir, boolean hovered) {
        int color = hovered ? 0xFFA8FFB8 : 0xFF44FF66;
        int size = 4;
        for (int i = 0; i < size; i++) {
            int half = size - i;
            switch (dir) {
                case 0 -> gui.fill(cx - half, cy + i, cx + half + 1, cy + i + 1, color); // down: wide on top
                case 1 -> gui.fill(cx - half, cy - i, cx + half + 1, cy - i + 1, color); // up: wide on bottom
                case 2 -> gui.fill(cx + i, cy - half, cx + i + 1, cy + half + 1, color); // right
                case 3 -> gui.fill(cx - i, cy - half, cx - i + 1, cy + half + 1, color); // left
            }
        }
    }

    /** Returns {left, top, right, bottom} of the rendered tab bar with current drag offset, scale, and spacing. */
    public static int[] computeTabBarBounds() {
        int tabW = effectiveTabWidth();
        int tabH = effectiveTabHeight();
        int spacing = currentEffectiveTabSpacing();
        int left = leftScreenPos + dragOffsetX;
        int top;
        int bottom;
        if (currentDisplayMode == TabDisplayMode.INVERTED) {
            top = topScreenPos + dragOffsetY;
            bottom = top + tabH;
        } else {
            top = topScreenPos + dragOffsetY - tabH;
            bottom = topScreenPos + dragOffsetY;
        }
        int barLength = currentTabsCount > 0 ? currentTabsCount * tabW + (currentTabsCount - 1) * spacing : 0;
        int right = left + barLength;
        return new int[]{left, top, right, bottom};
    }

    public static boolean isMouseOnTabBar(double mouseX, double mouseY) {
        int[] b = computeTabBarBounds();
        double[] m = inverseRotateMouseToBarFrame(mouseX, mouseY, b);
        return m[0] >= b[0] && m[0] < b[2] && m[1] >= b[1] && m[1] < b[3];
    }

    private static final int EDGE_SNAP_DIST = 8;

    private static void applyEdgeSnap(Screen screen) {
        if (screen == null) return;
        int[] b = computeTabBarBounds();
        float rot = currentEffectiveRotation();
        double cx = (b[0] + b[2]) / 2.0;
        double cy = (b[1] + b[3]) / 2.0;
        double cosA = Math.cos(Math.toRadians(rot));
        double sinA = Math.sin(Math.toRadians(rot));
        double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        double[][] corners = { {b[0], b[1]}, {b[2], b[1]}, {b[2], b[3]}, {b[0], b[3]} };
        for (double[] c : corners) {
            double dx = c[0] - cx;
            double dy = c[1] - cy;
            double rx = cx + dx * cosA - dy * sinA;
            double ry = cy + dx * sinA + dy * cosA;
            if (rx < minX) minX = rx;
            if (rx > maxX) maxX = rx;
            if (ry < minY) minY = ry;
            if (ry > maxY) maxY = ry;
        }
        // Snap target follows the active anchor: GUI mode snaps to the GUI box edges,
        // SCREEN mode snaps to the screen edges. Falls back to screen edges if the
        // GUI bounds aren't resolvable (no registered ScreenInfo for this screen).
        int snapL = 0, snapT = 0, snapR = screen.width, snapB = screen.height;
        ScreenLayout layout = ScreenLayoutStore.get(screen.getClass());
        if (layout.anchor != vodmordia.modtabs.layout.Anchor.SCREEN_ABSOLUTE) {
            ScreenInfo info = tabsScreens.get(screen.getClass());
            if (info != null) {
                try {
                    int guiW = info.width.apply(Minecraft.getInstance().player);
                    int guiH = info.height.apply(Minecraft.getInstance().player);
                    snapL = (screen.width - guiW) / 2;
                    snapT = (screen.height - guiH) / 2;
                    snapR = snapL + guiW;
                    snapB = snapT + guiH;
                } catch (Exception ignored) {}
            }
        }
        // Horizontal snaps are inside-aligned in both anchor modes (bar edge meets target
        // edge from the inside): left edge → snapL, right edge → snapR.
        int snapX = 0;
        if (Math.abs(minX - snapL) < EDGE_SNAP_DIST) snapX = (int) Math.round(snapL - minX);
        else if (Math.abs(snapR - maxX) < EDGE_SNAP_DIST) snapX = (int) Math.round(snapR - maxX);

        // Vertical snaps differ by anchor:
        //   GUI mode  → bar sits OUTSIDE the GUI (bottom edge snaps to GUI top, top edge
        //               snaps to GUI bottom), since tabs naturally hang above/below the GUI.
        //   SCREEN mode → inside-aligned to the screen edges, same as horizontal.
        int snapY = 0;
        boolean guiMode = layout.anchor != vodmordia.modtabs.layout.Anchor.SCREEN_ABSOLUTE
                && tabsScreens.containsKey(screen.getClass());
        if (guiMode) {
            if (Math.abs(maxY - snapT) < EDGE_SNAP_DIST) snapY = (int) Math.round(snapT - maxY);
            else if (Math.abs(minY - snapB) < EDGE_SNAP_DIST) snapY = (int) Math.round(snapB - minY);
        } else {
            if (Math.abs(minY - snapT) < EDGE_SNAP_DIST) snapY = (int) Math.round(snapT - minY);
            else if (Math.abs(snapB - maxY) < EDGE_SNAP_DIST) snapY = (int) Math.round(snapB - maxY);
        }
        dragOffsetX += snapX;
        dragOffsetY += snapY;
    }

    /**
     * Hit-test the next-page chevron, accounting for bar rotation, next-button screen offset,
     * and next-button's own rotation. Returns false if no NextTabsButton exists on the screen.
     */
    public static boolean isMouseOnNextButton(double mouseX, double mouseY) {
        Screen s = Minecraft.getInstance().screen;
        if (s == null) return false;
        for (var child : s.children()) {
            if (child instanceof NextTabsButton nb) {
                if (nb.isMouseOver(mouseX, mouseY)) return true;
            }
        }
        return false;
    }

    /**
     * Final on-screen center of the next-button chevron (after bar rotation, next-button
     * translation, and next-button rotation). Used as the pivot for next-button rotation drag.
     */
    public static double[] nextButtonScreenCenter() {
        Screen s = Minecraft.getInstance().screen;
        if (s == null) return new double[]{0, 0};
        for (var child : s.children()) {
            if (child instanceof NextTabsButton nb) {
                int animX = nb.getAnimatedAnchorX();
                int animY = nb.getAnimatedAnchorY();
                int cx = animX + nb.getWidth() / 2;
                int cy = animY + nb.getHeight() / 2;
                // Apply bar rotation around bar center
                float barRot = currentEffectiveRotation();
                double px = cx;
                double py = cy;
                if (barRot != 0f) {
                    double[] center = barCenter();
                    double[] r = rotatePoint(px, py, center[0], center[1], barRot);
                    px = r[0];
                    py = r[1];
                }
                // Translate by next-button screen offset
                px += currentNextOffsetX();
                py += currentNextOffsetY();
                return new double[]{px, py};
            }
        }
        return new double[]{0, 0};
    }

    /** Apply the inverse of the current rotation to a screen-space mouse point so callers
     *  can hit-test against handle/bar coordinates that are stored in the unrotated frame. */
    private static double[] inverseRotateMouseToBarFrame(double mouseX, double mouseY, int[] bounds) {
        float rot = currentEffectiveRotation();
        if (rot == 0f) return new double[]{mouseX, mouseY};
        double cx = (bounds[0] + bounds[2]) / 2.0;
        double cy = (bounds[1] + bounds[3]) / 2.0;
        return inverseRotatePoint(mouseX, mouseY, cx, cy, rot);
    }

    /**
     * Returns the ModTabsConfig key prefix for the given screen, e.g. "inventory" /
     * "cobblemon" / "advancements". The visibility (YES/NO/TUCK) and custom-icon fields
     * for that screen live at {@code <key>TabDisplayVisibility} and
     * {@code <key>TabCustomIcon} on {@link ModTabsConfig}.
     *
     * First checks every registered tab's {@code isCurrentlyUsed(screen)} — if a tab
     * claims the screen, its {@code @TabConfig(configKey = ...)} (minus the "Tab"
     * suffix) is the right key. The hardcoded switch below is a fallback for tabs that
     * don't own a screen via {@code isCurrentlyUsed} (e.g. screens shared between
     * multiple tabs).
     */
    public static String getConfigKeyForScreen(Screen screen) {
        if (screen == null) return null;
        for (ScreenInfo info : tabsScreens.values()) {
            for (List<TabBase> list : info.tabs.values()) {
                for (TabBase tab : list) {
                    if (tab.isCurrentlyUsed(screen)) {
                        TabConfig tc = tab.getClass().getAnnotation(TabConfig.class);
                        if (tc != null) {
                            String key = tc.configKey();
                            if (key.endsWith("Tab")) {
                                key = key.substring(0, key.length() - 3);
                            }
                            return key;
                        }
                    }
                }
            }
        }
        String screenClassName = screen.getClass().getName();
        switch (screenClassName) {
            case ScreenClasses.VANILLA_INVENTORY: return "inventory";
            case ScreenClasses.VANILLA_ADVANCEMENTS:
            case ScreenClasses.BETTER_ADVANCEMENTS: return "advancements";
            case ScreenClasses.ARS_NOUVEAU_SPELLBOOK_GUI_LEGACY1:
            case ScreenClasses.ARS_NOUVEAU_SPELLBOOK_GUI_LEGACY2: return "arsNouveau";
            case ScreenClasses.BACKPACKED_FLYWHEEL_TRANSFORM:
            case ScreenClasses.BACKPACKED_SCREEN_ALT:
            case ScreenClasses.BACKPACKED_SCREEN: return "backpacked";
            case ScreenClasses.LSO_BODY_HEALTH: return "bodyDamage";
            case ScreenClasses.COBBLEMON_PARTY_LEGACY:
            case ScreenClasses.COBBLEMON_PARTY: return "cobblemon";
            case ScreenClasses.APPLESKIN_FOOD_STATS: return "diet";
            case ScreenClasses.FTB_LIBRARY_WRAPPER: return "ftbQuests";
            case ScreenClasses.JOURNEYMAP_FULLSCREEN: return "journeyMap";
            case ScreenClasses.SCGUNS_ATTACHMENT:
            case ScreenClasses.DRACONIC_EVOLUTION_GUI: return "draconicEvolution";
            case ScreenClasses.MAP_ATLASES_ACCESS_UTILS: return "mapAtlases";
            case ScreenClasses.XAEROS_MAP: return "xaerosMap";
            case ScreenClasses.PUFFERFISH_SKILLS_ALT1:
            case ScreenClasses.PUFFERFISH_SKILLS_ALT2: return "pufferfishSkills";
            case ScreenClasses.SCGUNS_PASSIVE_SKILL: return "passiveSkillTree";
            case ScreenClasses.BRASSWORKS_MISSIONS_UI: return "brassworksMissions";
            case ScreenClasses.BIOLOGY_DICTIONARY_HOME_SCREEN:
            case ScreenClasses.BIOLOGY_DICTIONARY_ABOUT_SCREEN:
            case ScreenClasses.BIOLOGY_DICTIONARY_CONFIG_SCREEN:
            case ScreenClasses.BIOLOGY_DICTIONARY_ENTITY_OVERVIEW_SCREEN:
            case ScreenClasses.BIOLOGY_DICTIONARY_ENTITY_DETAIL_SCREEN: return "biologyDictionary";
            case ScreenClasses.WILDEX_SCREEN: return "wildex";
            case ScreenClasses.VANILLA_CONTAINER:
                if (screenClassName.contains("sophisticatedbackpacks")) return "sophisticatedBackpacks";
                if (screenClassName.contains("travelersbackpack")) return "travelersBackpack";
                break;
        }
        return null;
    }

    /**
     * Find the registered tab whose class name matches the configKey by convention
     * (configKey "cobblemon" → CobblemonTab class). Used by the panel preview to draw
     * a live render of the tab currently being edited. Returns null if no match.
     */
    private static TabBase findTabForConfigKey(String configKey) {
        if (configKey == null || configKey.isEmpty()) return null;
        String expected = Character.toUpperCase(configKey.charAt(0)) + configKey.substring(1) + "Tab";
        for (ScreenInfo info : tabsScreens.values()) {
            for (List<TabBase> list : info.tabs.values()) {
                for (TabBase tab : list) {
                    if (tab.getClass().getSimpleName().equals(expected)) {
                        return tab;
                    }
                }
            }
        }
        return null;
    }

    private static TabDisplayVisibility getTabDisplayVisibilityForScreen(Screen screen) {
        String key = getConfigKeyForScreen(screen);
        if (key == null) return TabDisplayVisibility.YES;
        try {
            java.lang.reflect.Field f = ModTabsConfig.class.getField(key + "TabDisplayVisibility");
            Object v = f.get(null);
            if (v instanceof TabDisplayVisibility tdv) return tdv;
        } catch (NoSuchFieldException | IllegalAccessException ignored) {}
        return TabDisplayVisibility.YES;
    }

    /**
     * Re-reads visibility for the given screen from config and rebuilds {@code isInTuckMode}
     * / {@code animationManager} accordingly. Call this after writing a new TabDisplayVisibility
     * value so the tabs respond without needing a full screen re-init.
     */
    public static void refreshTuckModeForScreen(Screen screen) {
        if (screen == null) return;
        TabDisplayVisibility visibility = getTabDisplayVisibilityForScreen(screen);
        boolean shouldTuck = (visibility == TabDisplayVisibility.TUCK);
        if (shouldTuck && !isInTuckMode) {
            animationManager = new TabBarAnimationManager();
        } else if (!shouldTuck) {
            animationManager = null;
        }
        isInTuckMode = shouldTuck;
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
        int[] b = computeTabBarBounds();
        double[] m = inverseRotateMouseToBarFrame(mouseX, mouseY, b);
        int left = b[0], top = b[1], right = b[2], bottom = b[3];
        // Extend by the FULL tuck distance in the user's chosen direction so the cursor
        // stays inside the hover zone for the entire animate-in cycle (using the live
        // animation offset would shrink and pop the cursor out, causing flicker).
        if (isInTuckMode) {
            int[] tuck = computeTuckUnrotatedDirection(0.6 * effectiveTabHeight());
            int tx = tuck[0];
            int ty = tuck[1];
            if (tx > 0) right += tx; else left += tx;
            if (ty > 0) bottom += ty; else top += ty;
        }
        if (m[0] >= left && m[0] <= right && m[1] >= top && m[1] <= bottom) {
            return true;
        }
        // Also count the next-page chevron: it sits outside the bar bounds (slot -1 or N)
        // but is functionally part of the bar — hovering it should reveal the tabs.
        for (var child : screen.children()) {
            if (child instanceof vodmordia.modtabs.client.screens.NextTabsButton btn
                    && btn.isMouseOver(mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    private static TabDisplayMode currentDisplayMode = TabDisplayMode.NORMAL;

    /**
     * Tuck offset in the bar's *unrotated* frame. The direction the tabs slide in
     * screen-space is set per-screen via {@link vodmordia.modtabs.layout.TuckDirection};
     * we inverse-rotate that screen-space vector through the current bar rotation to
     * get the equivalent unrotated-frame offset, so that R(rotation) · v lands at the
     * intended screen direction. {@code magnitude} is the full tuck distance (cross
     * size × 0.6); the live animation factor is folded in at the call site.
     */
    private static int[] computeTuckUnrotatedDirection(double magnitude) {
        Screen s = Minecraft.getInstance().screen;
        vodmordia.modtabs.layout.TuckDirection dir = (s != null)
                ? ScreenLayoutStore.get(s.getClass()).tuckDirection
                : vodmordia.modtabs.layout.TuckDirection.DOWN;
        if (dir == null) dir = vodmordia.modtabs.layout.TuckDirection.DOWN;
        // Screen-space target direction, then inverse-rotate by the bar's rotation
        // to land it in the unrotated frame.
        double sx = dir.dx();
        double sy = dir.dy();
        double rad = Math.toRadians(currentEffectiveRotation());
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        // R(-θ) · (sx, sy) = (sx·cos + sy·sin, -sx·sin + sy·cos)
        int offX = (int) Math.round(magnitude * (sx * cos + sy * sin));
        int offY = (int) Math.round(magnitude * (-sx * sin + sy * cos));
        return new int[]{offX, offY};
    }

    private static int[] computeTuckOffsetVector() {
        if (animationManager == null || !isInTuckMode) return ZERO_OFFSET;
        return computeTuckUnrotatedDirection(animationManager.getOffsetFactor() * effectiveTabHeight());
    }

    private static final int[] ZERO_OFFSET = new int[]{0, 0};

    public static int getAnimatedYOffset() {
        return computeTuckOffsetVector()[1];
    }

    public static int getAnimatedXOffset() {
        return computeTuckOffsetVector()[0];
    }

    public static void updateButtonsPosition(Screen screen, int guiLeft, int guiTop) {
        ScreenLayout layout = ScreenLayoutStore.get(screen.getClass());
        // Anchor decides the reference frame: GUI_RELATIVE tracks the GUI's top-left
        // (so the bar follows things like recipe book opening); SCREEN_ABSOLUTE pins
        // the bar to a fixed screen position regardless of GUI movement.
        int newLeft, newTop;
        if (layout.anchor == vodmordia.modtabs.layout.Anchor.SCREEN_ABSOLUTE) {
            newLeft = layout.offsetX;
            newTop = layout.offsetY;
        } else {
            newLeft = guiLeft + layout.offsetX;
            newTop = guiTop + layout.offsetY;
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
        pendingScreenRegistrations.add(new ScreenRegistration(screen, screenWidth, screenHeight, displayMode));
    }

    public static void forceRegisterScreenWithAllTabs(Class<? extends Screen> screen, Function<Player, Integer> screenWidth, Function<Player, Integer> screenHeight, TabDisplayMode displayMode) {
        pendingScreenRegistrations.removeIf(reg -> reg.screenClass.equals(screen));
        pendingScreenRegistrations.add(new ScreenRegistration(screen, screenWidth, screenHeight, displayMode));
    }

    public static void finalizePendingRegistrations() {
        // Process all pending screen registrations now that all tabs are registered
        for (ScreenRegistration registration : pendingScreenRegistrations) {
            if (!tabsScreens.containsKey(registration.screenClass)) {
                ScreenInfo screenInfo = new ScreenInfo(registration.screenWidth, registration.screenHeight, registration.displayMode);
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

            // Check tab display visibility for this screen type. NO hides tabs from the
            // bar but we still register the editor widgets so the user can enter the
            // layout editor (Shift+Z) and flip visibility back to YES/TUCK. The editor
            // widgets self-gate on isEditing — they don't render in play mode.
            TabDisplayVisibility visibility = getTabDisplayVisibilityForScreen(event.getScreen());
            if (visibility == TabDisplayVisibility.NO) {
                vodmordia.modtabs.client.screens.LayoutEditorButtons.addToScreen(
                        event.getScreen(), event::addListener);
                return;
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

            TabDisplayMode effectiveDisplayMode = TabDisplayMode.NORMAL;
            currentDisplayMode = effectiveDisplayMode;

            // Bar position is fully data-driven via the layout JSON's anchor + offsets.
            // SCREEN_ABSOLUTE = raw screen coords; GUI_RELATIVE = offsets from the GUI's
            // centered top-left (using the screen's registered width/height).
            ScreenLayout layout = ScreenLayoutStore.get(event.getScreen().getClass());
            if (layout.anchor == vodmordia.modtabs.layout.Anchor.SCREEN_ABSOLUTE) {
                TabsMenu.leftScreenPos = layout.offsetX;
                TabsMenu.topScreenPos = layout.offsetY;
            } else {
                int guiLeft = (event.getScreen().width - screenInfo.width.apply(Minecraft.getInstance().player)) / 2;
                int guiTop = (event.getScreen().height - screenInfo.height.apply(Minecraft.getInstance().player)) / 2;
                TabsMenu.leftScreenPos = guiLeft + layout.offsetX;
                TabsMenu.topScreenPos = guiTop + layout.offsetY;
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

            // Dynamic providers contribute transient tabs (e.g. nearby chests). We collect
            // them into a side list rather than appending straight to enabledTabs because
            // the pagination logic further down may need to slot them into the end of page 1
            // (displacing static tabs to page 2) rather than the very tail of the bar.
            List<TabBase> dynamicTabs = new ArrayList<>();
            if (!dynamicTabProviders.isEmpty()) {
                Player dynPlayer = Minecraft.getInstance().player;
                Screen dynScreen = event.getScreen();
                for (DynamicTabProvider provider : dynamicTabProviders) {
                    try {
                        provider.contribute(dynPlayer, dynScreen, dynamicTabs);
                    } catch (Exception ex) {
                        vodmordia.modtabs.ModTabs.LOGGER.warn(
                                "DynamicTabProvider {} threw during contribute(): {}",
                                provider.getClass().getName(), ex.toString());
                    }
                }
            }
            // Default placement: at the tail, after all static tabs. Pagination overflow
            // is handled below by re-inserting dynamics into the page-1 tail.
            enabledTabs.addAll(dynamicTabs);

            // Handle sticky tabs - any tab flagged sticky is pinned to the leading end of the
            // bar and stays visible across pagination. enabledTabs is already sorted, so sticky
            // tabs keep their relative order among themselves, as do the non-sticky tabs.
            List<TabBase> stickyTabs = new ArrayList<>();
            List<TabBase> nonStickyTabs = new ArrayList<>();
            for (TabBase tab : enabledTabs) {
                if (tab.isSticky()) {
                    stickyTabs.add(tab);
                } else {
                    nonStickyTabs.add(tab);
                }
            }
            int stickyCount = stickyTabs.size();


            // Pagination is now driven purely by the per-screen layout cap
            // (layout.maxTabsPerPage). Auto-fit-by-axis-space was removed because it
            // made the visible count drift on window resize — the user wants the
            // explicit cap to be the only knob.
            ScreenLayout savedLayout = ScreenLayoutStore.get(event.getScreen().getClass());
            int totalEnabledIncludingSticky = enabledTabs.size();
            // (enabledTabs already contains the inventory tab whether or not sticky mode is on;
            // it just gets pulled to the front separately below.)
            int perPageCap = savedLayout.maxTabsPerPage;
            currentTabsCount = (perPageCap > 0)
                    ? Math.min(totalEnabledIncludingSticky, perPageCap)
                    : totalEnabledIncludingSticky;

            // When pagination overflows AND we have dynamic tabs, pull them out of the bar's
            // tail and re-insert at the page-1 tail so they're visible on first open rather
            // than buried on a later page. Static tabs that would have occupied those slots
            // are bumped to page 2+. No-op when there's no overflow (everything fits anyway).
            if (!dynamicTabs.isEmpty() && perPageCap > 0
                    && nonStickyTabs.size() > perPageCap - stickyCount) {
                int nonStickyPage1Slots = Math.max(0, perPageCap - stickyCount);
                int targetPos = Math.max(0, nonStickyPage1Slots - dynamicTabs.size());
                // Dynamics are currently at the tail of nonStickyTabs (and of enabledTabs).
                // Pull them out and re-insert at the page-1 boundary.
                nonStickyTabs.removeAll(dynamicTabs);
                targetPos = Math.min(targetPos, nonStickyTabs.size());
                nonStickyTabs.addAll(targetPos, dynamicTabs);
                // Keep enabledTabs in sync with the displayed order (sticky tabs first, then
                // the rest) so cycleToNextTab and nextTabButtons see the same sequence.
                enabledTabs.clear();
                enabledTabs.addAll(stickyTabs);
                enabledTabs.addAll(nonStickyTabs);
            }


            // Sweep both children AND renderables for any leftover TabButton or
            // NextTabsButton from a prior init. Skipping renderables for NextTabsButton
            // would leave a ghost chevron behind on save (e.g. raising the per-page cap
            // re-inits, the old chevron stays in renderables, the new one is added,
            // and the user sees two arrows).
            event.getScreen().children().removeIf(child ->
                child instanceof TabButton || child instanceof NextTabsButton);
            event.getScreen().renderables.removeIf(renderable ->
                renderable instanceof TabButton || renderable instanceof NextTabsButton);


            // Second pass: create buttons for the correct range starting from startTabIndex.
            // When tabOrder is RIGHT_TO_LEFT we flip every visual slot index to (count - 1 - i)
            // so that "first" tabs land at the rightmost slot of the unrotated bar — combined
            // with a 180° bar rotation, that puts inventory back on the screen-left after rotation.
            int buttonPosition = 0;
            boolean reverseOrder = layout.tabOrder == vodmordia.modtabs.layout.TabOrder.RIGHT_TO_LEFT;

            // Sticky tabs always render first, pinned to the leading slots of the bar.
            for (TabBase stickyTab : stickyTabs) {
                if (buttonPosition >= currentTabsCount) break;
                int slot = reverseOrder ? (currentTabsCount - 1 - buttonPosition) : buttonPosition;
                TabButton stickyButton = new TabButton(stickyTab, Minecraft.getInstance().player, event.getScreen(), slot, TabsMenu.leftScreenPos, TabsMenu.topScreenPos, effectiveDisplayMode);
                event.addListener(stickyButton);
                buttonPosition++;
            }

            // Remaining slots after the sticky tabs are paginated among the non-sticky tabs.
            int availableSlots = Math.max(0, currentTabsCount - stickyCount);

            for (int tabIndex = 0; tabIndex < nonStickyTabs.size() && buttonPosition < currentTabsCount; tabIndex++) {
                TabBase tabBase = nonStickyTabs.get(tabIndex);
                int tabIndexToShow = tabIndex - startTabIndex;

                if (tabIndexToShow >= 0 && tabIndexToShow < availableSlots) {
                    int slot = reverseOrder ? (currentTabsCount - 1 - buttonPosition) : buttonPosition;
                    TabButton newButton = new TabButton(tabBase, Minecraft.getInstance().player, event.getScreen(), slot, TabsMenu.leftScreenPos, TabsMenu.topScreenPos, effectiveDisplayMode);
                    event.addListener(newButton);
                    buttonPosition++;
                }
            }

            // Next button is needed when the non-sticky tabs overflow the remaining slots.
            int totalTabsToPage = nonStickyTabs.size();
            int maxVisibleTabs = availableSlots;

            if (totalTabsToPage > maxVisibleTabs) {
                // Next-page chevron sits at the trailing end of the bar — index (count-1) for
                // LEFT_TO_RIGHT, or index 0 (the natural left end) for RIGHT_TO_LEFT.
                int nextSlot = reverseOrder ? -1 : currentTabsCount;
                event.addListener(new NextTabsButton(nextSlot, TabsMenu.leftScreenPos, TabsMenu.topScreenPos, effectiveDisplayMode,
                        button -> nextTabButtons(event.getScreen())));
            }

            // Layout editor controls (always added when tabs are visible on this screen)
            event.getScreen().children().removeIf(child ->
                child instanceof vodmordia.modtabs.client.screens.LayoutEditorButtons.EditToggle ||
                child instanceof vodmordia.modtabs.client.screens.LayoutEditorButtons.EditOnly ||
                child instanceof vodmordia.modtabs.client.screens.LayoutEditorButtons.IconScaleEditBox ||
                child instanceof vodmordia.modtabs.client.screens.LayoutEditorButtons.IconNudgeEditBox ||
                child instanceof vodmordia.modtabs.client.screens.LayoutEditorButtons.MaxTabsPerPageEditBox);
            event.getScreen().renderables.removeIf(r ->
                r instanceof vodmordia.modtabs.client.screens.LayoutEditorButtons.EditToggle ||
                r instanceof vodmordia.modtabs.client.screens.LayoutEditorButtons.EditOnly ||
                r instanceof vodmordia.modtabs.client.screens.LayoutEditorButtons.IconScaleEditBox ||
                r instanceof vodmordia.modtabs.client.screens.LayoutEditorButtons.IconNudgeEditBox ||
                r instanceof vodmordia.modtabs.client.screens.LayoutEditorButtons.MaxTabsPerPageEditBox);
            vodmordia.modtabs.client.screens.LayoutEditorButtons.addToScreen(event.getScreen(), event::addListener);
        }
    }

    public static void nextTabButtons(Screen screen) {
        List<? extends GuiEventListener> tabButtons = screen.children().stream().filter(button -> button instanceof TabButton).toList();

        // Sticky tabs occupy the first stickyCount buttons and don't paginate; only the
        // non-sticky tabs cycle through the remaining slots. With no sticky tabs this
        // reduces to plain pagination over the whole enabled list.
        List<TabBase> stickyTabs = new ArrayList<>();
        List<TabBase> nonStickyTabs = new ArrayList<>();
        for (TabBase tab : enabledTabs) {
            if (tab.isSticky()) {
                stickyTabs.add(tab);
            } else {
                nonStickyTabs.add(tab);
            }
        }

        int stickyCount = stickyTabs.size();
        int availableSlots = Math.max(0, currentTabsCount - stickyCount);
        if (availableSlots <= 0) {
            return; // sticky tabs fill the page — nothing to page through
        }

        if (startTabIndex + availableSlots >= nonStickyTabs.size())
            startTabIndex = 0;
        else
            startTabIndex += availableSlots + Math.min(nonStickyTabs.size() - availableSlots * 2 - startTabIndex, 0);

        // Update buttons: skip the leading sticky buttons.
        int buttonStartIndex = stickyCount;
        int currentTabIndex = 0;

        for (TabBase tabBase: nonStickyTabs) {
            int tabIndexToUpdate = currentTabIndex - startTabIndex;
            if (tabIndexToUpdate >= availableSlots)
                break;

            if (tabIndexToUpdate >= 0 && buttonStartIndex + tabIndexToUpdate < tabButtons.size())
                ((TabButton) tabButtons.get(buttonStartIndex + tabIndexToUpdate)).setTabBase(tabBase);

            currentTabIndex++;
        }
    }

    public static void register(TabBase tabBase) {
        allRegisteredTabs.add(tabBase);
        tabBase.initTabOnScreens();
    }

    /**
     * Register a source of transient tabs (e.g. nearby chests). Providers run during
     * {@link #initScreenButtons} after the static-tab list is built and append directly to it,
     * so dynamic tabs share the same sort / pagination / visibility / tuck pipeline.
     */
    public static void registerDynamicProvider(DynamicTabProvider provider) {
        dynamicTabProviders.add(provider);
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

    /** Package-private view of the screen registry for {@link GlobalSettingsPanel}. */
    static java.util.Collection<ScreenInfo> screenInfos() {
        return tabsScreens.values();
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

        // Calculate which page the next tab should be on and update startTabIndex.
        // Sticky tabs are always visible (no pagination); only non-sticky tabs page.
        List<TabBase> stickyTabs = new ArrayList<>();
        List<TabBase> nonStickyTabs = new ArrayList<>();
        for (TabBase tab : enabledTabs) {
            if (tab.isSticky()) {
                stickyTabs.add(tab);
            } else {
                nonStickyTabs.add(tab);
            }
        }

        int availableSlots = Math.max(1, currentTabsCount - stickyTabs.size());

        if (nextTab.isSticky()) {
            // Cycling to a sticky tab — it's always on the first page.
            startTabIndex = 0;
        } else {
            int nextNonStickyIndex = nonStickyTabs.indexOf(nextTab);
            if (nextNonStickyIndex != -1) {
                int targetPage = nextNonStickyIndex / availableSlots;
                startTabIndex = targetPage * availableSlots;
            }
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

        public ScreenInfo(Function<Player, Integer> width, Function<Player, Integer> height, TabBase newTab, int priority) {
            this.width = width;
            this.height = height;
            this.tabs = new TreeMap<>();
            this.displayMode = TabDisplayMode.NORMAL;
            this.addTab(priority, newTab);
        }

        public ScreenInfo(Function<Player, Integer> width, Function<Player, Integer> height) {
            this(width, height, TabDisplayMode.NORMAL);
        }

        public ScreenInfo(Function<Player, Integer> width, Function<Player, Integer> height, TabDisplayMode displayMode) {
            this.width = width;
            this.height = height;
            this.tabs = new TreeMap<>();
            this.displayMode = displayMode;
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

        public ScreenRegistration(Class<? extends Screen> screenClass, Function<Player, Integer> screenWidth, Function<Player, Integer> screenHeight) {
            this(screenClass, screenWidth, screenHeight, TabDisplayMode.NORMAL);
        }

        public ScreenRegistration(Class<? extends Screen> screenClass, Function<Player, Integer> screenWidth, Function<Player, Integer> screenHeight, TabDisplayMode displayMode) {
            this.screenClass = screenClass;
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
            this.displayMode = displayMode;
        }
    }
}
