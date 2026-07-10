package vodmordia.modtabs.api.tabs_menu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Supplier;

/**
 * A specialized version of SimpleItemTab that allows custom scaling for the item icon.
 * Use this for tabs that need their item icons rendered at a different scale than the default.
 */
@OnlyIn(Dist.CLIENT)
public abstract class ScaledItemTab extends TabBase {
    private final Supplier<ItemStack> iconItemSupplier;
    private final float scale;
    private final int iconX;
    private final int iconY;

    /**
     * Creates a scaled item tab with custom positioning and scale.
     *
     * @param iconItemSupplier Supplier for the item to display as the icon
     * @param iconX X position of the icon within the tab (default: 5)
     * @param iconY Y position of the icon within the tab (default: 4)
     * @param scale Scale factor for the item icon (default: 1.0f)
     */
    public ScaledItemTab(Supplier<ItemStack> iconItemSupplier, int iconX, int iconY, float scale) {
        this.iconItemSupplier = iconItemSupplier;
        this.iconX = iconX;
        this.iconY = iconY;
        this.scale = scale;
    }

    /**
     * Creates a scaled item tab with default positioning (5, 4) and custom scale.
     *
     * @param iconItemSupplier Supplier for the item to display as the icon
     * @param scale Scale factor for the item icon
     */
    public ScaledItemTab(Supplier<ItemStack> iconItemSupplier, float scale) {
        this(iconItemSupplier, 5, 4, scale);
    }

    @Override
    public void render(GuiGraphics gui, int x, int y, boolean hover) {
        TabRenderer.builder()
            .withBackground()
            .withItemIcon(iconItemSupplier.get(), iconX, iconY, scale)
            .render(gui, x, y, hover, false);
    }

    @Override
    protected void renderInverted(GuiGraphics gui, int x, int y, boolean hover) {
        TabRenderer.builder()
            .withBackground()
            .withItemIcon(iconItemSupplier.get(), iconX, iconY, scale)
            .render(gui, x, y, hover, true);
    }
}