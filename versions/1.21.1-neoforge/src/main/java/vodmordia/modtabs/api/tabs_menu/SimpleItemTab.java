package vodmordia.modtabs.api.tabs_menu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Supplier;

/**
 * Base class for tabs that simply render an item icon
 */
@OnlyIn(Dist.CLIENT)
public abstract class SimpleItemTab extends TabBase {

    private final Supplier<ItemStack> iconItemSupplier;

    /**
     * Creates a simple item tab with a fixed item
     */
    protected SimpleItemTab(ItemStack iconItem) {
        this(() -> iconItem);
    }

    /**
     * Creates a simple item tab with a dynamic item supplier
     * This is useful for items that might change or need to be resolved at render time
     */
    protected SimpleItemTab(Supplier<ItemStack> iconItemSupplier) {
        this.iconItemSupplier = iconItemSupplier;
    }

    @Override
    public void render(GuiGraphics gui, int x, int y, boolean hover) {
        TabRenderer.builder()
            .withBackground()
            .withItemIcon(getIconItem(), 5, 4)
            .render(gui, x, y, hover, false);
    }

    @Override
    protected void renderInverted(GuiGraphics gui, int x, int y, boolean hover) {
        TabRenderer.builder()
            .withBackground()
            .withItemIcon(getIconItem(), 5, 4)
            .render(gui, x, y, hover, true);
    }

    /**
     * Get the current icon item for this tab
     */
    protected ItemStack getIconItem() {
        return iconItemSupplier.get();
    }
}