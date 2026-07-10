package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
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

@TabConfig(configKey = "advancementsTab", defaultEnabled = true, defaultOrder = 0)
public class AdvancementsTab extends ConfigurableIconTab {
    private static final ResourceLocation ADVANCEMENTS_ICON = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/sprites/advancements/challenge_frame_obtained.png");

    public AdvancementsTab() {
        super(ADVANCEMENTS_ICON, Config.Baked.advancementsTabCustomIcon, "advancements");
    }

    @Override
    public void openTargetScreen(Player player) {
        if (ModIntegrationManager.isModLoaded(ModIntegration.BETTER_ADVANCEMENTS)) {
            // Use Better Advancements screen if the mod is loaded
            try {
                Class<?> betterScreenClass = Class.forName("betteradvancements.common.gui.BetterAdvancementsScreen");
                java.lang.reflect.Constructor<?> constructor = betterScreenClass.getConstructor(net.minecraft.client.multiplayer.ClientAdvancements.class);
                Screen betterScreen = (Screen) constructor.newInstance(Minecraft.getInstance().getConnection().getAdvancements());
                Minecraft.getInstance().setScreen(betterScreen);
                return;
            } catch (Exception e) {
                // Fallback to vanilla if Better Advancements fails to load
            }
        }
        // Use vanilla advancements screen (fallback or default)
        AdvancementsScreen newGui = new AdvancementsScreen(Minecraft.getInstance().getConnection().getAdvancements());
        Minecraft.getInstance().setScreen(newGui);
    }

    @Override
    public boolean isEnabled(Player player) {
        return Config.Baked.advancementsTabEnabled;
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        if (currentScreen instanceof AdvancementsScreen) {
            return true;
        }
        // Check if the screen is Better Advancements screen if the mod is loaded
        if (ModIntegrationManager.isModLoaded(ModIntegration.BETTER_ADVANCEMENTS)) {
            return vodmordia.modtabs.utils.ClassCache.isInstance(
                    vodmordia.modtabs.utils.ScreenClasses.BETTER_ADVANCEMENTS, currentScreen);
        }
        return false;
    }

    @Override
    public Component getTooltip() {
        return Component.translatable("tooltip." + ModTabs.MOD_ID + ".tab.advancements.description");
    }

    @Override
    public void initTabOnScreens() {
        // Register the advancements screen with tabs displayed inverted at the top
        ScreenRegistry.registerInvertedScreens(AdvancementsScreen.class);

        // Also register Better Advancements screen if the mod is loaded
        if (ModIntegrationManager.isModLoaded(ModIntegration.BETTER_ADVANCEMENTS)) {
            // Use string-based registration to avoid class loading issues
            ScreenRegistry.registerInvertedScreens(vodmordia.modtabs.utils.ScreenClasses.BETTER_ADVANCEMENTS);
        }
    }
}