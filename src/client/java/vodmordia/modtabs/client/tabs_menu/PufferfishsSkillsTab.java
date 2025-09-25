package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import vodmordia.modtabs.api.tabs_menu.TabBase;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

public class PufferfishsSkillsTab extends TabBase {

    @Override
    public void openTargetScreen(PlayerEntity player) {
        try {
            // Try to open Pufferfish's Skills GUI
            MinecraftClient minecraft = MinecraftClient.getInstance();

            // Use reflection to open the skills screen
            Class<?> skillsScreenClass = Class.forName("net.puffish.skillsmod.client.gui.SkillsScreen");

            // Try different constructor patterns that the mod might use
            try {
                // Try constructor with parent screen parameter
                Object skillsScreen = skillsScreenClass.getConstructor(Screen.class).newInstance(minecraft.currentScreen);
                minecraft.setScreen((Screen) skillsScreen);
                return;
            } catch (Exception e1) {
                try {
                    // Try default constructor
                    Object skillsScreen = skillsScreenClass.getConstructor().newInstance();
                    minecraft.setScreen((Screen) skillsScreen);
                    return;
                } catch (Exception e2) {
                    // Try constructor with player parameter
                    try {
                        Object skillsScreen = skillsScreenClass.getConstructor(PlayerEntity.class).newInstance(player);
                        minecraft.setScreen((Screen) skillsScreen);
                        return;
                    } catch (Exception e3) {
                        vodmordia.modtabs.ModTabs.LOGGER.warn("Failed to create Pufferfish's Skills screen with standard constructors");
                    }
                }
            }

        } catch (Exception e) {
            // Log error for debugging
            vodmordia.modtabs.ModTabs.LOGGER.warn("Failed to open Pufferfish's Skills screen: " + e.getMessage());
        }
    }

    @Override
    public boolean isEnabled(PlayerEntity player) {
        return ModIntegrationManager.isModLoaded(ModIntegration.SKILLS_FABRIC);
    }

    @Override
    public void initTabOnScreens() {
        if (!ModIntegrationManager.isModLoaded(ModIntegration.SKILLS_FABRIC)) return;

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
            } catch (Exception e) {
                // Registration failed
            }
        });
    }

    @Override
    public void render(DrawContext gui, int x, int y, boolean hover) {
        // Use experience bottle as icon for skills
        renderWithItem(gui, x, y, hover, new ItemStack(Items.EXPERIENCE_BOTTLE));
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        if (currentScreen == null) return false;

        // Check if current screen is a Pufferfish's Skills screen
        String screenName = currentScreen.getClass().getName();
        return screenName.contains("puffish") && screenName.contains("skillsmod") && screenName.contains("gui");
    }

    @Override
    public Text getTooltip() {
        return Text.literal("Pufferfish's Skills");
    }
}
