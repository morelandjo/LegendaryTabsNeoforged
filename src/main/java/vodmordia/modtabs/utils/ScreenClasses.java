package vodmordia.modtabs.utils;

/**
 * Central registry of fully-qualified class names for screens (and a few items) from every
 * integrated mod. When a mod renames or restructures a class, this file is the only place
 * that needs to change — instead of grepping across 35 tab classes plus the TabsMenu switch.
 *
 * <p>Constants are grouped by mod and named so the call site reads naturally:
 * {@code ScreenClasses.FTB_LIBRARY_WRAPPER} rather than a raw string literal.
 *
 * <p>All values are {@code public static final String} so they remain valid as
 * {@code switch} case labels and as compile-time constants.
 */
public final class ScreenClasses {
    private ScreenClasses() {}

    // -- Vanilla ----------------------------------------------------------
    public static final String VANILLA_INVENTORY =
            "net.minecraft.client.gui.screens.inventory.InventoryScreen";
    public static final String VANILLA_ADVANCEMENTS =
            "net.minecraft.client.gui.screens.advancements.AdvancementsScreen";
    public static final String VANILLA_CONTAINER =
            "net.minecraft.client.gui.screens.inventory.AbstractContainerScreen";

    // -- AppleSkin --------------------------------------------------------
    public static final String APPLESKIN_FOOD_STATS =
            "squeek.appleskin.client.gui.screen.FoodStatsScreen";

    // -- Ars Nouveau ------------------------------------------------------
    public static final String ARS_NOUVEAU_SPELLBOOK_GUI =
            "com.hollingsworth.arsnouveau.client.gui.book.GuiSpellBook";
    public static final String ARS_NOUVEAU_SPELLBOOK_ITEM =
            "com.hollingsworth.arsnouveau.common.items.SpellBook";
    // Legacy / alternate names also present in TabsMenu switch
    public static final String ARS_NOUVEAU_SPELLBOOK_GUI_LEGACY1 =
            "com.dhanantry.arsnouveau.client.gui.SpellBookGUI";
    public static final String ARS_NOUVEAU_SPELLBOOK_GUI_LEGACY2 =
            "com.dhanantry.arsnouveau.client.gui.SpellBookScreen";

    // -- Backpacked (mrcrayfish) ------------------------------------------
    public static final String BACKPACKED_SCREEN =
            "com.mrcrayfish.backpacked.client.gui.screen.inventory.BackpackScreen";
    public static final String BACKPACKED_ITEM =
            "com.mrcrayfish.backpacked.item.BackpackItem";
    public static final String BACKPACKED_SCREEN_ALT =
            "net.backpacked.client.screen.BackpackScreen";
    public static final String BACKPACKED_FLYWHEEL_TRANSFORM =
            "com.jozufozu.flywheel.util.transform.TransformStack";

    // -- Better Advancements ----------------------------------------------
    public static final String BETTER_ADVANCEMENTS =
            "betteradvancements.common.gui.BetterAdvancementsScreen";

    // -- Body Damage (Legendary Survival Overhaul) ------------------------
    public static final String LSO_BODY_HEALTH =
            "sfiomn.legendarysurvivaloverhaul.client.gui.BodyHealthScreen";

    // -- Brassworks Missions ----------------------------------------------
    public static final String BRASSWORKS_MISSIONS_UI =
            "net.swzo.brassworksmissions.client.gui.UiScreen";

    // -- Cobblemon --------------------------------------------------------
    public static final String COBBLEMON_PARTY =
            "com.cobblemon.mod.common.client.gui.party.PartyGUI";
    public static final String COBBLEMON_PARTY_LEGACY =
            "com.cobblemon.mod.common.client.gui.PartyGUI";

    // -- Cosmetic Armor Reworked ------------------------------------------
    public static final String COSMETIC_ARMOR_GUI =
            "lain.mods.cos.impl.client.gui.GuiCosArmorInventory";

    // -- Draconic Evolution / SCGuns ---------------------------------------
    public static final String DRACONIC_EVOLUTION_GUI =
            "com.brandon3055.draconicevolution.client.gui.GuiDraconicEvolution";
    public static final String SCGUNS_ATTACHMENT =
            "com.dhanantry.scguns.client.screen.AttachmentScreen";
    public static final String SCGUNS_PASSIVE_SKILL =
            "com.dhanantry.scguns.client.screen.PassiveSkillScreen";

    // -- Eccentric Tome ---------------------------------------------------
    public static final String ECCENTRIC_TOME_SCREEN =
            "website.eccentric.tome.client.TomeScreen";
    public static final String ECCENTRIC_TOME_ITEM =
            "website.eccentric.tome.TomeItem";

