package vodmordia.modtabs.client.tabs_menu;

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

/** Opens NeoOrigins' origin/class information screen, matching its O keybind. */
@TabConfig(configKey = "originsTab", defaultEnabled = true, defaultOrder = 0)
public final class OriginsTab extends IntegrationIconTab {
    private static final String INFO_SCREEN = "com.cyberday1.neoorigins.screen.OriginInfoScreen";
    private static final String SELECTION_SCREEN = "com.cyberday1.neoorigins.screen.OriginSelectionScreen";

    private static final TabSpec SPEC = new TabSpec(
            "originsTab", ModIntegration.NEO_ORIGINS, () -> ModTabsConfig.originsTabEnabled,
            "origins", "origins", TabSpec.Layout.guiRelative(),
            new String[]{INFO_SCREEN, SELECTION_SCREEN},
            new String[]{INFO_SCREEN, SELECTION_SCREEN});

    public OriginsTab() {
        super(SPEC, ResourceLocation.fromNamespaceAndPath("neoorigins", "textures/item/orb_of_origin.png"),
                ModTabsConfig.originsTabCustomIcon);
    }

    @Override
    public void openTargetScreen(Player player) {
        try {
            Class<?> state = ClassCache.resolve("com.cyberday1.neoorigins.client.ClientOriginState");
            if (state != null) state.getMethod("openInfoScreen").invoke(null);
        } catch (ReflectiveOperationException e) {
            ModTabs.LOGGER.warn("Failed to open NeoOrigins info screen", e);
        }
    }
}
