package vodmordia.modtabs.integration;

/**
 * Enum for all mod integrations with their mod IDs and human-readable names
 */
public enum ModIntegration {
    BACKPACKED("backpacked", "Backpacked"),
    TRAVELERS_BACKPACK("travelersbackpack", "Traveler's Backpack"),
    LEGENDARY_SURVIVAL_OVERHAUL("legendarysurvivaloverhaul", "Legendary Survival Overhaul"),
    CURIOS("curios", "Curios"),
    RESKILLABLE("rereskillable", "Rereskillable"),
    RESKILLABLE_REIMAGINED("reskillable", "Reskillable Reimagined"),
    FTB_QUESTS("ftbquests", "FTB Quests"),
    FTB_TEAMS("ftbteams", "FTB Teams"),
    FTB_CHUNKS("ftbchunks", "FTB Chunks"),
    QUARK_ODDITIES("quarkoddities", "Quark Oddities"),
    COSMETIC_ARMOR("cosmeticarmorreworked", "Cosmetic Armor"),
    MAP_ATLASES("map_atlases", "Map Atlases"),
    XAEROS_MAP("xaeroworldmap", "Xaero's World Map"),
    JOURNEY_MAP("journeymap", "JourneyMap"),
    DIET("diet", "Diet"),
    PASSIVE_SKILL_TREE("skilltree", "Passive Skill Tree"),
    PUFFERFISHS_SKILLS("puffish_skills", "Pufferfish's Skills"),
    L2_HOSTILITY("l2hostility", "L2 Hostility"),
    L2_LIBRARY("l2library", "L2 Library"),
    L2_COMPLEMENTS("l2complements", "L2 Complements"),
    L2_ARTIFACTS("l2artifacts", "L2 Artifacts"),
    L2_ATTRIBUTES("l2tabs", "L2 Attributes"),
    SOPHISTICATED_BACKPACKS("sophisticatedbackpacks", "Sophisticated Backpacks"),
    COBBLEMON("cobblemon", "Cobblemon"),
    MODULAR_GOLEMS("modulargolems", "Modular Golems"),
    ARS_ELIXIRUM("elixirum", "Ars Elixirum"),
    ARS_NOUVEAU("ars_nouveau", "Ars Nouveau"),
    DRACONIC_EVOLUTION("draconicevolution", "Draconic Evolution"),
    BRASSWORKS_MISSIONS("brassworksmissions", "Create: Brassworks Missions"),
    ECCENTRIC_TOME("eccentrictome", "Eccentric Tome"),
    BETTER_ADVANCEMENTS("betteradvancements", "Better Advancements"),
    RPG_CRAFTING("rpgcrafting", "RPG Crafting"),
    MOTP("memory_of_the_past", "MOTP - RPG Player Leveling"),
    BIOLOGY_DICTIONARY("biologydictionary", "Biology Dictionary"),
    RELIABLE_BACKPACKS("reliable_backpacks", "Reliable Backpacks"),
    WILDEX("wildex", "Wildex Bestiary"),
    APOTHIC_ATTRIBUTES("apothic_attributes", "Apothic Attributes"),
    AETHER("aether", "The Aether"),
    EPIC_FIGHT("epicfight", "Epic Fight"),
    EPIC_SKILLS("epicskills", "Epic Fight Skill Tree"),
    COMPLETIONISTS_INDEX("completionistsindex", "Completionist's Index"),
    SDM_SHOP("sdmshop", "SDM Shop"),
    JOBS_PLUS("jobsplus", "Jobs+"),
    QUEST_LOG("questlog", "Quest Log"),
    MAPWRIGHT("mapwright", "Mapwright"),
    FIELD_GUIDE("fieldguide", "Field Guide"),
    MODONOMICON("modonomicon", "Modonomicon"),
    QUARK("quark", "Quark");

    private final String modId;
    private final String displayName;

    ModIntegration(String modId, String displayName) {
        this.modId = modId;
        this.displayName = displayName;
    }

    public String getModId() {
        return modId;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Find ModIntegration by mod ID
     */
    public static ModIntegration byModId(String modId) {
        for (ModIntegration mod : values()) {
            if (mod.getModId().equals(modId)) {
                return mod;
            }
        }
        return null;
    }
}