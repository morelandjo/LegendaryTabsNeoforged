package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.IntegrationItemTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.utils.ClassCache;
import vodmordia.modtabs.utils.ScreenClasses;

import java.lang.reflect.Field;

/**
 * Tab for Blue Skies' Arcs inventory. Mirrors the mod's own Arcs button in
 * {@code SkiesClientEvents.openGuiEvent} — sends a no-arg
 * {@code OpenInventoryPacket$Arcs} through Blue Skies' own {@code PacketHandler.INSTANCE}
 * {@link net.minecraftforge.network.simple.SimpleChannel}, and the server's handler opens
 * an {@code ArcsMenu} on the player which the client renders as {@link ScreenClasses#BLUE_SKIES_ARCS_SCREEN}.
 *
 * <p>The packet class and SimpleChannel are resolved through {@link ClassCache} so this
 * class compiles and loads without a hard runtime dependency on Blue Skies.
 */
@TabConfig(configKey = "blueSkiesArcsTab", defaultEnabled = true, defaultOrder = 0)
public class BlueSkiesArcsTab extends IntegrationItemTab {
    private static final ResourceLocation ARC_ICON_ID =
            new ResourceLocation("blue_skies", "ethereal_arc");

    private static final TabSpec SPEC = new TabSpec(
            "blueSkiesArcsTab",
            ModIntegration.BLUE_SKIES,
            () -> Config.Baked.blueSkiesArcsTabEnabled,
            "blueSkiesArcs",
            "blue_skies_arcs",
            TabSpec.Layout.guiRelative(),
            new String[] { ScreenClasses.BLUE_SKIES_ARCS_SCREEN },
            new String[] { ScreenClasses.BLUE_SKIES_ARCS_SCREEN }
    );

    public BlueSkiesArcsTab() {
        super(SPEC, BlueSkiesArcsTab::getArcIcon, Config.Baked.blueSkiesArcsTabCustomIcon);
    }

    private static ItemStack getArcIcon() {
        Item item = BuiltInRegistries.ITEM.get(ARC_ICON_ID);
        if (item != null && item != Items.AIR) {
            return new ItemStack(item);
        }
        return new ItemStack(Items.ENDER_PEARL);
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.blueSkiesArcsTabEnabled || !player.level().isClientSide) return;
        try {
            Class<?> packetClass = ClassCache.resolve(ScreenClasses.BLUE_SKIES_OPEN_ARCS_PACKET);
            Class<?> handlerClass = ClassCache.resolve(ScreenClasses.BLUE_SKIES_PACKET_HANDLER);
            if (packetClass == null || handlerClass == null) return;

            Object packet = packetClass.getConstructor().newInstance();

            Field instanceField = handlerClass.getField("INSTANCE");
            Object channel = instanceField.get(null);
            channel.getClass().getMethod("sendToServer", Object.class).invoke(channel, packet);
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Error opening Blue Skies Arcs inventory: " + e.getMessage());
        }
    }
}
