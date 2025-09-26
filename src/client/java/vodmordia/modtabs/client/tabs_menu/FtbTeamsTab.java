package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import vodmordia.modtabs.api.tabs_menu.SimpleTextureTab;
import net.minecraft.util.Identifier;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

public class FtbTeamsTab extends SimpleTextureTab {
    private static final Identifier TEAMS_TEXTURE = new Identifier("ftbteams", "textures/teams.png");

    public FtbTeamsTab() {
        super(TEAMS_TEXTURE, 12, 12);
    }

    @Override
    public void openTargetScreen(PlayerEntity player) {
        try {
            MinecraftClient minecraft = MinecraftClient.getInstance();

            // Try first approach: Direct screen creation
            try {
                Class<?> teamsScreenClass = Class.forName("dev.ftb.mods.ftbteams.client.gui.MyTeamScreen");
                Object teamsScreen = teamsScreenClass.getConstructor().newInstance();
                minecraft.setScreen((Screen) teamsScreen);
                return;
            } catch (Exception e1) {
                vodmordia.modtabs.ModTabs.LOGGER.debug("Direct screen creation failed: " + e1.getMessage());
            }

            // Try second approach: Look for GUI opening methods
            try {
                Class<?> clientEventsClass = Class.forName("dev.ftb.mods.ftbteams.client.FTBTeamsClient");
                java.lang.reflect.Method openMyTeamGuiMethod = clientEventsClass.getDeclaredMethod("openMyTeamGui");
                openMyTeamGuiMethod.setAccessible(true);
                openMyTeamGuiMethod.invoke(null);
                return;
            } catch (Exception e2) {
                vodmordia.modtabs.ModTabs.LOGGER.debug("FTBTeamsClient.openMyTeamGui failed: " + e2.getMessage());
            }

            // Try third approach: Look for keybinding trigger
            try {
                Class<?> keybindingsClass = Class.forName("dev.ftb.mods.ftbteams.client.FTBTeamsClientConfig");
                java.lang.reflect.Field openGuiKeyField = keybindingsClass.getDeclaredField("openGuiKey");
                openGuiKeyField.setAccessible(true);
                Object keyBinding = openGuiKeyField.get(null);

                // Simulate key press
                java.lang.reflect.Method setPressed = keyBinding.getClass().getMethod("setPressed", boolean.class);
                setPressed.invoke(keyBinding, true);
                setPressed.invoke(keyBinding, false);
            } catch (Exception e3) {
                vodmordia.modtabs.ModTabs.LOGGER.debug("Keybinding trigger failed: " + e3.getMessage());
            }

        } catch (Exception e) {
            vodmordia.modtabs.ModTabs.LOGGER.warn("Failed to open FTB Teams screen: " + e.getMessage());
        }
    }

    @Override
    public boolean isEnabled(PlayerEntity player) {
        return ModIntegrationManager.isModLoaded(ModIntegration.FTB_TEAMS);
    }

    @Override
    public void initTabOnScreens() {
        if (!ModIntegrationManager.isModLoaded(ModIntegration.FTB_TEAMS)) return;

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
    public boolean isCurrentlyUsed(Screen currentScreen) {
        if (currentScreen == null) return false;

        // Check if current screen is an FTB Teams screen
        String screenName = currentScreen.getClass().getName();
        return screenName.contains("ftbteams") && screenName.contains("gui");
    }

    @Override
    public Text getTooltip() {
        return Text.literal("FTB Teams");
    }
}
