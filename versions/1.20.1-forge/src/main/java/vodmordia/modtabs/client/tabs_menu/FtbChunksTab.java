package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.IntegrationIconTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.utils.ScreenClasses;

@TabConfig(configKey = "ftbChunksTab", defaultEnabled = true, defaultOrder = 0)
public class FtbChunksTab extends IntegrationIconTab {
    // 1.20.1 FTB Chunks doesn't ship a single minimap_info.png; only fragments like
    // square_border, circle_border, waypoint_*, etc. Use a vanilla map item icon as a stand-in.
    private static final ResourceLocation FTB_CHUNKS_ICON =
            new ResourceLocation("minecraft", "textures/item/filled_map.png");

    private static final TabSpec SPEC = new TabSpec(
            "ftbChunksTab",
            ModIntegration.FTB_CHUNKS,
            () -> Config.Baked.ftbChunksTabEnabled,
            "ftbChunks",
            "ftb_chunks",
            TabSpec.Layout.standardBottom(),
            new String[] { ScreenClasses.FTB_CHUNKS_MAP },
            new String[] {
                    ScreenClasses.FTB_CHUNKS_MAP,
                    ScreenClasses.FTB_CHUNKS_MAP_ALT1,
                    ScreenClasses.FTB_CHUNKS_MAP_ALT2
            }
    );

    public FtbChunksTab() {
        super(SPEC, FTB_CHUNKS_ICON, Config.Baked.ftbChunksTabCustomIcon);
    }

    @Override
    public void openTargetScreen(Player player) {
        if (Config.Baked.ftbChunksTabEnabled && player.level().isClientSide) {
            try {
                Class<?> ftbChunksClientClass = Class.forName("dev.ftb.mods.ftbchunks.client.FTBChunksClient");
                Object[] enumConstants = ftbChunksClientClass.getEnumConstants();

                if (enumConstants != null && enumConstants.length > 0) {
                    java.lang.reflect.Method openGuiMethod = ftbChunksClientClass.getMethod("openGui");
                    openGuiMethod.invoke(enumConstants[0]);
                }
            } catch (Exception e) {
                ModTabs.LOGGER.error("Failed to open FTB Chunks map: " + e.getMessage());
            }
        }
    }
}
