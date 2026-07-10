package vodmordia.modtabs.api.tabs_menu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import vodmordia.modtabs.config.ModTabsConfig;
import vodmordia.modtabs.utils.IconResolver;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A tab that supports both custom icon configuration and custom rendering logic.
 * If a custom icon is configured, it will be used. Otherwise, the custom rendering logic will be used.
 * Custom icon string is read dynamically from {@link ModTabsConfig} via the subclass's
 * {@code @TabConfig} annotation, so layout-editor changes apply without tab re-registration.
 */
@OnlyIn(Dist.CLIENT)
public abstract class ConfigurableCustomIconTab extends CustomIconTab {
    private final String tabId;
    private final Consumer<TabRenderer.RenderContext> customRenderer;
    private final int iconCenterX;
    private final int iconCenterY;
    private String lastResolvedFor;
    private ResourceLocation lastResolved;

    protected ConfigurableCustomIconTab(Consumer<TabRenderer.RenderContext> customRenderer, String customIconConfig, String tabId) {
        super(customRenderer);
        this.tabId = tabId;
        this.customRenderer = customRenderer;
        this.iconCenterX = -1;
        this.iconCenterY = -1;
    }

    /**
     * Use this overload when the custom Consumer draws its icon away from the tab's
     * geometric center (e.g. an off-center 14×14 icon) — passes the icon's drawn
     * center to {@link CustomIconTab} so {@code currentIconRotation()} pivots correctly.
     */
    protected ConfigurableCustomIconTab(Consumer<TabRenderer.RenderContext> customRenderer,
                                        String customIconConfig, String tabId,
                                        int iconCenterX, int iconCenterY) {
        super(customRenderer, iconCenterX, iconCenterY);
        this.tabId = tabId;
        this.customRenderer = customRenderer;
        this.iconCenterX = iconCenterX;
        this.iconCenterY = iconCenterY;
    }

    @Override
    public void render(@NotNull GuiGraphics gui, int x, int y, boolean hover) {
        ResourceLocation customIcon = currentCustomIcon();
        float scale = currentIconScale();
        int[] n = currentIconNudge();
        boolean nudged = n[0] != 0 || n[1] != 0;
        if (customIcon != null) {
            TabRenderer.builder()
                .withBackground()
                .withTextureIcon(customIcon, 5, 4, 0, 0, 16, 16, 16, 16)
                .withIconScale(scale)
                .withIconNudge(n[0], n[1])
                .render(gui, x, y, hover, false);
        } else if (scale != 1.0f || nudged) {
            // Re-route through TabRenderer so the icon (but not the background) gets transformed.
            TabRenderer r = TabRenderer.builder().withBackground();
            if (iconCenterX >= 0 && iconCenterY >= 0) {
                r.withCustomIcon(customRenderer, iconCenterX, iconCenterY);
            } else {
                r.withCustomIcon(customRenderer);
            }
            r.withIconScale(scale).withIconNudge(n[0], n[1]).render(gui, x, y, hover, false);
        } else {
            super.render(gui, x, y, hover);
        }
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

    private ResourceLocation currentCustomIcon() {
        String cfg = readCurrentCustomIconConfig();
        if (!Objects.equals(cfg, lastResolvedFor)) {
            lastResolvedFor = cfg;
            lastResolved = IconResolver.resolveIcon(cfg, tabId);
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
}
