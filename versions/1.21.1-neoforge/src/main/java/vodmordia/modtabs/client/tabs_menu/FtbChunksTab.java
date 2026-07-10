package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.gui.screens.Screen;
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
    private static final ResourceLocation FTB_CHUNKS_ICON =
            ResourceLocation.fromNamespaceAndPath("ftbchunks", "textures/minimap_info.png");

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
    public boolean isHomeTab(Screen currentScreen) {
        // The default isHomeTab (=isCurrentlyUsed) only matches the raw MapScreen FQNs in the
        // spec. But FTB Chunks' large map renders inside FTB Library's shared ScreenWrapper
        // (wrappedGui = LargeMapScreen), which is not an instance of MapScreen — so the
        // long-press-to-edit gesture would never arm on the chunks map. Match the wrapper here
        // by unwrapping it, the same way FtbQuestsTab/FtbTeamsTab do.
        if (currentScreen == null) return false;
        String name = currentScreen.getClass().getName();
        if (name.startsWith("dev.ftb.mods.ftbchunks.")) return true;
        if (ScreenClasses.FTB_LIBRARY_WRAPPER.equals(name)) {
            String wrapped = FtbScreenWrapperUtil.getWrappedGuiClassName(currentScreen);
            return wrapped != null && wrapped.startsWith("dev.ftb.mods.ftbchunks.");
        }
        return false;
    }

    @Override
    public void openTargetScreen(Player player) {
        if (Config.Baked.ftbChunksTabEnabled && player.level().isClientSide) {
            try {
                // The large map is opened via LargeMapScreen.openMap() (public static boolean).
                // Older builds exposed FTBChunksClient.openGui(), which no longer exists.
                Class<?> largeMapScreenClass = Class.forName("dev.ftb.mods.ftbchunks.client.gui.LargeMapScreen");
                java.lang.reflect.Method openMapMethod = largeMapScreenClass.getMethod("openMap");
                openMapMethod.invoke(null);
            } catch (Exception e) {
                ModTabs.LOGGER.error("Failed to open FTB Chunks map: " + e.getMessage());
            }
        }
    }
}
