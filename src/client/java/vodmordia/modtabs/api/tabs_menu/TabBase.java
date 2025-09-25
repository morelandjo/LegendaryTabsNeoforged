package vodmordia.modtabs.api.tabs_menu;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.entity.player.PlayerEntity;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.config.Config;
import net.minecraft.client.gui.screen.Screen;


public abstract class TabBase {
    public static final int TAB_HEIGHT = 22;
    public static final int TAB_WIDTH = 26;

    public TabBase() {
    }

    public abstract void openTargetScreen(PlayerEntity player);

    public abstract boolean isEnabled(PlayerEntity player);

    public abstract void initTabOnScreens();

    public abstract void render(DrawContext gui, int x, int y, boolean hover);

    public void render(DrawContext gui, int x, int y, boolean hover, TabDisplayMode displayMode) {
        if (displayMode == TabDisplayMode.INVERTED) {
            renderInverted(gui, x, y, hover);
        } else {
            render(gui, x, y, hover);
        }
    }

    protected void renderInverted(DrawContext gui, int x, int y, boolean hover) {
        // Default: use TabRenderer for inverted rendering
        getTabRenderer().render(gui, x, y, hover, true);
    }

    /**
     * Get the tab renderer for this tab. Subclasses should override this.
     */
    protected TabRenderer getTabRenderer() {
        // Default renderer with just background
        return TabRenderer.builder()
                .withBackground();
    }

    /**
     * Helper method to render tab with icon
     */
    protected void renderWithIcon(DrawContext gui, int x, int y, boolean hover, net.minecraft.util.Identifier iconTexture) {
        TabRenderer.builder()
                .withBackground()
                .withTextureIcon(iconTexture, 5, 3, 16, 16)
                .render(gui, x, y, hover, false);
    }

    /**
     * Helper method to render tab with item stack
     */
    protected void renderWithItem(DrawContext gui, int x, int y, boolean hover, net.minecraft.item.ItemStack itemStack) {
        TabRenderer.builder()
                .withBackground()
                .withItemIcon(itemStack, 5, 3)
                .render(gui, x, y, hover, false);
    }


    public abstract boolean isCurrentlyUsed(Screen currentScreen);

    public abstract Text getTooltip();

    public int getOverrideOrder() {
        return TabRegistry.getTabOrder(this);
    }
}