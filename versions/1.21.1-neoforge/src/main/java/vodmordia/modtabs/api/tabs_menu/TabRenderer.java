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
    public static final ResourceLocation TAB_TEXTURE = ResourceLocation.fromNamespaceAndPath(ModTabs.MOD_ID, "textures/gui/tab_menu_buttons.png");
    public static final int TAB_BACKGROUND_U = 0;
    public static final int TAB_BACKGROUND_V = 138;
    public static final int HOVER_OFFSET = 54;

    // Builder for fluent API
    private boolean hasBackground = false;
    private ResourceLocation iconTexture = null;
    private int iconWidth, iconHeight;
    private int iconU, iconV, iconTextureWidth, iconTextureHeight;
    private ItemStack iconItem = null;
    private float itemScale = 1.0f;
    private Consumer<RenderContext> customIconRenderer = null;
    /** -1 means "use tab geometric center". Otherwise offsets from tab top-left. */
    private int customIconCenterX = -1;
    private int customIconCenterY = -1;
    /** Per-tab icon scale (1.0 = 100%). Applied around the icon's geometric center. */
    private float iconScale = 1.0f;
    /** Per-tab icon translation in screen pixels (positive y = down, positive x = right). */
    private int iconNudgeX = 0;
    private int iconNudgeY = 0;

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
        this.itemScale = 1.0f;
        return this;
    }

    /**
     * Adds an ItemStack icon at the specified position with custom scale
     */
    public TabRenderer withItemIcon(ItemStack item, int x, int y, float scale) {
        this.iconItem = item;
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
     * For custom-icon renderers whose drawn icon doesn't visually center on the tab
     * (e.g. a 14×14 icon at offset (6,6) lands centered at (13,13) inside a 26×22 tab,
     * not at the tab center (13,11)). Pass the icon's actual center offsets so
     * iconRotation pivots around the icon's center instead of looking corner-y.
     */
    public TabRenderer withCustomIcon(Consumer<RenderContext> renderer, int iconCenterX, int iconCenterY) {
        this.customIconRenderer = renderer;
        this.customIconCenterX = iconCenterX;
        this.customIconCenterY = iconCenterY;
        return this;
    }

    /**
     * Per-tab icon scale (1.0 = 100%). Scales the icon (texture, item, or custom) around
     * its visual center, leaving the tab base unchanged.
     */
    public TabRenderer withIconScale(float scale) {
        this.iconScale = scale;
        return this;
    }

    /** Per-tab icon nudge in pixels (dx positive = right, dy positive = down). Translates
     *  the icon (texture, item, or custom) without moving the tab base. */
    public TabRenderer withIconNudge(int dx, int dy) {
        this.iconNudgeX = dx;
        this.iconNudgeY = dy;
        return this;
    }

    /**
     * Renders the tab with all specified components.
     */
    public void render(GuiGraphics gui, int x, int y, boolean hover, boolean inverted) {
        // Panel preview: render the tab base behind the icon, but never inverted, so the
        // user sees the icon at its natural pose. (currentIconRotation() also short-circuits
        // to 0 while previewRendering is true.)
        if (TabsMenu.previewRendering) {
            inverted = false;
        }
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

    private void renderBackground(GuiGraphics gui, int x, int y, boolean hover, boolean inverted) {
        int hoverOffset = hover ? HOVER_OFFSET : 0;

        if (inverted) {
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

    private void renderTextureIcon(GuiGraphics gui, int x, int y, boolean inverted) {
        // Geometrically center the icon inside the tab. The per-tab iconX/iconY values
        // used to be additive offsets; ignoring them means every tab gets perfectly
        // centered, which is what we want.
        int tabW = TabBase.TAB_WIDTH;
        int tabH = TabBase.TAB_HEIGHT;
        int dx = TabsMenu.previewRendering ? 0 : (vodmordia.modtabs.config.Config.Baked.iconOffsetLeft - vodmordia.modtabs.config.Config.Baked.iconOffsetRight);
        int dy = TabsMenu.previewRendering ? 0 : (vodmordia.modtabs.config.Config.Baked.iconOffsetTop - vodmordia.modtabs.config.Config.Baked.iconOffsetBottom);
        int finalX = x + (tabW - iconWidth) / 2 + dx + iconNudgeX;
        int finalY = y + (tabH - iconHeight) / 2 + dy + iconNudgeY;
        int iconRot = TabsMenu.currentIconRotation();
        boolean rotated = iconRot != 0;
        boolean scaled = iconScale != 1.0f;
        int cx = finalX + iconWidth / 2;
        int cy = finalY + iconHeight / 2;
        if (rotated || scaled) {
            gui.pose().pushPose();
            gui.pose().translate(cx, cy, 0);
            if (rotated) gui.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(iconRot));
            if (scaled) gui.pose().scale(iconScale, iconScale, 1.0f);
            gui.pose().translate(-cx, -cy, 0);
        }
        gui.blit(iconTexture, finalX, finalY, iconU, iconV, iconWidth, iconHeight, iconTextureWidth, iconTextureHeight);
        if (rotated || scaled) {
            gui.pose().popPose();
        }
    }

    private void renderItemIcon(GuiGraphics gui, int x, int y, boolean inverted) {
        int tabW = TabBase.TAB_WIDTH;
        int tabH = TabBase.TAB_HEIGHT;
        int dx = TabsMenu.previewRendering ? 0 : (vodmordia.modtabs.config.Config.Baked.iconOffsetLeft - vodmordia.modtabs.config.Config.Baked.iconOffsetRight);
        int dy = TabsMenu.previewRendering ? 0 : (vodmordia.modtabs.config.Config.Baked.iconOffsetTop - vodmordia.modtabs.config.Config.Baked.iconOffsetBottom);
        int finalX = x + (tabW - 16) / 2 + dx + iconNudgeX;
        int finalY = y + (tabH - 16) / 2 + dy + iconNudgeY;
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
        float effectiveScale = itemScale * iconScale;
        if (effectiveScale != 1.0f) {
            gui.pose().pushPose();
            gui.pose().translate(finalX + 8, finalY + 8, 0);
            gui.pose().scale(effectiveScale, effectiveScale, 1.0f);
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

    private void renderCustomIcon(GuiGraphics gui, int x, int y, boolean hover, boolean inverted) {
        int iconRot = TabsMenu.currentIconRotation();
        boolean rotated = iconRot != 0;
        boolean scaled = iconScale != 1.0f;
        boolean nudged = iconNudgeX != 0 || iconNudgeY != 0;
        if (nudged) {
            gui.pose().pushPose();
            gui.pose().translate(iconNudgeX, iconNudgeY, 0);
        }
        if (rotated || scaled) {
            // Pivot/scale around the icon's actual center if the tab declared one; otherwise
            // fall back to the tab's geometric center. Tabs whose Consumer draws away
            // from the tab center (e.g. Reskillable's 14×14 icon at (6,6)) MUST declare
            // an explicit center via withCustomIcon(renderer, cx, cy) or the rotation
            // visibly pivots near a corner of the icon.
            int defaultCx = TabBase.TAB_WIDTH / 2;
            int defaultCy = TabBase.TAB_HEIGHT / 2;
            int cx = x + (customIconCenterX >= 0 ? customIconCenterX : defaultCx);
            int cy = y + (customIconCenterY >= 0 ? customIconCenterY : defaultCy);
            gui.pose().pushPose();
            gui.pose().translate(cx, cy, 0);
            if (rotated) gui.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(iconRot));
            if (scaled) gui.pose().scale(iconScale, iconScale, 1.0f);
            gui.pose().translate(-cx, -cy, 0);
        }
        RenderContext context = new RenderContext(gui, x, y, hover, inverted);
        customIconRenderer.accept(context);
        if (rotated || scaled) {
            gui.pose().popPose();
        }
        if (nudged) {
            gui.pose().popPose();
        }
    }

    /**
     * Context object passed to custom icon renderers
     */
    public static class RenderContext {
        public final GuiGraphics gui;
        public final int x, y;
        public final boolean hover, inverted;

        public RenderContext(GuiGraphics gui, int x, int y, boolean hover, boolean inverted) {
            this.gui = gui;
            this.x = x;
            this.y = y;
            this.hover = hover;
            this.inverted = inverted;
        }
    }
}
