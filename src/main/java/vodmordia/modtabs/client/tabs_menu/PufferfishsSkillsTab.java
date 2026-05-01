package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.IntegrationIconTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.utils.ScreenClasses;

import java.util.Optional;

@TabConfig(configKey = "pufferfishSkillsTab", defaultEnabled = true, defaultOrder = 0)
public class PufferfishsSkillsTab extends IntegrationIconTab {
    private static final ResourceLocation PUFFER_ICON =
            new ResourceLocation(ModTabs.MOD_ID, "textures/gui/puffer.png");

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
    public void openTargetScreen(Player player) {
        if (Config.Baked.pufferfishSkillsTabEnabled && player.level().isClientSide) {
            try {
                Class<?> skillsClientClass = Class.forName("net.puffish.skillsmod.client.SkillsClientMod");
                Object instance = skillsClientClass.getMethod("getInstance").invoke(null);
                instance.getClass().getMethod("openScreen", Optional.class).invoke(instance, Optional.empty());
            } catch (Exception ignored) {
            }
        }
    }
}
