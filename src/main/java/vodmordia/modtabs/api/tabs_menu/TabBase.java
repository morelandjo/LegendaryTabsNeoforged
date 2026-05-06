package vodmordia.modtabs.api.tabs_menu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.config.ModTabsConfig;


public abstract class TabBase {
    public static final int TAB_HEIGHT = 22;
    public static final int TAB_WIDTH = 26;

    // Rotated dimensions for vertical (left/right edge) tab placement
    public static final int TAB_WIDTH_VERTICAL = 22;
    public static final int TAB_HEIGHT_VERTICAL = 26;

    public static int primaryAxisSize(TabPositioning positioning) {
        return positioning != null && positioning.isVertical() ? TAB_HEIGHT_VERTICAL : TAB_WIDTH;
    }

    public static int crossAxisSize(TabPositioning positioning) {
        return positioning != null && positioning.isVertical() ? TAB_WIDTH_VERTICAL : TAB_HEIGHT;
    }

    public TabBase() {
    }

    public abstract void openTargetScreen(Player player);

    public abstract boolean isEnabled(Player player);

    public abstract void initTabOnScreens();

    public abstract void render(GuiGraphics gui, int x, int y, boolean hover);

    public void render(GuiGraphics gui, int x, int y, boolean hover, TabDisplayMode displayMode) {
        if (displayMode == TabDisplayMode.INVERTED) {
            renderInverted(gui, x, y, hover);
        } else {
            render(gui, x, y, hover);
        }
    }

    public void render(GuiGraphics gui, int x, int y, boolean hover, TabDisplayMode displayMode, TabPositioning positioning) {
        render(gui, x, y, hover, displayMode);
    }

    protected void renderInverted(GuiGraphics gui, int x, int y, boolean hover) {
        // Default: subclasses should override for custom inverted rendering
        // Most tabs can use TabRenderer which handles inversion automatically
        render(gui, x, y, hover);
    }


    public abstract boolean isCurrentlyUsed(Screen currentScreen);

    public abstract Component getTooltip();

    public int getOverrideOrder() {
        return TabRegistry.getTabOrder(this);
    }
}
