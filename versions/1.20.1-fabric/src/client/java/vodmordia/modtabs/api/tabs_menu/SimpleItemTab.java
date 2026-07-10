package vodmordia.modtabs.api.tabs_menu;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

import java.util.function.Supplier;

/**
 * Base class for tabs that simply render an item icon
 */
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
    public void render(DrawContext gui, int x, int y, boolean hover) {
        renderWithItem(gui, x, y, hover, getIconItem());
    }

    /**
     * Get the current icon item for this tab
     */
    protected ItemStack getIconItem() {
        return iconItemSupplier.get();
    }
}