package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import vodmordia.modtabs.api.tabs_menu.IntegrationIconTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.utils.ScreenClasses;

@TabConfig(configKey = "runicSkillsTab", defaultEnabled = true, defaultOrder = 0)
public class RunicSkillsTab extends IntegrationIconTab {
    // Reuse the leveling-book icon shipped by Runic Skills itself.
    private static final ResourceLocation RUNIC_SKILLS_ICON =
            new ResourceLocation("runicskills", "textures/item/leveling_book.png");

    // RunicSkillsScreen extends Screen (full-screen) rather than AbstractContainerScreen,
    // so anchor the tab bar to the top of the window — same layout JourneyMap and Pufferfish
    // Skills use for their full-screen UIs.
    private static final TabSpec SPEC = new TabSpec(
            "runicSkillsTab",
            ModIntegration.RUNIC_SKILLS,
            () -> Config.Baked.runicSkillsTabEnabled,
            "runicSkills",
            "runic_skills",
            TabSpec.Layout.invertedTop(),
            new String[] { ScreenClasses.RUNIC_SKILLS_SCREEN },
            new String[] { ScreenClasses.RUNIC_SKILLS_SCREEN }
    );

    public RunicSkillsTab() {
        super(SPEC, RUNIC_SKILLS_ICON, Config.Baked.runicSkillsTabCustomIcon);
    }

    @Override
    public void openTargetScreen(Player player) {
        if (Config.Baked.runicSkillsTabEnabled && player.level().isClientSide) {
            try {
                Class<?> screenClass = Class.forName(ScreenClasses.RUNIC_SKILLS_SCREEN);
                Screen screen = (Screen) screenClass.getDeclaredConstructor().newInstance();
                Minecraft.getInstance().setScreen(screen);
            } catch (Exception ignored) {
            }
        }
    }
}
