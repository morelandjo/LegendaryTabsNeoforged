package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.IntegrationIconTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.utils.ScreenClasses;

@TabConfig(configKey = "villagerRecruitsClaimMapTab", defaultEnabled = true, defaultOrder = 0)
public class VillagerRecruitsClaimMapTab extends IntegrationIconTab {
    // Recruits ships no dedicated claim-map icon, so reuse the vanilla filled-map texture
    // (same approach as FtbChunksTab).
    private static final ResourceLocation CLAIM_MAP_ICON =
            new ResourceLocation("minecraft", "textures/item/filled_map.png");

    // Attaches to both the faction screen and the world-map screen so the recruits tab row
    // is consistent across both. WorldMapScreen extends Screen (full-window), so anchor the
    // bar to the top of the window like the faction tab.
    private static final TabSpec SPEC = new TabSpec(
            "villagerRecruitsClaimMapTab",
            ModIntegration.VILLAGER_RECRUITS,
            () -> Config.Baked.villagerRecruitsClaimMapTabEnabled,
            "villagerRecruitsClaimMap",
            "villager_recruits_claim_map",
            TabSpec.Layout.invertedTop(),
            new String[] { ScreenClasses.VILLAGER_RECRUITS_WORLD_MAP_SCREEN },
            new String[] {
                    ScreenClasses.VILLAGER_RECRUITS_FACTION_MAIN_SCREEN,
                    ScreenClasses.VILLAGER_RECRUITS_WORLD_MAP_SCREEN
            }
    );

    public VillagerRecruitsClaimMapTab() {
        super(SPEC, CLAIM_MAP_ICON, Config.Baked.villagerRecruitsClaimMapTabCustomIcon);
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.villagerRecruitsClaimMapTabEnabled || !player.level().isClientSide) {
            return;
        }
        // Mirror Recruits' KeyEvents.onKeyInput MAP_SCREEN_KEY handler:
        //   if (level != null && level.dimension() == Level.OVERWORLD)
        //       mc.setScreen(new WorldMapScreen());
        // The mod gates the keybind on overworld and the screen itself reads overworld chunk
        // data, so skip silently in other dimensions to match the keybind's behaviour.
        if (player.level().dimension() != Level.OVERWORLD) {
            return;
        }
        try {
            Class<?> screenClass = Class.forName(ScreenClasses.VILLAGER_RECRUITS_WORLD_MAP_SCREEN);
            Screen screen = (Screen) screenClass.getConstructor().newInstance();
            Minecraft.getInstance().setScreen(screen);
        } catch (Exception e) {
            ModTabs.LOGGER.error("Error opening Villager Recruits claim map screen", e);
        }
    }
}