    // -- FTB Library / Quests / Teams -------------------------------------
    public static final String FTB_LIBRARY_WRAPPER =
            "dev.ftb.mods.ftblibrary.ui.ScreenWrapper";
    public static final String FTB_QUESTS_QUEST_SCREEN =
            "dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen";
    public static final String FTB_QUESTS_QUESTION_SCREEN =
            "dev.ftb.mods.ftbquests.client.gui.QuestionScreen";
    public static final String FTB_QUESTS_QUESTS_SCREEN =
            "dev.ftb.mods.ftbquests.client.gui.QuestsScreen";
    public static final String FTB_QUESTS_QUEST_SCREEN_ALT1 =
            "dev.ftb.mods.ftbquests.client.screens.QuestScreen";
    public static final String FTB_QUESTS_QUEST_SCREEN_ALT2 =
            "dev.ftb.mods.ftbquests.client.QuestScreen";
    public static final String FTB_TEAMS_SCREEN =
            "dev.ftb.mods.ftbteams.client.gui.TeamsScreen";
    public static final String FTB_TEAMS_SCREEN_ALT1 =
            "dev.ftb.mods.ftbteams.client.screens.TeamsScreen";
    public static final String FTB_TEAMS_SCREEN_ALT2 =
            "dev.ftb.mods.ftbteams.client.TeamsScreen";

    // -- JourneyMap -------------------------------------------------------
    public static final String JOURNEYMAP_FULLSCREEN =
            "journeymap.client.ui.fullscreen.Fullscreen";

    // -- Map Atlases ------------------------------------------------------
    public static final String MAP_ATLASES_OVERVIEW =
            "pepjebs.mapatlases.client.screen.AtlasOverviewScreen";
    public static final String MAP_ATLASES_ACCESS_UTILS =
            "pepjebs.mapatlases.client.ui.MapAtlasesAccessUtils";
    public static final String MAP_ATLASES_ITEM =
            "pepjebs.mapatlases.item.MapAtlasItem";

    // -- Pufferfish Skills ------------------------------------------------
    public static final String PUFFERFISH_SKILLS =
            "net.puffish.skillsmod.client.gui.SkillsScreen";
    public static final String PUFFERFISH_SKILLS_ALT1 =
            "puffish.skillsmod.client.screen.SkillScreen";
    public static final String PUFFERFISH_SKILLS_ALT2 =
            "net.puffish.skillsmod.client.screen.SkillScreen";

    // -- Xaero's World Map ------------------------------------------------
    public static final String XAEROS_MAP =
            "xaero.map.gui.GuiMap";

    // -- Cobblemon (additional screens for tab registration) --------------
    public static final String COBBLEMON_SUMMARY =
            "com.cobblemon.mod.common.client.gui.summary.Summary";
    public static final String COBBLEMON_STARTER_SELECTION =
            "com.cobblemon.mod.common.client.gui.startselection.StarterSelectionScreen";

    // -- FTB Chunks -------------------------------------------------------
    public static final String FTB_CHUNKS_MAP =
            "dev.ftb.mods.ftbchunks.client.map.MapScreen";
    public static final String FTB_CHUNKS_MAP_ALT1 =
            "dev.ftb.mods.ftbchunks.client.gui.MapScreen";
    public static final String FTB_CHUNKS_MAP_ALT2 =
            "dev.ftb.mods.ftbchunks.client.screens.MapScreen";

    // -- Memory of the Past (MOTP) ----------------------------------------
    public static final String MOTP_PLAYER_STATS =
            "tn.mbs.memory.client.gui.PlayerStatsGUIScreen";

    // -- Passive Skill Tree (daripher) ------------------------------------
    public static final String PASSIVE_SKILL_TREE =
            "daripher.skilltree.client.screen.SkillTreeScreen";

    // -- L2 family --------------------------------------------------------
    public static final String L2_ARTIFACTS_SET_EFFECT =
            "dev.xkmc.l2artifacts.content.client.tab.SetEffectScreen";
    public static final String L2_HOSTILITY_DIFFICULTY =
            "dev.xkmc.l2hostility.content.menu.tab.DifficultyScreen";

    // -- Modular Golems ---------------------------------------------------
    public static final String MODULAR_GOLEMS_INFO =
            "dev.xkmc.modulargolems.content.client.tracker.GolemInfoScreen";

    // -- RPG Crafting -----------------------------------------------------
    public static final String RPG_CRAFTING_HAND =
            "com.github.theredbrain.rpgcrafting.gui.screen.ingame.HandCraftingScreen";

