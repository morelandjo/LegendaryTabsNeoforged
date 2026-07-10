package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.gui.screens.Screen;
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

@TabConfig(configKey = "cosmeticArmorTab", defaultEnabled = true, defaultOrder = 0)
public class CosmeticArmorTab extends ConfigurableIconTab {
    private static final ResourceLocation COSMETIC_ARMOR_ICON = ResourceLocation.fromNamespaceAndPath(ModTabs.MOD_ID, "textures/gui/cosmeticarmor.png");

    public CosmeticArmorTab() {
        super(COSMETIC_ARMOR_ICON, Config.Baked.cosmeticArmorTabCustomIcon, "cosmeticArmor");
    }

    @Override
    public void openTargetScreen(Player player) {
        if (Config.Baked.cosmeticArmorTabEnabled) {
            try {
                // Use reflection to send the PayloadOpenCosArmorInventory packet
                Class<?> packetDistributorClass = Class.forName("net.neoforged.neoforge.network.PacketDistributor");
                Class<?> payloadClass = Class.forName("lain.mods.cos.impl.network.payload.PayloadOpenCosArmorInventory");

                // Create the payload using default constructor
                Object payload = payloadClass.getDeclaredConstructor().newInstance();

                // Find the sendToServer method
                for (java.lang.reflect.Method method : packetDistributorClass.getDeclaredMethods()) {
                    if (method.getName().equals("sendToServer") && method.getParameterCount() == 2) {
                        Class<?>[] paramTypes = method.getParameterTypes();
                        // Create empty array for the second parameter
                        Object[] emptyArray = (Object[]) java.lang.reflect.Array.newInstance(paramTypes[1].getComponentType(), 0);
                        method.invoke(null, payload, emptyArray);
                        return;
                    }
                }
            } catch (Exception e) {
                // Silent fail if packet sending doesn't work
            }
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
                .registerAllTabs(screenClass);
        } catch (ClassNotFoundException e) {
            // Cosmetic Armor not present, skip registration
        }
    }
}