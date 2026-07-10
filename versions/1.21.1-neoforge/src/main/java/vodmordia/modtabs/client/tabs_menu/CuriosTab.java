package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.IntegrationIconTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.utils.ClassCache;
import vodmordia.modtabs.utils.ScreenClasses;

import java.lang.reflect.Constructor;

/**
 * Tab for Curios. Reuses the mod's own packet path
 * ({@code CPacketOpenCurios}): the client sends an empty-carry packet to the
 * server, which opens the curios menu — same flow as clicking the mod's own
 * CuriosButton on the inventory screen.
 *
 * The packet class is resolved through {@link ClassCache} so this class
 * compiles and loads without a hard runtime dependency on Curios.
 */
@TabConfig(configKey = "curiosTab", defaultEnabled = true, defaultOrder = 0)
public class CuriosTab extends IntegrationIconTab {

    // Curios' own inventory button texture, pulled straight from the mod's assets so the tab
    // matches the icon players already associate with Curios. It's a 10x10 image; the tab
    // renderer stretches the full texture to fill the 16x16 icon slot (width == textureWidth in
    // ConfigurableIconTab, so the blit samples the whole texture).
    private static final ResourceLocation CURIOS_ICON =
            ResourceLocation.fromNamespaceAndPath("curios", "textures/gui/curios/button_highlighted.png");

    // Both V1 (CuriosScreen, legacy) and V2 (CuriosScreenV2, default) need tabs — the
    // server picks V2 by default; V1 only opens when CuriosConfig.SERVER.enableLegacyMenu
    // is set. Listing both here keeps tabs working regardless of the player's config.
    private static final String[] SCREENS = {
            ScreenClasses.CURIOS_SCREEN,
            ScreenClasses.CURIOS_SCREEN_V2
    };

    private static final TabSpec SPEC = new TabSpec(
            "curiosTab",
            ModIntegration.CURIOS,
            () -> Config.Baked.curiosTabEnabled,
            "curios",
            "curios",
            TabSpec.Layout.guiRelative(),
            SCREENS,
            SCREENS
    );

    public CuriosTab() {
        super(SPEC, CURIOS_ICON, Config.Baked.curiosTabCustomIcon);
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.curiosTabEnabled || !player.level().isClientSide) return;
        try {
            Class<?> packetClass = ClassCache.resolve(ScreenClasses.CURIOS_OPEN_PACKET);
            if (packetClass == null) return;
            Constructor<?> ctor = packetClass.getConstructor(ItemStack.class);
            Object packet = ctor.newInstance(ItemStack.EMPTY);
            PacketDistributor.sendToServer((CustomPacketPayload) packet);
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Error opening Curios screen: " + e.getMessage());
        }
    }
}
