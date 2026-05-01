package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.ConfigurableIconTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.api.tabs_menu.TabPositioning;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

@TabConfig(configKey = "cosmeticArmorTab", defaultEnabled = true, defaultOrder = 0)
public class CosmeticArmorTab extends ConfigurableIconTab {
    private static final ResourceLocation COSMETIC_ARMOR_ICON = new ResourceLocation(ModTabs.MOD_ID, "textures/gui/cosmeticarmor.png");

    public CosmeticArmorTab() {
        super(COSMETIC_ARMOR_ICON, Config.Baked.cosmeticArmorTabCustomIcon, "cosmeticArmor");
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.cosmeticArmorTabEnabled) return;
        // 1.20.1 Cosmetic Armor Reworked uses a SimpleChannel-based NetworkManager
        // (lain.mods.cos.impl.network.NetworkManager) stored on lain.mods.cos.impl.ModObjects.network.
        // The packet is PacketOpenCosArmorInventory (not "PayloadOpen..." as on NeoForge 1.21+).
        try {
            Class<?> modObjectsClass = Class.forName("lain.mods.cos.impl.ModObjects");
            java.lang.reflect.Field networkField = modObjectsClass.getField("network");
            Object network = networkField.get(null);

            Class<?> packetClass = Class.forName("lain.mods.cos.impl.network.packet.PacketOpenCosArmorInventory");
            Object packet = packetClass.getDeclaredConstructor().newInstance();

            Class<?> packetSuper = Class.forName("lain.mods.cos.impl.network.NetworkManager$NetworkPacket");
            network.getClass().getMethod("sendToServer", packetSuper).invoke(network, packet);
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean isEnabled(Player player) {
        return Config.Baked.cosmeticArmorTabEnabled && ModIntegrationManager.isModLoaded(ModIntegration.COSMETIC_ARMOR);
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        try {
            Class<?> guiCosArmorInventoryClass = Class.forName("lain.mods.cos.impl.client.gui.GuiCosArmorInventory");
            return guiCosArmorInventoryClass.isInstance(currentScreen);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public Component getTooltip() {
        return Component.translatable("tooltip." + ModTabs.MOD_ID + ".tab.cosmetic_armor.description");
    }

    @Override
    public void initTabOnScreens() {
        try {
            Class<?> guiCosArmorInventoryClass = Class.forName("lain.mods.cos.impl.client.gui.GuiCosArmorInventory");
            @SuppressWarnings("unchecked")
            Class<? extends Screen> screenClass = (Class<? extends Screen>) guiCosArmorInventoryClass;
            ScreenRegistry.builder()
                .withStandardDimensions()
                .withPositioning(TabPositioning.GUI_RELATIVE)
                .registerAllTabs(screenClass);
        } catch (ClassNotFoundException e) {
            // Cosmetic Armor not present, skip registration
        }
    }
}