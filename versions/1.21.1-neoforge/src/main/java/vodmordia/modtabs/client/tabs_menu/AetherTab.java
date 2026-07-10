package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.IntegrationItemTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.utils.ClassCache;
import vodmordia.modtabs.utils.ScreenClasses;

import java.lang.reflect.Constructor;

/**
 * Tab for The Aether's accessory inventory. Reuses the mod's own packet path
 * ({@code OpenAccessoriesPacket}): the client sends an empty-carry packet to the
 * server, which calls {@code serverPlayer.openMenu(...)} with an
 * {@code AetherAccessoriesMenu}. The accessory screen is then registered
 * client-side via the menu's screen factory — same flow as pressing "I".
 *
 * Both the packet class and the screen FQN are resolved through {@link ClassCache}
 * so this class compiles and loads without a hard runtime dependency on The Aether.
 */
@TabConfig(configKey = "aetherTab", defaultEnabled = true, defaultOrder = 0)
public class AetherTab extends IntegrationItemTab {
    private static final ResourceLocation IRON_RING_ID =
            ResourceLocation.fromNamespaceAndPath("aether", "iron_ring");

    private static final TabSpec SPEC = new TabSpec(
            "aetherTab",
            ModIntegration.AETHER,
            () -> Config.Baked.aetherTabEnabled,
            "aether",
            "aether",
            TabSpec.Layout.guiRelative(),
            new String[] { ScreenClasses.AETHER_ACCESSORIES_SCREEN },
            new String[] { ScreenClasses.AETHER_ACCESSORIES_SCREEN }
    );

    public AetherTab() {
        super(SPEC, AetherTab::getRingIcon, Config.Baked.aetherTabCustomIcon);
    }

    private static ItemStack getRingIcon() {
        Item item = BuiltInRegistries.ITEM.get(IRON_RING_ID);
        if (item != null && item != Items.AIR) {
            return new ItemStack(item);
        }
        return new ItemStack(Items.GOLD_INGOT);
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.aetherTabEnabled || !player.level().isClientSide) return;
        try {
            Class<?> packetClass = ClassCache.resolve(ScreenClasses.AETHER_OPEN_ACCESSORIES_PACKET);
            if (packetClass == null) return;
            Constructor<?> ctor = packetClass.getConstructor(ItemStack.class);
            Object packet = ctor.newInstance(ItemStack.EMPTY);
            PacketDistributor.sendToServer((CustomPacketPayload) packet);
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Error opening Aether accessories: " + e.getMessage());
        }
    }
}
