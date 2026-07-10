package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import vodmordia.modtabs.api.tabs_menu.IntegrationItemTab;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.network.OpenQuarkBackpackPayload;
import vodmordia.modtabs.utils.ClassCache;
import vodmordia.modtabs.utils.ScreenClasses;

/**
 * Tab for the Quark backpack (Quark's "Oddities" addon, mod id {@code quark}).
 *
 * Quark's backpack is chest-slot armor that is itself a {@code MenuProvider}; its slots
 * render in a dedicated {@code BackpackInventoryScreen}. The tab is visible only while a
 * Quark backpack is actually worn in the chest slot (the only state Quark can open it
 * from). Clicking sends an {@link OpenQuarkBackpackPayload}; the server replicates Quark's
 * own open logic ({@code player.openMenu(backpack)}).
 */
@TabConfig(configKey = "quarkBackpackTab", defaultEnabled = true, defaultOrder = 0)
public class QuarkBackpackTab extends IntegrationItemTab {
    private static final ResourceLocation BACKPACK_ITEM_ID =
            ResourceLocation.fromNamespaceAndPath("quark", "backpack");

    private static final TabSpec SPEC = new TabSpec(
            "quarkBackpackTab",
            ModIntegration.QUARK,
            () -> Config.Baked.quarkBackpackTabEnabled,
            "quarkBackpack",
            "quark_backpack",
            TabSpec.Layout.guiRelative(),
            new String[] { ScreenClasses.QUARK_BACKPACK_SCREEN },
            new String[] { ScreenClasses.QUARK_BACKPACK_SCREEN }
    );

    public QuarkBackpackTab() {
        super(SPEC, QuarkBackpackTab::getBackpackIcon, Config.Baked.quarkBackpackTabCustomIcon);
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
        return super.isEnabled(player) && isWearingBackpack(player);
    }

    /** Quark only opens the backpack when it's worn in the chest slot, so gate on that. */
    private static boolean isWearingBackpack(Player player) {
        Class<?> itemClass = ClassCache.resolve(ScreenClasses.QUARK_BACKPACK_ITEM);
        if (itemClass == null) return false;
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        return !chest.isEmpty() && itemClass.isInstance(chest.getItem());
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.quarkBackpackTabEnabled || !player.level().isClientSide) return;
        PacketDistributor.sendToServer(new OpenQuarkBackpackPayload());
    }

    @Override
    public void initTabOnScreens() {
        // Quark's backpack screen is taller than a vanilla inventory (extra rows), so use
        // the dedicated QUARK_BACKPACK_HEIGHT so the bar anchors correctly on that screen.
        Class<?> screenClass = ClassCache.resolve(ScreenClasses.QUARK_BACKPACK_SCREEN);
        if (screenClass == null) return;
        @SuppressWarnings("unchecked")
        Class<? extends Screen> typed = (Class<? extends Screen>) screenClass;
        ScreenRegistry.builder()
                .withDimensions(ScreenRegistry.STANDARD_WIDTH, ScreenRegistry.QUARK_BACKPACK_HEIGHT)
                .registerAllTabs(typed);
    }
}
