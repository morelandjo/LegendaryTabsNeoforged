package vodmordia.modtabs.api.tabs_menu;

import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import java.util.function.Consumer;

/**
 * Base class for tabs that need custom icon rendering beyond simple textures or items.
 * Provides the TabRenderer framework with custom icon rendering capability.
 */
@OnlyIn(Dist.CLIENT)
public abstract class CustomIconTab extends TabBase {
    private final Consumer<TabRenderer.RenderContext> iconRenderer;
    /** -1 = use tab geometric center as the icon-rotation pivot. */
    private final int iconCenterX;
    private final int iconCenterY;

    public CustomIconTab(Consumer<TabRenderer.RenderContext> iconRenderer) {
        this(iconRenderer, -1, -1);
    }

    /**
     * @param iconCenterX X offset (from tab top-left) of the icon's drawn center,
     *                    used as the pivot for {@code currentIconRotation()}. Pass -1
     *                    to default to the tab's geometric center.
     * @param iconCenterY Y offset of the icon's drawn center.
     */
    public CustomIconTab(Consumer<TabRenderer.RenderContext> iconRenderer, int iconCenterX, int iconCenterY) {
        super();
        this.iconRenderer = iconRenderer;
        this.iconCenterX = iconCenterX;
        this.iconCenterY = iconCenterY;
    }

    private TabRenderer renderer() {
        TabRenderer r = TabRenderer.builder().withBackground();
        if (iconCenterX >= 0 && iconCenterY >= 0) {
            r.withCustomIcon(iconRenderer, iconCenterX, iconCenterY);
        } else {
            r.withCustomIcon(iconRenderer);
        }
        return r;
    }

    @Override
    public void render(GuiGraphics gui, int x, int y, boolean hover) {
        renderer().render(gui, x, y, hover, false);
    }

    @Override
    protected void renderInverted(GuiGraphics gui, int x, int y, boolean hover) {
        renderer().render(gui, x, y, hover, true);
    }
}