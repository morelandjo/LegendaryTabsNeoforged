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

public class JourneyMapTab extends TabBase {

    @Override
    public void openTargetScreen(PlayerEntity player) {
        try {
            // Try to open JourneyMap fullscreen GUI
            MinecraftClient minecraft = MinecraftClient.getInstance();

            // Use reflection to open the fullscreen map
            Class<?> fullscreenClass = Class.forName("journeymap.client.ui.fullscreen.Fullscreen");

            // Try different constructor patterns that JourneyMap might use
            try {
                // Try constructor with parent screen parameter
                Object fullscreen = fullscreenClass.getConstructor(Screen.class).newInstance(minecraft.currentScreen);
                minecraft.setScreen((Screen) fullscreen);
                return;
            } catch (Exception e1) {
                try {
                    // Try default constructor
                    Object fullscreen = fullscreenClass.getConstructor().newInstance();
                    minecraft.setScreen((Screen) fullscreen);
                    return;
                } catch (Exception e2) {
                    // Try alternative approach - look for static method to open map
                    try {
                        // Try to find and call a static open method
                        java.lang.reflect.Method openMethod = fullscreenClass.getMethod("open");
                        openMethod.invoke(null);
                        return;
                    } catch (Exception e3) {
                        vodmordia.modtabs.ModTabs.LOGGER.warn("Failed to create JourneyMap fullscreen with standard constructors");
                    }
                }
            }

        } catch (Exception e) {
            // Log error for debugging
            vodmordia.modtabs.ModTabs.LOGGER.warn("Failed to open JourneyMap screen: " + e.getMessage());
        }
    }

    @Override
    public boolean isEnabled(PlayerEntity player) {
        return ModIntegrationManager.isModLoaded(ModIntegration.JOURNEYMAP);
    }

    @Override
    public void initTabOnScreens() {
        if (!ModIntegrationManager.isModLoaded(ModIntegration.JOURNEYMAP)) return;

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
        // Use map as icon for JourneyMap
        renderWithItem(gui, x, y, hover, new ItemStack(Items.MAP));
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        if (currentScreen == null) return false;

        // Check if current screen is a JourneyMap screen
        String screenName = currentScreen.getClass().getName();
        return screenName.contains("journeymap") && (screenName.contains("fullscreen") || screenName.contains("ui"));
    }

    @Override
    public Text getTooltip() {
        return Text.literal("JourneyMap");
    }
}
