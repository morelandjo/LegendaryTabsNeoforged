package vodmordia.modtabs.client.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.TabDisplayMode;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;

import static vodmordia.modtabs.api.tabs_menu.TabBase.TAB_HEIGHT;
import static vodmordia.modtabs.api.tabs_menu.TabBase.TAB_WIDTH;

public class NextTabsButton extends Button {
    private final ResourceLocation TAB_ICONS = new ResourceLocation(ModTabs.MOD_ID, "textures/gui/tab_menu_buttons.png");
    public static final int NEXT_TABS_ICON_TEX_X = 135;
    public static final int NEXT_TABS_ICON_TEX_Y = 0;
    public static final int NEXT_TABS_BUTTON_WIDTH = 12;
    public static final int NEXT_TABS_BUTTON_HEIGHT = 21;
    public int tabPositionIndex;
    public TabDisplayMode displayMode;

    public NextTabsButton(int tabPositionIndex, int leftScreenPos, int topScreenPos, net.minecraft.client.gui.components.Button.OnPress press) {
        super(leftScreenPos + tabPositionIndex * (TAB_WIDTH + 1), topScreenPos, NEXT_TABS_BUTTON_WIDTH, NEXT_TABS_BUTTON_HEIGHT, Component.literal(""), press, DEFAULT_NARRATION);
        this.tabPositionIndex = tabPositionIndex;
        this.displayMode = TabDisplayMode.NORMAL; // Default for backward compatibility
    }

    public NextTabsButton(int tabPositionIndex, int leftScreenPos, int topScreenPos, TabDisplayMode displayMode, net.minecraft.client.gui.components.Button.OnPress press) {
        super(leftScreenPos + tabPositionIndex * (TAB_WIDTH + 1),
              calculateYPosition(topScreenPos, displayMode),
              NEXT_TABS_BUTTON_WIDTH, NEXT_TABS_BUTTON_HEIGHT, Component.literal(""), press, DEFAULT_NARRATION);
        this.tabPositionIndex = tabPositionIndex;
        this.displayMode = displayMode;
    }

    private static int calculateYPosition(int topScreenPos, TabDisplayMode displayMode) {
        // Use the same positioning logic as TabButton
        return displayMode == TabDisplayMode.INVERTED ?
            topScreenPos :
            topScreenPos - TAB_HEIGHT;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partial) {
        // Apply animation offset for tuck mode
        int animatedY = this.getY() + TabsMenu.getAnimatedYOffset();

        // Check if mouse is over the animated position
        boolean isMouseOverAnimated = mouseX >= this.getX() && mouseX < this.getX() + this.width &&
                                     mouseY >= animatedY && mouseY < animatedY + this.height;

        int texOffsetX = 0;
        if (isMouseOverAnimated)
            texOffsetX = 54;

        gui.blit(TAB_ICONS, this.getX(), animatedY, NEXT_TABS_ICON_TEX_X + texOffsetX, NEXT_TABS_ICON_TEX_Y, NEXT_TABS_BUTTON_WIDTH, NEXT_TABS_BUTTON_HEIGHT);
    }

    public void updatePosition(int leftScreenPos, int topScreenPos) {
        setX(leftScreenPos + tabPositionIndex * (TAB_WIDTH + 1));
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
