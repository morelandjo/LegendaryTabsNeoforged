package vodmordia.modtabs.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class ModTabsConfig extends MidnightConfig {

    public static class CommentText {}

    @Comment(category = "tabs") public static CommentText placement;

    // Edited via the in-game global-settings modal (General tab); hidden from the
    // MidnightConfig list to avoid duplicate UIs.
    @Entry(category = "tabs") @Hidden public static int iconOffsetTop = 0;
    @Entry(category = "tabs") @Hidden public static int iconOffsetRight = 0;
    @Entry(category = "tabs") @Hidden public static int iconOffsetBottom = 0;
    @Entry(category = "tabs") @Hidden public static int iconOffsetLeft = 0;

    @Comment(category = "tabs") public static CommentText spacer_placement;

    @Comment(category = "tabs") public static CommentText inventory;

    // --- Sticky tabs ---------------------------------------------------------
    // Sticky tabs are pinned to the leading (left) end of the bar and stay
    // visible across pagination. Any tab can be sticky; toggled in-game via the
    // Global Settings "Sticky" panel. Inventory is sticky by default. Hidden
    // from the MidnightConfig list since they're edited only through that panel.
    @Entry(category = "tabs") @Hidden public static boolean inventoryTabSticky = true;
    @Entry(category = "tabs") @Hidden public static boolean advancementsTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean rpgCraftingTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean arsElixirumTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean arsNouveauTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean backpackedTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean bodyDamageTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean biologyDictionaryTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean wildexTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean apothicAttributesTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean aetherTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean curiosTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean epicFightTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean epicSkillsTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean completionistsIndexTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean sdmShopTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean jobsPlusTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean questLogTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean mapwrightTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean fieldGuideTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean modonomiconTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean reliableBackpacksTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean quarkBackpackTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean brassworksMissionsTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean cobblemonTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean cosmeticArmorTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean draconicEvolutionTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean eccentricTomeTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean dietTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean ftbQuestsTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean ftbChunksTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean ftbTeamsTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean journeyMapTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean l2ArtifactsTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean l2AttributesTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean l2HostilityTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean mapAtlasesTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean modularGolemsTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean motpTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean passiveSkillTreeTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean pufferfishSkillsTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean reskillableReimaginedTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean sophisticatedBackpacksTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean travelersBackpackTabSticky = false;
    @Entry(category = "tabs") @Hidden public static boolean xaerosMapTabSticky = false;

    // Modpack-maker lock. When false, all three layout-editor entry points
    // (Shift+Z, long-press on a tab, the Edit button in LayoutEditorButtons) are
    // no-ops, so packs can ship a fixed tab layout that players can't rearrange.
    @Entry(category = "tabs")
    public static boolean allowEditing = true;

    @Entry(category = "tabs") @Hidden
    public static boolean inventoryTabEnabled = true;


    @Entry(category = "tabs") @Hidden
    public static String inventoryTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int inventoryTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int inventoryTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int inventoryTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int inventoryTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int inventoryTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int inventoryTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility inventoryTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer1;

    @Comment(category = "tabs") public static CommentText advancements_and_rpg_crafting;

    @Comment(category = "tabs") public static CommentText advancements;

    @Entry(category = "tabs") @Hidden
    public static boolean advancementsTabEnabled = true;


    @Entry(category = "tabs") @Hidden
    public static String advancementsTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int advancementsTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int advancementsTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int advancementsTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int advancementsTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int advancementsTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int advancementsTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility advancementsTabDisplayVisibility = TabDisplayVisibility.TUCK;

    @Comment(category = "tabs") public static CommentText spacer1b;

    @Comment(category = "tabs") public static CommentText rpgCrafting;

    @Entry(category = "tabs") @Hidden
    public static boolean rpgCraftingTabEnabled = true;


    @Entry(category = "tabs") @Hidden
    public static String rpgCraftingTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int rpgCraftingTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int rpgCraftingTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int rpgCraftingTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int rpgCraftingTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int rpgCraftingTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int rpgCraftingTabOrder = 0;

    @Comment(category = "tabs") public static CommentText spacer2;

    @Comment(category = "tabs") public static CommentText arsElixirum;

    @Entry(category = "tabs") @Hidden
    public static boolean arsElixirumTabEnabled = true;


    @Entry(category = "tabs") @Hidden
    public static String arsElixirumTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int arsElixirumTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int arsElixirumTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int arsElixirumTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int arsElixirumTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int arsElixirumTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int arsElixirumTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility arsElixirumTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer3;

    @Comment(category = "tabs") public static CommentText arsNouveau;

    @Entry(category = "tabs") @Hidden
    public static boolean arsNouveauTabEnabled = true;


    @Entry(category = "tabs") @Hidden
    public static String arsNouveauTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int arsNouveauTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int arsNouveauTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int arsNouveauTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int arsNouveauTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int arsNouveauTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int arsNouveauTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility arsNouveauTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer3b;

    @Comment(category = "tabs") public static CommentText backpacked;

    @Entry(category = "tabs") @Hidden
    public static boolean backpackedTabEnabled = true;


    @Entry(category = "tabs") @Hidden
    public static String backpackedTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int backpackedTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int backpackedTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int backpackedTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int backpackedTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int backpackedTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int backpackedTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility backpackedTabDisplayVisibility = TabDisplayVisibility.TUCK;


    @Comment(category = "tabs") public static CommentText spacer4;

    @Comment(category = "tabs") public static CommentText bodyDamage;

    @Entry(category = "tabs") @Hidden
    public static boolean bodyDamageTabEnabled = true;


    @Entry(category = "tabs") @Hidden
    public static String bodyDamageTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int bodyDamageTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int bodyDamageTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int bodyDamageTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int bodyDamageTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int bodyDamageTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int bodyDamageTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility bodyDamageTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer_biologydictionary;

    @Comment(category = "tabs") public static CommentText biologyDictionary;

    @Entry(category = "tabs") @Hidden
    public static boolean biologyDictionaryTabEnabled = true;

    @Entry(category = "tabs") @Hidden
    public static String biologyDictionaryTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int biologyDictionaryTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int biologyDictionaryTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int biologyDictionaryTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int biologyDictionaryTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int biologyDictionaryTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int biologyDictionaryTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility biologyDictionaryTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer_wildex;

    @Comment(category = "tabs") public static CommentText wildex;

    @Entry(category = "tabs") @Hidden
    public static boolean wildexTabEnabled = true;

    @Entry(category = "tabs") @Hidden
    public static String wildexTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int wildexTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int wildexTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int wildexTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int wildexTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int wildexTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int wildexTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility wildexTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer_apothic_attributes;

    @Comment(category = "tabs") public static CommentText apothicAttributes;

    @Entry(category = "tabs") @Hidden
    public static boolean apothicAttributesTabEnabled = true;

    @Entry(category = "tabs") @Hidden
    public static String apothicAttributesTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int apothicAttributesTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int apothicAttributesTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int apothicAttributesTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int apothicAttributesTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int apothicAttributesTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int apothicAttributesTabOrder = 0;

    @Comment(category = "tabs") public static CommentText spacer_aether;

    @Comment(category = "tabs") public static CommentText aether;

    @Entry(category = "tabs") @Hidden
    public static boolean aetherTabEnabled = true;

    @Entry(category = "tabs") @Hidden
    public static String aetherTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int aetherTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int aetherTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int aetherTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int aetherTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int aetherTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int aetherTabOrder = 0;

    @Comment(category = "tabs") public static CommentText spacer_curios;

    @Comment(category = "tabs") public static CommentText curios;

    @Entry(category = "tabs") @Hidden
    public static boolean curiosTabEnabled = true;

    @Entry(category = "tabs") @Hidden
    public static String curiosTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int curiosTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int curiosTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int curiosTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int curiosTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int curiosTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int curiosTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility curiosTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer_epic_fight;

    @Comment(category = "tabs") public static CommentText epicFight;

    @Entry(category = "tabs") @Hidden
    public static boolean epicFightTabEnabled = true;

    @Entry(category = "tabs") @Hidden
    public static String epicFightTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int epicFightTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int epicFightTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int epicFightTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int epicFightTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int epicFightTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int epicFightTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility epicFightTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer_epic_skills;

    @Comment(category = "tabs") public static CommentText epicSkills;

    @Entry(category = "tabs") @Hidden
    public static boolean epicSkillsTabEnabled = true;

    @Entry(category = "tabs") @Hidden
    public static String epicSkillsTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int epicSkillsTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int epicSkillsTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int epicSkillsTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int epicSkillsTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int epicSkillsTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int epicSkillsTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility epicSkillsTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer_completionists_index;

    @Comment(category = "tabs") public static CommentText completionistsIndex;

    @Entry(category = "tabs") @Hidden
    public static boolean completionistsIndexTabEnabled = true;

    @Entry(category = "tabs") @Hidden
    public static String completionistsIndexTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int completionistsIndexTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int completionistsIndexTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int completionistsIndexTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int completionistsIndexTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int completionistsIndexTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int completionistsIndexTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility completionistsIndexTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer_sdm_shop;

    @Comment(category = "tabs") public static CommentText sdmShop;

    @Entry(category = "tabs") @Hidden
    public static boolean sdmShopTabEnabled = true;

    @Entry(category = "tabs") @Hidden
    public static String sdmShopTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int sdmShopTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int sdmShopTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int sdmShopTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int sdmShopTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int sdmShopTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int sdmShopTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility sdmShopTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer_jobs_plus;

    @Comment(category = "tabs") public static CommentText jobsPlus;

    @Entry(category = "tabs") @Hidden
    public static boolean jobsPlusTabEnabled = true;

    @Entry(category = "tabs") @Hidden
    public static String jobsPlusTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int jobsPlusTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int jobsPlusTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int jobsPlusTabIconNudgeDown = 1;
    @Entry(category = "tabs") @Hidden
    public static int jobsPlusTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int jobsPlusTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int jobsPlusTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility jobsPlusTabDisplayVisibility = TabDisplayVisibility.TUCK;

    @Comment(category = "tabs") public static CommentText spacer_quest_log;

    @Comment(category = "tabs") public static CommentText questLog;

    @Entry(category = "tabs") @Hidden
    public static boolean questLogTabEnabled = true;

    @Entry(category = "tabs") @Hidden
    public static String questLogTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int questLogTabIconScale = 80;
    @Entry(category = "tabs") @Hidden
    public static int questLogTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int questLogTabIconNudgeDown = 1;
    @Entry(category = "tabs") @Hidden
    public static int questLogTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int questLogTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int questLogTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility questLogTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer_mapwright;

    @Comment(category = "tabs") public static CommentText mapwright;

    @Entry(category = "tabs") @Hidden
    public static boolean mapwrightTabEnabled = true;

    @Entry(category = "tabs") @Hidden
    public static String mapwrightTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int mapwrightTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int mapwrightTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int mapwrightTabIconNudgeDown = 1;
    @Entry(category = "tabs") @Hidden
    public static int mapwrightTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int mapwrightTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int mapwrightTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility mapwrightTabDisplayVisibility = TabDisplayVisibility.TUCK;

    @Comment(category = "tabs") public static CommentText spacer_field_guide;

    @Comment(category = "tabs") public static CommentText fieldGuide;

    @Entry(category = "tabs") @Hidden
    public static boolean fieldGuideTabEnabled = true;

    @Entry(category = "tabs") @Hidden
    public static String fieldGuideTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int fieldGuideTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int fieldGuideTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int fieldGuideTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int fieldGuideTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int fieldGuideTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int fieldGuideTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility fieldGuideTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer_modonomicon;

    @Comment(category = "tabs") public static CommentText modonomicon;

    @Entry(category = "tabs") @Hidden
    public static boolean modonomiconTabEnabled = true;

    @Entry(category = "tabs") @Hidden
    public static String modonomiconTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int modonomiconTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int modonomiconTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int modonomiconTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int modonomiconTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int modonomiconTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int modonomiconTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility modonomiconTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer_reliable_backpacks;

    @Comment(category = "tabs") public static CommentText reliableBackpacks;

    @Entry(category = "tabs") @Hidden
    public static boolean reliableBackpacksTabEnabled = true;

    @Entry(category = "tabs") @Hidden
    public static String reliableBackpacksTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int reliableBackpacksTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int reliableBackpacksTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int reliableBackpacksTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int reliableBackpacksTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int reliableBackpacksTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int reliableBackpacksTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility reliableBackpacksTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer_quark_backpack;

    @Comment(category = "tabs") public static CommentText quarkBackpack;

    @Entry(category = "tabs") @Hidden
    public static boolean quarkBackpackTabEnabled = true;

    @Entry(category = "tabs") @Hidden
    public static String quarkBackpackTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int quarkBackpackTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int quarkBackpackTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int quarkBackpackTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int quarkBackpackTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int quarkBackpackTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int quarkBackpackTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility quarkBackpackTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer4b;

    @Comment(category = "tabs") public static CommentText brassworksMissions;

    @Entry(category = "tabs") @Hidden
    public static boolean brassworksMissionsTabEnabled = true;


    @Entry(category = "tabs") @Hidden
    public static String brassworksMissionsTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int brassworksMissionsTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int brassworksMissionsTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int brassworksMissionsTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int brassworksMissionsTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int brassworksMissionsTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int brassworksMissionsTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility brassworksMissionsTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer5;

    @Comment(category = "tabs") public static CommentText cobblemon;

    @Entry(category = "tabs") @Hidden
    public static boolean cobblemonTabEnabled = true;


    @Entry(category = "tabs") @Hidden
    public static String cobblemonTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int cobblemonTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int cobblemonTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int cobblemonTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int cobblemonTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int cobblemonTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int cobblemonTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility cobblemonTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer6;

    @Comment(category = "tabs") public static CommentText cosmeticArmor;

    @Entry(category = "tabs") @Hidden
    public static boolean cosmeticArmorTabEnabled = true;


    @Entry(category = "tabs") @Hidden
    public static String cosmeticArmorTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int cosmeticArmorTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int cosmeticArmorTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int cosmeticArmorTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int cosmeticArmorTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int cosmeticArmorTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int cosmeticArmorTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility cosmeticArmorTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer7;

    @Comment(category = "tabs") public static CommentText draconicEvolution;

    @Entry(category = "tabs") @Hidden
    public static boolean draconicEvolutionTabEnabled = true;


    @Entry(category = "tabs") @Hidden
    public static String draconicEvolutionTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int draconicEvolutionTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int draconicEvolutionTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int draconicEvolutionTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int draconicEvolutionTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int draconicEvolutionTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int draconicEvolutionTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility draconicEvolutionTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer7a;

    @Comment(category = "tabs") public static CommentText eccentricTome;

    @Entry(category = "tabs") @Hidden
    public static boolean eccentricTomeTabEnabled = true;


    @Entry(category = "tabs") @Hidden
    public static String eccentricTomeTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int eccentricTomeTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int eccentricTomeTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int eccentricTomeTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int eccentricTomeTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int eccentricTomeTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int eccentricTomeTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility eccentricTomeTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer7b;

    @Comment(category = "tabs") public static CommentText diet;

    @Entry(category = "tabs") @Hidden
    public static boolean dietTabEnabled = true;


    @Entry(category = "tabs") @Hidden
    public static String dietTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int dietTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int dietTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int dietTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int dietTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int dietTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int dietTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility dietTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer8;

    @Comment(category = "tabs") public static CommentText ftbQuests;

    @Entry(category = "tabs") @Hidden
    public static boolean ftbQuestsTabEnabled = true;


    @Entry(category = "tabs") @Hidden
    public static String ftbQuestsTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int ftbQuestsTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int ftbQuestsTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int ftbQuestsTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int ftbQuestsTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int ftbQuestsTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int ftbQuestsTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility ftbQuestsTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer9;

    @Comment(category = "tabs") public static CommentText ftbChunks;

    @Entry(category = "tabs") @Hidden
    public static boolean ftbChunksTabEnabled = true;


    @Entry(category = "tabs") @Hidden
    public static String ftbChunksTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int ftbChunksTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int ftbChunksTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int ftbChunksTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int ftbChunksTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int ftbChunksTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int ftbChunksTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility ftbChunksTabDisplayVisibility = TabDisplayVisibility.TUCK;

    @Comment(category = "tabs") public static CommentText spacer9b;

    @Comment(category = "tabs") public static CommentText ftbTeams;

    @Entry(category = "tabs") @Hidden
    public static boolean ftbTeamsTabEnabled = true;


    @Entry(category = "tabs") @Hidden
    public static String ftbTeamsTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int ftbTeamsTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int ftbTeamsTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int ftbTeamsTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int ftbTeamsTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int ftbTeamsTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int ftbTeamsTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility ftbTeamsTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer10;

    @Comment(category = "tabs") public static CommentText journeyMap;

    @Entry(category = "tabs") @Hidden
    public static boolean journeyMapTabEnabled = true;


    @Entry(category = "tabs") @Hidden
    public static String journeyMapTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int journeyMapTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int journeyMapTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int journeyMapTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int journeyMapTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int journeyMapTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int journeyMapTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility journeyMapTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer11;

    @Comment(category = "tabs") public static CommentText l2Artifacts;

    @Entry(category = "tabs") @Hidden
    public static boolean l2ArtifactsTabEnabled = true;


    @Entry(category = "tabs") @Hidden
    public static String l2ArtifactsTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int l2ArtifactsTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int l2ArtifactsTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int l2ArtifactsTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int l2ArtifactsTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int l2ArtifactsTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int l2ArtifactsTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility l2ArtifactsTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer12;

    @Comment(category = "tabs") public static CommentText l2Attributes;

    @Entry(category = "tabs") @Hidden
    public static boolean l2AttributesTabEnabled = true;


    @Entry(category = "tabs") @Hidden
    public static String l2AttributesTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int l2AttributesTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int l2AttributesTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int l2AttributesTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int l2AttributesTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int l2AttributesTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int l2AttributesTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility l2AttributesTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer13;

    @Comment(category = "tabs") public static CommentText l2Hostility;

    @Entry(category = "tabs") @Hidden
    public static boolean l2HostilityTabEnabled = true;


    @Entry(category = "tabs") @Hidden
    public static String l2HostilityTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int l2HostilityTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int l2HostilityTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int l2HostilityTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int l2HostilityTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int l2HostilityTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int l2HostilityTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility l2HostilityTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer14;

    @Comment(category = "tabs") public static CommentText mapAtlases;

    @Entry(category = "tabs") @Hidden
    public static boolean mapAtlasesTabEnabled = true;


    @Entry(category = "tabs") @Hidden
    public static String mapAtlasesTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int mapAtlasesTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int mapAtlasesTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int mapAtlasesTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int mapAtlasesTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int mapAtlasesTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int mapAtlasesTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility mapAtlasesTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer15;

    @Comment(category = "tabs") public static CommentText modularGolems;

    @Entry(category = "tabs") @Hidden
    public static boolean modularGolemsTabEnabled = true;


    @Entry(category = "tabs") @Hidden
    public static String modularGolemsTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int modularGolemsTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int modularGolemsTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int modularGolemsTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int modularGolemsTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int modularGolemsTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int modularGolemsTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility modularGolemsTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer16;

    @Comment(category = "tabs") public static CommentText motp;

    @Entry(category = "tabs") @Hidden
    public static boolean motpTabEnabled = true;


    @Entry(category = "tabs") @Hidden
    public static String motpTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int motpTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int motpTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int motpTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int motpTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int motpTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int motpTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility motpTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer16a;

    @Comment(category = "tabs") public static CommentText passiveSkillTree;

    @Entry(category = "tabs") @Hidden
    public static boolean passiveSkillTreeTabEnabled = true;


    @Entry(category = "tabs") @Hidden
    public static String passiveSkillTreeTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int passiveSkillTreeTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int passiveSkillTreeTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int passiveSkillTreeTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int passiveSkillTreeTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int passiveSkillTreeTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int passiveSkillTreeTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility passiveSkillTreeTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer17;

    @Comment(category = "tabs") public static CommentText pufferfishSkills;

    @Entry(category = "tabs") @Hidden
    public static boolean pufferfishSkillsTabEnabled = true;


    @Entry(category = "tabs") @Hidden
    public static String pufferfishSkillsTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int pufferfishSkillsTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int pufferfishSkillsTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int pufferfishSkillsTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int pufferfishSkillsTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int pufferfishSkillsTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int pufferfishSkillsTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility pufferfishSkillsTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer18;

    @Comment(category = "tabs") public static CommentText reskillableReimagined;

    @Entry(category = "tabs") @Hidden
    public static boolean reskillableReimaginedTabEnabled = true;


    @Entry(category = "tabs") @Hidden
    public static String reskillableReimaginedTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int reskillableReimaginedTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int reskillableReimaginedTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int reskillableReimaginedTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int reskillableReimaginedTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int reskillableReimaginedTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int reskillableReimaginedTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility reskillableReimaginedTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer19;

    @Comment(category = "tabs") public static CommentText sophisticatedBackpacks;

    @Entry(category = "tabs") @Hidden
    public static boolean sophisticatedBackpacksTabEnabled = true;


    @Entry(category = "tabs") @Hidden
    public static String sophisticatedBackpacksTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int sophisticatedBackpacksTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int sophisticatedBackpacksTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int sophisticatedBackpacksTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int sophisticatedBackpacksTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int sophisticatedBackpacksTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int sophisticatedBackpacksTabOrder = 0;

    @Entry(category = "tabs")
    public static BackpackSlot sophisticatedBackpacksPreferredSlot = BackpackSlot.DEFAULT;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility sophisticatedBackpacksTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer20;

    @Comment(category = "tabs") public static CommentText travelersBackpack;

    @Entry(category = "tabs") @Hidden
    public static boolean travelersBackpackTabEnabled = true;


    @Entry(category = "tabs") @Hidden
    public static String travelersBackpackTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int travelersBackpackTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int travelersBackpackTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int travelersBackpackTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int travelersBackpackTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int travelersBackpackTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int travelersBackpackTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility travelersBackpackTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacer21;

    @Comment(category = "tabs") public static CommentText xaerosMap;

    @Entry(category = "tabs") @Hidden
    public static boolean xaerosMapTabEnabled = true;


    @Entry(category = "tabs") @Hidden
    public static String xaerosMapTabCustomIcon = "";
    @Entry(category = "tabs") @Hidden
    public static int xaerosMapTabIconScale = 100;
    @Entry(category = "tabs") @Hidden
    public static int xaerosMapTabIconNudgeUp = 0;
    @Entry(category = "tabs") @Hidden
    public static int xaerosMapTabIconNudgeDown = 0;
    @Entry(category = "tabs") @Hidden
    public static int xaerosMapTabIconNudgeLeft = 0;
    @Entry(category = "tabs") @Hidden
    public static int xaerosMapTabIconNudgeRight = 0;
    @Entry(category = "tabs") @Hidden
    public static int xaerosMapTabOrder = 0;

    @Entry(category = "tabs") @Hidden
    public static TabDisplayVisibility xaerosMapTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "tabs") public static CommentText spacerNearbyContainers;

    @Comment(category = "tabs") public static CommentText nearbyContainersHeader;

    @Entry(category = "tabs")
    public static boolean nearbyContainersTabEnabled = false;

    @Entry(category = "tabs", min = 1, max = 16)
    public static int nearbyContainersTabRange = 5;

    @Entry(category = "tabs")
    public static boolean nearbyContainersTabRequireLineOfSight = false;

    @Comment(category = "customTabs") public static CommentText spacer22;

    @Comment(category = "customTabs") public static CommentText customTabs;

    @Entry(category = "customTabs")
    public static boolean customTabsEnabled = true;

    @Entry(category = "customTabs")
    public static boolean customTabsDebugLogging = false;
}