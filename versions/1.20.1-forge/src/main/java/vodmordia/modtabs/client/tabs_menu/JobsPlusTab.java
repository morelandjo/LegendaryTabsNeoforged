package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.IntegrationIconTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.utils.ClassCache;
import vodmordia.modtabs.utils.ScreenClasses;

/**
 * Tab for DAQEM's Jobs+. {@code JobsScreen} needs server-supplied data (jobs + coins) to
 * construct, so we can't open it directly client-side. Instead we mirror the mod's own
 * keybind path in {@code EventKeyPressed}: send an empty {@code PacketOpenMenuC2S} and the
 * server replies with {@code PacketOpenMenuS2C} that constructs and opens the screen with
 * proper data.
 *
 * The packet class is resolved through {@link ClassCache} so this class compiles and
 * loads without a hard runtime dependency on Jobs+. The packet extends architectury's
 * {@code BaseC2SMessage}, which exposes an instance {@code sendToServer()} method — same
 * dispatch path the mod's own keybind uses.
 */
@TabConfig(configKey = "jobsPlusTab", defaultEnabled = true, defaultOrder = 0)
public class JobsPlusTab extends IntegrationIconTab {
    private static final ResourceLocation JOBS_PLUS_ICON =
            new ResourceLocation(ModTabs.MOD_ID, "textures/gui/jobsplus.png");

    private static final TabSpec SPEC = new TabSpec(
            "jobsPlusTab",
            ModIntegration.JOBS_PLUS,
            () -> Config.Baked.jobsPlusTabEnabled,
            "jobsPlus",
            "jobs_plus",
            TabSpec.Layout.guiRelative(),
            new String[] { ScreenClasses.JOBS_PLUS_SCREEN },
            new String[] { ScreenClasses.JOBS_PLUS_SCREEN }
    );

    public JobsPlusTab() {
        super(SPEC, JOBS_PLUS_ICON, Config.Baked.jobsPlusTabCustomIcon);
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.jobsPlusTabEnabled || !player.level().isClientSide) return;
        try {
            Class<?> packetClass = ClassCache.resolve(ScreenClasses.JOBS_PLUS_OPEN_PACKET);
            if (packetClass == null) return;
            Object packet = packetClass.getDeclaredConstructor().newInstance();
            packetClass.getMethod("sendToServer").invoke(packet);
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Error opening Jobs+ screen: " + e.getMessage());
        }
    }
}
