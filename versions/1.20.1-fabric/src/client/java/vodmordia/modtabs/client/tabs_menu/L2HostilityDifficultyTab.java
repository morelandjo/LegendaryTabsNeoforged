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

public class L2HostilityDifficultyTab extends TabBase {

    @Override
    public void openTargetScreen(PlayerEntity player) {
        try {
            // Try to open L2 Hostility Difficulty GUI
            MinecraftClient minecraft = MinecraftClient.getInstance();

            // Use reflection to open the difficulty screen
            Class<?> difficultyScreenClass = Class.forName("karashokleo.l2hostility.content.screen.tab.DifficultyScreen");

            // Try different constructor patterns that L2 Hostility might use
            try {
                // Try constructor with parent screen parameter
                Object difficultyScreen = difficultyScreenClass.getConstructor(Screen.class).newInstance(minecraft.currentScreen);
                minecraft.setScreen((Screen) difficultyScreen);
                return;
            } catch (Exception e1) {
                try {
                    // Try default constructor
                    Object difficultyScreen = difficultyScreenClass.getConstructor().newInstance();
                    minecraft.setScreen((Screen) difficultyScreen);
                    return;
                } catch (Exception e2) {
                    // Try constructor with player parameter
                    try {
                        Object difficultyScreen = difficultyScreenClass.getConstructor(PlayerEntity.class).newInstance(player);
                        minecraft.setScreen((Screen) difficultyScreen);
                        return;
                    } catch (Exception e3) {
                        vodmordia.modtabs.ModTabs.LOGGER.warn("Failed to create L2 Hostility Difficulty screen with standard constructors");
                    }
                }
            }

        } catch (Exception e) {
            // Log error for debugging
            vodmordia.modtabs.ModTabs.LOGGER.warn("Failed to open L2 Hostility Difficulty screen: " + e.getMessage());
        }
    }

    @Override
    public boolean isEnabled(PlayerEntity player) {
        return ModIntegrationManager.isModLoaded(ModIntegration.L2_HOSTILITY);
    }

    @Override
    public void initTabOnScreens() {
        if (!ModIntegrationManager.isModLoaded(ModIntegration.L2_HOSTILITY)) return;

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
        // Use wither skeleton skull as icon for hostility/difficulty
        renderWithItem(gui, x, y, hover, new ItemStack(Items.WITHER_SKELETON_SKULL));
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        if (currentScreen == null) return false;

        // Check if current screen is an L2 Hostility screen
        String screenName = currentScreen.getClass().getName();
        return screenName.contains("l2hostility") && screenName.contains("screen");
    }

    @Override
    public Text getTooltip() {
        return Text.literal("L2 Hostility");
    }
}
