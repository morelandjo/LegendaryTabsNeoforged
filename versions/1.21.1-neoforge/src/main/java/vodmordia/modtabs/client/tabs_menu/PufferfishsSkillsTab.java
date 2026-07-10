package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.puffish.skillsmod.client.SkillsClientMod;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.IntegrationIconTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.utils.ClassCache;
import vodmordia.modtabs.utils.ScreenClasses;

import java.util.Optional;

@TabConfig(configKey = "pufferfishSkillsTab", defaultEnabled = true, defaultOrder = 0)
public class PufferfishsSkillsTab extends IntegrationIconTab {
    private static final ResourceLocation PUFFER_ICON =
            ResourceLocation.fromNamespaceAndPath(ModTabs.MOD_ID, "textures/gui/puffer.png");

    private static final TabSpec SPEC = TabSpec.withoutCurrentScreen(
            "pufferfishSkillsTab",
            ModIntegration.PUFFERFISHS_SKILLS,
            () -> Config.Baked.pufferfishSkillsTabEnabled,
            "pufferfishSkills",
            "pufferfish_skills",
            TabSpec.Layout.invertedTop(),
            ScreenClasses.PUFFERFISH_SKILLS
    );

    public PufferfishsSkillsTab() {
        super(SPEC, PUFFER_ICON, Config.Baked.pufferfishSkillsTabCustomIcon);
    }

    @Override
    public boolean isHomeTab(Screen currentScreen) {
        // Spec uses withoutCurrentScreen so the tab stays clickable on the skills screen
        // (acts as a refresh / category switch). The long-press-to-edit gesture keys off
        // isHomeTab, so match the skills screen FQNs directly here — otherwise holding
        // the tab on its own screen never enters the layout editor.
        return ClassCache.isInstance(ScreenClasses.PUFFERFISH_SKILLS, currentScreen)
            || ClassCache.isInstance(ScreenClasses.PUFFERFISH_SKILLS_ALT1, currentScreen)
            || ClassCache.isInstance(ScreenClasses.PUFFERFISH_SKILLS_ALT2, currentScreen);
    }

    @Override
    public void openTargetScreen(Player player) {
        if (Config.Baked.pufferfishSkillsTabEnabled && player.level().isClientSide) {
            SkillsClientMod.getInstance().openScreen(Optional.empty());
        }
    }
}
