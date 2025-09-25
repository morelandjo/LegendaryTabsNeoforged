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

public class XaerosMapTab extends TabBase {

    @Override
    public void openTargetScreen(PlayerEntity player) {
        try {
            // Try to open Xaero's World Map GUI
            MinecraftClient minecraft = MinecraftClient.getInstance();

            // Use reflection to open the world map screen
            Class<?> guiMapClass = Class.forName("xaero.map.gui.GuiMap");

            // Try different constructor patterns that Xaero's might use
            try {
                // Try constructor with parent screen parameter
                Object guiMap = guiMapClass.getConstructor(Screen.class).newInstance(minecraft.currentScreen);
                minecraft.setScreen((Screen) guiMap);
                return;
            } catch (Exception e1) {
                try {
                    // Try default constructor
                    Object guiMap = guiMapClass.getConstructor().newInstance();
                    minecraft.setScreen((Screen) guiMap);
                    return;
                } catch (Exception e2) {
                    // Try constructor with different parameters
                    vodmordia.modtabs.ModTabs.LOGGER.warn("Failed to create Xaero's World Map GUI with standard constructors");
                }
            }

        } catch (Exception e) {
            // Log error for debugging
            vodmordia.modtabs.ModTabs.LOGGER.warn("Failed to open Xaero's World Map screen: " + e.getMessage());
        }
    }

    @Override
    public boolean isEnabled(PlayerEntity player) {
        return ModIntegrationManager.isModLoaded(ModIntegration.XAEROS_WORLDMAP);
    }

    @Override
    public void initTabOnScreens() {
        if (!ModIntegrationManager.isModLoaded(ModIntegration.XAEROS_WORLDMAP)) return;

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
        // Use compass as icon for world map
        renderWithItem(gui, x, y, hover, new ItemStack(Items.COMPASS));
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        if (currentScreen == null) return false;

        // Check if current screen is a Xaero's World Map screen
        String screenName = currentScreen.getClass().getName();
        return screenName.contains("xaero") && screenName.contains("map") && screenName.contains("gui");
    }

    @Override
    public Text getTooltip() {
        return Text.literal("Xaero's World Map");
    }
}
