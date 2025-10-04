package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.SimpleTextureTab;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

import java.util.Optional;

public class PufferfishsSkillsTab extends SimpleTextureTab {
    private static final Identifier PUFFER_ICON = Identifier.of(ModTabs.MOD_ID, "textures/gui/puffer.png");

    public PufferfishsSkillsTab() {
        super(PUFFER_ICON);
    }

    @Override
    public void openTargetScreen(PlayerEntity player) {
        if (player.getWorld().isClient) {
            try {
                // Use the proper SkillsClientMod.openScreen method like the NeoForge version
                Class<?> clientModClass = Class.forName("net.puffish.skillsmod.client.SkillsClientMod");
                Object clientModInstance = clientModClass.getMethod("getInstance").invoke(null);
                clientModClass.getMethod("openScreen", Optional.class).invoke(clientModInstance, Optional.empty());
            } catch (Exception e) {
                vodmordia.modtabs.ModTabs.LOGGER.warn("Failed to open Pufferfish's Skills screen: " + e.getMessage());
            }
        }
    }

    @Override
    public boolean isEnabled(PlayerEntity player) {
        return ModIntegrationManager.isModLoaded(ModIntegration.PUFFERFISH_SKILLS);
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        // Always return false so this tab is never disabled - we want it visible on all screens including SkillsScreen
        return false;
    }

    @Override
    public Text getTooltip() {
        return Text.literal("Pufferfish's Skills");
    }

    @Override
    public void initTabOnScreens() {
        vodmordia.modtabs.api.tabs_menu.ScreenRegistry.builder()
            .withStandardDimensions()
            .inverted()
            .atTop()
            .registerAllTabs("net.puffish.skillsmod.client.gui.SkillsScreen");
    }
}
