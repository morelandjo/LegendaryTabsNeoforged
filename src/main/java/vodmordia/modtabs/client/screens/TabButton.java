package vodmordia.modtabs.client.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import vodmordia.modtabs.api.tabs_menu.TabBase;
import vodmordia.modtabs.api.tabs_menu.TabPositioning;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;
import vodmordia.modtabs.api.tabs_menu.TabDisplayMode;

import static vodmordia.modtabs.api.tabs_menu.TabBase.TAB_HEIGHT;
import static vodmordia.modtabs.api.tabs_menu.TabBase.TAB_WIDTH;
import static vodmordia.modtabs.api.tabs_menu.TabBase.TAB_HEIGHT_VERTICAL;
import static vodmordia.modtabs.api.tabs_menu.TabBase.TAB_WIDTH_VERTICAL;


public class TabButton extends Button {
    public int tabPositionIndex;
    public TabBase tabBase;
    public Player player;
    public Screen screen;
    public boolean isDisabled;
    public TabDisplayMode displayMode;
    public TabPositioning positioning;

    public TabButton(TabBase tabBase, Player player, Screen screen, int tabPositionIndex, int leftScreenPos, int topScreenPos, TabDisplayMode displayMode) {
        this(tabBase, player, screen, tabPositionIndex, leftScreenPos, topScreenPos, displayMode, TabPositioning.GUI_RELATIVE);
    }

    public TabButton(TabBase tabBase, Player player, Screen screen, int tabPositionIndex, int leftScreenPos, int topScreenPos, TabDisplayMode displayMode, TabPositioning positioning) {
        super(calculateX(leftScreenPos, tabPositionIndex, positioning),
              calculateY(topScreenPos, tabPositionIndex, displayMode, positioning),
              widthFor(positioning), heightFor(positioning), Component.literal(""), button -> {}, DEFAULT_NARRATION);


        this.tabPositionIndex = tabPositionIndex;
        this.player = player;
        this.screen = screen;
        this.displayMode = displayMode;
        this.positioning = positioning;
        this.setTabBase(tabBase);
    }

    private static int widthFor(TabPositioning positioning) {
        return positioning != null && positioning.isVertical() ? TAB_WIDTH_VERTICAL : TAB_WIDTH;
    }

    private static int heightFor(TabPositioning positioning) {
        return positioning != null && positioning.isVertical() ? TAB_HEIGHT_VERTICAL : TAB_HEIGHT;
    }

    private static int calculateX(int leftScreenPos, int tabPositionIndex, TabPositioning positioning) {
        if (positioning != null && positioning.isVertical()) {
            // Vertical tabs: X is fixed at the screen edge column
            return leftScreenPos;
        }
        return leftScreenPos + tabPositionIndex * (TAB_WIDTH + 1);
    }

    private static int calculateY(int topScreenPos, int tabPositionIndex, TabDisplayMode displayMode, TabPositioning positioning) {
        if (positioning != null && positioning.isVertical()) {
            // Vertical tabs stack down the column from the top anchor
            return topScreenPos + tabPositionIndex * (TAB_HEIGHT_VERTICAL + 1);
        }
        // Horizontal: inverted hangs below the GUI top edge, normal floats above
        return displayMode == TabDisplayMode.INVERTED ?
            topScreenPos :
            topScreenPos - TAB_HEIGHT;
    }

    @Override
    public void onPress() {
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
        // Apply animation offset for tuck mode along the cross-axis
        int offX = positioning != null && positioning.isVertical() ? TabsMenu.getAnimatedXOffset() : 0;
        int offY = positioning != null && positioning.isVertical() ? 0 : TabsMenu.getAnimatedYOffset();
        int animatedX = this.getX() + offX;
        int animatedY = this.getY() + offY;

        boolean isMouseOverAnimated = mouseX >= animatedX && mouseX < animatedX + this.width &&
                                     mouseY >= animatedY && mouseY < animatedY + this.height;

        this.tabBase.render(gui, animatedX, animatedY, this.isDisabled || isMouseOverAnimated, this.displayMode, this.positioning);
    }

    public void updatePosition(int leftScreenPos, int topScreenPos) {
        setX(calculateX(leftScreenPos, tabPositionIndex, positioning));
        setY(calculateY(topScreenPos, tabPositionIndex, displayMode, positioning));
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        int offX = positioning != null && positioning.isVertical() ? TabsMenu.getAnimatedXOffset() : 0;
        int offY = positioning != null && positioning.isVertical() ? 0 : TabsMenu.getAnimatedYOffset();
        int animatedX = this.getX() + offX;
        int animatedY = this.getY() + offY;
        return mouseX >= animatedX && mouseX < animatedX + this.width &&
               mouseY >= animatedY && mouseY < animatedY + this.height;
    }
}
