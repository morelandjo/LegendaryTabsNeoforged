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
    protected void renderInverted(DrawContext gui, int x, int y, boolean hover) {
        // Custom positioning for inverted tab
        vodmordia.modtabs.api.tabs_menu.TabRenderer.builder()
            .withBackground()
            .withTextureIcon(getIconTexture(), 7, 5, 12, 12)
            .render(gui, x, y, hover, true);
    }

    @Override
    public void openTargetScreen(PlayerEntity player) {
        if (ModIntegrationManager.isModLoaded(ModIntegration.FTB_TEAMS) && player.getWorld().isClient) {
            try {
                Class<?> openGUIMessageClass = Class.forName("dev.ftb.mods.ftbteams.net.OpenGUIMessage");
                Object openGUIMessage = openGUIMessageClass.getConstructor().newInstance();
                java.lang.reflect.Method sendToServerMethod = openGUIMessageClass.getMethod("sendToServer");
                sendToServerMethod.invoke(openGUIMessage);
            } catch (Exception e) {
                // FTB Teams not present or failed to open screen
            }
        }
    }


    @Override
    public boolean isEnabled(PlayerEntity player) {
        return ModIntegrationManager.isModLoaded(ModIntegration.FTB_TEAMS);
    }

    @Override
    public void initTabOnScreens() {
        // Register FTB Teams screen classes with inverted display at the top
        vodmordia.modtabs.api.tabs_menu.ScreenRegistry.builder()
            .withStandardDimensions()
            .inverted()
            .atTop()
            .registerAllTabs(
                "dev.ftb.mods.ftblibrary.ui.ScreenWrapper",
                "dev.ftb.mods.ftbteams.client.gui.TeamsScreen",
                "dev.ftb.mods.ftbteams.client.screens.TeamsScreen",
                "dev.ftb.mods.ftbteams.client.TeamsScreen"
            );

        // Force register ScreenWrapper to override any existing registration from FTB Quests
        vodmordia.modtabs.api.tabs_menu.ScreenRegistry.builder()
            .withStandardDimensions()
            .inverted()
            .atTop()
            .forceRegisterAllTabs("dev.ftb.mods.ftblibrary.ui.ScreenWrapper");
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
