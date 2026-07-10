package vodmordia.modtabs.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class ModTabsConfig extends MidnightConfig {

    public static class CommentText {}

    // ========== MOD TABS SETTINGS ==========

    @Comment(category = "1_modTabsSettings") public static CommentText inventory;

    @Entry(category = "1_modTabsSettings")
    public static boolean stickyInventoryTab = true;

    @Entry(category = "1_modTabsSettings")
    public static boolean inventoryTabEnabled = true;

    @Entry(category = "1_modTabsSettings")
    public static int inventoryTabOrder = 0;

    @Entry(category = "1_modTabsSettings")
    public static TabDisplayVisibility inventoryTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "1_modTabsSettings") public static CommentText spacer1;

    @Comment(category = "1_modTabsSettings") public static CommentText advancements;

    @Entry(category = "1_modTabsSettings")
    public static boolean advancementsTabEnabled = true;

    @Entry(category = "1_modTabsSettings")
    public static int advancementsTabOrder = 0;

    @Entry(category = "1_modTabsSettings")
    public static TabDisplayVisibility advancementsTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "1_modTabsSettings") public static CommentText spacer2;

    @Comment(category = "1_modTabsSettings") public static CommentText ftbQuests;

    @Entry(category = "1_modTabsSettings")
    public static boolean ftbQuestsTabEnabled = true;

    @Entry(category = "1_modTabsSettings")
    public static int ftbQuestsTabOrder = 0;

    @Entry(category = "1_modTabsSettings")
    public static TabDisplayVisibility ftbQuestsTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "1_modTabsSettings") public static CommentText spacer3;

    @Comment(category = "1_modTabsSettings") public static CommentText ftbTeams;

    @Entry(category = "1_modTabsSettings")
    public static boolean ftbTeamsTabEnabled = true;

    @Entry(category = "1_modTabsSettings")
    public static int ftbTeamsTabOrder = 0;

    @Entry(category = "1_modTabsSettings")
    public static TabDisplayVisibility ftbTeamsTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "1_modTabsSettings") public static CommentText spacer4;

    @Comment(category = "1_modTabsSettings") public static CommentText mapAtlases;

    @Entry(category = "1_modTabsSettings")
    public static boolean mapAtlasesTabEnabled = true;

    @Entry(category = "1_modTabsSettings")
    public static int mapAtlasesTabOrder = 0;

    @Entry(category = "1_modTabsSettings")
    public static TabDisplayVisibility mapAtlasesTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "1_modTabsSettings") public static CommentText spacer5;

    @Comment(category = "1_modTabsSettings") public static CommentText xaerosMap;

    @Entry(category = "1_modTabsSettings")
    public static boolean xaerosMapTabEnabled = true;

    @Entry(category = "1_modTabsSettings")
    public static int xaerosMapTabOrder = 0;

    @Entry(category = "1_modTabsSettings")
    public static TabDisplayVisibility xaerosMapTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "1_modTabsSettings") public static CommentText spacer6;

    @Comment(category = "1_modTabsSettings") public static CommentText pufferfishSkills;

    @Entry(category = "1_modTabsSettings")
    public static boolean pufferfishSkillsTabEnabled = true;

    @Entry(category = "1_modTabsSettings")
    public static int pufferfishSkillsTabOrder = 0;

    @Entry(category = "1_modTabsSettings")
    public static TabDisplayVisibility pufferfishSkillsTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "1_modTabsSettings") public static CommentText spacer7;

    @Comment(category = "1_modTabsSettings") public static CommentText sophisticatedBackpacks;

    @Entry(category = "1_modTabsSettings")
    public static boolean sophisticatedBackpacksTabEnabled = true;

    @Entry(category = "1_modTabsSettings")
    public static int sophisticatedBackpacksTabOrder = 0;

    @Entry(category = "1_modTabsSettings")
    public static BackpackSlot sophisticatedBackpacksPreferredSlot = BackpackSlot.DEFAULT;

    @Entry(category = "1_modTabsSettings")
    public static TabDisplayVisibility sophisticatedBackpacksTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "1_modTabsSettings") public static CommentText spacer8;

    @Comment(category = "1_modTabsSettings") public static CommentText travelersBackpack;

    @Entry(category = "1_modTabsSettings")
    public static boolean travelersBackpackTabEnabled = true;

    @Entry(category = "1_modTabsSettings")
    public static int travelersBackpackTabOrder = 0;

    @Entry(category = "1_modTabsSettings")
    public static TabDisplayVisibility travelersBackpackTabDisplayVisibility = TabDisplayVisibility.YES;

    @Comment(category = "1_modTabsSettings") public static CommentText spacerOrigins;
    @Comment(category = "1_modTabsSettings") public static CommentText origins;
    @Entry(category = "1_modTabsSettings") public static boolean originsTabEnabled = true;
    @Entry(category = "1_modTabsSettings") public static int originsTabOrder = 0;
    @Entry(category = "1_modTabsSettings")
    public static TabDisplayVisibility originsTabDisplayVisibility = TabDisplayVisibility.YES;

    // ========== CUSTOM TAB ==========

    @Comment(category = "2_customTab") public static CommentText spacer9;

    @Comment(category = "2_customTab") public static CommentText customTabs;

    @Entry(category = "2_customTab")
    public static boolean customTabsEnabled = true;

    @Entry(category = "2_customTab")
    public static boolean customTabsDebugLogging = false;
}
