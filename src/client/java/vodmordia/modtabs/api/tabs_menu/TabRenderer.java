package vodmordia.modtabs.api.tabs_menu;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.item.ItemStack;
import vodmordia.modtabs.ModTabs;

import java.util.function.Consumer;

/**
 * Centralized tab rendering utility that handles all common tab rendering patterns.
 * Supports both normal and inverted rendering with consistent behavior across all tabs.
 */
public class TabRenderer {

    // Common constants used by all tabs
    public static final Identifier TAB_TEXTURE = new Identifier(ModTabs.MOD_ID, "textures/gui/tab_menu_buttons.png");
    public static final int TAB_BACKGROUND_U = 0;
    public static final int TAB_BACKGROUND_V = 138;
    public static final int HOVER_OFFSET = 54;

    // Builder for fluent API
    private boolean hasBackground = false;
    private Identifier iconTexture = null;
    private int iconX, iconY, iconWidth, iconHeight;
    private int iconU, iconV, iconTextureWidth, iconTextureHeight;
    private ItemStack iconItem = null;
    private int itemX, itemY;
    private float itemScale = 1.0f;
    private Consumer<RenderContext> customIconRenderer = null;

    private TabRenderer() {}

    public static TabRenderer builder() {
        return new TabRenderer();
    }

    /**
     * Adds the standard tab background
     */
    public TabRenderer withBackground() {
        this.hasBackground = true;
        return this;
    }

    /**
     * Adds a texture icon at the specified position
     */
    public TabRenderer withTextureIcon(Identifier texture, int x, int y, int width, int height) {
        this.iconTexture = texture;
        this.iconX = x;
        this.iconY = y;
        this.iconWidth = width;
        this.iconHeight = height;
        this.iconU = 0;
        this.iconV = 0;
        this.iconTextureWidth = width;
        this.iconTextureHeight = height;
        return this;
    }

    /**
     * Adds a texture icon with custom UV coordinates
     */
    public TabRenderer withTextureIcon(Identifier texture, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight) {
        this.iconTexture = texture;
        this.iconX = x;
        this.iconY = y;
        this.iconU = u;
        this.iconV = v;
        this.iconWidth = width;
        this.iconHeight = height;
        this.iconTextureWidth = textureWidth;
        this.iconTextureHeight = textureHeight;
        return this;
    }

    /**
     * Adds an ItemStack icon at the specified position
     */
    public TabRenderer withItemIcon(ItemStack item, int x, int y) {
        this.iconItem = item;
        this.itemX = x;
        this.itemY = y;
        this.itemScale = 1.0f;
        return this;
    }

    /**
     * Adds an ItemStack icon at the specified position with custom scale
     */
    public TabRenderer withItemIcon(ItemStack item, int x, int y, float scale) {
        this.iconItem = item;
        this.itemX = x;
        this.itemY = y;
        this.itemScale = scale;
        return this;
    }

    /**
     * Adds a custom icon renderer for complex cases
     */
    public TabRenderer withCustomIcon(Consumer<RenderContext> renderer) {
        this.customIconRenderer = renderer;
        return this;
    }

    /**
     * Renders the tab with all specified components
     */
    public void render(DrawContext gui, int x, int y, boolean hover, boolean inverted) {
        if (hasBackground) {
            renderBackground(gui, x, y, hover, inverted);
        }

        if (iconTexture != null) {
            renderTextureIcon(gui, x, y, inverted);
        } else if (iconItem != null) {
            renderItemIcon(gui, x, y, inverted);
        } else if (customIconRenderer != null) {
            renderCustomIcon(gui, x, y, hover, inverted);
        }
    }

    private void renderBackground(DrawContext gui, int x, int y, boolean hover, boolean inverted) {
        int hoverOffset = hover ? HOVER_OFFSET : 0;

        if (inverted) {
            // Render rotated background
            gui.getMatrices().push();
            gui.getMatrices().translate(x + TabBase.TAB_WIDTH / 2.0f, y + TabBase.TAB_HEIGHT / 2.0f, 0);
            gui.getMatrices().multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Z.rotationDegrees(180));
            gui.getMatrices().translate(-TabBase.TAB_WIDTH / 2.0f, -TabBase.TAB_HEIGHT / 2.0f, 0);
            gui.drawTexture(TAB_TEXTURE, 0, 0, TAB_BACKGROUND_U + hoverOffset, TAB_BACKGROUND_V, TabBase.TAB_WIDTH, TabBase.TAB_HEIGHT, 256, 256);
            gui.getMatrices().pop();
        } else {
            // Render normal background
            gui.drawTexture(TAB_TEXTURE, x, y, TAB_BACKGROUND_U + hoverOffset, TAB_BACKGROUND_V, TabBase.TAB_WIDTH, TabBase.TAB_HEIGHT, 256, 256);
        }
    }

    private void renderTextureIcon(DrawContext gui, int x, int y, boolean inverted) {
        // Icons always render upright, regardless of tab orientation
        // Move icon up 3px when tabs are inverted
        int yOffset = inverted ? -3 : 0;
        gui.drawTexture(iconTexture, x + iconX, y + iconY + yOffset, iconU, iconV, iconWidth, iconHeight, iconTextureWidth, iconTextureHeight);
    }

    private void renderItemIcon(DrawContext gui, int x, int y, boolean inverted) {
        // Items always render upright, regardless of tab orientation
        // Move icon up 3px when tabs are inverted
        int yOffset = inverted ? -3 : 0;
        if (itemScale != 1.0f) {
            gui.getMatrices().push();
            gui.getMatrices().translate(x + itemX + 8, y + itemY + yOffset + 8, 0); // Center around item center
            gui.getMatrices().scale(itemScale, itemScale, 1.0f);
            gui.getMatrices().translate(-8, -8, 0); // Move back to render position
            gui.drawItem(iconItem, 0, 0);
            gui.getMatrices().pop();
        } else {
            gui.drawItem(iconItem, x + itemX, y + itemY + yOffset);
        }
    }

    private void renderCustomIcon(DrawContext gui, int x, int y, boolean hover, boolean inverted) {
        // Custom icons always render upright, regardless of tab orientation
        RenderContext context = new RenderContext(gui, x, y, hover, inverted);
        customIconRenderer.accept(context);
    }

    /**
     * Context object passed to custom icon renderers
     */
    public static class RenderContext {
        public final DrawContext gui;
        public final int x, y;
        public final boolean hover, inverted;

        public RenderContext(DrawContext gui, int x, int y, boolean hover, boolean inverted) {
            this.gui = gui;
            this.x = x;
            this.y = y;
            this.hover = hover;
            this.inverted = inverted;
        }
    }
}