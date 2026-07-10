package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.ConfigurableCustomIconTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

@TabConfig(configKey = "reskillableReimaginedTab", defaultEnabled = true, defaultOrder = 0)
public class ReskillableReimaginedTab extends ConfigurableCustomIconTab {

    public ReskillableReimaginedTab() {
        super((context) -> {
            // Render the book icon from Reskillable's skills.png
            try {
                // Book icon coordinates based on exact measurements
                int u = 241; // X position of book icon
                int v = 145; // Y position of book icon

                // Draw the 16x16 book icon, scaled down to 14x14 and moved down 2px
                context.gui.blit(ResourceLocation.fromNamespaceAndPath("reskillable", "textures/gui/skills.png"),
                    context.x + 6, context.y + 6, u, v, 14, 14, 256, 256);
            } catch (Exception e) {
                context.gui.fill(context.x + 7, context.y + 5, context.x + 19, context.y + 17, 0xFF8B4513);
                context.gui.fill(context.x + 9, context.y + 7, context.x + 17, context.y + 15, 0xFFFFF8DC);
            }
        }, Config.Baked.reskillableReimaginedTabCustomIcon, "reskillableReimagined", 13, 13);
    }

    @Override
    public void openTargetScreen(Player player) {
        if (Config.Baked.reskillableReimaginedTabEnabled && player.level().isClientSide) {
            try {
                Class<?> skillScreenClass = Class.forName("net.bandit.reskillable.client.screen.SkillScreen");
                Screen skillScreen = (Screen) skillScreenClass.getDeclaredConstructor().newInstance();
                Minecraft.getInstance().setScreen(skillScreen);
            } catch (Exception e) {
                // Reskillable Reimagined not present or failed to open screen
            }
        }
    }

    @Override
    public boolean isEnabled(Player player) {
        return Config.Baked.reskillableReimaginedTabEnabled && ModIntegrationManager.isModLoaded(ModIntegration.RESKILLABLE_REIMAGINED);
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        try {
            Class<?> skillScreenClass = Class.forName("net.bandit.reskillable.client.screen.SkillScreen");
            return skillScreenClass.isInstance(currentScreen);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public Component getTooltip() {
        return Component.translatable("tooltip." + ModTabs.MOD_ID + ".tab.reskillable_reimagined.description");
    }

    @Override
    public void initTabOnScreens() {
        try {
            Class<?> skillScreenClass = Class.forName("net.bandit.reskillable.client.screen.SkillScreen");
            @SuppressWarnings("unchecked")
            Class<? extends Screen> screenClass = (Class<? extends Screen>) skillScreenClass;
            ScreenRegistry.builder()
                .withStandardDimensions()
                .registerAllTabs(screenClass);
        } catch (ClassNotFoundException e) {
            // Reskillable Reimagined not present, skip registration
        }
    }
}
