package vodmordia.modtabs.api.tabs_menu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import vodmordia.modtabs.ModTabs;

import java.util.function.Consumer;

/**
 * Centralized tab rendering utility that handles all common tab rendering patterns.
 * Supports both normal and inverted rendering with consistent behavior across all tabs.
 */
public class TabRenderer {

    // Common constants used by all tabs
    public static final ResourceLocation TAB_TEXTURE = new ResourceLocation(ModTabs.MOD_ID, "textures/gui/tab_menu_buttons.png");
    public static final ResourceLocation TAB_TEXTURE_VERTICAL = new ResourceLocation(ModTabs.MOD_ID, "textures/gui/tab_menu_buttons_vertical.png");
    public static final int TAB_BACKGROUND_U = 0;
    public static final int TAB_BACKGROUND_V = 138;
    public static final int HOVER_OFFSET = 54;

    // After 90° CW rotation of the 256x256 source: the (0,138,26x22) sprite lands at (96,0,22x26)
    // and the hover-variant offset of 54 (originally along U) now runs along V.
    public static final int TAB_BACKGROUND_U_VERTICAL = 96;
    public static final int TAB_BACKGROUND_V_VERTICAL = 0;
    public static final int HOVER_OFFSET_VERTICAL = 54;

    // Builder for fluent API
    private boolean hasBackground = false;
    private ResourceLocation iconTexture = null;
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
    public TabRenderer withTextureIcon(ResourceLocation texture, int x, int y, int width, int height) {
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
    public TabRenderer withTextureIcon(ResourceLocation texture, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight) {
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
     * Renders the tab with all specified components.
     * Vertical orientation is read from {@link TabsMenu#isCurrentVertical()} so existing
     * tab subclasses don't need to know about it explicitly.
     */
    public void render(GuiGraphics gui, int x, int y, boolean hover, boolean inverted) {
        render(gui, x, y, hover, inverted, TabsMenu.isCurrentVertical());
    }

    public void render(GuiGraphics gui, int x, int y, boolean hover, boolean inverted, boolean vertical) {

        if (hasBackground) {
            renderBackground(gui, x, y, hover, inverted, vertical);
        }

        if (iconTexture != null) {
            renderTextureIcon(gui, x, y, inverted, vertical);
        } else if (iconItem != null) {
            renderItemIcon(gui, x, y, inverted, vertical);
        } else if (customIconRenderer != null) {
            renderCustomIcon(gui, x, y, hover, inverted, vertical);
        }

    }

    private void renderBackground(GuiGraphics gui, int x, int y, boolean hover, boolean inverted, boolean vertical) {
        int hoverOffset = hover ? HOVER_OFFSET : 0;

        if (vertical) {
            int verticalHover = hover ? HOVER_OFFSET_VERTICAL : 0;
            gui.blit(TAB_TEXTURE_VERTICAL, x, y, TAB_BACKGROUND_U_VERTICAL, TAB_BACKGROUND_V_VERTICAL + verticalHover, TabBase.TAB_WIDTH_VERTICAL, TabBase.TAB_HEIGHT_VERTICAL);
        } else if (inverted) {
            // Render rotated background
            gui.pose().pushPose();
            gui.pose().translate(x + TabBase.TAB_WIDTH / 2.0f, y + TabBase.TAB_HEIGHT / 2.0f, 0);
            gui.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(180));
            gui.pose().translate(-TabBase.TAB_WIDTH / 2.0f, -TabBase.TAB_HEIGHT / 2.0f, 0);
            gui.blit(TAB_TEXTURE, 0, 0, TAB_BACKGROUND_U + hoverOffset, TAB_BACKGROUND_V, TabBase.TAB_WIDTH, TabBase.TAB_HEIGHT);
            gui.pose().popPose();
        } else {
            // Render normal background
            gui.blit(TAB_TEXTURE, x, y, TAB_BACKGROUND_U + hoverOffset, TAB_BACKGROUND_V, TabBase.TAB_WIDTH, TabBase.TAB_HEIGHT);
        }
    }

    private void renderTextureIcon(GuiGraphics gui, int x, int y, boolean inverted, boolean vertical) {
        // Icons always render upright, regardless of tab orientation
        // Move icon up 3px when tabs are inverted
        int yOffset = inverted ? -3 : 0;
        // For vertical tabs, center the icon within the rotated tab background
        int xCenterOffset = vertical ? (TabBase.TAB_WIDTH_VERTICAL - iconWidth) / 2 - iconX : 0;
        int yCenterOffset = vertical ? (TabBase.TAB_HEIGHT_VERTICAL - iconHeight) / 2 - iconY : 0;
        gui.blit(iconTexture, x + iconX + xCenterOffset, y + iconY + yOffset + yCenterOffset, iconU, iconV, iconWidth, iconHeight, iconTextureWidth, iconTextureHeight);
    }

    private void renderItemIcon(GuiGraphics gui, int x, int y, boolean inverted, boolean vertical) {
        // Items always render upright, regardless of tab orientation
        // Move icon up 3px when tabs are inverted
        int yOffset = inverted ? -3 : 0;
        // For vertical tabs, center the 16x16 item within the rotated tab background
        int xCenterOffset = vertical ? (TabBase.TAB_WIDTH_VERTICAL - 16) / 2 - itemX : 0;
        int yCenterOffset = vertical ? (TabBase.TAB_HEIGHT_VERTICAL - 16) / 2 - itemY : 0;
        if (itemScale != 1.0f) {
            gui.pose().pushPose();
            gui.pose().translate(x + itemX + xCenterOffset + 8, y + itemY + yOffset + yCenterOffset + 8, 0);
            gui.pose().scale(itemScale, itemScale, 1.0f);
            gui.pose().translate(-8, -8, 0);
            gui.renderItem(iconItem, 0, 0);
            gui.pose().popPose();
        } else {
            gui.renderItem(iconItem, x + itemX + xCenterOffset, y + itemY + yOffset + yCenterOffset);
        }
    }

    private void renderCustomIcon(GuiGraphics gui, int x, int y, boolean hover, boolean inverted, boolean vertical) {
        // Custom icons always render upright, regardless of tab orientation.
        // For vertical tabs the footprint is 22x26 instead of 26x22, so existing custom
        // renderers (which hardcode horizontal offsets like x+6, y+5) draw off-center.
        // Apply a single translation to compensate so renderers don't need to know about it.
        if (vertical) {
            int dx = (TabBase.TAB_WIDTH_VERTICAL - TabBase.TAB_WIDTH) / 2;   // -2
            int dy = (TabBase.TAB_HEIGHT_VERTICAL - TabBase.TAB_HEIGHT) / 2; // +2
            gui.pose().pushPose();
            gui.pose().translate(dx, dy, 0);
            RenderContext context = new RenderContext(gui, x, y, hover, inverted, vertical);
            customIconRenderer.accept(context);
            gui.pose().popPose();
        } else {
            RenderContext context = new RenderContext(gui, x, y, hover, inverted, vertical);
            customIconRenderer.accept(context);
        }
    }

    /**
     * Context object passed to custom icon renderers
     */
    public static class RenderContext {
        public final GuiGraphics gui;
        public final int x, y;
        public final boolean hover, inverted, vertical;

        public RenderContext(GuiGraphics gui, int x, int y, boolean hover, boolean inverted) {
            this(gui, x, y, hover, inverted, false);
        }

        public RenderContext(GuiGraphics gui, int x, int y, boolean hover, boolean inverted, boolean vertical) {
            this.gui = gui;
            this.x = x;
            this.y = y;
            this.hover = hover;
            this.inverted = inverted;
            this.vertical = vertical;
        }
    }
}
