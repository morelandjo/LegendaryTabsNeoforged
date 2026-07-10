package vodmordia.modtabs.api.tabs_menu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import vodmordia.modtabs.config.ModTabsConfig;
import vodmordia.modtabs.utils.IconResolver;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Base class for tabs that support custom icons from config.
 * The custom-icon config string is read dynamically from {@link ModTabsConfig} on every
 * render via the subclass's {@code @TabConfig} annotation, so changing the config at
 * runtime (e.g. through the layout-editor dropdown) immediately updates the rendered
 * icon without requiring tab re-registration.
 */
@OnlyIn(Dist.CLIENT)
public abstract class ConfigurableIconTab extends SimpleTextureTab {

    private final ResourceLocation defaultIcon;
    private final String tabId;
    private final int defaultU;
    private final int defaultV;
    private final int defaultWidth;
    private final int defaultHeight;
    private final int defaultTextureWidth;
    private final int defaultTextureHeight;
    private String lastResolvedFor;
    private ResourceLocation lastResolved;

    protected ConfigurableIconTab(ResourceLocation defaultIcon, String customIconConfig, String tabId) {
        this(defaultIcon, customIconConfig, tabId, 0, 0, 16, 16, 16, 16);
    }

    protected ConfigurableIconTab(ResourceLocation defaultIcon, String customIconConfig, String tabId,
                                  int u, int v, int width, int height, int textureWidth, int textureHeight) {
        super(defaultIcon);
        this.defaultIcon = defaultIcon;
        this.tabId = tabId;
        this.defaultU = u;
        this.defaultV = v;
        this.defaultWidth = width;
        this.defaultHeight = height;
        this.defaultTextureWidth = textureWidth;
        this.defaultTextureHeight = textureHeight;
        // customIconConfig is captured-at-construction and therefore stale after a runtime
        // config change. We keep the parameter for API compatibility but do the lookup
        // dynamically in currentIcon().
    }

    private ResourceLocation currentIcon() {
        String cfg = readCurrentCustomIconConfig();
        if (!Objects.equals(cfg, lastResolvedFor)) {
            lastResolvedFor = cfg;
            ResourceLocation custom = IconResolver.resolveIcon(cfg, tabId);
            lastResolved = custom != null ? custom : defaultIcon;
        }
        return lastResolved;
    }

    private String readCurrentCustomIconConfig() {
        try {
            TabConfig tc = this.getClass().getAnnotation(TabConfig.class);
            if (tc == null) return null;
            String key = tc.configKey();
            Field f = ModTabsConfig.class.getField(key + "CustomIcon");
            Object v = f.get(null);
            return v instanceof String s ? s : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public void render(@NotNull GuiGraphics gui, int x, int y, boolean hover) {
        int[] nudge = currentIconNudge();
        ResourceLocation icon = currentIcon();
        TabRenderer renderer = TabRenderer.builder().withBackground();
        if (icon.equals(defaultIcon)) {
            renderer.withTextureIcon(icon, 5, 4, defaultU, defaultV, defaultWidth, defaultHeight,
                    defaultTextureWidth, defaultTextureHeight);
        } else {
            renderer.withTextureIcon(icon, 5, 4, 16, 16);
        }
        renderer
                .withIconScale(currentIconScale())
                .withIconNudge(nudge[0], nudge[1])
                .render(gui, x, y, hover, false);
    }

    @Override
    protected void renderInverted(@NotNull GuiGraphics gui, int x, int y, boolean hover) {
        int[] nudge = currentIconNudge();
        ResourceLocation icon = currentIcon();
        TabRenderer renderer = TabRenderer.builder().withBackground();
        if (icon.equals(defaultIcon)) {
            renderer.withTextureIcon(icon, 5, 4, defaultU, defaultV, defaultWidth, defaultHeight,
                    defaultTextureWidth, defaultTextureHeight);
        } else {
            renderer.withTextureIcon(icon, 5, 4, 16, 16);
        }
        renderer
                .withIconScale(currentIconScale())
                .withIconNudge(nudge[0], nudge[1])
                .render(gui, x, y, hover, true);
    }

    private float currentIconScale() {
        try {
            TabConfig tc = this.getClass().getAnnotation(TabConfig.class);
            if (tc == null) return 1.0f;
            String key = tc.configKey();
            Field f = ModTabsConfig.class.getField(key + "IconScale");
            Object v = f.get(null);
            if (v instanceof Integer i) return Math.max(0, i) / 100f;
        } catch (Exception ignored) {}
        return 1.0f;
    }

    private int[] currentIconNudge() {
        try {
            TabConfig tc = this.getClass().getAnnotation(TabConfig.class);
            if (tc == null) return new int[]{0, 0};
            String key = tc.configKey();
            int up = readNudge(key + "IconNudgeUp");
            int down = readNudge(key + "IconNudgeDown");
            int left = readNudge(key + "IconNudgeLeft");
            int right = readNudge(key + "IconNudgeRight");
            return new int[]{ right - left, down - up };
        } catch (Exception ignored) {}
        return new int[]{0, 0};
    }

    private static int readNudge(String fieldName) {
        try {
            Field f = ModTabsConfig.class.getField(fieldName);
            Object v = f.get(null);
            if (v instanceof Integer i) return i;
        } catch (Exception ignored) {}
        return 0;
    }
}
