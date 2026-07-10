package vodmordia.modtabs.integration;

/**
 * Enum defining all supported mod integrations
 */
public enum ModIntegration {
    DIET("diet"),
    TRAVELERS_BACKPACK("travelersbackpack"),
    BACKPACKED("backpacked"),
    PATCHOULI("patchouli"),
    FTB_QUESTS("ftbquests"),
    FTB_TEAMS("ftbteams"),
    ARS_NOUVEAU("ars_nouveau"),
    ARS_ELIXIRUM("ars_elixirum"),
    L2_LIBRARY("l2library"),
    L2_HOSTILITY("l2hostility"),
    L2_ARTIFACTS("l2artifacts"),
    MODULAR_GOLEMS("modulargolems"),

    // Fabric-specific mods
    TRINKETS("trinkets"),
    XAEROS_MINIMAP("xaerominimap"),
    XAEROS_WORLDMAP("xaeroworldmap"),
    JOURNEYMAP("journeymap"),
    MAP_ATLASES("map_atlases"),

    // Skills and progression mods
    LEVELZ("levelz"),
    ORIGINS("origins"),
    PUFFERFISH_SKILLS("puffish_skills"),

    // Equipment and inventory mods
    SOPHISTICATED_BACKPACKS("sophisticatedbackpacks"),
    COSMETIC_ARMOR("cosmeticarmor"),

    // Utility mods
    REI("roughlyenoughitems"),
    EMI("emi"),
    JEI("jei");

    private final String modId;

    ModIntegration(String modId) {
        this.modId = modId;
    }

    public String getModId() {
        return modId;
    }
}