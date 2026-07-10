package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import vodmordia.modtabs.api.tabs_menu.IntegrationIconTab;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.utils.ClassCache;
import vodmordia.modtabs.utils.ScreenClasses;

@TabConfig(configKey = "draconicEvolutionTab", defaultEnabled = true, defaultOrder = 0)
public class DraconicEvolutionTab extends IntegrationIconTab {
    private static final ResourceLocation INFO_TABLET_ICON =
            ResourceLocation.fromNamespaceAndPath("draconicevolution", "textures/item/info_tablet.png");

    // Layout/registration for this tab is non-standard (custom dimensions + forceRegister),
    // so we override initTabOnScreens below; the spec only carries declarative metadata.
    private static final TabSpec SPEC = new TabSpec(
            "draconicEvolutionTab",
            ModIntegration.DRACONIC_EVOLUTION,
            () -> Config.Baked.draconicEvolutionTabEnabled,
            "draconicEvolution",
            "draconic_evolution",
            TabSpec.Layout.invertedTop(),
            new String[] { ScreenClasses.DRACONIC_EVOLUTION_CONFIGURABLE_ITEM },
            new String[] { ScreenClasses.DRACONIC_EVOLUTION_CONFIGURABLE_ITEM }
    );

    public DraconicEvolutionTab() {
        super(SPEC, INFO_TABLET_ICON, Config.Baked.draconicEvolutionTabCustomIcon);
    }

    @Override
    public boolean isEnabled(Player player) {
        // The tab only makes sense when the player is holding a configurable DE item.
        return super.isEnabled(player) && hasConfigurableItem(player);
    }

    private boolean hasConfigurableItem(Player player) {
        try {
            Class<?> hostClass = ClassCache.resolve("com.brandon3055.draconicevolution.api.capability.DECapabilities$Host");
            if (hostClass == null) return false;
            java.lang.reflect.Field itemField = hostClass.getDeclaredField("ITEM");
            Object itemCapability = itemField.get(null);

            for (ItemStack stack : player.getInventory().items) {
                if (!stack.isEmpty() && hasCapability(stack, itemCapability)) return true;
            }
            for (ItemStack stack : player.getInventory().armor) {
                if (!stack.isEmpty() && hasCapability(stack, itemCapability)) return true;
            }
            ItemStack offhand = player.getOffhandItem();
            return !offhand.isEmpty() && hasCapability(offhand, itemCapability);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean hasCapability(ItemStack stack, Object capability) {
        try {
            Class<?> itemCapabilityClass = ClassCache.resolve("net.neoforged.neoforge.capabilities.ItemCapability");
            if (itemCapabilityClass == null) return false;
            java.lang.reflect.Method getCapabilityMethod = ItemStack.class.getMethod("getCapability", itemCapabilityClass);
            return getCapabilityMethod.invoke(stack, capability) != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void initTabOnScreens() {
        // DE's GUI uses 218×230 (not the standard 176×166), and it needs forceRegister to win
        // over any earlier registration attempts.
        ScreenRegistry.builder()
                .withDimensions(p -> 218, p -> 230)
                .inverted()
                .forceRegisterAllTabs(ScreenClasses.DRACONIC_EVOLUTION_CONFIGURABLE_ITEM);
    }

    @Override
    public void openTargetScreen(Player player) {
        if (Config.Baked.draconicEvolutionTabEnabled && player.level().isClientSide) {
            try {
                Class<?> draconicNetworkClass = Class.forName("com.brandon3055.draconicevolution.network.DraconicNetwork");
                java.lang.reflect.Method sendOpenItemConfigMethod = draconicNetworkClass.getMethod(
                        "sendOpenItemConfig", net.minecraft.core.RegistryAccess.class, boolean.class);
                sendOpenItemConfigMethod.invoke(null, player.registryAccess(), false);
            } catch (Exception ignored) {}
        }
    }
}
