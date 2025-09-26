package vodmordia.modtabs.client.screens;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import vodmordia.modtabs.api.tabs_menu.TabBase;
import vodmordia.modtabs.api.tabs_menu.TabDisplayMode;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;

import static vodmordia.modtabs.api.tabs_menu.TabBase.TAB_HEIGHT;
import static vodmordia.modtabs.api.tabs_menu.TabBase.TAB_WIDTH;

public class TabButton extends ButtonWidget {
    public int tabPositionIndex;
    public TabBase tabBase;
    public PlayerEntity player;
    public Screen screen;
    public boolean isDisabled;
    public TabDisplayMode displayMode;

    public TabButton(TabBase tabBase, PlayerEntity player, Screen screen, int tabPositionIndex, int leftScreenPos, int topScreenPos, TabDisplayMode displayMode) {
        super(leftScreenPos + tabPositionIndex * (TAB_WIDTH + 1),
              calculateYPosition(topScreenPos, displayMode),
              TAB_WIDTH, TAB_HEIGHT, Text.empty(), button -> {}, DEFAULT_NARRATION_SUPPLIER);

        this.tabPositionIndex = tabPositionIndex;
        this.player = player;
        this.screen = screen;
        this.displayMode = displayMode;
        this.setTabBase(tabBase);
    }

    private static int calculateYPosition(int topScreenPos, TabDisplayMode displayMode) {
        // Adjust Y position for inverted tabs - they hang down instead of up
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
        setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(this.tabBase.getTooltip()));
    }

    @Override
    public void render(DrawContext gui, int mouseX, int mouseY, float partial) {
        // Apply animation offset for tuck mode
        int animatedY = this.getY() + TabsMenu.getAnimatedYOffset();

        // Check if mouse is over the animated position
        boolean isMouseOverAnimated = mouseX >= this.getX() && mouseX < this.getX() + this.width &&
                                     mouseY >= animatedY && mouseY < animatedY + this.height;

        this.tabBase.render(gui, this.getX(), animatedY, this.isDisabled || isMouseOverAnimated, this.displayMode);

        // Don't call super.render() to avoid default button rendering
    }

    public void renderWidget(DrawContext gui, int mouseX, int mouseY, float partial) {
        // This method is left empty to prevent default button widget rendering
    }

    public void updatePosition(int leftScreenPos, int topScreenPos) {
        int finalX = leftScreenPos + tabPositionIndex * (TAB_WIDTH + 1);
        setX(finalX);
        setY(calculateYPosition(topScreenPos, displayMode));
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        // Override to account for animation offset in tuck mode
        int animatedY = this.getY() + TabsMenu.getAnimatedYOffset();
        return mouseX >= this.getX() && mouseX < this.getX() + this.width &&
               mouseY >= animatedY && mouseY < animatedY + this.height;
    }
}