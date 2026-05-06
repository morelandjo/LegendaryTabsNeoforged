package vodmordia.modtabs.config;

import vodmordia.modtabs.ModTabs;

public class Config
{
	// Old NeoForge config system completely removed - now using MidnightConfig only

	public static class Baked
	{
		// Tab enable/disable settings
		public static boolean inventoryTabEnabled;
		public static boolean backpackedTabEnabled;
		public static boolean travelersBackpackTabEnabled;
		public static boolean mapAtlasesTabEnabled;
		public static boolean xaerosMapTabEnabled;
		public static boolean journeyMapTabEnabled;
		public static boolean bodyDamageTabEnabled;
		public static boolean reskillableReimaginedTabEnabled;
		public static boolean ftbQuestsTabEnabled;
		public static boolean ftbTeamsTabEnabled;
		public static boolean ftbChunksTabEnabled;
		public static boolean dietTabEnabled;
		public static boolean pufferfishSkillsTabEnabled;
		public static boolean passiveSkillTreeTabEnabled;
		public static boolean sophisticatedBackpacksTabEnabled;
		public static boolean cobblemonTabEnabled;
		public static boolean arsElixirumTabEnabled;
		public static boolean arsNouveauTabEnabled;
		public static boolean l2HostilityTabEnabled;
		public static boolean l2AttributesTabEnabled;
		public static boolean l2ArtifactsTabEnabled;
		public static boolean cosmeticArmorTabEnabled;
		public static boolean modularGolemsTabEnabled;
		public static boolean motpTabEnabled;
		public static boolean advancementsTabEnabled;
		public static boolean draconicEvolutionTabEnabled;
		public static boolean brassworksMissionsTabEnabled;
		public static boolean eccentricTomeTabEnabled;
		public static boolean rpgCraftingTabEnabled;
		public static boolean biologyDictionaryTabEnabled;
		public static boolean reliableBackpacksTabEnabled;
		public static boolean runicSkillsTabEnabled;
		public static boolean advancedTeamTabEnabled;
		public static boolean villagerRecruitsTabEnabled;
		public static boolean apothicAttributesTabEnabled;
		public static boolean stickyInventoryTab;
		public static StandardTabPlacement standardTabPlacement;

		// Custom icon settings
		public static String inventoryTabCustomIcon;
		public static String advancementsTabCustomIcon;
		public static String rpgCraftingTabCustomIcon;
		public static String arsElixirumTabCustomIcon;
		public static String arsNouveauTabCustomIcon;
		public static String backpackedTabCustomIcon;
		public static String bodyDamageTabCustomIcon;
		public static String brassworksMissionsTabCustomIcon;
		public static String cobblemonTabCustomIcon;
		public static String cosmeticArmorTabCustomIcon;
		public static String draconicEvolutionTabCustomIcon;
		public static String eccentricTomeTabCustomIcon;
		public static String dietTabCustomIcon;
		public static String ftbQuestsTabCustomIcon;
		public static String ftbTeamsTabCustomIcon;
		public static String ftbChunksTabCustomIcon;
		public static String journeyMapTabCustomIcon;
		public static String l2ArtifactsTabCustomIcon;
		public static String l2AttributesTabCustomIcon;
		public static String l2HostilityTabCustomIcon;
		public static String mapAtlasesTabCustomIcon;
		public static String modularGolemsTabCustomIcon;
		public static String motpTabCustomIcon;
		public static String passiveSkillTreeTabCustomIcon;
		public static String pufferfishSkillsTabCustomIcon;
		public static String reskillableReimaginedTabCustomIcon;
		public static String sophisticatedBackpacksTabCustomIcon;
		public static String travelersBackpackTabCustomIcon;
		public static String xaerosMapTabCustomIcon;
		public static String biologyDictionaryTabCustomIcon;
		public static String reliableBackpacksTabCustomIcon;
		public static String runicSkillsTabCustomIcon;
		public static String advancedTeamTabCustomIcon;
		public static String villagerRecruitsTabCustomIcon;
		public static String apothicAttributesTabCustomIcon;

