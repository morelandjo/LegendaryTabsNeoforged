package vodmordia.modtabs.client.tabs_menu;

import dev.ftb.mods.ftbteams.net.OpenGUIMessage;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.ConfigurableIconTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

@TabConfig(configKey = "ftbTeamsTab", defaultEnabled = true, defaultOrder = 0)
public class FtbTeamsTab extends ConfigurableIconTab {
    private static final ResourceLocation TEAMS_TEXTURE = ResourceLocation.fromNamespaceAndPath("ftbteams", "textures/teams.png");

    public FtbTeamsTab() {
        super(TEAMS_TEXTURE, Config.Baked.ftbTeamsTabCustomIcon, "ftbTeams");
    }

    @Override
    public void openTargetScreen(Player player) {
        if (ModIntegrationManager.isModLoaded(ModIntegration.FTB_TEAMS) && player.level().isClientSide) {
            (new OpenGUIMessage()).sendToServer();
        }
    }

    @Override
    public boolean isEnabled(Player player) {
        return Config.Baked.ftbTeamsTabEnabled && ModIntegrationManager.isModLoaded(ModIntegration.FTB_TEAMS);
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        return false;
    }

    @Override
    public boolean isHomeTab(Screen currentScreen) {
        // isCurrentlyUsed is forced to false to keep this tab clickable, so the default
        // isHomeTab (=isCurrentlyUsed) never matches — the long-press-to-edit gesture
        // would never arm on FTB Teams screens. Match the screen class directly here.
        if (currentScreen == null) return false;
        String name = currentScreen.getClass().getName();
        if (name.startsWith("dev.ftb.mods.ftbteams.")) return true;
        if (vodmordia.modtabs.utils.ScreenClasses.FTB_LIBRARY_WRAPPER.equals(name)) {
            String wrapped = FtbScreenWrapperUtil.getWrappedGuiClassName(currentScreen);
            return wrapped != null && wrapped.startsWith("dev.ftb.mods.ftbteams.");
        }
        return false;
    }

    @Override
    public Component getTooltip() {
        return Component.translatable("tooltip." + ModTabs.MOD_ID + ".tab.ftb_teams.description");
    }

    @Override
    public void initTabOnScreens() {
        if (!ModIntegrationManager.isModLoaded(ModIntegration.FTB_TEAMS)) {
            return;
        }
        // FTB Quests and FTB Teams share dev.ftb.mods.ftblibrary.ui.ScreenWrapper. When Quests is
        // also installed it owns the ScreenWrapper registration, so we only claim it here as a
        // fallback — otherwise the two tabs would register the same screen with identical params
        // and fight over it via forceRegister.
        boolean questsLoaded = ModIntegrationManager.isModLoaded(ModIntegration.FTB_QUESTS);

        String[] screens = questsLoaded
            ? new String[] {
                vodmordia.modtabs.utils.ScreenClasses.FTB_TEAMS_SCREEN,
                vodmordia.modtabs.utils.ScreenClasses.FTB_TEAMS_SCREEN_ALT1,
                vodmordia.modtabs.utils.ScreenClasses.FTB_TEAMS_SCREEN_ALT2
            }
            : new String[] {
                vodmordia.modtabs.utils.ScreenClasses.FTB_LIBRARY_WRAPPER,
                vodmordia.modtabs.utils.ScreenClasses.FTB_TEAMS_SCREEN,
                vodmordia.modtabs.utils.ScreenClasses.FTB_TEAMS_SCREEN_ALT1,
                vodmordia.modtabs.utils.ScreenClasses.FTB_TEAMS_SCREEN_ALT2
            };

        ScreenRegistry.builder()
            .withStandardDimensions()
            .inverted()
            .registerAllTabs(screens);
    }
}
