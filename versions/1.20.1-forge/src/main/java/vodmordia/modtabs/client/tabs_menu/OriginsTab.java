package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.IntegrationIconTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.ModTabsConfig;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.utils.ClassCache;

/** Opens Origins' current-origin screen, matching its O keybind. */
@TabConfig(configKey = "originsTab", defaultEnabled = true, defaultOrder = 0)
public final class OriginsTab extends IntegrationIconTab {
    private static final String VIEW_SCREEN = "io.github.apace100.origins.screen.ViewOriginScreen";

    private static final TabSpec SPEC = new TabSpec(
            "originsTab", ModIntegration.ORIGINS, () -> ModTabsConfig.originsTabEnabled,
            "origins", "origins", TabSpec.Layout.guiRelative(),
            new String[]{VIEW_SCREEN},
            new String[]{VIEW_SCREEN});

    public OriginsTab() {
        super(SPEC, new ResourceLocation("origins", "icon.png"), ModTabsConfig.originsTabCustomIcon);
    }

    @Override
    public void openTargetScreen(Player player) {
        try {
            Class<?> type = ClassCache.resolve(VIEW_SCREEN);
            if (type != null) Minecraft.getInstance().setScreen((Screen) type.getConstructor().newInstance());
        } catch (ReflectiveOperationException e) {
            ModTabs.LOGGER.warn("Failed to open Origins screen", e);
        }
    }
}