    // -- Body Damage / LSO (modern path) ----------------------------------
    public static final String LSO_BODY_HEALTH_SCREENS =
            "sfiomn.legendarysurvivaloverhaul.client.screens.BodyHealthScreen";

    // -- Draconic Evolution (configurable item GUI) -----------------------
    public static final String DRACONIC_EVOLUTION_CONFIGURABLE_ITEM =
            "com.brandon3055.draconicevolution.client.gui.modular.itemconfig.ConfigurableItemGui$Screen";

    // -- Travelers Backpack -----------------------------------------------
    public static final String TRAVELERS_BACKPACK_SCREEN =
            "com.tiviacz.travelersbackpack.client.screens.BackpackScreen";

    // -- Biology Dictionary -----------------------------------------------
    public static final String BIOLOGY_DICTIONARY_HOME_SCREEN =
            "io.github.xienaoban.biologydictionary.gui.screen.BdHomeScreen";
    public static final String BIOLOGY_DICTIONARY_ABOUT_SCREEN =
            "io.github.xienaoban.biologydictionary.gui.screen.BdAboutScreen";
    public static final String BIOLOGY_DICTIONARY_CONFIG_SCREEN =
            "io.github.xienaoban.biologydictionary.gui.screen.BdConfigScreen";
    public static final String BIOLOGY_DICTIONARY_ENTITY_OVERVIEW_SCREEN =
            "io.github.xienaoban.biologydictionary.gui.screen.BdEntityOverviewScreen";
    public static final String BIOLOGY_DICTIONARY_ENTITY_DETAIL_SCREEN =
            "io.github.xienaoban.biologydictionary.gui.screen.BdEntityDetailScreen";
    /** {@code BiologyDictionaryItem.createBook()} / {@code isBook(ItemStack)}. */
    public static final String BIOLOGY_DICTIONARY_ITEM =
            "io.github.xienaoban.biologydictionary.core.BiologyDictionaryItem";
    /** Static {@code openBookScreen(Minecraft)} entry point used by the keybind and right-click. */
    public static final String BIOLOGY_DICTIONARY_EVENT =
            "io.github.xienaoban.biologydictionary.client.BiologyDictionaryEvent";

    // -- Reliable Backpacks -----------------------------------------------
    public static final String RELIABLE_BACKPACKS_ITEM =
            "com.evandev.reliable_backpacks.common.items.BackpackItem";
    public static final String RELIABLE_BACKPACKS_CONTAINER =
            "com.evandev.reliable_backpacks.common.items.BackpackItemContainer";

    // -- Runic Skills -----------------------------------------------------
    public static final String RUNIC_SKILLS_SCREEN =
            "com.otectus.runicskills.client.screen.RunicSkillsScreen";

    // -- Advanced Team (clefal) -------------------------------------------
    /** Abstract parent of HasTeamScreen / NoTeamScreen — used for tab attachment. */
    public static final String ADVANCED_TEAM_SCREEN =
            "com.clefal.teams.client.gui.screens.TeamsScreen";
    public static final String ADVANCED_TEAM_HAS_TEAM_SCREEN =
            "com.clefal.teams.client.gui.screens.hasteam.HasTeamScreen";
    public static final String ADVANCED_TEAM_NO_TEAM_SCREEN =
            "com.clefal.teams.client.gui.screens.noteam.NoTeamScreen";
    /** Static {@code INSTANCE} field; {@code isInTeam()} picks which screen to open. */
    public static final String ADVANCED_TEAM_CLIENT_TEAM =
            "com.clefal.teams.client.core.ClientTeam";

    // -- Villager Recruits ------------------------------------------------
    /** Faction landing screen opened by the U keybind. Constructor takes a {@link net.minecraft.world.entity.player.Player}. */
    public static final String VILLAGER_RECRUITS_FACTION_MAIN_SCREEN =
            "com.talhanation.recruits.client.gui.faction.FactionMainScreen";

    // -- Apothic Attributes (modid attributeslib on 1.20.1) ---------------
    /** Renderable overlay attached to {@code InventoryScreen} via the mod's own ScreenEvent.Init.Post listener.
     *  Holds the static {@code wasOpen} flag we flip before opening the inventory. */
    public static final String APOTHIC_ATTRIBUTES_GUI =
            "dev.shadowsoffire.attributeslib.client.AttributesGui";
    /** Static {@code enableAttributesGui} field — server admins can disable the panel entirely. */
    public static final String APOTHIC_ATTRIBUTES_CONFIG =
            "dev.shadowsoffire.attributeslib.ALConfig";
}
