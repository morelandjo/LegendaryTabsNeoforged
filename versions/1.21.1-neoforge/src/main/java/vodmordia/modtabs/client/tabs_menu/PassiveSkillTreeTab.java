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

@TabConfig(configKey = "passiveSkillTreeTab", defaultEnabled = true, defaultOrder = 0)
public class PassiveSkillTreeTab extends IntegrationIconTab {
    private static final ResourceLocation SKILL_TREE_ICON =
            ResourceLocation.fromNamespaceAndPath(ModTabs.MOD_ID, "textures/gui/skill_tree.png");

    private static final TabSpec SPEC = new TabSpec(
            "passiveSkillTreeTab",
            ModIntegration.PASSIVE_SKILL_TREE,
            () -> Config.Baked.passiveSkillTreeTabEnabled,
            "passiveSkillTree",
            "passive_skill_tree",
            TabSpec.Layout.guiRelative(),
            new String[] { ScreenClasses.PASSIVE_SKILL_TREE },
            new String[] { ScreenClasses.PASSIVE_SKILL_TREE }
    );

    public PassiveSkillTreeTab() {
        super(SPEC, SKILL_TREE_ICON, Config.Baked.passiveSkillTreeTabCustomIcon);
    }

    @Override
    public void openTargetScreen(Player player) {
        if (Config.Baked.passiveSkillTreeTabEnabled && player.level().isClientSide) {
            try {
                Class<?> skillTreeScreenClass = Class.forName(ScreenClasses.PASSIVE_SKILL_TREE);
                ResourceLocation mainTreeLocation = ResourceLocation.fromNamespaceAndPath("skilltree", "main_tree");
                Screen skillTreeScreen = (Screen) skillTreeScreenClass.getDeclaredConstructor(ResourceLocation.class)
                        .newInstance(mainTreeLocation);
                Minecraft.getInstance().setScreen(skillTreeScreen);
            } catch (Exception e) {
                // Passive Skill Tree mod not present or failed to open screen
            }
        }
    }
}
