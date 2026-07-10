package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.IntegrationIconTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;

@TabConfig(configKey = "journeyMapTab", defaultEnabled = true, defaultOrder = 0)
public class JourneyMapTab extends IntegrationIconTab {
    private static final ResourceLocation JOURNEYMAP_ICON =
            ResourceLocation.fromNamespaceAndPath(ModTabs.MOD_ID, "textures/gui/journeymap.png");

    // JourneyMap opens its UI programmatically, so screenFqns is empty —
    // no auto-registration. The tab still attaches to every other registered screen.
    private static final TabSpec SPEC = TabSpec.withoutCurrentScreen(
            "journeyMapTab",
            ModIntegration.JOURNEY_MAP,
            () -> Config.Baked.journeyMapTabEnabled,
            "journeyMap",
            "journey_map",
            TabSpec.Layout.invertedTop()
    );

    public JourneyMapTab() {
        super(SPEC, JOURNEYMAP_ICON, Config.Baked.journeyMapTabCustomIcon);
    }

    @Override
    public void openTargetScreen(Player player) {
        if (Config.Baked.journeyMapTabEnabled && player.level().isClientSide) {
            try {
                Class<?> uiManagerClass = Class.forName("journeymap.client.ui.UIManager");
                java.lang.reflect.Field instanceField = uiManagerClass.getField("INSTANCE");
                Object uiManagerInstance = instanceField.get(null);
                java.lang.reflect.Method openFullscreenMapMethod = uiManagerClass.getMethod("openFullscreenMap");
                Minecraft.getInstance().setScreen(null);
                openFullscreenMapMethod.invoke(uiManagerInstance);
            } catch (Exception e) {
                // JourneyMap not present or failed to open map
            }
        }
    }
}
