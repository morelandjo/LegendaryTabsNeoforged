package vodmordia.modtabs.client.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import vodmordia.modtabs.api.tabs_menu.TabBase;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;
import vodmordia.modtabs.api.tabs_menu.TabDisplayMode;

import static vodmordia.modtabs.api.tabs_menu.TabBase.TAB_HEIGHT;
import static vodmordia.modtabs.api.tabs_menu.TabBase.TAB_WIDTH;


public class TabButton extends Button {
    /** Long-press duration to enter the layout editor. */
    private static final long LONG_PRESS_MS = 1500L;
    /** Don't show the pie until this much has elapsed; avoids flicker on quick clicks. */
    private static final long LONG_PRESS_GRACE_MS = 120L;

    public int tabPositionIndex;
    public TabBase tabBase;
    public Player player;
    public Screen screen;
    public boolean isDisabled;
    public TabDisplayMode displayMode;

    // Long-press state — set when the user starts holding the mouse on the current screen's tab.
    private long pressStartMs = 0L;

    /** True between mouseClicked and mouseReleased on this specific tab. Used by the
     *  screenwide release handler so it only cancels vanilla's release flow when the
     *  release actually belongs to a tab — otherwise releases over slots are swallowed. */
    public boolean hasPendingPress() { return pressStartMs > 0L; }

    /** Stored anchor (top-left of the tab BAR for this screen). Per-tab position derives from this + index + scale + spacing. */
    private int barLeft;
    private int barTop;

    public TabButton(TabBase tabBase, Player player, Screen screen, int tabPositionIndex, int leftScreenPos, int topScreenPos, TabDisplayMode displayMode) {
        // Initial position uses *current* effective scale/spacing so the first frame is correct.
        super(calculateX(leftScreenPos, tabPositionIndex),
              calculateY(topScreenPos, tabPositionIndex, displayMode),
              TAB_WIDTH, TAB_HEIGHT, Component.literal(""), button -> {}, DEFAULT_NARRATION);

        this.tabPositionIndex = tabPositionIndex;
        this.player = player;
        this.screen = screen;
        this.displayMode = displayMode;
        this.barLeft = leftScreenPos;
        this.barTop = topScreenPos;
        this.setTabBase(tabBase);
    }

    private static int calculateX(int leftScreenPos, int tabPositionIndex) {
        return leftScreenPos + tabPositionIndex * TabsMenu.primaryAxisStep();
    }

