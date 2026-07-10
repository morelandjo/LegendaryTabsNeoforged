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
import vodmordia.modtabs.utils.ScreenClasses;

@TabConfig(configKey = "motpTab", defaultEnabled = true, defaultOrder = 0)
public class MotpTab extends IntegrationIconTab {
    private static final ResourceLocation MOTP_ICON =
            ResourceLocation.fromNamespaceAndPath("memory_of_the_past", "textures/item/book_02f.png");

    private static final TabSpec SPEC = new TabSpec(
            "motpTab",
            ModIntegration.MOTP,
            () -> Config.Baked.motpTabEnabled,
            "motp",
            "motp",
            TabSpec.Layout.invertedTop(),
            new String[] { ScreenClasses.MOTP_PLAYER_STATS },
            new String[] { ScreenClasses.MOTP_PLAYER_STATS }
    );

    public MotpTab() {
        super(SPEC, MOTP_ICON, Config.Baked.motpTabCustomIcon);
    }

    @Override
    public void openTargetScreen(Player player) {
        try {
            Class<?> messageClass = Class.forName("tn.mbs.memory.network.OpenStatsMenuKeybindMessage");
            Object message = messageClass.getConstructor(int.class, int.class).newInstance(0, 0);

            net.minecraft.client.multiplayer.ClientPacketListener connection = Minecraft.getInstance().getConnection();
            if (connection != null) {
                java.lang.reflect.Method sendMethod = connection.getClass().getMethod("send",
                        Class.forName("net.minecraft.network.protocol.common.custom.CustomPacketPayload"));
                sendMethod.invoke(connection, message);
            }
        } catch (Exception e) {
            ModTabs.LOGGER.error("Error opening MOTP stats screen", e);
        }
    }
}
