package vodmordia.modtabs.api.tabs_menu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Base class for tabs that simply render a texture icon
 */
@OnlyIn(Dist.CLIENT)
public abstract class SimpleTextureTab extends TabBase {

    private final ResourceLocation iconTexture;
    private final int iconWidth;
    private final int iconHeight;

    /**
     * Creates a simple texture tab with 16x16 icon at position (5, 4)
     */
    protected SimpleTextureTab(ResourceLocation iconTexture) {
        this(iconTexture, 16, 16);
    }

    /**
     * Creates a simple texture tab with custom icon size at position (5, 4)
     */
    protected SimpleTextureTab(ResourceLocation iconTexture, int iconWidth, int iconHeight) {
        this.iconTexture = iconTexture;
        this.iconWidth = iconWidth;
        this.iconHeight = iconHeight;
    }

    @Override
    public void render(GuiGraphics gui, int x, int y, boolean hover) {
        TabRenderer.builder()
            .withBackground()
            .withTextureIcon(iconTexture, 5, 4, iconWidth, iconHeight)
            .render(gui, x, y, hover, false);
    }

    @Override
    protected void renderInverted(GuiGraphics gui, int x, int y, boolean hover) {
        TabRenderer.builder()
            .withBackground()
            .withTextureIcon(iconTexture, 5, 4, iconWidth, iconHeight)
            .render(gui, x, y, hover, true);
    }

    /**
     * Get the icon texture for this tab
     */
    protected ResourceLocation getIconTexture() {
        return iconTexture;
    }
}