package vodmordia.modtabs.client.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.TabDisplayMode;
import vodmordia.modtabs.api.tabs_menu.TabPositioning;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;

import static vodmordia.modtabs.api.tabs_menu.TabBase.TAB_WIDTH;
import static vodmordia.modtabs.api.tabs_menu.TabBase.TAB_WIDTH_VERTICAL;

public class NextTabsButton extends Button {
    private final ResourceLocation TAB_ICONS = new ResourceLocation(ModTabs.MOD_ID, "textures/gui/tab_menu_buttons.png");
    public static final int NEXT_TABS_ICON_TEX_X = 135;
    public static final int NEXT_TABS_ICON_TEX_Y = 0;
    public static final int NEXT_TABS_BUTTON_WIDTH = 12;
    public static final int NEXT_TABS_BUTTON_HEIGHT = 21;
    public int tabPositionIndex;
    public TabDisplayMode displayMode;
    public TabPositioning positioning;

    public NextTabsButton(int tabPositionIndex, int leftScreenPos, int topScreenPos, net.minecraft.client.gui.components.Button.OnPress press) {
        super(leftScreenPos + tabPositionIndex * TabsMenu.primaryAxisStep(), topScreenPos, NEXT_TABS_BUTTON_WIDTH, NEXT_TABS_BUTTON_HEIGHT, Component.literal(""), press, DEFAULT_NARRATION);
        this.tabPositionIndex = tabPositionIndex;
        this.displayMode = TabDisplayMode.NORMAL;
        this.positioning = TabPositioning.GUI_RELATIVE;
    }

    public NextTabsButton(int tabPositionIndex, int leftScreenPos, int topScreenPos, TabDisplayMode displayMode, net.minecraft.client.gui.components.Button.OnPress press) {
        this(tabPositionIndex, leftScreenPos, topScreenPos, displayMode, TabPositioning.GUI_RELATIVE, press);
    }

    public NextTabsButton(int tabPositionIndex, int leftScreenPos, int topScreenPos, TabDisplayMode displayMode, TabPositioning positioning, net.minecraft.client.gui.components.Button.OnPress press) {
        super(calculateX(leftScreenPos, tabPositionIndex, positioning),
              calculateY(topScreenPos, tabPositionIndex, displayMode, positioning),
              widthFor(positioning), heightFor(positioning), Component.literal(""), press, DEFAULT_NARRATION);
        this.tabPositionIndex = tabPositionIndex;
        this.displayMode = displayMode;
        this.positioning = positioning;
    }

    private static int widthFor(TabPositioning positioning) {
        // Vertical: keep the next-tab button slim (12px wide is fine), but shorter than a tab.
        return positioning != null && positioning.isVertical() ? TAB_WIDTH_VERTICAL : NEXT_TABS_BUTTON_WIDTH;
    }

    private static int heightFor(TabPositioning positioning) {
        return positioning != null && positioning.isVertical() ? NEXT_TABS_BUTTON_WIDTH : NEXT_TABS_BUTTON_HEIGHT;
    }

    private static int calculateX(int leftScreenPos, int tabPositionIndex, TabPositioning positioning) {
        if (positioning != null && positioning.isVertical()) {
            return leftScreenPos;
        }
        return leftScreenPos + tabPositionIndex * TabsMenu.primaryAxisStep();
    }

    private static int calculateY(int topScreenPos, int tabPositionIndex, TabDisplayMode displayMode, TabPositioning positioning) {
        if (positioning != null && positioning.isVertical()) {
            return topScreenPos + tabPositionIndex * TabsMenu.primaryAxisStep();
        }
        return displayMode == TabDisplayMode.INVERTED ?
            topScreenPos :
            topScreenPos - TabsMenu.effectiveTabHeight();
    }

    /** Natural anchor X (top-left in unrotated bar frame, including bar drag offset). */
    public int getAnimatedAnchorX() {
        int offX = TabsMenu.getAnimatedXOffset();
        Screen current = net.minecraft.client.Minecraft.getInstance().screen;
        if (current != null && TabsMenu.isEditing(current)) {
            offX += TabsMenu.getDragOffsetX();
        }
        return this.getX() + offX;
    }