    private static int calculateY(int topScreenPos, int tabPositionIndex, TabDisplayMode displayMode) {
        // Inverted hangs below the GUI top edge, normal floats above by the scaled tab height.
        return displayMode == TabDisplayMode.INVERTED ?
            topScreenPos :
            topScreenPos - TabsMenu.effectiveTabHeight();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.isMouseOver(mouseX, mouseY) && !TabsMenu.isEditing(this.screen)) {
            pressStartMs = System.currentTimeMillis();
            setTooltip(null); // suppress tooltip while gesturing
            return true;
        }
        // In edit mode, never consume the click. Vanilla Button.mouseClicked would
        // return true for any click in our bounds (calling our onPress, which is a no-op
        // in edit mode) — that swallows clicks that should reach editor-panel widgets
        // registered AFTER us in the screen's children list.
        if (TabsMenu.isEditing(this.screen)) {
            return false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (pressStartMs > 0L && button == 0) {
            boolean wasShortClick = (System.currentTimeMillis() - pressStartMs) < LONG_PRESS_MS;
            pressStartMs = 0L;
            if (!TabsMenu.isEditing(this.screen)) {
                setTooltip(Tooltip.create(tabBase.getTooltip()));
                if (wasShortClick && this.isMouseOver(mouseX, mouseY) && !this.isDisabled) {
                    TabsMenu.markScreenOpenedViaTab(this.screen);
                    tabBase.openTargetScreen(this.player);
                }
            }
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onPress() {
        // mouseClicked/mouseReleased handle the long-press path; onPress is dead code for tabs now.
        if (TabsMenu.isEditing(this.screen)) return;
        super.onPress();
        if (!this.isDisabled) {
            TabsMenu.markScreenOpenedViaTab(this.screen);
            tabBase.openTargetScreen(this.player);
        }
    }

    public void setTabBase(TabBase tabBase) {
        this.tabBase = tabBase;
        this.isDisabled = this.tabBase.isCurrentlyUsed(this.screen);
        this.setTooltip(Tooltip.create(this.tabBase.getTooltip()));
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partial) {
        // Render strategy:
        //  - Edit mode: render in the normal widget pass (on top), never in the
        //    pre-panel pass — the user needs handles to be reachable.
        //  - Play mode on a container screen: render ONLY in the pre-panel pass so the
        //    inventory panel covers overlapping tabs. The mixin sets renderingBehindPanel.
        //  - Play mode on a non-container screen (e.g. AdvancementsScreen): there's no
        //    mixin and no panel to go behind, so render in the normal widget pass.
        boolean editing = TabsMenu.isEditing(this.screen);
        boolean isContainer = this.screen instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?>;
        // Container screens that fully override render (no super.render call) miss our
        // behind-panel mixin and would never draw. Treat them like non-container screens —
        // render on top in the normal pass.
        boolean rendersOnTop = isContainer && TabsMenu.rendersTabsOnTop(this.screen);
        if (editing) {
            if (TabsMenu.renderingBehindPanel) return;
        } else if (isContainer && !rendersOnTop) {
            if (!TabsMenu.renderingBehindPanel) return;
        } else {
            if (TabsMenu.renderingBehindPanel) return;
        }
        // Recompute position from the bar anchor each render so live scale/spacing edits are visible.
        int x = calculateX(barLeft, tabPositionIndex);
        int y = calculateY(barTop, tabPositionIndex, displayMode);
        setX(x);
        setY(y);

        // Override raw-bounds isHovered (set by AbstractWidget.render before this method) with
        // our scaled-bounds isMouseOver. updateTooltip runs AFTER renderWidget and reads
        // isHovered, so this ensures only the tab actually under the cursor shows its tooltip —
        // otherwise neighboring tabs' raw 32px-wide bounds overlap at small scale and the last-
        // rendered tab's tooltip wins, making R→L tab tooltips appear backwards.
        this.isHovered = this.isMouseOver(mouseX, mouseY);

        // Tuck offset is now a rotation-aware 2D vector (always points screen-down after
        // rotation), so consume both axes regardless of bar orientation.
        int offX = TabsMenu.getAnimatedXOffset();
        int offY = TabsMenu.getAnimatedYOffset();
        if (TabsMenu.isEditing(this.screen)) {
            offX += TabsMenu.getDragOffsetX();
            offY += TabsMenu.getDragOffsetY();
        }
        int animatedX = x + offX;
        int animatedY = y + offY;

        float scale = TabsMenu.currentEffectiveScale();
        int scaledW = TabsMenu.effectiveTabWidth();
        int scaledH = TabsMenu.effectiveTabHeight();
        float rotation = TabsMenu.currentEffectiveRotation();

        // Hit-test in the bar's unrotated frame, applying inverse rotation to the mouse.
        boolean isMouseOverAnimated;
        if (rotation != 0f) {
            double[] center = TabsMenu.barCenter();
            double[] m = TabsMenu.inverseRotatePoint(mouseX, mouseY, center[0], center[1], rotation);
            isMouseOverAnimated = m[0] >= animatedX && m[0] < animatedX + scaledW &&
                                  m[1] >= animatedY && m[1] < animatedY + scaledH;
        } else {
            isMouseOverAnimated = mouseX >= animatedX && mouseX < animatedX + scaledW &&
                                  mouseY >= animatedY && mouseY < animatedY + scaledH;
        }

        boolean suppressMouseHover = pressStartMs > 0L || TabsMenu.isEditing(this.screen);
        boolean effectiveHover = this.isDisabled || (!suppressMouseHover && isMouseOverAnimated);

        boolean needsPose = scale != 1.0f || rotation != 0f;
        if (needsPose) {
            gui.pose().pushPose();
            if (rotation != 0f) {
                double[] center = TabsMenu.barCenter();
                gui.pose().translate(center[0], center[1], 0);
                gui.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(rotation));
                gui.pose().translate(-center[0], -center[1], 0);
            }
            if (scale != 1.0f) {
                gui.pose().translate(animatedX, animatedY, 0);
                gui.pose().scale(scale, scale, 1.0f);
                this.tabBase.render(gui, 0, 0, effectiveHover, this.displayMode);
            } else {
                this.tabBase.render(gui, animatedX, animatedY, effectiveHover, this.displayMode);
            }
            gui.pose().popPose();
        } else {
            this.tabBase.render(gui, animatedX, animatedY, effectiveHover, this.displayMode);
        }

        // Long-press to enter edit mode is only armed on the *home* tab — i.e. the one whose
        // openTargetScreen() represents the screen the user is currently viewing. This stops
        // users from accidentally entering edit mode by holding any tab, and gives a single
        // discoverable gesture per screen (long-press the home icon). isHomeTab is distinct
        // from isCurrentlyUsed because some tabs (Inventory, FTB Quests, etc.) deliberately
        // return false from isCurrentlyUsed to keep the click action available — but they're
        // still the "home" tab for their screen as far as the long-press gesture is concerned.
        if (pressStartMs > 0L && this.tabBase.isHomeTab(this.screen) && !TabsMenu.isEditing(this.screen)) {
            long elapsed = System.currentTimeMillis() - pressStartMs;
            if (elapsed >= LONG_PRESS_MS) {
                pressStartMs = 0L;
                TabsMenu.enterEditMode(this.screen);
            } else if (elapsed >= LONG_PRESS_GRACE_MS) {
                // Pie is drawn OUTSIDE the rotated pose, so we have to compute the tab
                // center's actual on-screen location (rotated around bar center) ourselves.
                double pieX = animatedX + scaledW / 2.0;
                double pieY = animatedY + scaledH / 2.0;
                if (rotation != 0f) {
                    double[] center = TabsMenu.barCenter();
                    double[] r = TabsMenu.rotatePoint(pieX, pieY, center[0], center[1], rotation);
                    pieX = r[0];
                    pieY = r[1];
                }
                drawLongPressPie(gui, (int) Math.round(pieX), (int) Math.round(pieY),
                        elapsed / (float) LONG_PRESS_MS);
            }
        }
    }

    public void updatePosition(int leftScreenPos, int topScreenPos) {
        this.barLeft = leftScreenPos;
        this.barTop = topScreenPos;
        setX(calculateX(leftScreenPos, tabPositionIndex));
        setY(calculateY(topScreenPos, tabPositionIndex, displayMode));
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        // Recompute position from the bar anchor instead of trusting this.getX()/getY().
        // updatePosition is called once per frame, but a click event arriving before that
        // frame's render runs would otherwise see stale coordinates after save/scale/spacing
        // changes.
        int x = calculateX(barLeft, tabPositionIndex);
        int y = calculateY(barTop, tabPositionIndex, displayMode);

        // Tuck offset is now a rotation-aware 2D vector (always points screen-down after
        // rotation), so consume both axes regardless of bar orientation.
        int offX = TabsMenu.getAnimatedXOffset();
        int offY = TabsMenu.getAnimatedYOffset();
        if (TabsMenu.isEditing(this.screen)) {
            offX += TabsMenu.getDragOffsetX();
            offY += TabsMenu.getDragOffsetY();
        }
        int animatedX = x + offX;
        int animatedY = y + offY;
        int scaledW = TabsMenu.effectiveTabWidth();
        int scaledH = TabsMenu.effectiveTabHeight();
        float rotation = TabsMenu.currentEffectiveRotation();
        if (rotation != 0f) {
            double[] center = TabsMenu.barCenter();
            double[] m = TabsMenu.inverseRotatePoint(mouseX, mouseY, center[0], center[1], rotation);
            return m[0] >= animatedX && m[0] < animatedX + scaledW &&
                   m[1] >= animatedY && m[1] < animatedY + scaledH;
        }
        return mouseX >= animatedX && mouseX < animatedX + scaledW &&
               mouseY >= animatedY && mouseY < animatedY + scaledH;
    }

    /** Called by TabsMenu when entering/exiting edit mode to suppress/restore tooltips. */
    public void setTooltipSuppressed(boolean suppressed) {
        if (suppressed) {
            setTooltip(null);
        } else if (tabBase != null) {
            setTooltip(Tooltip.create(tabBase.getTooltip()));
        }
    }

    /**
     * Render a clock-face style pie at (cx, cy) filling clockwise from 12 o'clock as
     * progress goes from 0..1.
     */
    private static void drawLongPressPie(GuiGraphics gui, int cx, int cy, float progress) {
        int segments = 24;
        int innerRadius = 1;
        int outerRadius = 7;
        int tickThickness = 1;
        int filled = 0xFF44FF66;
        int dim = 0x60000000;

        for (int i = 0; i < segments; i++) {
            float t = i / (float) segments;
            float angle = t * 360f - 90f;
            int color = t < progress ? filled : dim;
            gui.pose().pushPose();
            gui.pose().translate(cx, cy, 0);
            gui.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(angle));
            gui.fill(innerRadius, -tickThickness, outerRadius, tickThickness, color);
            gui.pose().popPose();
        }
    }
}
