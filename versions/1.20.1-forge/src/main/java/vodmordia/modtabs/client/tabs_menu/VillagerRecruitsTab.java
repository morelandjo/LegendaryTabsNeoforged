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

@TabConfig(configKey = "villagerRecruitsTab", defaultEnabled = true, defaultOrder = 0)
public class VillagerRecruitsTab extends IntegrationIconTab {
    // Reuse the leader_crown image shipped by Recruits — already 16x16, faction-themed.
    private static final ResourceLocation VILLAGER_RECRUITS_ICON =
            new ResourceLocation("recruits", "textures/gui/image/leader_crown.png");

    // FactionMainScreen extends RecruitsScreenBase → Screen (full-screen), not
    // AbstractContainerScreen, so anchor the tab bar to the top of the window.
    private static final TabSpec SPEC = new TabSpec(
            "villagerRecruitsTab",
            ModIntegration.VILLAGER_RECRUITS,
            () -> Config.Baked.villagerRecruitsTabEnabled,
            "villagerRecruits",
            "villager_recruits",
            TabSpec.Layout.invertedTop(),
            new String[] { ScreenClasses.VILLAGER_RECRUITS_FACTION_MAIN_SCREEN },
            new String[] {
                    ScreenClasses.VILLAGER_RECRUITS_FACTION_MAIN_SCREEN,
                    ScreenClasses.VILLAGER_RECRUITS_WORLD_MAP_SCREEN
            }
    );

    public VillagerRecruitsTab() {
        super(SPEC, VILLAGER_RECRUITS_ICON, Config.Baked.villagerRecruitsTabCustomIcon);
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.villagerRecruitsTabEnabled || !player.level().isClientSide) {
            return;
        }
        // Mirror Recruits' KeyEvents.onKeyInput TEAM_SCREEN_KEY handler:
        //   new FactionMainScreen(localPlayer)
        try {
            Class<?> screenClass = Class.forName(ScreenClasses.VILLAGER_RECRUITS_FACTION_MAIN_SCREEN);
            Screen screen = (Screen) screenClass.getConstructor(Player.class).newInstance(player);
            Minecraft.getInstance().setScreen(screen);
        } catch (Exception e) {
            ModTabs.LOGGER.error("Error opening Villager Recruits faction screen", e);
        }
    }
}
