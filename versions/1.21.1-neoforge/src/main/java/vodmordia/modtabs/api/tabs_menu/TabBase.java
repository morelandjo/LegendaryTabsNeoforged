package vodmordia.modtabs.api.tabs_menu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;


@OnlyIn(Dist.CLIENT)
public abstract class TabBase {
    public static final int TAB_HEIGHT = 22;
    public static final int TAB_WIDTH = 26;

    public static int primaryAxisSize() {
        return TAB_WIDTH;
    }

    public static int crossAxisSize() {
        return TAB_HEIGHT;
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


    protected void renderInverted(GuiGraphics gui, int x, int y, boolean hover) {
        // Default: subclasses should override for custom inverted rendering
        // Most tabs can use TabRenderer which handles inversion automatically
        render(gui, x, y, hover);
    }


    public abstract boolean isCurrentlyUsed(Screen currentScreen);

    /**
     * True when this tab represents the screen the user is currently viewing — used by the
     * long-press-to-edit gesture so only the home/active tab triggers it.
     *
     * Defaults to {@link #isCurrentlyUsed}; tabs that override that to always return false
     * (so the tab stays clickable for "refresh" semantics — Inventory, FTB Quests, FTB Teams,
     * CustomJson) should override this method to return the actual home-screen check.
     */
    public boolean isHomeTab(Screen currentScreen) {
        return isCurrentlyUsed(currentScreen);
    }

    public abstract Component getTooltip();

    public int getOverrideOrder() {
        return TabRegistry.getTabOrder(this);
    }

    /**
     * True when this tab is pinned to the leading end of the bar and kept visible across
     * pagination. Backed by the per-tab {@code <configKey>Sticky} flag, toggled via the
     * Global Settings "Sticky" panel. Any tab can be sticky; defaults to false for tabs
     * without the flag (e.g. dynamic nearby-container tabs).
     */
    public boolean isSticky() {
        return TabRegistry.isTabSticky(this);
    }
}