		// Custom tabs settings
		public static boolean customTabsEnabled;
		public static boolean customTabsDebugLogging;

		// Tab display visibility settings
		public static TabDisplayVisibility inventoryTabDisplayVisibility;
		public static TabDisplayVisibility advancementsTabDisplayVisibility;
		public static TabDisplayVisibility arsElixirumTabDisplayVisibility;
		public static TabDisplayVisibility arsNouveauTabDisplayVisibility;
		public static TabDisplayVisibility backpackedTabDisplayVisibility;
		public static TabDisplayVisibility bodyDamageTabDisplayVisibility;
		public static TabDisplayVisibility cobblemonTabDisplayVisibility;
		public static TabDisplayVisibility cosmeticArmorTabDisplayVisibility;
		public static TabDisplayVisibility draconicEvolutionTabDisplayVisibility;
		public static TabDisplayVisibility dietTabDisplayVisibility;
		public static TabDisplayVisibility ftbQuestsTabDisplayVisibility;
		public static TabDisplayVisibility ftbTeamsTabDisplayVisibility;
		public static TabDisplayVisibility ftbChunksTabDisplayVisibility;
		public static TabDisplayVisibility journeyMapTabDisplayVisibility;
		public static TabDisplayVisibility l2ArtifactsTabDisplayVisibility;
		public static TabDisplayVisibility l2AttributesTabDisplayVisibility;
		public static TabDisplayVisibility l2HostilityTabDisplayVisibility;
		public static TabDisplayVisibility mapAtlasesTabDisplayVisibility;
		public static TabDisplayVisibility modularGolemsTabDisplayVisibility;
		public static TabDisplayVisibility motpTabDisplayVisibility;
		public static TabDisplayVisibility passiveSkillTreeTabDisplayVisibility;
		public static TabDisplayVisibility pufferfishSkillsTabDisplayVisibility;
		public static TabDisplayVisibility reskillableReimaginedTabDisplayVisibility;
		public static TabDisplayVisibility sophisticatedBackpacksTabDisplayVisibility;
		public static TabDisplayVisibility travelersBackpackTabDisplayVisibility;
		public static TabDisplayVisibility xaerosMapTabDisplayVisibility;
		public static TabDisplayVisibility brassworksMissionsTabDisplayVisibility;
		public static TabDisplayVisibility eccentricTomeTabDisplayVisibility;
		public static TabDisplayVisibility rpgCraftingTabDisplayVisibility;
		public static TabDisplayVisibility biologyDictionaryTabDisplayVisibility;
		public static TabDisplayVisibility reliableBackpacksTabDisplayVisibility;
		public static TabDisplayVisibility runicSkillsTabDisplayVisibility;
		public static TabDisplayVisibility advancedTeamTabDisplayVisibility;
		public static TabDisplayVisibility villagerRecruitsTabDisplayVisibility;

		// Tab order overrides
		public static int inventoryTabOrder;
		public static int backpackedTabOrder;
		public static int travelersBackpackTabOrder;
		public static int bodyDamageTabOrder;
		public static int reskillableReimaginedTabOrder;
		public static int ftbQuestsTabOrder;
		public static int ftbTeamsTabOrder;
		public static int ftbChunksTabOrder;
		public static int mapAtlasesTabOrder;
		public static int xaerosMapTabOrder;
		public static int journeyMapTabOrder;
		public static int dietTabOrder;
		public static int pufferfishSkillsTabOrder;
		public static int passiveSkillTreeTabOrder;
		public static int sophisticatedBackpacksTabOrder;
		public static BackpackSlot sophisticatedBackpacksPreferredSlot;
		public static int cobblemonTabOrder;
		public static int modularGolemsTabOrder;
		public static int motpTabOrder;
		public static int arsElixirumTabOrder;
		public static int arsNouveauTabOrder;
		public static int l2HostilityTabOrder;
		public static int l2AttributesTabOrder;
		public static int l2ArtifactsTabOrder;
		public static int cosmeticArmorTabOrder;
		public static int advancementsTabOrder;
		public static int draconicEvolutionTabOrder;
		public static int brassworksMissionsTabOrder;
		public static int eccentricTomeTabOrder;
		public static int rpgCraftingTabOrder;
		public static int biologyDictionaryTabOrder;
		public static int reliableBackpacksTabOrder;
		public static int runicSkillsTabOrder;
		public static int advancedTeamTabOrder;
		public static int villagerRecruitsTabOrder;
		public static int apothicAttributesTabOrder;

