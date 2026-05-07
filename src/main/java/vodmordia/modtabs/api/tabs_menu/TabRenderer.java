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
        // Panel preview: just the icon, no background, never inverted, never vertical.
        // (currentIconRotation() and isCurrentVertical() also short-circuit to 0/false
        // while TabsMenu.previewRendering is true.)
        if (TabsMenu.previewRendering) {
            inverted = false;
            vertical = false;
        } else if (hasBackground) {
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
        // Geometrically center the icon inside the tab regardless of orientation. The
        // per-tab iconX/iconY values used to be additive offsets; ignoring them means
        // every tab gets perfectly centered, which is what we want.
        int tabW = vertical ? TabBase.TAB_WIDTH_VERTICAL : TabBase.TAB_WIDTH;
        int tabH = vertical ? TabBase.TAB_HEIGHT_VERTICAL : TabBase.TAB_HEIGHT;
        int dx = TabsMenu.previewRendering ? 0 : (vodmordia.modtabs.config.Config.Baked.iconOffsetLeft - vodmordia.modtabs.config.Config.Baked.iconOffsetRight);
        int dy = TabsMenu.previewRendering ? 0 : (vodmordia.modtabs.config.Config.Baked.iconOffsetTop - vodmordia.modtabs.config.Config.Baked.iconOffsetBottom);
        int finalX = x + (tabW - iconWidth) / 2 + dx;
        int finalY = y + (tabH - iconHeight) / 2 + dy;
        int iconRot = TabsMenu.currentIconRotation();
        if (iconRot != 0) {
            int cx = finalX + iconWidth / 2;
            int cy = finalY + iconHeight / 2;
            gui.pose().pushPose();
            gui.pose().translate(cx, cy, 0);
            gui.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(iconRot));
            gui.pose().translate(-cx, -cy, 0);
            gui.blit(iconTexture, finalX, finalY, iconU, iconV, iconWidth, iconHeight, iconTextureWidth, iconTextureHeight);
            gui.pose().popPose();
        } else {
            gui.blit(iconTexture, finalX, finalY, iconU, iconV, iconWidth, iconHeight, iconTextureWidth, iconTextureHeight);
        }
    }

    private void renderItemIcon(GuiGraphics gui, int x, int y, boolean inverted, boolean vertical) {
        int tabW = vertical ? TabBase.TAB_WIDTH_VERTICAL : TabBase.TAB_WIDTH;
        int tabH = vertical ? TabBase.TAB_HEIGHT_VERTICAL : TabBase.TAB_HEIGHT;
        int dx = TabsMenu.previewRendering ? 0 : (vodmordia.modtabs.config.Config.Baked.iconOffsetLeft - vodmordia.modtabs.config.Config.Baked.iconOffsetRight);
        int dy = TabsMenu.previewRendering ? 0 : (vodmordia.modtabs.config.Config.Baked.iconOffsetTop - vodmordia.modtabs.config.Config.Baked.iconOffsetBottom);
        int finalX = x + (tabW - 16) / 2 + dx;
        int finalY = y + (tabH - 16) / 2 + dy;
        int iconRot = TabsMenu.currentIconRotation();
        boolean rotated = iconRot != 0;
        if (rotated) {
            gui.pose().pushPose();
            int cx = finalX + 8;
            int cy = finalY + 8;
            gui.pose().translate(cx, cy, 0);
            gui.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(iconRot));
            gui.pose().translate(-cx, -cy, 0);
        }
        if (itemScale != 1.0f) {
            gui.pose().pushPose();
            gui.pose().translate(finalX + 8, finalY + 8, 0);
            gui.pose().scale(itemScale, itemScale, 1.0f);
            gui.pose().translate(-8, -8, 0);
            gui.renderItem(iconItem, 0, 0);
            gui.pose().popPose();
        } else {
            gui.renderItem(iconItem, finalX, finalY);
        }
        if (rotated) {
            gui.pose().popPose();
        }
    }

    private void renderCustomIcon(GuiGraphics gui, int x, int y, boolean hover, boolean inverted, boolean vertical) {
        int iconRot = TabsMenu.currentIconRotation();
        boolean rotated = iconRot != 0;
        if (rotated) {
            // Rotate around the tab's natural icon center (offset 13/11 from tab top-left for
            // the standard 26x22 horizontal tab). Custom icons that diverge from this position
            // will appear slightly off-center when rotated, which is acceptable for now.
            int cx = x + (vertical ? TabBase.TAB_WIDTH_VERTICAL : TabBase.TAB_WIDTH) / 2;
            int cy = y + (vertical ? TabBase.TAB_HEIGHT_VERTICAL : TabBase.TAB_HEIGHT) / 2;
            gui.pose().pushPose();
            gui.pose().translate(cx, cy, 0);
            gui.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(iconRot));
            gui.pose().translate(-cx, -cy, 0);
        }
        if (vertical) {
            int dx = (TabBase.TAB_WIDTH_VERTICAL - TabBase.TAB_WIDTH) / 2;
            int dy = (TabBase.TAB_HEIGHT_VERTICAL - TabBase.TAB_HEIGHT) / 2;
            gui.pose().pushPose();
            gui.pose().translate(dx, dy, 0);
            RenderContext context = new RenderContext(gui, x, y, hover, inverted, vertical);
            customIconRenderer.accept(context);
            gui.pose().popPose();
        } else {
            RenderContext context = new RenderContext(gui, x, y, hover, inverted, vertical);
            customIconRenderer.accept(context);
        }
        if (rotated) {
            gui.pose().popPose();
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
