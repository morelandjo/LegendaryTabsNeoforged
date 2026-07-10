package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import vodmordia.modtabs.api.tabs_menu.IntegrationItemTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.network.OpenReliableBackpackPayload;
import vodmordia.modtabs.utils.ClassCache;
import vodmordia.modtabs.utils.ScreenClasses;

/**
 * Tab for the Reliable Backpacks mod. Visible only when the player is wearing the
 * backpack in their chest slot (the only place the mod tracks it). Clicking sends
 * a {@link OpenReliableBackpackPayload} to the server, which opens the backpack via
 * vanilla {@link net.minecraft.world.inventory.ShulkerBoxMenu}.
 *
 * The mod itself ships no client-side open API by design — its only "open" path
 * is "interact with another player from behind". This tab adds an open-from-self
 * shortcut by replicating the mod's server-side open logic in our own packet handler.
 */
@TabConfig(configKey = "reliableBackpacksTab", defaultEnabled = true, defaultOrder = 0)
public class ReliableBackpackTab extends IntegrationItemTab {
    private static final ResourceLocation BACKPACK_ITEM_ID =
            ResourceLocation.fromNamespaceAndPath("reliable_backpacks", "backpack");

    private static final TabSpec SPEC = TabSpec.withoutCurrentScreen(
            "reliableBackpacksTab",
            ModIntegration.RELIABLE_BACKPACKS,
            () -> Config.Baked.reliableBackpacksTabEnabled,
            "reliableBackpacks",
            "reliable_backpacks",
            TabSpec.Layout.guiRelative()
    );

    public ReliableBackpackTab() {
        super(SPEC, ReliableBackpackTab::getBackpackIcon, Config.Baked.reliableBackpacksTabCustomIcon);
    }

    private static ItemStack getBackpackIcon() {
        Item item = BuiltInRegistries.ITEM.get(BACKPACK_ITEM_ID);
        if (item != null && item != Items.AIR) {
            return new ItemStack(item);
        }
        return new ItemStack(Items.LEATHER);
    }

    @Override
    public boolean isEnabled(Player player) {
        return super.isEnabled(player) && hasBackpack(player);
    }

    /**
     * Tab is visible whenever a backpack is present anywhere on the player — main
     * inventory, hotbar, offhand, or already equipped in the chest slot. The server
     * handler will swap an inventory-held backpack into the chest slot on click.
     */
    private static boolean hasBackpack(Player player) {
        Class<?> itemClass = ClassCache.resolve(ScreenClasses.RELIABLE_BACKPACKS_ITEM);
        if (itemClass == null) return false;

        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && itemClass.isInstance(stack.getItem())) return true;
        }
        for (ItemStack stack : player.getInventory().armor) {
            if (!stack.isEmpty() && itemClass.isInstance(stack.getItem())) return true;
        }
        ItemStack offhand = player.getOffhandItem();
        return !offhand.isEmpty() && itemClass.isInstance(offhand.getItem());
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.reliableBackpacksTabEnabled || !player.level().isClientSide) return;
        PacketDistributor.sendToServer(new OpenReliableBackpackPayload());
    }
}
