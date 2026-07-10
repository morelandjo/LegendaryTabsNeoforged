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
import top.theillusivec4.curios.client.gui.CuriosScreen;

@TabConfig(configKey = "inventoryTab", defaultEnabled = true, defaultOrder = 0)
public class InventoryTab extends ConfigurableIconTab {
    private static final ResourceLocation INVENTORY_ICON = ResourceLocation.fromNamespaceAndPath(ModTabs.MOD_ID, "textures/gui/inventory.png");

    public InventoryTab() {
        super(INVENTORY_ICON, Config.Baked.inventoryTabCustomIcon, "inventory");
    }

    @Override
    public void openTargetScreen(Player player) {
        Minecraft mc = Minecraft.getInstance();
        // If a non-inventory menu is open (e.g. the player clicked this tab from a chest
        // screen mounted via ScreenRegistry.registerStandardScreens(ContainerScreen.class)),
        // tell the server to close it first — a plain setScreen(InventoryScreen) only
        // changes the client view and leaves the server still holding the container menu,
        // which desyncs the next interaction.
        if (mc.player != null && mc.player.containerMenu != null
                && mc.player.containerMenu != mc.player.inventoryMenu
                && mc.getConnection() != null) {
            mc.getConnection().send(new net.minecraft.network.protocol.game.ServerboundContainerClosePacket(
                    mc.player.containerMenu.containerId));
        }
        mc.setScreen(new InventoryScreen(player));
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
            ScreenRegistry.registerStandardScreens(CuriosScreen.class);
        }
    }
}