		public static void bakeClient()
		{
			try
			{
				// Read from MidnightConfig fields
				inventoryTabEnabled = ModTabsConfig.inventoryTabEnabled;
				backpackedTabEnabled = ModTabsConfig.backpackedTabEnabled;
				travelersBackpackTabEnabled = ModTabsConfig.travelersBackpackTabEnabled;
				bodyDamageTabEnabled = ModTabsConfig.bodyDamageTabEnabled;
				reskillableReimaginedTabEnabled = ModTabsConfig.reskillableReimaginedTabEnabled;
				ftbQuestsTabEnabled = ModTabsConfig.ftbQuestsTabEnabled;
				ftbChunksTabEnabled = ModTabsConfig.ftbChunksTabEnabled;
				mapAtlasesTabEnabled = ModTabsConfig.mapAtlasesTabEnabled;
				xaerosMapTabEnabled = ModTabsConfig.xaerosMapTabEnabled;
				journeyMapTabEnabled = ModTabsConfig.journeyMapTabEnabled;
				ftbTeamsTabEnabled = ModTabsConfig.ftbTeamsTabEnabled;
				dietTabEnabled = ModTabsConfig.dietTabEnabled;
				pufferfishSkillsTabEnabled = ModTabsConfig.pufferfishSkillsTabEnabled;
				passiveSkillTreeTabEnabled = ModTabsConfig.passiveSkillTreeTabEnabled;
				sophisticatedBackpacksTabEnabled = ModTabsConfig.sophisticatedBackpacksTabEnabled;
				cobblemonTabEnabled = ModTabsConfig.cobblemonTabEnabled;
				arsElixirumTabEnabled = ModTabsConfig.arsElixirumTabEnabled;
				arsNouveauTabEnabled = ModTabsConfig.arsNouveauTabEnabled;

				// Additional tabs not in original config
				l2HostilityTabEnabled = ModTabsConfig.l2HostilityTabEnabled;
				l2AttributesTabEnabled = ModTabsConfig.l2AttributesTabEnabled;
				l2ArtifactsTabEnabled = ModTabsConfig.l2ArtifactsTabEnabled;
				cosmeticArmorTabEnabled = ModTabsConfig.cosmeticArmorTabEnabled;
				modularGolemsTabEnabled = ModTabsConfig.modularGolemsTabEnabled;
				motpTabEnabled = ModTabsConfig.motpTabEnabled;
				advancementsTabEnabled = ModTabsConfig.advancementsTabEnabled;
				draconicEvolutionTabEnabled = ModTabsConfig.draconicEvolutionTabEnabled;
				brassworksMissionsTabEnabled = ModTabsConfig.brassworksMissionsTabEnabled;
				eccentricTomeTabEnabled = ModTabsConfig.eccentricTomeTabEnabled;
				rpgCraftingTabEnabled = ModTabsConfig.rpgCraftingTabEnabled;
				biologyDictionaryTabEnabled = ModTabsConfig.biologyDictionaryTabEnabled;
				reliableBackpacksTabEnabled = ModTabsConfig.reliableBackpacksTabEnabled;
				runicSkillsTabEnabled = ModTabsConfig.runicSkillsTabEnabled;
				advancedTeamTabEnabled = ModTabsConfig.advancedTeamTabEnabled;
				villagerRecruitsTabEnabled = ModTabsConfig.villagerRecruitsTabEnabled;
				apothicAttributesTabEnabled = ModTabsConfig.apothicAttributesTabEnabled;
				stickyInventoryTab = ModTabsConfig.stickyInventoryTab;
				standardTabPlacement = ModTabsConfig.standardTabPlacement;

				// Load custom tabs settings
				customTabsEnabled = ModTabsConfig.customTabsEnabled;
				customTabsDebugLogging = ModTabsConfig.customTabsDebugLogging;

				// Load tab order overrides
				inventoryTabOrder = ModTabsConfig.inventoryTabOrder;
				backpackedTabOrder = ModTabsConfig.backpackedTabOrder;
				travelersBackpackTabOrder = ModTabsConfig.travelersBackpackTabOrder;
				bodyDamageTabOrder = ModTabsConfig.bodyDamageTabOrder;
				reskillableReimaginedTabOrder = ModTabsConfig.reskillableReimaginedTabOrder;
				ftbQuestsTabOrder = ModTabsConfig.ftbQuestsTabOrder;
				ftbTeamsTabOrder = ModTabsConfig.ftbTeamsTabOrder;
				ftbChunksTabOrder = ModTabsConfig.ftbChunksTabOrder;
				mapAtlasesTabOrder = ModTabsConfig.mapAtlasesTabOrder;
				xaerosMapTabOrder = ModTabsConfig.xaerosMapTabOrder;
				journeyMapTabOrder = ModTabsConfig.journeyMapTabOrder;
				dietTabOrder = ModTabsConfig.dietTabOrder;
				pufferfishSkillsTabOrder = ModTabsConfig.pufferfishSkillsTabOrder;
				passiveSkillTreeTabOrder = ModTabsConfig.passiveSkillTreeTabOrder;
				sophisticatedBackpacksTabOrder = ModTabsConfig.sophisticatedBackpacksTabOrder;
				sophisticatedBackpacksPreferredSlot = ModTabsConfig.sophisticatedBackpacksPreferredSlot;
				cobblemonTabOrder = ModTabsConfig.cobblemonTabOrder;
				modularGolemsTabOrder = ModTabsConfig.modularGolemsTabOrder;
				motpTabOrder = ModTabsConfig.motpTabOrder;
				arsElixirumTabOrder = ModTabsConfig.arsElixirumTabOrder;
				arsNouveauTabOrder = ModTabsConfig.arsNouveauTabOrder;
				l2HostilityTabOrder = ModTabsConfig.l2HostilityTabOrder;
				l2AttributesTabOrder = ModTabsConfig.l2AttributesTabOrder;
				l2ArtifactsTabOrder = ModTabsConfig.l2ArtifactsTabOrder;
				cosmeticArmorTabOrder = ModTabsConfig.cosmeticArmorTabOrder;
				advancementsTabOrder = ModTabsConfig.advancementsTabOrder;
				draconicEvolutionTabOrder = ModTabsConfig.draconicEvolutionTabOrder;
				brassworksMissionsTabOrder = ModTabsConfig.brassworksMissionsTabOrder;
				eccentricTomeTabOrder = ModTabsConfig.eccentricTomeTabOrder;
				rpgCraftingTabOrder = ModTabsConfig.rpgCraftingTabOrder;
				biologyDictionaryTabOrder = ModTabsConfig.biologyDictionaryTabOrder;
				reliableBackpacksTabOrder = ModTabsConfig.reliableBackpacksTabOrder;
				runicSkillsTabOrder = ModTabsConfig.runicSkillsTabOrder;
				advancedTeamTabOrder = ModTabsConfig.advancedTeamTabOrder;
				villagerRecruitsTabOrder = ModTabsConfig.villagerRecruitsTabOrder;
				apothicAttributesTabOrder = ModTabsConfig.apothicAttributesTabOrder;

				// Load tab display visibility settings
				inventoryTabDisplayVisibility = ModTabsConfig.inventoryTabDisplayVisibility;
				advancementsTabDisplayVisibility = ModTabsConfig.advancementsTabDisplayVisibility;
				arsElixirumTabDisplayVisibility = ModTabsConfig.arsElixirumTabDisplayVisibility;
				arsNouveauTabDisplayVisibility = ModTabsConfig.arsNouveauTabDisplayVisibility;
				backpackedTabDisplayVisibility = ModTabsConfig.backpackedTabDisplayVisibility;
				bodyDamageTabDisplayVisibility = ModTabsConfig.bodyDamageTabDisplayVisibility;
				brassworksMissionsTabDisplayVisibility = ModTabsConfig.brassworksMissionsTabDisplayVisibility;
				cobblemonTabDisplayVisibility = ModTabsConfig.cobblemonTabDisplayVisibility;
				cosmeticArmorTabDisplayVisibility = ModTabsConfig.cosmeticArmorTabDisplayVisibility;
				draconicEvolutionTabDisplayVisibility = ModTabsConfig.draconicEvolutionTabDisplayVisibility;
				eccentricTomeTabDisplayVisibility = ModTabsConfig.eccentricTomeTabDisplayVisibility;
				dietTabDisplayVisibility = ModTabsConfig.dietTabDisplayVisibility;
				ftbQuestsTabDisplayVisibility = ModTabsConfig.ftbQuestsTabDisplayVisibility;
				ftbTeamsTabDisplayVisibility = ModTabsConfig.ftbTeamsTabDisplayVisibility;
				ftbChunksTabDisplayVisibility = ModTabsConfig.ftbChunksTabDisplayVisibility;
				journeyMapTabDisplayVisibility = ModTabsConfig.journeyMapTabDisplayVisibility;
				l2ArtifactsTabDisplayVisibility = ModTabsConfig.l2ArtifactsTabDisplayVisibility;
				l2AttributesTabDisplayVisibility = ModTabsConfig.l2AttributesTabDisplayVisibility;
				l2HostilityTabDisplayVisibility = ModTabsConfig.l2HostilityTabDisplayVisibility;
				mapAtlasesTabDisplayVisibility = ModTabsConfig.mapAtlasesTabDisplayVisibility;
				modularGolemsTabDisplayVisibility = ModTabsConfig.modularGolemsTabDisplayVisibility;
				motpTabDisplayVisibility = ModTabsConfig.motpTabDisplayVisibility;
				passiveSkillTreeTabDisplayVisibility = ModTabsConfig.passiveSkillTreeTabDisplayVisibility;
				pufferfishSkillsTabDisplayVisibility = ModTabsConfig.pufferfishSkillsTabDisplayVisibility;
				reskillableReimaginedTabDisplayVisibility = ModTabsConfig.reskillableReimaginedTabDisplayVisibility;
				sophisticatedBackpacksTabDisplayVisibility = ModTabsConfig.sophisticatedBackpacksTabDisplayVisibility;
				travelersBackpackTabDisplayVisibility = ModTabsConfig.travelersBackpackTabDisplayVisibility;
				xaerosMapTabDisplayVisibility = ModTabsConfig.xaerosMapTabDisplayVisibility;
				rpgCraftingTabDisplayVisibility = ModTabsConfig.advancementsTabDisplayVisibility; // Shares config with advancements
				biologyDictionaryTabDisplayVisibility = ModTabsConfig.biologyDictionaryTabDisplayVisibility;
				reliableBackpacksTabDisplayVisibility = ModTabsConfig.reliableBackpacksTabDisplayVisibility;
				runicSkillsTabDisplayVisibility = ModTabsConfig.runicSkillsTabDisplayVisibility;
				advancedTeamTabDisplayVisibility = ModTabsConfig.advancedTeamTabDisplayVisibility;
				villagerRecruitsTabDisplayVisibility = ModTabsConfig.villagerRecruitsTabDisplayVisibility;

				// Load custom icon settings
				inventoryTabCustomIcon = ModTabsConfig.inventoryTabCustomIcon;
				advancementsTabCustomIcon = ModTabsConfig.advancementsTabCustomIcon;
				rpgCraftingTabCustomIcon = ModTabsConfig.rpgCraftingTabCustomIcon;
				arsElixirumTabCustomIcon = ModTabsConfig.arsElixirumTabCustomIcon;
				arsNouveauTabCustomIcon = ModTabsConfig.arsNouveauTabCustomIcon;
				backpackedTabCustomIcon = ModTabsConfig.backpackedTabCustomIcon;
				bodyDamageTabCustomIcon = ModTabsConfig.bodyDamageTabCustomIcon;
				brassworksMissionsTabCustomIcon = ModTabsConfig.brassworksMissionsTabCustomIcon;
				cobblemonTabCustomIcon = ModTabsConfig.cobblemonTabCustomIcon;
				cosmeticArmorTabCustomIcon = ModTabsConfig.cosmeticArmorTabCustomIcon;
				draconicEvolutionTabCustomIcon = ModTabsConfig.draconicEvolutionTabCustomIcon;
				eccentricTomeTabCustomIcon = ModTabsConfig.eccentricTomeTabCustomIcon;
				dietTabCustomIcon = ModTabsConfig.dietTabCustomIcon;
				ftbQuestsTabCustomIcon = ModTabsConfig.ftbQuestsTabCustomIcon;
				ftbTeamsTabCustomIcon = ModTabsConfig.ftbTeamsTabCustomIcon;
				ftbChunksTabCustomIcon = ModTabsConfig.ftbChunksTabCustomIcon;
				journeyMapTabCustomIcon = ModTabsConfig.journeyMapTabCustomIcon;
				l2ArtifactsTabCustomIcon = ModTabsConfig.l2ArtifactsTabCustomIcon;
				l2AttributesTabCustomIcon = ModTabsConfig.l2AttributesTabCustomIcon;
				l2HostilityTabCustomIcon = ModTabsConfig.l2HostilityTabCustomIcon;
				mapAtlasesTabCustomIcon = ModTabsConfig.mapAtlasesTabCustomIcon;
				modularGolemsTabCustomIcon = ModTabsConfig.modularGolemsTabCustomIcon;
				motpTabCustomIcon = ModTabsConfig.motpTabCustomIcon;
				passiveSkillTreeTabCustomIcon = ModTabsConfig.passiveSkillTreeTabCustomIcon;
				pufferfishSkillsTabCustomIcon = ModTabsConfig.pufferfishSkillsTabCustomIcon;
				reskillableReimaginedTabCustomIcon = ModTabsConfig.reskillableReimaginedTabCustomIcon;
				sophisticatedBackpacksTabCustomIcon = ModTabsConfig.sophisticatedBackpacksTabCustomIcon;
				travelersBackpackTabCustomIcon = ModTabsConfig.travelersBackpackTabCustomIcon;
				xaerosMapTabCustomIcon = ModTabsConfig.xaerosMapTabCustomIcon;
				biologyDictionaryTabCustomIcon = ModTabsConfig.biologyDictionaryTabCustomIcon;
				reliableBackpacksTabCustomIcon = ModTabsConfig.reliableBackpacksTabCustomIcon;
				runicSkillsTabCustomIcon = ModTabsConfig.runicSkillsTabCustomIcon;
				advancedTeamTabCustomIcon = ModTabsConfig.advancedTeamTabCustomIcon;
				villagerRecruitsTabCustomIcon = ModTabsConfig.villagerRecruitsTabCustomIcon;
				apothicAttributesTabCustomIcon = ModTabsConfig.apothicAttributesTabCustomIcon;
			}
			catch (Exception e)
			{
				ModTabs.LOGGER.warn("An exception was caused trying to load the client config for Mod Tabs.");
				e.printStackTrace();
			}
		}
	}
}