    public int getAnimatedAnchorY() {
        int offY = TabsMenu.getAnimatedYOffset();
        Screen current = net.minecraft.client.Minecraft.getInstance().screen;
        if (current != null && TabsMenu.isEditing(current)) {
            offY += TabsMenu.getDragOffsetY();
        }
        return this.getY() + offY;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partial) {
        // Match TabButton's render-strategy gate (see comment there).
        Screen current = net.minecraft.client.Minecraft.getInstance().screen;
        boolean editing = current != null && TabsMenu.isEditing(current);
        boolean isContainer = current instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?>;
        if (editing) {
            if (TabsMenu.renderingBehindPanel) return;
        } else if (isContainer) {
            if (!TabsMenu.renderingBehindPanel) return;
        } else {
            if (TabsMenu.renderingBehindPanel) return;
        }
        boolean vertical = positioning != null && positioning.isVertical();
        int animatedX = getAnimatedAnchorX();
        int animatedY = getAnimatedAnchorY();
        float barRotation = TabsMenu.currentEffectiveRotation();
        int nextOffX = TabsMenu.currentNextOffsetX();
        int nextOffY = TabsMenu.currentNextOffsetY();
        float nextRotation = TabsMenu.currentNextEffectiveRotation();

        // Hit-test inverse-transforms mouse: undo screen offset, undo bar rotation, undo next rotation.
        int btnCenterX = animatedX + this.width / 2;
        int btnCenterY = animatedY + this.height / 2;
        double mx = mouseX - nextOffX;
        double my = mouseY - nextOffY;
        if (barRotation != 0f) {
            double[] center = TabsMenu.barCenter();
            double[] m = TabsMenu.inverseRotatePoint(mx, my, center[0], center[1], barRotation);
            mx = m[0]; my = m[1];
        }
        if (nextRotation != 0f) {
            double[] m = TabsMenu.inverseRotatePoint(mx, my, btnCenterX, btnCenterY, nextRotation);
            mx = m[0]; my = m[1];
        }
        boolean isMouseOverAnimated = mx >= animatedX && mx < animatedX + this.width &&
                                     my >= animatedY && my < animatedY + this.height;

        int texOffsetX = isMouseOverAnimated ? 54 : 0;

        // Apply outer transforms: next-button screen offset, then bar rotation around bar
        // center, then next-button's own rotation around its (natural-frame) center.
        boolean needsPose = nextOffX != 0 || nextOffY != 0 || barRotation != 0f || nextRotation != 0f;
        if (needsPose) {
            gui.pose().pushPose();
            if (nextOffX != 0 || nextOffY != 0) {
                gui.pose().translate(nextOffX, nextOffY, 0);
            }
            if (barRotation != 0f) {
                double[] center = TabsMenu.barCenter();
                gui.pose().translate(center[0], center[1], 0);
                gui.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(barRotation));
                gui.pose().translate(-center[0], -center[1], 0);
            }
            if (nextRotation != 0f) {
                gui.pose().translate(btnCenterX, btnCenterY, 0);
                gui.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(nextRotation));
                gui.pose().translate(-btnCenterX, -btnCenterY, 0);
            }
        }

        if (vertical) {
            gui.pose().pushPose();
            gui.pose().translate(animatedX + TAB_WIDTH_VERTICAL / 2.0f, animatedY + NEXT_TABS_BUTTON_WIDTH / 2.0f, 0);
            gui.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(90));
            gui.pose().translate(-NEXT_TABS_BUTTON_WIDTH / 2.0f, -NEXT_TABS_BUTTON_HEIGHT / 2.0f, 0);
            gui.blit(TAB_ICONS, 0, 0, NEXT_TABS_ICON_TEX_X + texOffsetX, NEXT_TABS_ICON_TEX_Y, NEXT_TABS_BUTTON_WIDTH, NEXT_TABS_BUTTON_HEIGHT);
            gui.pose().popPose();
        } else {
            gui.blit(TAB_ICONS, animatedX, animatedY, NEXT_TABS_ICON_TEX_X + texOffsetX, NEXT_TABS_ICON_TEX_Y, NEXT_TABS_BUTTON_WIDTH, NEXT_TABS_BUTTON_HEIGHT);
        }
        if (needsPose) {
            gui.pose().popPose();
        }
    }

    public void updatePosition(int leftScreenPos, int topScreenPos) {
        setX(calculateX(leftScreenPos, tabPositionIndex, positioning));
        setY(calculateY(topScreenPos, tabPositionIndex, displayMode, positioning));
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        int animatedX = getAnimatedAnchorX();
        int animatedY = getAnimatedAnchorY();
        int btnCenterX = animatedX + this.width / 2;
        int btnCenterY = animatedY + this.height / 2;
        float barRotation = TabsMenu.currentEffectiveRotation();
        int nextOffX = TabsMenu.currentNextOffsetX();
        int nextOffY = TabsMenu.currentNextOffsetY();
        float nextRotation = TabsMenu.currentNextEffectiveRotation();

        double mx = mouseX - nextOffX;
        double my = mouseY - nextOffY;
        if (barRotation != 0f) {
            double[] center = TabsMenu.barCenter();
            double[] m = TabsMenu.inverseRotatePoint(mx, my, center[0], center[1], barRotation);
            mx = m[0]; my = m[1];
        }
        if (nextRotation != 0f) {
            double[] m = TabsMenu.inverseRotatePoint(mx, my, btnCenterX, btnCenterY, nextRotation);
            mx = m[0]; my = m[1];
        }
        return mx >= animatedX && mx < animatedX + this.width &&
               my >= animatedY && my < animatedY + this.height;
    }
}
