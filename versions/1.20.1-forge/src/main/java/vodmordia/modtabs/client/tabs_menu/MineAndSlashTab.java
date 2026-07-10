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
import vodmordia.modtabs.utils.ClassCache;
import vodmordia.modtabs.utils.ScreenClasses;

/**
 * Tab for Mine and Slash Rework's talent tree. The mod's own flow opens this via
 * {@code MainHubScreen} (H keybind) → click the Talents button, which constructs a
 * {@code new TalentsScreen()} and calls {@code Minecraft.setScreen(...)}. We skip the
 * hub and jump straight to the talents view by mirroring that constructor reflectively.
 *
 * {@code TalentsScreen} is a plain {@link Screen} subclass (not a container screen)
 * whose {@code render()} wraps {@code super.render()} inside a zoom pose transform —
 * so any tab buttons added as children would render scaled. The screen FQN is in the
 * manual-renderables list in {@code ClientNeoForgeEvents.onScreenRenderPost} so the
 * tab bar draws at the correct (unscaled) size.
 */
@TabConfig(configKey = "mineAndSlashTab", defaultEnabled = true, defaultOrder = 0)
public class MineAndSlashTab extends IntegrationIconTab {
    private static final ResourceLocation TALENTS_ICON =
            new ResourceLocation("mmorpg", "textures/gui/main_hub/icons/talents.png");

    private static final TabSpec SPEC = new TabSpec(
            "mineAndSlashTab",
            ModIntegration.MINE_AND_SLASH,
            () -> Config.Baked.mineAndSlashTabEnabled,
            "mineAndSlash",
            "mine_and_slash",
            TabSpec.Layout.guiRelative(),
            new String[] { ScreenClasses.MINE_AND_SLASH_TALENTS_SCREEN },
            new String[] { ScreenClasses.MINE_AND_SLASH_TALENTS_SCREEN }
    );

    public MineAndSlashTab() {
        super(SPEC, TALENTS_ICON, Config.Baked.mineAndSlashTabCustomIcon);
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.mineAndSlashTabEnabled || !player.level().isClientSide) return;
        try {
            Class<?> screenClass = ClassCache.resolve(ScreenClasses.MINE_AND_SLASH_TALENTS_SCREEN);
            if (screenClass == null) return;
            Screen screen = (Screen) screenClass.getConstructor().newInstance();
            Minecraft.getInstance().setScreen(screen);
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Error opening Mine and Slash talent tree: " + e.getMessage());
        }
    }
}
