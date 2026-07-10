package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.IntegrationIconTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.utils.ScreenClasses;

@TabConfig(configKey = "advancedTeamTab", defaultEnabled = true, defaultOrder = 0)
public class AdvancedTeamTab extends IntegrationIconTab {
    private static final ResourceLocation ADVANCED_TEAM_ICON =
            new ResourceLocation(ModTabs.MOD_ID, "textures/gui/advancedteam.png");

    // Advanced Team's TeamsScreen is a centered popup (256x166) that extends Screen directly,
    // not AbstractContainerScreen. Anchor the tab bar to the top of the window — same layout
    // FTB Teams uses for its similarly-shaped ScreenWrapper.
    private static final TabSpec SPEC = new TabSpec(
            "advancedTeamTab",
            ModIntegration.ADVANCED_TEAM,
            () -> Config.Baked.advancedTeamTabEnabled,
            "advancedTeam",
            "advanced_team",
            TabSpec.Layout.invertedTop(),
            new String[] {
                    ScreenClasses.ADVANCED_TEAM_HAS_TEAM_SCREEN,
                    ScreenClasses.ADVANCED_TEAM_NO_TEAM_SCREEN
            },
            new String[] {
                    ScreenClasses.ADVANCED_TEAM_HAS_TEAM_SCREEN,
                    ScreenClasses.ADVANCED_TEAM_NO_TEAM_SCREEN
            }
    );

    public AdvancedTeamTab() {
        super(SPEC, ADVANCED_TEAM_ICON, Config.Baked.advancedTeamTabCustomIcon);
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.advancedTeamTabEnabled || !player.level().isClientSide) {
            return;
        }
        // Mirror the in-game inventory button's behavior (com.clefal.teams.client.gui.inventory
        // .InventoryButton): pick HasTeamScreen vs NoTeamScreen by ClientTeam.INSTANCE.isInTeam(),
        // and pass the current screen as parent so the in-screen "Go Back" button returns there.
        try {
            Minecraft minecraft = Minecraft.getInstance();
            Screen parent = minecraft.screen;

            Class<?> clientTeamClass = Class.forName(ScreenClasses.ADVANCED_TEAM_CLIENT_TEAM);
            Object instance = clientTeamClass.getField("INSTANCE").get(null);
            boolean inTeam = (boolean) clientTeamClass.getMethod("isInTeam").invoke(instance);

            String targetClassName = inTeam
                    ? ScreenClasses.ADVANCED_TEAM_HAS_TEAM_SCREEN
                    : ScreenClasses.ADVANCED_TEAM_NO_TEAM_SCREEN;
            Class<?> screenClass = Class.forName(targetClassName);
            Screen screen = (Screen) screenClass.getConstructor(Screen.class).newInstance(parent);
            minecraft.setScreen(screen);
        } catch (Exception e) {
            ModTabs.LOGGER.error("Error opening Advanced Team screen", e);
        }
    }
}
