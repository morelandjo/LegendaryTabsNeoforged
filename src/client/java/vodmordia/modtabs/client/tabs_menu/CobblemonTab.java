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
import vodmordia.modtabs.integration.ModIntegrationManager;

public class CobblemonTab extends TabBase {

    @Override
    public void openTargetScreen(PlayerEntity player) {
        try {
            MinecraftClient minecraft = MinecraftClient.getInstance();

            // Try to open Cobblemon party interface first
            try {
                Class<?> partyOverlayClass = Class.forName("com.cobblemon.mod.common.client.gui.PartyOverlay");

                // Try different constructor patterns
                try {
                    Object partyOverlay = partyOverlayClass.getConstructor().newInstance();
                    minecraft.setScreen((Screen) partyOverlay);
                    return;
                } catch (Exception e1) {
                    // Try with different parameters
                    try {
                        Object partyOverlay = partyOverlayClass.getConstructor(Screen.class).newInstance(minecraft.currentScreen);
                        minecraft.setScreen((Screen) partyOverlay);
                        return;
                    } catch (Exception e2) {
                        // Continue to next approach
                    }
                }
            } catch (Exception e) {
                // Continue to next approach
            }

            // Fallback: Try party select GUI
            try {
                Class<?> partySelectClass = Class.forName("com.cobblemon.mod.common.client.gui.interact.partyselect.PartySelectGUI");
                Object partySelect = partySelectClass.getConstructor().newInstance();
                minecraft.setScreen((Screen) partySelect);
                return;
            } catch (Exception e) {
                // Continue to next approach
            }

            // Last resort: Try starter selection screen
            try {
                Class<?> starterScreenClass = Class.forName("com.cobblemon.mod.common.client.gui.startselection.StarterSelectionScreen");
                Object starterScreen = starterScreenClass.getConstructor().newInstance();
                minecraft.setScreen((Screen) starterScreen);
                return;
            } catch (Exception e) {
                vodmordia.modtabs.ModTabs.LOGGER.warn("Failed to create Cobblemon screens with standard constructors");
            }

        } catch (Exception e) {
            // Log error for debugging
            vodmordia.modtabs.ModTabs.LOGGER.warn("Failed to open Cobblemon screen: " + e.getMessage());
        }
    }

    @Override
    public boolean isEnabled(PlayerEntity player) {
        // Check if Cobblemon is loaded
        return ModIntegrationManager.isModLoaded("cobblemon");
    }

    @Override
    public void initTabOnScreens() {
        if (!ModIntegrationManager.isModLoaded("cobblemon")) return;

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
        // Use spawn egg as icon for Pokemon/creatures
        renderWithItem(gui, x, y, hover, new ItemStack(Items.TURTLE_EGG));
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        if (currentScreen == null) return false;

        // Check if current screen is a Cobblemon screen
        String screenName = currentScreen.getClass().getName();
        return screenName.contains("cobblemon") && screenName.contains("gui");
    }

    @Override
    public Text getTooltip() {
        return Text.literal("Cobblemon");
    }
}
