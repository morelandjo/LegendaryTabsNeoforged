package vodmordia.modtabs.api.tabs_menu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import vodmordia.modtabs.config.ModTabsConfig;
import vodmordia.modtabs.utils.IconResolver;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Base class for tabs that render an item icon but support custom texture overrides from config.
 * Custom icon string is read dynamically from {@link ModTabsConfig} via the subclass's
 * {@code @TabConfig} annotation, so layout-editor changes apply without tab re-registration.
 */
@OnlyIn(Dist.CLIENT)
public abstract class ConfigurableItemTab extends SimpleItemTab {

    private final String tabId;
    private String lastResolvedFor;
    private ResourceLocation lastResolved;

    protected ConfigurableItemTab(Supplier<ItemStack> iconItemSupplier, String customIconConfig, String tabId) {
        super(iconItemSupplier);
        this.tabId = tabId;
    }

    protected ConfigurableItemTab(ItemStack iconItem, String customIconConfig, String tabId) {
        super(iconItem);
        this.tabId = tabId;
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
            TabRenderer.builder()
                .withBackground()
                .withItemIcon(getIconItem(), 5, 4)
                .withIconScale(scale)
                .withIconNudge(n[0], n[1])
                .render(gui, x, y, hover, false);
        } else {
            super.render(gui, x, y, hover);
        }
    }

    @Override
    protected void renderInverted(@NotNull GuiGraphics gui, int x, int y, boolean hover) {
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
                .render(gui, x, y, hover, true);
        } else if (scale != 1.0f || nudged) {
            TabRenderer.builder()
                .withBackground()
                .withItemIcon(getIconItem(), 5, 4)
                .withIconScale(scale)
                .withIconNudge(n[0], n[1])
                .render(gui, x, y, hover, true);
        } else {
            super.renderInverted(gui, x, y, hover);
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
