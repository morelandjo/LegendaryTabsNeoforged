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
        if (!ModIntegrationManager.isModLoaded(ModIntegration.PUFFERFISH_SKILLS)) return;

        TabsMenu.addPendingRegistration(() -> {
            // Register for common screens
            try {
                TabsMenu.registerScreenForTabs(net.minecraft.client.gui.screen.ingame.InventoryScreen.class, this);

                // Try to register for other common container screens
                String[] screenClasses = {
                    "net.minecraft.client.gui.screen.ingame.GenericContainerScreen",
                    "net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen",
                    "net.minecraft.client.gui.screen.ingame.ChestScreen"
                };

                for (String className : screenClasses) {
                    try {
                        Class<?> screenClass = Class.forName(className);
                        TabsMenu.registerScreenForTabs((Class<? extends Screen>) screenClass, this);
                    } catch (ClassNotFoundException e) {
                        // Screen class not found, continue
                    }
                }

                // Also register for the skills screen itself
                try {
                    Class<?> skillsScreenClass = Class.forName("net.puffish.skillsmod.client.gui.SkillsScreen");
                    TabsMenu.registerScreenForTabs((Class<? extends Screen>) skillsScreenClass, this);
                } catch (ClassNotFoundException e) {
                    // Skills screen not found
                }
            } catch (Exception e) {
                // Registration failed
            }
        });
    }
}
