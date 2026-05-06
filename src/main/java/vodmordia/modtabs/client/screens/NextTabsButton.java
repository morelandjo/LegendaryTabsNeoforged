package vodmordia.modtabs.client.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.TabDisplayMode;
import vodmordia.modtabs.api.tabs_menu.TabPositioning;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;

import static vodmordia.modtabs.api.tabs_menu.TabBase.TAB_HEIGHT;
import static vodmordia.modtabs.api.tabs_menu.TabBase.TAB_WIDTH;
import static vodmordia.modtabs.api.tabs_menu.TabBase.TAB_HEIGHT_VERTICAL;
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
        super(leftScreenPos + tabPositionIndex * (TAB_WIDTH + 1), topScreenPos, NEXT_TABS_BUTTON_WIDTH, NEXT_TABS_BUTTON_HEIGHT, Component.literal(""), press, DEFAULT_NARRATION);
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
        return leftScreenPos + tabPositionIndex * (TAB_WIDTH + 1);
    }

    private static int calculateY(int topScreenPos, int tabPositionIndex, TabDisplayMode displayMode, TabPositioning positioning) {
        if (positioning != null && positioning.isVertical()) {
            return topScreenPos + tabPositionIndex * (TAB_HEIGHT_VERTICAL + 1);
        }
        return displayMode == TabDisplayMode.INVERTED ?
            topScreenPos :
            topScreenPos - TAB_HEIGHT;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partial) {
        boolean vertical = positioning != null && positioning.isVertical();
        int offX = vertical ? TabsMenu.getAnimatedXOffset() : 0;
        int offY = vertical ? 0 : TabsMenu.getAnimatedYOffset();
        int animatedX = this.getX() + offX;
        int animatedY = this.getY() + offY;

        boolean isMouseOverAnimated = mouseX >= animatedX && mouseX < animatedX + this.width &&
                                     mouseY >= animatedY && mouseY < animatedY + this.height;

        int texOffsetX = isMouseOverAnimated ? 54 : 0;

        if (vertical) {
            // Rotate the chevron 90° CW so it points down (toward the next page below).
            // The button footprint is TAB_WIDTH_VERTICAL × NEXT_TABS_BUTTON_WIDTH (22×12);
            // we rotate around its center so the rotated icon sits centered inside.
            gui.pose().pushPose();
            gui.pose().translate(animatedX + TAB_WIDTH_VERTICAL / 2.0f, animatedY + NEXT_TABS_BUTTON_WIDTH / 2.0f, 0);
            gui.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(90));
            gui.pose().translate(-NEXT_TABS_BUTTON_WIDTH / 2.0f, -NEXT_TABS_BUTTON_HEIGHT / 2.0f, 0);
            gui.blit(TAB_ICONS, 0, 0, NEXT_TABS_ICON_TEX_X + texOffsetX, NEXT_TABS_ICON_TEX_Y, NEXT_TABS_BUTTON_WIDTH, NEXT_TABS_BUTTON_HEIGHT);
            gui.pose().popPose();
        } else {
            gui.blit(TAB_ICONS, animatedX, animatedY, NEXT_TABS_ICON_TEX_X + texOffsetX, NEXT_TABS_ICON_TEX_Y, NEXT_TABS_BUTTON_WIDTH, NEXT_TABS_BUTTON_HEIGHT);
        }
    }

    public void updatePosition(int leftScreenPos, int topScreenPos) {
        setX(calculateX(leftScreenPos, tabPositionIndex, positioning));
        setY(calculateY(topScreenPos, tabPositionIndex, displayMode, positioning));
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        boolean vertical = positioning != null && positioning.isVertical();
        int offX = vertical ? TabsMenu.getAnimatedXOffset() : 0;
        int offY = vertical ? 0 : TabsMenu.getAnimatedYOffset();
        int animatedX = this.getX() + offX;
        int animatedY = this.getY() + offY;
        return mouseX >= animatedX && mouseX < animatedX + this.width &&
               mouseY >= animatedY && mouseY < animatedY + this.height;
    }
}
