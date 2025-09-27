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
    public void render(DrawContext gui, int x, int y, boolean hover) {
        // Custom positioning: 2 pixels right, 1 pixel down from default (5, 4)
        vodmordia.modtabs.api.tabs_menu.TabRenderer.builder()
            .withBackground()
            .withTextureIcon(getIconTexture(), 7, 5, 12, 12)
            .render(gui, x, y, hover, false);
    }

    @Override
    public void openTargetScreen(PlayerEntity player) {
        try {
            MinecraftClient minecraft = MinecraftClient.getInstance();

            // Direct screen creation approach
            Class<?> teamsScreenClass = Class.forName("dev.ftb.mods.ftbteams.client.gui.MyTeamScreen");
            Object teamsScreen = teamsScreenClass.getConstructor().newInstance();
            minecraft.setScreen((Screen) teamsScreen);
        } catch (Exception e) {
            // FTB Teams not present or failed to open screen
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
