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
 * Tab for Epic Fight's skill editor. Mirrors the mod's own "K" keybind path —
 * {@code ControlEngine.openSkillEditor()} just calls
 * {@code Minecraft.setScreen(new SkillEditScreen(player, patch.getPlayerSkills()))}
 * with no networking, so the tab does the same via reflection.
 *
 * The patch is fetched through {@code EpicFightCapabilities.getLocalPlayerPatch(LocalPlayer)},
 * which reads Epic Fight's NeoForge data-attachment. If the patch or its skills payload
 * isn't ready yet (e.g. player just logged in), the click is a no-op.
 */
@TabConfig(configKey = "epicFightTab", defaultEnabled = true, defaultOrder = 0)
public class EpicFightTab extends IntegrationItemTab {
    private static final ResourceLocation SKILLBOOK_ID =
            ResourceLocation.fromNamespaceAndPath("epicfight", "skillbook");

    private static final TabSpec SPEC = new TabSpec(
            "epicFightTab",
            ModIntegration.EPIC_FIGHT,
            () -> Config.Baked.epicFightTabEnabled,
            "epicFight",
            "epic_fight",
            TabSpec.Layout.guiRelative(),
            new String[] { ScreenClasses.EPIC_FIGHT_SKILL_EDIT_SCREEN },
            new String[] { ScreenClasses.EPIC_FIGHT_SKILL_EDIT_SCREEN }
    );

    public EpicFightTab() {
        super(SPEC, EpicFightTab::getSkillbookIcon, Config.Baked.epicFightTabCustomIcon);
    }

    private static ItemStack getSkillbookIcon() {
        Item item = BuiltInRegistries.ITEM.get(SKILLBOOK_ID);
        if (item != null && item != Items.AIR) {
            return new ItemStack(item);
        }
        return new ItemStack(Items.IRON_SWORD);
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.epicFightTabEnabled || !player.level().isClientSide) return;
        if (!(player instanceof LocalPlayer localPlayer)) return;
        try {
            Class<?> capsClass = ClassCache.resolve(ScreenClasses.EPIC_FIGHT_CAPABILITIES);
            if (capsClass == null) return;
            Method getPatch = capsClass.getMethod("getLocalPlayerPatch", LocalPlayer.class);
            Object patch = getPatch.invoke(null, localPlayer);
            if (patch == null) return;

            Method getSkills = patch.getClass().getMethod("getPlayerSkills");
            Object skills = getSkills.invoke(patch);
            if (skills == null) return;

            Class<?> screenClass = ClassCache.resolve(ScreenClasses.EPIC_FIGHT_SKILL_EDIT_SCREEN);
            if (screenClass == null) return;
            // SkillEditScreen's second parameter is declared as PlayerSkills (the parameter type
            // returned by getPlayerSkills()), not whatever concrete subclass the instance is —
            // so resolve by name rather than skills.getClass().
            Class<?> skillsClass = getSkills.getReturnType();
            Constructor<?> ctor = screenClass.getConstructor(Player.class, skillsClass);
            Screen screen = (Screen) ctor.newInstance(localPlayer, skills);
            Minecraft.getInstance().setScreen(screen);
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Error opening Epic Fight skill editor: " + e.getMessage());
        }
    }
}
