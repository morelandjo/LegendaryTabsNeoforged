package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.ConfigurableIconTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

@TabConfig(configKey = "inventoryTab", defaultEnabled = true, defaultOrder = 0)
public class InventoryTab extends ConfigurableIconTab {
    private static final ResourceLocation INVENTORY_ICON = new ResourceLocation(ModTabs.MOD_ID, "textures/gui/inventory.png");

    public InventoryTab() {
        super(INVENTORY_ICON, Config.Baked.inventoryTabCustomIcon, "inventory");
    }

    @Override
    public void openTargetScreen(Player player) {
        InventoryScreen newGui = new InventoryScreen(player);
        Minecraft.getInstance().setScreen(newGui);
    }

    @Override
    public boolean isEnabled(Player player) {
        return Config.Baked.inventoryTabEnabled;
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        // InventoryTab should always be visible as it represents the "home" screen
        return false;
    }

    @Override
    public boolean isHomeTab(Screen currentScreen) {
        return currentScreen instanceof InventoryScreen;
    }

    @Override
    public Component getTooltip() {
        return Component.translatable("tooltip." + ModTabs.MOD_ID + ".tab.inventory.description");
    }

    @Override
    public void initTabOnScreens() {
        // Register common screens with all tabs using the new ScreenRegistry
        ScreenRegistry.registerStandardScreens(InventoryScreen.class);

        // Note: BodyHealthScreen registration is handled by BodyDamageTab to avoid duplicates

        if (ModIntegrationManager.isModLoaded(ModIntegration.CURIOS)) {
            try {
                @SuppressWarnings("unchecked")
                Class<? extends Screen> curiosScreenClass =
                        (Class<? extends Screen>) Class.forName("top.theillusivec4.curios.client.gui.CuriosScreen");
                ScreenRegistry.registerStandardScreens(curiosScreenClass);
            } catch (ClassNotFoundException ignored) {
            }
        }
    }
}
