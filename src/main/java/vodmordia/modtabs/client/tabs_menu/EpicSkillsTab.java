package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.IntegrationItemTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.utils.ClassCache;
import vodmordia.modtabs.utils.ScreenClasses;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Tab for the Epic Fight Skill Tree addon (epicskills) — Forge 1.20.1 build. Mirrors the
 * mod's own keybind path: {@code Minecraft.setScreen(new SkillTreeScreen(localPlayerPatch))}.
 * Same constructor on both 1.20.1 and 1.21.1, so this differs from the 1.21.1 version only
 * in {@code ResourceLocation} construction.
 *
 * <p>{@code SkillTreeScreen.discarded()} returns true when the player has no unlocked
 * skill trees — calling it before {@code setScreen} matches the mod's own keybind guard.
 */
@TabConfig(configKey = "epicSkillsTab", defaultEnabled = true, defaultOrder = 0)
public class EpicSkillsTab extends IntegrationItemTab {
    private static final ResourceLocation ABILITY_STONE_ID =
            new ResourceLocation("epicskills", "ability_stone");

    private static final TabSpec SPEC = new TabSpec(
            "epicSkillsTab",
            ModIntegration.EPIC_SKILLS,
            () -> Config.Baked.epicSkillsTabEnabled,
            "epicSkills",
            "epic_skills",
            TabSpec.Layout.guiRelative(),
            new String[] { ScreenClasses.EPIC_SKILLS_SKILL_TREE_SCREEN },
            new String[] { ScreenClasses.EPIC_SKILLS_SKILL_TREE_SCREEN }
    );

    public EpicSkillsTab() {
        super(SPEC, EpicSkillsTab::getAbilityStoneIcon, Config.Baked.epicSkillsTabCustomIcon);
    }

    private static ItemStack getAbilityStoneIcon() {
        Item item = BuiltInRegistries.ITEM.get(ABILITY_STONE_ID);
        if (item != null && item != Items.AIR) {
            return new ItemStack(item);
        }
        return new ItemStack(Items.EXPERIENCE_BOTTLE);
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.epicSkillsTabEnabled || !player.level().isClientSide) return;
        if (!(player instanceof LocalPlayer localPlayer)) return;
        try {
            Class<?> capsClass = ClassCache.resolve(ScreenClasses.EPIC_FIGHT_CAPABILITIES);
            if (capsClass == null) return;
            Method getPatch = capsClass.getMethod("getLocalPlayerPatch", LocalPlayer.class);
            Object patch = getPatch.invoke(null, localPlayer);
            if (patch == null) return;

            Class<?> screenClass = ClassCache.resolve(ScreenClasses.EPIC_SKILLS_SKILL_TREE_SCREEN);
            if (screenClass == null) return;
            Class<?> patchClass = ClassCache.resolve(ScreenClasses.EPIC_FIGHT_LOCAL_PLAYER_PATCH);
            if (patchClass == null) return;
            Constructor<?> ctor = screenClass.getConstructor(patchClass);
            Screen screen = (Screen) ctor.newInstance(patch);

            Method discarded = screenClass.getMethod("discarded");
            Object discardedResult = discarded.invoke(screen);
            if (discardedResult instanceof Boolean b && b) return;

            Minecraft.getInstance().setScreen(screen);
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Error opening Epic Fight Skill Tree: " + e.getMessage());
        }
    }
}
