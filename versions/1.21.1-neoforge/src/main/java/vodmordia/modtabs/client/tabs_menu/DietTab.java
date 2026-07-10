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


@TabConfig(configKey = "dietTab", defaultEnabled = false, defaultOrder = 0)
public class DietTab extends ConfigurableIconTab {
    private static final ResourceLocation DIET_ICON = ResourceLocation.fromNamespaceAndPath(ModTabs.MOD_ID, "textures/gui/diet.png");

    public DietTab() {
        super(DIET_ICON, Config.Baked.dietTabCustomIcon, "diet");
    }

    @Override
    public void openTargetScreen(Player player) {
        // Commented out until Diet mod is updated to NeoForge 1.21.1
        //if (Config.Baked.dietTabEnabled && player.level().isClientSide) {
        //    try {
        //        Class<?> dietScreenClass = Class.forName("com.illusivesoulworks.diet.client.screen.DietScreen");
        //        Screen dietScreen = (Screen) dietScreenClass.getDeclaredConstructor(boolean.class)
        //            .newInstance(Minecraft.getInstance().screen instanceof InventoryScreen);
        //        Minecraft.getInstance().setScreen(dietScreen);
        //    } catch (Exception e) {
        //        // Diet mod not present or failed to open screen
        //    }
        //}
    }

    @Override
    public boolean isEnabled(Player player) {
        // Commented out until Diet mod is updated to NeoForge 1.21.1
        return false && Config.Baked.dietTabEnabled && ModIntegrationManager.isModLoaded(ModIntegration.DIET);
    }


    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        // Commented out until Diet mod is updated to NeoForge 1.21.1
        //try {
        //    Class<?> dietScreenClass = Class.forName("com.illusivesoulworks.diet.client.screen.DietScreen");
        //    return dietScreenClass.isInstance(currentScreen);
        //} catch (ClassNotFoundException e) {
        //    return false;
        //}
        return false;
    }

    @Override
    public Component getTooltip() {
        return Component.translatable("tooltip." + ModTabs.MOD_ID + ".tab.diet.description");
    }

    @Override
    public void initTabOnScreens() {
        // Diet mod is temporarily disabled - mod is not updated to NeoForge 1.21.1
        // Once Diet mod is updated, we can register the DietScreen here:
        //try {
        //    Class<?> dietScreenClass = Class.forName("com.illusivesoulworks.diet.client.screen.DietScreen");
        //    @SuppressWarnings("unchecked")
        //    Class<? extends Screen> screenClass = (Class<? extends Screen>) dietScreenClass;
        //    ScreenRegistry.builder()
        //        .withDietDimensions()
        //        .registerAllTabs(screenClass);
        //} catch (ClassNotFoundException e) {
        //    // Diet mod not present, skip registration
        //}
    }
}
