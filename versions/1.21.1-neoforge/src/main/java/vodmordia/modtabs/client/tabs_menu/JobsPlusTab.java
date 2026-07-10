package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
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
 * Tab for DAQEM's Jobs+. {@code JobsScreen} needs server-supplied {@code JobsScreenOptions}
 * (jobs + coins) to construct, so we can't open it directly client-side. Instead we mirror
 * the mod's own keybind path in {@code EventKeyPressed}: send an empty
 * {@code ServerboundOpenJobsScreenPacket} and the server replies with
 * {@code ClientboundOpenJobsScreenPacket} that constructs and opens the screen with proper
 * data.
 *
 * Dispatch goes through architectury's {@code NetworkManager.sendToServer}, NOT
 * NeoForge's {@code PacketDistributor.sendToServer}. Architectury registers the packet
 * wrapped in its own {@code NetworkAggregator$BufCustomPacketPayload}; sending the raw
 * mod payload via {@code PacketDistributor} skips that wrapping and the encoder throws
 * {@code ClassCastException}. Going through {@code NetworkManager} preserves the wrap —
 * same path the mod's own keybind ({@code EventKeyPressed}) uses.
 *
 * Both the packet class and architectury's {@code NetworkManager} are resolved through
 * {@link Class#forName} so this class loads without architectury or Jobs+ on the classpath.
 */
@TabConfig(configKey = "jobsPlusTab", defaultEnabled = true, defaultOrder = 0)
public class JobsPlusTab extends IntegrationIconTab {
    private static final ResourceLocation JOBS_PLUS_ICON =
            ResourceLocation.fromNamespaceAndPath(ModTabs.MOD_ID, "textures/gui/jobsplus.png");

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
            Object packet = packetClass.getConstructor().newInstance();
            Class<?> networkManager = Class.forName("dev.architectury.networking.NetworkManager");
            networkManager.getMethod("sendToServer", CustomPacketPayload.class).invoke(null, packet);
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Error opening Jobs+ screen: " + e.getMessage());
        }
    }
}
