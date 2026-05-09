package vodmordia.modtabs.api.tabs_menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
// (Phase 2 helpers reference TabsMenu state directly; no additional imports beyond above.)
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
     * Renders the screen's TabButtons / NextTabsButton manually, before the GUI panel
     * draws. Called from {@link vodmordia.modtabs.mixin.AbstractContainerScreenMixin}.
     * Skipped while editing — edit mode wants tabs on top so the user can grab handles.
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
        globalSettingsOpen = false;
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

    public static int currentMaxTabsPerPage() {
        Screen s = Minecraft.getInstance().screen;
        if (s == null) return 0;
        return ScreenLayoutStore.get(s.getClass()).maxTabsPerPage;
    }

    /** Persists the per-page cap. The widget triggers re-init on focus-lost so the
     *  bar repaginates once the user is done typing — re-init on every keystroke
     *  would tear down the focused EditBox and reset the value. */
    public static void setMaxTabsPerPage(int value) {
        Screen s = Minecraft.getInstance().screen;
        if (s == null) return;
        ScreenLayout layout = ScreenLayoutStore.get(s.getClass());
        layout.maxTabsPerPage = Math.max(0, value);
        ScreenLayoutStore.save(s.getClass().getName(), layout);
    }

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

    public static void cycleTabOrder() {
        Screen s = Minecraft.getInstance().screen;
        if (s == null) return;
        ScreenLayout layout = ScreenLayoutStore.get(s.getClass());
        if (layout.tabOrder == null) layout.tabOrder = vodmordia.modtabs.layout.TabOrder.LEFT_TO_RIGHT;
        layout.tabOrder = layout.tabOrder.next();
        ScreenLayoutStore.save(s.getClass().getName(), layout);
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
     * the same absolute screen position. Saves immediately and re-fires
     * {@code initScreenButtons} via {@code setScreen}.
     */
    public static void cycleAnchor() {
        Screen s = Minecraft.getInstance().screen;
        if (s == null) return;
        ScreenLayout layout = ScreenLayoutStore.get(s.getClass());
        vodmordia.modtabs.layout.Anchor oldAnchor = (layout.anchor != null)
                ? layout.anchor
                : vodmordia.modtabs.layout.Anchor.GUI_RELATIVE;
        vodmordia.modtabs.layout.Anchor newAnchor = oldAnchor.next();
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

    public static float currentEffectiveRotation() {
        Screen s = Minecraft.getInstance().screen;
        if (s == null) return 0f;
        ScreenLayout layout = ScreenLayoutStore.get(s.getClass());
        return layout.rotation + (isEditing(s) ? tempRotation : 0f);
    }

    public static double[] barCenter() {
        int[] b = computeTabBarBounds();
        return new double[]{(b[0] + b[2]) / 2.0, (b[1] + b[3]) / 2.0};
    }

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

    public static double[] inverseRotatePoint(double x, double y, double cx, double cy, double degrees) {
        return rotatePoint(x, y, cx, cy, -degrees);
    }

    private static double[] primaryAxisUnit() {
        double rad = Math.toRadians(currentEffectiveRotation());
        return new double[]{Math.cos(rad), Math.sin(rad)};
    }

    public static int currentEffectiveTabSpacing() {
        Screen s = Minecraft.getInstance().screen;
        if (s == null) return 1;
        ScreenLayout layout = ScreenLayoutStore.get(s.getClass());
        return layout.tabSpacing + (isEditing(s) ? tempSpacingDelta : 0);
    }

    public static int primaryAxisStep() {
        return Math.round(TAB_WIDTH * currentEffectiveScale()) + currentEffectiveTabSpacing();
    }

    public static int effectiveTabWidth() {
        return Math.round(TAB_WIDTH * currentEffectiveScale());
    }

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
        // the 9-arg ctor doesn't set them, and clobbering each one caused observable bugs.
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
        // skill screens, etc.) pick up the new position immediately.
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

    private static int handleHitTest(double mouseX, double mouseY) {
        int[] b = computeTabBarBounds();
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
        if (within(mx, my, midX, ft - ROTATION_HANDLE_OFFSET, rotHit)) return 6;
        // Only test handle 7 when an actual next button exists, otherwise a click near
        // (0,0) would hit it (nextButtonScreenCenter falls back to (0,0) without one).
        Screen current = Minecraft.getInstance().screen;
        boolean hasNextButton = current != null
                && current.children().stream().anyMatch(c -> c instanceof NextTabsButton);
        if (hasNextButton) {
            double[] nbCenter = nextButtonScreenCenter();
            float totalNextRot = currentEffectiveRotation() + currentNextEffectiveRotation();
            double rad = Math.toRadians(totalNextRot - 90);
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

        // Pass 1: dim + tabs at Z=400. ItemRenderer pushes +150 internally so item icons
        // land at Z≈550 — above the player model (<400) but below the panel layer below.
        gui.pose().pushPose();
        gui.pose().translate(0, 0, 400);

        gui.fill(0, 0, sw, sh, dim);

        // Anchor outline: thin green rect around the host GUI box (when GUI_RELATIVE)
        // or the screen edges (when SCREEN_ABSOLUTE).
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

        // Pass 2: decorations at Z=600 so they sit above 3D-rendered item icons (Z≈550).
        gui.pose().pushPose();
        gui.pose().translate(0, 0, 600);

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

        int rotHandleX = midX;
        int rotHandleY = ft - ROTATION_HANDLE_OFFSET;
        gui.fill(midX, rotHandleY + 1, midX + 1, ft, green);
        drawRotationHandle(gui, rotHandleX, rotHandleY, hovered == 6);

        gui.pose().popPose();

        // Next-button rotation handle — only when a next button actually exists, otherwise
        // nextButtonScreenCenter falls back to (0,0) and parks a stray purple disc in the
        // upper-left corner.
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

        drawOptionsPanel(gui, screen);

        gui.pose().popPose();

        // Pass 3: panel controls at Z=700 — must be ABOVE the panel BG (Z=600).
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

        // Pass 4: global settings modal (Z=900) — sits above everything else.
        if (globalSettingsOpen) {
            gui.pose().pushPose();
            gui.pose().translate(0, 0, 900);
            renderGlobalSettings(gui, screen, mouseX, mouseY);
            gui.pose().popPose();
        }
    }

    public static final int PANEL_W = 230;
    public static final int PANEL_H = 224;
    public static final int PANEL_TITLE_H = 14;
    public static final int PANEL_ROW_H = 16;
    public static final int PANEL_LABEL_W = 78;
    public static final int PANEL_PAD = 6;
    public static final int PANEL_PREVIEW_H = 28;
    public static final int PANEL_HANDLE_W = 12;
    public static final int PANEL_HANDLE_H = 40;
    private static final int PANEL_MARGIN_X = 8;

    private static boolean panelCollapsed = false;

    public static int currentPanelX(Screen screen) {
        if (panelCollapsed) return -PANEL_W;
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

    // ============================================================================
    // Global settings (cogwheel) — modal window editing per-tab visibility & order
    // GLOBALLY (across all screens). Bound to the in-edit-mode cogwheel button.
    // ============================================================================
    public enum GlobalSettingsTab { VISIBILITY, ORDER, GENERAL }
    private enum GsField { NONE, OFFSET_TOP, OFFSET_RIGHT, OFFSET_BOTTOM, OFFSET_LEFT }
    private static GsField gsFocusedField = GsField.NONE;
    private static String gsDraftOffsetTop = "0";
    private static String gsDraftOffsetRight = "0";
    private static String gsDraftOffsetBottom = "0";
    private static String gsDraftOffsetLeft = "0";
    private static boolean globalSettingsOpen = false;
    private static GlobalSettingsTab gsActiveTab = GlobalSettingsTab.VISIBILITY;
    private static java.util.Map<String, Boolean> gsDraftEnabled = null;
    private static java.util.List<String> gsDraftOrder = null;
    /** Snapshot of the configurable tabs at modal-open time, keyed by short configKey. */
    private static java.util.Map<String, TabBase> gsTabsCache = null;
    private static int gsScrollVisibility = 0;
    private static int gsScrollOrder = 0;
    private static int gsDraggingIndex = -1;
    private static double gsDragMouseX = 0, gsDragMouseY = 0;

    private static final int GS_NAV_W = 90;
    private static final int GS_HEADER_H = 18;
    private static final int GS_FOOTER_H = 24;
    private static final int GS_NAV_BTN_H = 18;
    private static final int GS_PAD = 8;
    private static final int GS_CELL = 28;
    private static final int GS_FOOTER_BTN_W = 56;
    private static final int GS_FOOTER_BTN_H = 16;

    private static final ResourceLocation COGWHEEL_TEX =
            new ResourceLocation(ModTabs.MOD_ID, "textures/gui/cogwheel.png");

    public static ResourceLocation cogwheelTexture() { return COGWHEEL_TEX; }
    public static boolean isGlobalSettingsOpen() { return globalSettingsOpen; }

    private static String shortConfigKey(TabBase tab) {
        TabConfig tc = tab.getClass().getAnnotation(TabConfig.class);
        if (tc == null) return null;
        String k = tc.configKey();
        return k.endsWith("Tab") ? k.substring(0, k.length() - 3) : k;
    }

    private static java.util.List<TabBase> collectConfigurableTabs() {
        java.util.LinkedHashMap<Class<?>, TabBase> seen = new java.util.LinkedHashMap<>();
        for (ScreenInfo info : tabsScreens.values()) {
            for (List<TabBase> list : info.tabs.values()) {
                for (TabBase tab : list) {
                    if (tab.getClass().isAnnotationPresent(TabConfig.class) && isTabAvailable(tab)) {
                        seen.putIfAbsent(tab.getClass(), tab);
                    }
                }
            }
        }
        return new java.util.ArrayList<>(seen.values());
    }

    /**
     * "Available" = the tab's mod is loaded. Many integration tabs register their screens
     * unconditionally but their {@code isEnabled(player)} short-circuits via a
     * {@code ModIntegrationManager.isModLoaded(...)} check. We probe by temporarily
     * forcing the user-enabled flag on, calling {@code isEnabled}, and restoring the flag.
     * If it returns false, the mod isn't present and the tab can never appear in any row.
     */
    private static boolean isTabAvailable(TabBase tab) {
        String key = shortConfigKey(tab);
        if (key == null) return false;
        Player p = Minecraft.getInstance().player;
        if (p == null) return true;
        java.lang.reflect.Field f;
        try {
            f = Config.Baked.class.getField(key + "TabEnabled");
        } catch (NoSuchFieldException e) {
            return true;
        }
        try {
            boolean original = f.getBoolean(null);
            try {
                f.setBoolean(null, true);
                return tab.isEnabled(p);
            } finally {
                f.setBoolean(null, original);
            }
        } catch (IllegalAccessException e) {
            return true;
        }
    }

    private static boolean readEnabledField(String shortKey) {
        try { return ModTabsConfig.class.getField(shortKey + "TabEnabled").getBoolean(null); }
        catch (Exception ignored) { return true; }
    }
    private static int readOrderField(String shortKey) {
        try { return ModTabsConfig.class.getField(shortKey + "TabOrder").getInt(null); }
        catch (Exception ignored) { return 0; }
    }
    private static void writeEnabledField(String shortKey, boolean value) {
        try { ModTabsConfig.class.getField(shortKey + "TabEnabled").setBoolean(null, value); }
        catch (Exception ignored) {}
    }
    private static void writeOrderField(String shortKey, int value) {
        try { ModTabsConfig.class.getField(shortKey + "TabOrder").setInt(null, value); }
        catch (Exception ignored) {}
    }

    private static int parseSafeInt(String s, int fallback) {
        if (s == null || s.isEmpty() || s.equals("-")) return fallback;
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return fallback; }
    }

    public static void openGlobalSettings() {
        gsDraftEnabled = new java.util.LinkedHashMap<>();
        gsDraftOrder = new java.util.ArrayList<>();
        gsTabsCache = new java.util.LinkedHashMap<>();
        java.util.List<TabBase> tabs = collectConfigurableTabs();
        tabs.sort(java.util.Comparator.<TabBase>comparingInt(t -> readOrderField(shortConfigKey(t)))
                .thenComparing(t -> t.getClass().getSimpleName()));
        for (TabBase t : tabs) {
            String k = shortConfigKey(t);
            if (k == null) continue;
            gsDraftEnabled.put(k, readEnabledField(k));
            gsDraftOrder.add(k);
            gsTabsCache.put(k, t);
        }
        gsActiveTab = GlobalSettingsTab.VISIBILITY;
        gsDraggingIndex = -1;
        gsFocusedField = GsField.NONE;
        gsDraftOffsetTop = String.valueOf(Config.Baked.iconOffsetTop);
        gsDraftOffsetRight = String.valueOf(Config.Baked.iconOffsetRight);
        gsDraftOffsetBottom = String.valueOf(Config.Baked.iconOffsetBottom);
        gsDraftOffsetLeft = String.valueOf(Config.Baked.iconOffsetLeft);
        globalSettingsOpen = true;
    }

    public static void closeGlobalSettings(boolean save) {
        boolean configChanged = false;
        if (save && gsDraftEnabled != null && gsDraftOrder != null) {
            for (var entry : gsDraftEnabled.entrySet()) {
                writeEnabledField(entry.getKey(), entry.getValue());
            }
            // Write 1-based indices: the existing sort comparator treats order==0 as
            // "no explicit override" and banishes the tab to an alphabetical bucket.
            for (int i = 0; i < gsDraftOrder.size(); i++) {
                writeOrderField(gsDraftOrder.get(i), i + 1);
            }
            // General tab settings
            ModTabsConfig.iconOffsetTop = parseSafeInt(gsDraftOffsetTop, 0);
            ModTabsConfig.iconOffsetRight = parseSafeInt(gsDraftOffsetRight, 0);
            ModTabsConfig.iconOffsetBottom = parseSafeInt(gsDraftOffsetBottom, 0);
            ModTabsConfig.iconOffsetLeft = parseSafeInt(gsDraftOffsetLeft, 0);
            ModTabsConfig.write("modtabs");
            Config.Baked.bakeClient();
            configChanged = true;
        }
        globalSettingsOpen = false;
        gsDraftEnabled = null;
        gsDraftOrder = null;
        gsTabsCache = null;
        gsDraggingIndex = -1;
        gsScrollVisibility = 0;
        gsScrollOrder = 0;
        gsFocusedField = GsField.NONE;
        // Force a re-init of the current screen so its tab row picks up the new
        // enabled/order config — TabButton instances are added to screen.children
        // at Init.Post time and won't reflect later config writes otherwise.
        if (configChanged) {
            Screen current = Minecraft.getInstance().screen;
            if (current != null) {
                Minecraft.getInstance().setScreen(current);
            }
        }
    }

    private static TabBase tabForKey(String shortKey) {
        if (gsTabsCache != null) return gsTabsCache.get(shortKey);
        for (TabBase t : collectConfigurableTabs()) {
            if (shortKey.equals(shortConfigKey(t))) return t;
        }
        return null;
    }

    private static int[] gsWindowRect(Screen screen) {
        int sw = screen.width;
        int sh = screen.height;
        int w = (int)(sw * 0.75);
        int h = Math.min((int)(sh * 0.85), Math.max(160, sh - 40));
        int x = (sw - w) / 2;
        int y = (sh - h) / 2;
        return new int[]{x, y, w, h};
    }

    private static int[] gsContentRect(Screen screen) {
        int[] win = gsWindowRect(screen);
        int x = win[0] + GS_NAV_W;
        int y = win[1] + GS_HEADER_H;
        int w = win[2] - GS_NAV_W - 1;
        int h = win[3] - GS_HEADER_H - GS_FOOTER_H;
        return new int[]{x, y, w, h};
    }

    private static int[] gsNavButtonRect(Screen screen, int index) {
        int[] win = gsWindowRect(screen);
        int x = win[0] + 4;
        int y = win[1] + GS_HEADER_H + 4 + index * (GS_NAV_BTN_H + 2);
        return new int[]{x, y, GS_NAV_W - 8, GS_NAV_BTN_H};
    }

    private static int[] gsSaveButtonRect(Screen screen) {
        int[] win = gsWindowRect(screen);
        int x = win[0] + win[2] - GS_FOOTER_BTN_W - 6;
        int y = win[1] + win[3] - GS_FOOTER_BTN_H - 5;
        return new int[]{x, y, GS_FOOTER_BTN_W, GS_FOOTER_BTN_H};
    }
    private static int[] gsCancelButtonRect(Screen screen) {
        int[] win = gsWindowRect(screen);
        int x = win[0] + win[2] - GS_FOOTER_BTN_W * 2 - 12;
        int y = win[1] + win[3] - GS_FOOTER_BTN_H - 5;
        return new int[]{x, y, GS_FOOTER_BTN_W, GS_FOOTER_BTN_H};
    }

    private static int[] gsVisibilitySlotRect(Screen screen, int column, int slotIndex) {
        int[] cr = gsContentRect(screen);
        int colW = cr[2] / 2;
        int colX = cr[0] + column * colW + GS_PAD;
        int colY = cr[1] + GS_HEADER_H;
        int cellsPerRow = Math.max(1, (colW - GS_PAD * 2) / GS_CELL);
        int row = slotIndex / cellsPerRow;
        int col = slotIndex % cellsPerRow;
        return new int[]{colX + col * GS_CELL, colY + row * GS_CELL - gsScrollVisibility, GS_CELL, GS_CELL};
    }

    private static int[] gsOrderSlotRect(Screen screen, int slotIndex) {
        int[] cr = gsContentRect(screen);
        int areaX = cr[0] + GS_PAD;
        int areaY = cr[1] + GS_HEADER_H;
        int areaW = cr[2] - GS_PAD * 2;
        int cellsPerRow = Math.max(1, areaW / GS_CELL);
        int row = slotIndex / cellsPerRow;
        int col = slotIndex % cellsPerRow;
        return new int[]{areaX + col * GS_CELL, areaY + row * GS_CELL - gsScrollOrder, GS_CELL, GS_CELL};
    }

    private static int gsMaxScrollVisibility(Screen screen) {
        if (gsDraftOrder == null) return 0;
        int[] cr = gsContentRect(screen);
        int colW = cr[2] / 2;
        int cellsPerRow = Math.max(1, (colW - GS_PAD * 2) / GS_CELL);
        int visCount = 0, hidCount = 0;
        for (String key : gsDraftOrder) {
            if (Boolean.TRUE.equals(gsDraftEnabled.get(key))) visCount++; else hidCount++;
        }
        int rows = Math.max(rowsFor(visCount, cellsPerRow), rowsFor(hidCount, cellsPerRow));
        int viewportH = cr[3] - GS_HEADER_H;
        return Math.max(0, rows * GS_CELL - viewportH);
    }

    private static int gsMaxScrollOrder(Screen screen) {
        if (gsDraftOrder == null) return 0;
        int[] cr = gsContentRect(screen);
        int areaW = cr[2] - GS_PAD * 2;
        int cellsPerRow = Math.max(1, areaW / GS_CELL);
        int rows = rowsFor(gsDraftOrder.size(), cellsPerRow);
        int viewportH = cr[3] - GS_HEADER_H;
        return Math.max(0, rows * GS_CELL - viewportH);
    }

    private static int rowsFor(int count, int cellsPerRow) {
        return count == 0 ? 0 : (count + cellsPerRow - 1) / cellsPerRow;
    }

    private static int gsOrderHitTest(Screen screen, double mx, double my) {
        int n = gsDraftOrder == null ? 0 : gsDraftOrder.size();
        for (int i = 0; i < n; i++) {
            int[] r = gsOrderSlotRect(screen, i);
            if (mx >= r[0] && mx < r[0] + r[2] && my >= r[1] && my < r[1] + r[3]) return i;
        }
        return -1;
    }

    public static void renderGlobalSettings(GuiGraphics gui, Screen screen, int mouseX, int mouseY) {
        if (!globalSettingsOpen || gsDraftEnabled == null) return;
        int[] win = gsWindowRect(screen);
        int wx = win[0], wy = win[1], ww = win[2], wh = win[3];
        gui.fill(0, 0, screen.width, screen.height, 0xC0000000);
        int bg = 0xF0101418;
        int border = 0xFF44FF66;
        gui.fill(wx, wy, wx + ww, wy + wh, bg);
        gui.fill(wx, wy, wx + ww, wy + 1, border);
        gui.fill(wx, wy + wh - 1, wx + ww, wy + wh, border);
        gui.fill(wx, wy, wx + 1, wy + wh, border);
        gui.fill(wx + ww - 1, wy, wx + ww, wy + wh, border);
        gui.drawString(Minecraft.getInstance().font, "Global Settings", wx + GS_PAD, wy + 5, 0xFFCCFFCC, false);
        gui.fill(wx + 1, wy + GS_HEADER_H, wx + ww - 1, wy + GS_HEADER_H + 1, border);
        gui.fill(wx + GS_NAV_W, wy + GS_HEADER_H, wx + GS_NAV_W + 1, wy + wh, border);
        String[] navLabels = { "Visibility", "Order", "General" };
        for (int i = 0; i < navLabels.length; i++) {
            int[] r = gsNavButtonRect(screen, i);
            boolean active = (gsActiveTab == GlobalSettingsTab.values()[i]);
            int btnBg = active ? 0xFF1F3322 : 0xFF1A1F23;
            gui.fill(r[0], r[1], r[0] + r[2], r[1] + r[3], btnBg);
            gui.fill(r[0], r[1], r[0] + r[2], r[1] + 1, border);
            gui.fill(r[0], r[1] + r[3] - 1, r[0] + r[2], r[1] + r[3], border);
            gui.fill(r[0], r[1], r[0] + 1, r[1] + r[3], border);
            gui.fill(r[0] + r[2] - 1, r[1], r[0] + r[2], r[1] + r[3], border);
            int textColor = active ? 0xFFCCFFCC : 0xFFAAAAAA;
            gui.drawString(Minecraft.getInstance().font, navLabels[i], r[0] + 6, r[1] + 5, textColor, false);
        }
        if (gsActiveTab == GlobalSettingsTab.VISIBILITY) {
            renderVisibilityTab(gui, screen);
        } else if (gsActiveTab == GlobalSettingsTab.ORDER) {
            renderOrderTab(gui, screen, mouseX, mouseY);
        } else {
            renderGeneralTab(gui, screen);
        }
        int[] sbr = gsSaveButtonRect(screen);
        int[] cbr = gsCancelButtonRect(screen);
        drawFooterButton(gui, sbr, "save", 0xFF1F3322);
        drawFooterButton(gui, cbr, "cancel", 0xFF331F1F);
    }

    private static void drawFooterButton(GuiGraphics gui, int[] r, String label, int bg) {
        int border = 0xFF44FF66;
        gui.fill(r[0], r[1], r[0] + r[2], r[1] + r[3], bg);
        gui.fill(r[0], r[1], r[0] + r[2], r[1] + 1, border);
        gui.fill(r[0], r[1] + r[3] - 1, r[0] + r[2], r[1] + r[3], border);
        gui.fill(r[0], r[1], r[0] + 1, r[1] + r[3], border);
        gui.fill(r[0] + r[2] - 1, r[1], r[0] + r[2], r[1] + r[3], border);
        int tw = Minecraft.getInstance().font.width(label);
        gui.drawString(Minecraft.getInstance().font, label,
                r[0] + (r[2] - tw) / 2, r[1] + 4, 0xFFCCFFCC, false);
    }

    private static void renderVisibilityTab(GuiGraphics gui, Screen screen) {
        int[] cr = gsContentRect(screen);
        int colW = cr[2] / 2;
        int leftColX = cr[0];
        int rightColX = cr[0] + colW;
        gui.fill(rightColX, cr[1], cr[0] + cr[2], cr[1] + cr[3], 0x30FF4444);
        gui.drawString(Minecraft.getInstance().font, "Visible", leftColX + GS_PAD, cr[1] + 4, 0xFFAAFFAA, false);
        gui.drawString(Minecraft.getInstance().font, "Hidden", rightColX + GS_PAD, cr[1] + 4, 0xFFFFAAAA, false);
        gui.fill(leftColX, cr[1] + GS_HEADER_H - 4, cr[0] + cr[2], cr[1] + GS_HEADER_H - 3, 0xFF44FF66);

        gsScrollVisibility = Math.max(0, Math.min(gsScrollVisibility, gsMaxScrollVisibility(screen)));

        gui.enableScissor(cr[0], cr[1] + GS_HEADER_H, cr[0] + cr[2], cr[1] + cr[3]);
        int visIdx = 0, hidIdx = 0;
        for (String key : gsDraftOrder) {
            boolean enabled = Boolean.TRUE.equals(gsDraftEnabled.get(key));
            int[] slot = gsVisibilitySlotRect(screen, enabled ? 0 : 1, enabled ? visIdx++ : hidIdx++);
            renderTabIconAt(gui, key, slot[0], slot[1]);
        }
        gui.disableScissor();
    }

    private static void renderOrderTab(GuiGraphics gui, Screen screen, int mouseX, int mouseY) {
        int[] cr = gsContentRect(screen);
        gui.drawString(Minecraft.getInstance().font, "Drag to reorder",
                cr[0] + GS_PAD, cr[1] + 4, 0xFFAAAAAA, false);
        gui.fill(cr[0], cr[1] + GS_HEADER_H - 4, cr[0] + cr[2], cr[1] + GS_HEADER_H - 3, 0xFF44FF66);

        gsScrollOrder = Math.max(0, Math.min(gsScrollOrder, gsMaxScrollOrder(screen)));

        gui.enableScissor(cr[0], cr[1] + GS_HEADER_H, cr[0] + cr[2], cr[1] + cr[3]);
        for (int i = 0; i < gsDraftOrder.size(); i++) {
            if (i == gsDraggingIndex) continue;
            int[] slot = gsOrderSlotRect(screen, i);
            renderTabIconAt(gui, gsDraftOrder.get(i), slot[0], slot[1]);
        }
        if (gsDraggingIndex >= 0 && gsDraggingIndex < gsDraftOrder.size()) {
            int[] slot = gsOrderSlotRect(screen, gsDraggingIndex);
            int dragX = (int) (mouseX - GS_CELL / 2);
            int dragY = (int) (mouseY - GS_CELL / 2);
            gui.fill(slot[0], slot[1], slot[0] + slot[2], slot[1] + 1, 0xFFFFAA00);
            gui.fill(slot[0], slot[1] + slot[3] - 1, slot[0] + slot[2], slot[1] + slot[3], 0xFFFFAA00);
            gui.fill(slot[0], slot[1], slot[0] + 1, slot[1] + slot[3], 0xFFFFAA00);
            gui.fill(slot[0] + slot[2] - 1, slot[1], slot[0] + slot[2], slot[1] + slot[3], 0xFFFFAA00);
            renderTabIconAt(gui, gsDraftOrder.get(gsDraggingIndex), dragX, dragY);
        }
        gui.disableScissor();
    }

    // ---- General tab ---------------------------------------------------------
    private static final int GS_INPUT_W = 44;
    private static final int GS_INPUT_H = 14;

    private static int[] gsOffsetInputRect(Screen screen, int index) {
        int[] cr = gsContentRect(screen);
        int gap = 6;
        int totalW = GS_INPUT_W * 4 + gap * 3;
        int rowX = cr[0] + (cr[2] - totalW) / 2;
        int rowY = cr[1] + GS_HEADER_H + 18;
        return new int[]{rowX + index * (GS_INPUT_W + gap), rowY, GS_INPUT_W, GS_INPUT_H};
    }

    private static GsField gsHitGeneralInput(Screen screen, double mx, double my) {
        GsField[] offsetFields = { GsField.OFFSET_TOP, GsField.OFFSET_RIGHT, GsField.OFFSET_BOTTOM, GsField.OFFSET_LEFT };
        for (int i = 0; i < 4; i++) {
            int[] r = gsOffsetInputRect(screen, i);
            if (mx >= r[0] && mx < r[0] + r[2] && my >= r[1] && my < r[1] + r[3]) return offsetFields[i];
        }
        return null;
    }

    private static String gsDraftValue(GsField field) {
        switch (field) {
            case OFFSET_TOP: return gsDraftOffsetTop;
            case OFFSET_RIGHT: return gsDraftOffsetRight;
            case OFFSET_BOTTOM: return gsDraftOffsetBottom;
            case OFFSET_LEFT: return gsDraftOffsetLeft;
            default: return "";
        }
    }

    private static void gsSetDraftValue(GsField field, String value) {
        switch (field) {
            case OFFSET_TOP: gsDraftOffsetTop = value; break;
            case OFFSET_RIGHT: gsDraftOffsetRight = value; break;
            case OFFSET_BOTTOM: gsDraftOffsetBottom = value; break;
            case OFFSET_LEFT: gsDraftOffsetLeft = value; break;
            default: break;
        }
    }

    private static void renderGeneralTab(GuiGraphics gui, Screen screen) {
        int[] cr = gsContentRect(screen);
        gui.drawString(Minecraft.getInstance().font, "Icon offset (pixels)",
                cr[0] + GS_PAD, cr[1] + 4, 0xFFAAAAAA, false);
        gui.fill(cr[0], cr[1] + GS_HEADER_H - 4, cr[0] + cr[2], cr[1] + GS_HEADER_H - 3, 0xFF44FF66);

        String[] labels = { "top", "right", "bottom", "left" };
        GsField[] fields = { GsField.OFFSET_TOP, GsField.OFFSET_RIGHT, GsField.OFFSET_BOTTOM, GsField.OFFSET_LEFT };
        for (int i = 0; i < 4; i++) {
            int[] r = gsOffsetInputRect(screen, i);
            int lw = Minecraft.getInstance().font.width(labels[i]);
            gui.drawString(Minecraft.getInstance().font, labels[i],
                    r[0] + (r[2] - lw) / 2, r[1] - 10, 0xFFCCCCCC, false);
            gsRenderInput(gui, r, gsDraftValue(fields[i]), gsFocusedField == fields[i]);
        }
    }

    private static void gsRenderInput(GuiGraphics gui, int[] r, String value, boolean focused) {
        int bg = 0xFF1A1F23;
        int border = focused ? 0xFFFFEE66 : 0xFF44FF66;
        gui.fill(r[0], r[1], r[0] + r[2], r[1] + r[3], bg);
        gui.fill(r[0], r[1], r[0] + r[2], r[1] + 1, border);
        gui.fill(r[0], r[1] + r[3] - 1, r[0] + r[2], r[1] + r[3], border);
        gui.fill(r[0], r[1], r[0] + 1, r[1] + r[3], border);
        gui.fill(r[0] + r[2] - 1, r[1], r[0] + r[2], r[1] + r[3], border);
        String display = value + (focused ? "_" : "");
        int tw = Minecraft.getInstance().font.width(display);
        int textX = r[0] + Math.max(4, r[2] - 4 - tw);
        gui.drawString(Minecraft.getInstance().font, display, textX, r[1] + 3, 0xFFFFFFFF, false);
    }

    public static boolean handleGlobalSettingsCharTyped(char c) {
        if (!globalSettingsOpen || gsFocusedField == GsField.NONE) return false;
        String cur = gsDraftValue(gsFocusedField);
        if (c >= '0' && c <= '9') {
            if (cur.length() < 6) gsSetDraftValue(gsFocusedField, cur + c);
            return true;
        }
        if (c == '-' && cur.isEmpty()) {
            gsSetDraftValue(gsFocusedField, "-");
            return true;
        }
        return false;
    }

    public static boolean handleGlobalSettingsKey(int keyCode) {
        if (!globalSettingsOpen || gsFocusedField == GsField.NONE) return false;
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE) {
            String cur = gsDraftValue(gsFocusedField);
            if (!cur.isEmpty()) gsSetDraftValue(gsFocusedField, cur.substring(0, cur.length() - 1));
            return true;
        }
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER) {
            gsFocusedField = GsField.NONE;
            return true;
        }
        return false;
    }

    private static void renderTabIconAt(GuiGraphics gui, String shortKey, int slotX, int slotY) {
        TabBase tab = tabForKey(shortKey);
        if (tab == null) return;
        int tabOffX = slotX + (GS_CELL - 26) / 2;
        int tabOffY = slotY + (GS_CELL - 22) / 2;
        previewRendering = true;
        try {
            tab.render(gui, tabOffX, tabOffY, false);
        } finally {
            previewRendering = false;
        }
    }

    public static boolean handleGlobalSettingsMouseDown(Screen screen, double mx, double my) {
        if (!globalSettingsOpen) return false;
        int[] sbr = gsSaveButtonRect(screen);
        if (mx >= sbr[0] && mx < sbr[0] + sbr[2] && my >= sbr[1] && my < sbr[1] + sbr[3]) {
            closeGlobalSettings(true);
            return true;
        }
        int[] cbr = gsCancelButtonRect(screen);
        if (mx >= cbr[0] && mx < cbr[0] + cbr[2] && my >= cbr[1] && my < cbr[1] + cbr[3]) {
            closeGlobalSettings(false);
            return true;
        }
        for (int i = 0; i < GlobalSettingsTab.values().length; i++) {
            int[] r = gsNavButtonRect(screen, i);
            if (mx >= r[0] && mx < r[0] + r[2] && my >= r[1] && my < r[1] + r[3]) {
                gsActiveTab = GlobalSettingsTab.values()[i];
                gsDraggingIndex = -1;
                gsScrollVisibility = 0;
                gsScrollOrder = 0;
                gsFocusedField = GsField.NONE;
                return true;
            }
        }
        if (gsActiveTab == GlobalSettingsTab.GENERAL) {
            GsField hit = gsHitGeneralInput(screen, mx, my);
            gsFocusedField = (hit != null) ? hit : GsField.NONE;
            return true;
        }
        if (gsActiveTab == GlobalSettingsTab.VISIBILITY) {
            int visIdx = 0, hidIdx = 0;
            for (String key : gsDraftOrder) {
                boolean enabled = Boolean.TRUE.equals(gsDraftEnabled.get(key));
                int[] slot = gsVisibilitySlotRect(screen, enabled ? 0 : 1, enabled ? visIdx++ : hidIdx++);
                if (mx >= slot[0] && mx < slot[0] + slot[2] && my >= slot[1] && my < slot[1] + slot[3]) {
                    gsDraftEnabled.put(key, !enabled);
                    return true;
                }
            }
        } else {
            int idx = gsOrderHitTest(screen, mx, my);
            if (idx >= 0) {
                gsDraggingIndex = idx;
                gsDragMouseX = mx;
                gsDragMouseY = my;
                return true;
            }
        }
        return true;
    }

    public static boolean handleGlobalSettingsMouseDrag(Screen screen, double mx, double my) {
        if (!globalSettingsOpen) return false;
        if (gsActiveTab == GlobalSettingsTab.ORDER && gsDraggingIndex >= 0) {
            gsDragMouseX = mx;
            gsDragMouseY = my;
            int targetIdx = gsOrderHitTest(screen, mx, my);
            if (targetIdx >= 0 && targetIdx != gsDraggingIndex) {
                String moving = gsDraftOrder.remove(gsDraggingIndex);
                gsDraftOrder.add(targetIdx, moving);
                gsDraggingIndex = targetIdx;
            }
        }
        return true;
    }

    public static boolean handleGlobalSettingsMouseUp(Screen screen, double mx, double my) {
        if (!globalSettingsOpen) return false;
        gsDraggingIndex = -1;
        return true;
    }

    /** Mouse-wheel scroll on the modal. {@code dy} > 0 means scroll up (content moves down). */
    public static boolean handleGlobalSettingsMouseScroll(Screen screen, double mx, double my, double dy) {
        if (!globalSettingsOpen) return false;
        int[] cr = gsContentRect(screen);
        if (mx < cr[0] || mx > cr[0] + cr[2] || my < cr[1] || my > cr[1] + cr[3]) return true;
        int step = GS_CELL;
        if (gsActiveTab == GlobalSettingsTab.VISIBILITY) {
            int max = gsMaxScrollVisibility(screen);
            gsScrollVisibility = Math.max(0, Math.min(max, gsScrollVisibility - (int) (dy * step)));
        } else {
            int max = gsMaxScrollOrder(screen);
            gsScrollOrder = Math.max(0, Math.min(max, gsScrollOrder - (int) (dy * step)));
        }
        return true;
    }

    private static void drawOptionsPanel(GuiGraphics gui, Screen screen) {
        int x = currentPanelX(screen), y = currentPanelY(screen), w = PANEL_W, h = PANEL_H;
        int bg = 0xE8101418;
        int border = 0xFF44FF66;
        gui.fill(x, y, x + w, y + h, bg);
        gui.fill(x, y, x + w, y + 1, border);
        gui.fill(x, y + h - 1, x + w, y + h, border);
        gui.fill(x, y, x + 1, y + h, border);
        gui.fill(x + w - 1, y, x + w, y + h, border);
        gui.drawString(Minecraft.getInstance().font, "Layout Options",
                x + PANEL_PAD, y + 4, 0xFFCCFFCC, false);
        gui.fill(x + 1, y + PANEL_TITLE_H, x + w - 1, y + PANEL_TITLE_H + 1, border);
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
        gui.drawString(Minecraft.getInstance().font, "%",
                x + PANEL_W - PANEL_PAD - 8, scaleRowY + 4, 0xFFCCCCCC, false);

        // Icon-nudge row: four small inputs (U/D/L/R) for per-direction pixel offsets.
        // Letters are drawn here; addToScreen registers the IconNudgeEditBox widgets.
        int nudgeRowY = scaleRowY + PANEL_ROW_H;
        gui.drawString(Minecraft.getInstance().font, "Icon nudge:", x + PANEL_PAD, nudgeRowY + 4, 0xFFCCCCCC, false);
        int relCellX = PANEL_PAD + PANEL_LABEL_W;
        int controlW = PANEL_W - PANEL_PAD - PANEL_LABEL_W - PANEL_PAD;
        int nudgeLetterW = 8;
        int nudgeBoxW = 22;
        int nudgeCellW = nudgeLetterW + nudgeBoxW;
        int nudgePitch = (controlW - nudgeCellW) / 3;
        String[] nudgeLetters = { "U", "D", "L", "R" };
        for (int i = 0; i < 4; i++) {
            int cellX = x + relCellX + i * nudgePitch;
            gui.drawString(Minecraft.getInstance().font, nudgeLetters[i], cellX, nudgeRowY + 4, 0xFFCCCCCC, false);
        }

        // Anchor row: GUI/SCREEN toggle. The corresponding outline (GUI box vs screen
        // edges) is drawn during pass 1 of renderEditModeOverlay so the user sees the
        // chosen frame.
        int anchorRowY = nudgeRowY + PANEL_ROW_H;
        gui.drawString(Minecraft.getInstance().font, "Anchor:", x + PANEL_PAD, anchorRowY + 4, 0xFFCCCCCC, false);

        // Tab Order row: L→R vs R→L visual ordering of tabs in the bar.
        int tabOrderRowY = anchorRowY + PANEL_ROW_H;
        gui.drawString(Minecraft.getInstance().font, "Tab order:", x + PANEL_PAD, tabOrderRowY + 4, 0xFFCCCCCC, false);

        // Max tabs/page row: per-screen pagination cap. 0 = unlimited (single page).
        int maxTabsRowY = tabOrderRowY + PANEL_ROW_H;
        gui.drawString(Minecraft.getInstance().font, "Tabs/page:", x + PANEL_PAD, maxTabsRowY + 4, 0xFFCCCCCC, false);

        int hx = panelHandleX(screen);
        int hy = panelHandleY(screen);
        gui.fill(hx, hy, hx + PANEL_HANDLE_W, hy + PANEL_HANDLE_H, bg);
        gui.fill(hx, hy, hx + PANEL_HANDLE_W, hy + 1, border);
        gui.fill(hx, hy + PANEL_HANDLE_H - 1, hx + PANEL_HANDLE_W, hy + PANEL_HANDLE_H, border);
        gui.fill(hx + PANEL_HANDLE_W - 1, hy, hx + PANEL_HANDLE_W, hy + PANEL_HANDLE_H, border);
        String arrow = panelCollapsed ? ">" : "<";
        int aw = Minecraft.getInstance().font.width(arrow);
        gui.drawString(Minecraft.getInstance().font, arrow,
                hx + (PANEL_HANDLE_W - aw) / 2, hy + (PANEL_HANDLE_H - 8) / 2, 0xFFCCFFCC, false);
    }

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

    private static void drawCornerHandle(GuiGraphics gui, int cx, int cy, int corner, boolean hovered) {
        int color = hovered ? 0xFFA8DDFF : 0xFF44AAFF;
        int side = 8;
        double medianLen = side * Math.sqrt(3) / 2.0;
        double halfBase = side / 2.0;

        double dx = (corner == 0 || corner == 2) ? -1 : 1;
        double dy = (corner == 0 || corner == 1) ? -1 : 1;
        double mag = Math.sqrt(dx * dx + dy * dy);
        dx /= mag;
        dy /= mag;

        double bcx = cx + dx * medianLen;
        double bcy = cy + dy * medianLen;
        double px = -dy;
        double py = dx;
        int v1x = cx;
        int v1y = cy;
        int v2x = (int) Math.round(bcx + px * halfBase);
        int v2y = (int) Math.round(bcy + py * halfBase);
        int v3x = (int) Math.round(bcx - px * halfBase);
        int v3y = (int) Math.round(bcy - py * halfBase);

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

    private static void drawInwardTriangle(GuiGraphics gui, int cx, int cy, int dir, boolean hovered) {
        int color = hovered ? 0xFFA8FFB8 : 0xFF44FF66;
        int size = 4;
        for (int i = 0; i < size; i++) {
            int half = size - i;
            switch (dir) {
                case 0 -> gui.fill(cx - half, cy + i, cx + half + 1, cy + i + 1, color);
                case 1 -> gui.fill(cx - half, cy - i, cx + half + 1, cy - i + 1, color);
                case 2 -> gui.fill(cx + i, cy - half, cx + i + 1, cy + half + 1, color);
                case 3 -> gui.fill(cx - i, cy - half, cx - i + 1, cy + half + 1, color);
            }
        }
    }

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
        // GUI bounds aren't resolvable.
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
        // Horizontal snaps are inside-aligned in both anchor modes.
        int snapX = 0;
        if (Math.abs(minX - snapL) < EDGE_SNAP_DIST) snapX = (int) Math.round(snapL - minX);
        else if (Math.abs(snapR - maxX) < EDGE_SNAP_DIST) snapX = (int) Math.round(snapR - maxX);

        // Vertical snaps differ by anchor: GUI mode snaps the bar OUTSIDE the GUI box
        // (bottom edge to GUI top / top edge to GUI bottom); SCREEN mode is inside-aligned.
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

    public static double[] nextButtonScreenCenter() {
        Screen s = Minecraft.getInstance().screen;
        if (s == null) return new double[]{0, 0};
        for (var child : s.children()) {
            if (child instanceof NextTabsButton nb) {
                int animX = nb.getAnimatedAnchorX();
                int animY = nb.getAnimatedAnchorY();
                int cx = animX + nb.getWidth() / 2;
                int cy = animY + nb.getHeight() / 2;
                float barRot = currentEffectiveRotation();
                double px = cx;
                double py = cy;
                if (barRot != 0f) {
                    double[] center = barCenter();
                    double[] r = rotatePoint(px, py, center[0], center[1], barRot);
                    px = r[0];
                    py = r[1];
                }
                px += currentNextOffsetX();
                py += currentNextOffsetY();
                return new double[]{px, py};
            }
        }
        return new double[]{0, 0};
    }

    private static double[] inverseRotateMouseToBarFrame(double mouseX, double mouseY, int[] bounds) {
        float rot = currentEffectiveRotation();
        if (rot == 0f) return new double[]{mouseX, mouseY};
        double cx = (bounds[0] + bounds[2]) / 2.0;
        double cy = (bounds[1] + bounds[3]) / 2.0;
        return inverseRotatePoint(mouseX, mouseY, cx, cy, rot);
    }

    public static String getConfigKeyForScreen(Screen screen) {
        if (screen == null) return null;
        // Dynamic match first: ask each registered tab whether it owns this screen, and
        // if so derive the configKey from its @TabConfig annotation. Fallback switch
        // below handles screens that aren't claimed by any tab.
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
        // Also count the next-page chevron: it sits outside the bar bounds but is
        // functionally part of the bar — hovering it should reveal the tabs.
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
     * Tuck offset in the bar's *unrotated* frame. The direction in screen-space is set
     * per-screen via TuckDirection; we inverse-rotate that vector through the current
     * bar rotation so R(rotation) · v lands at the chosen direction.
     */
    private static int[] computeTuckUnrotatedDirection(double magnitude) {
        Screen s = Minecraft.getInstance().screen;
        vodmordia.modtabs.layout.TuckDirection dir = (s != null)
                ? ScreenLayoutStore.get(s.getClass()).tuckDirection
                : vodmordia.modtabs.layout.TuckDirection.DOWN;
        if (dir == null) dir = vodmordia.modtabs.layout.TuckDirection.DOWN;
        double sx = dir.dx();
        double sy = dir.dy();
        double rad = Math.toRadians(currentEffectiveRotation());
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
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


            // Pagination is now driven purely by the per-screen layout cap
            // (layout.maxTabsPerPage). Auto-fit-by-axis-space was removed because it
            // made the visible count drift on window resize.
            ScreenLayout savedLayout = ScreenLayoutStore.get(event.getScreen().getClass());
            int totalEnabledIncludingSticky = enabledTabs.size();
            int perPageCap = savedLayout.maxTabsPerPage;
            currentTabsCount = (perPageCap > 0)
                    ? Math.min(totalEnabledIncludingSticky, perPageCap)
                    : totalEnabledIncludingSticky;


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
            // RIGHT_TO_LEFT flips every visual slot to (count - 1 - i) so that "first"
            // tabs land at the rightmost natural-frame slot — combined with a 180° rotation
            // that lands them on the screen-left visually.
            int buttonPosition = 0;
            boolean reverseOrder = layout.tabOrder == vodmordia.modtabs.layout.TabOrder.RIGHT_TO_LEFT;

            if (Config.Baked.stickyInventoryTab && inventoryTab != null && currentTabsCount > 0) {
                int slot = reverseOrder ? (currentTabsCount - 1 - buttonPosition) : buttonPosition;
                TabButton inventoryButton = new TabButton(inventoryTab, Minecraft.getInstance().player, event.getScreen(), slot, TabsMenu.leftScreenPos, TabsMenu.topScreenPos, effectiveDisplayMode);
                event.addListener(inventoryButton);
                buttonPosition++;
            }

            List<TabBase> tabsToRender = Config.Baked.stickyInventoryTab ? nonInventoryTabs : enabledTabs;
            int availableSlots = Config.Baked.stickyInventoryTab && inventoryTab != null ? currentTabsCount - 1 : currentTabsCount;

            for (int tabIndex = 0; tabIndex < tabsToRender.size() && buttonPosition < currentTabsCount; tabIndex++) {
                TabBase tabBase = tabsToRender.get(tabIndex);
                int tabIndexToShow = tabIndex - startTabIndex;

                if (tabIndexToShow >= 0 && tabIndexToShow < availableSlots) {
                    int slot = reverseOrder ? (currentTabsCount - 1 - buttonPosition) : buttonPosition;
                    TabButton newButton = new TabButton(tabBase, Minecraft.getInstance().player, event.getScreen(), slot, TabsMenu.leftScreenPos, TabsMenu.topScreenPos, effectiveDisplayMode);
                    event.addListener(newButton);
                    buttonPosition++;
                }
            }

            int totalTabsToPage = Config.Baked.stickyInventoryTab ? nonInventoryTabs.size() : enabledTabs.size();
            int maxVisibleTabs = Config.Baked.stickyInventoryTab && inventoryTab != null ? currentTabsCount - 1 : currentTabsCount;

            if (totalTabsToPage > maxVisibleTabs) {
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
