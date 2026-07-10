package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.IntegrationIconTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.utils.ClassCache;
import vodmordia.modtabs.utils.ScreenClasses;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * Tab for Curios. Reuses the mod's own packet path
 * ({@code CPacketOpenCurios}): the client sends an empty-carry packet to the
 * server via Curios' own {@code NetworkHandler.INSTANCE} SimpleChannel, which
 * opens the curios menu — same flow as clicking the mod's own CuriosButton on
 * the inventory screen.
 *
 * Both the packet class and the SimpleChannel are resolved through
 * {@link ClassCache} so this class compiles and loads without a hard runtime
 * dependency on Curios.
 */
@TabConfig(configKey = "curiosTab", defaultEnabled = true, defaultOrder = 0)
public class CuriosTab extends IntegrationIconTab {

    private static final ResourceLocation CURIOS_INVENTORY =
            new ResourceLocation("curios", "textures/gui/inventory.png");

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
        // Curios 1.20.1 keeps the same 10x10 highlighted symbol inside its inventory atlas.
        super(SPEC, CURIOS_INVENTORY, Config.Baked.curiosTabCustomIcon,
                52, 2, 10, 10, 256, 256);
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.curiosTabEnabled || !player.level().isClientSide) return;
        try {
            Class<?> packetClass = ClassCache.resolve(ScreenClasses.CURIOS_OPEN_PACKET);
            Class<?> handlerClass = ClassCache.resolve(ScreenClasses.CURIOS_NETWORK_HANDLER);
            if (packetClass == null || handlerClass == null) return;

            Constructor<?> ctor = packetClass.getConstructor(ItemStack.class);
            Object packet = ctor.newInstance(ItemStack.EMPTY);

            Field instanceField = handlerClass.getField("INSTANCE");
            Object channel = instanceField.get(null);
            channel.getClass().getMethod("sendToServer", Object.class).invoke(channel, packet);
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Error opening Curios screen: " + e.getMessage());
        }
    }
}
