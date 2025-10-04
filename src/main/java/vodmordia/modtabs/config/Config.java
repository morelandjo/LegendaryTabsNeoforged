package vodmordia.modtabs.config;

import vodmordia.modtabs.ModTabs;

public class Config
{
	public static class Baked
	{
		// Tab enable/disable settings - only available mods
		public static boolean inventoryTabEnabled;
		public static boolean advancementsTabEnabled;
		public static boolean ftbQuestsTabEnabled;
		public static boolean ftbTeamsTabEnabled;
		public static boolean mapAtlasesTabEnabled;
		public static boolean xaerosMapTabEnabled;
		public static boolean pufferfishSkillsTabEnabled;
		public static boolean sophisticatedBackpacksTabEnabled;
		public static boolean travelersBackpackTabEnabled;
		public static boolean stickyInventoryTab;

		// Custom tabs settings
		public static boolean customTabsEnabled;
		public static boolean customTabsDebugLogging;

		// Tab display visibility settings
		public static TabDisplayVisibility inventoryTabDisplayVisibility;
		public static TabDisplayVisibility advancementsTabDisplayVisibility;
		public static TabDisplayVisibility ftbQuestsTabDisplayVisibility;
		public static TabDisplayVisibility ftbTeamsTabDisplayVisibility;
		public static TabDisplayVisibility mapAtlasesTabDisplayVisibility;
		public static TabDisplayVisibility xaerosMapTabDisplayVisibility;
		public static TabDisplayVisibility pufferfishSkillsTabDisplayVisibility;
		public static TabDisplayVisibility sophisticatedBackpacksTabDisplayVisibility;
		public static TabDisplayVisibility travelersBackpackTabDisplayVisibility;

		// Tab order overrides
		public static int inventoryTabOrder;
		public static int advancementsTabOrder;
		public static int ftbQuestsTabOrder;
		public static int ftbTeamsTabOrder;
		public static int mapAtlasesTabOrder;
		public static int xaerosMapTabOrder;
		public static int pufferfishSkillsTabOrder;
		public static int sophisticatedBackpacksTabOrder;
		public static BackpackSlot sophisticatedBackpacksPreferredSlot;
		public static int travelersBackpackTabOrder;

		public static void bakeClient()
		{
			try
			{
				// Read from MidnightConfig fields - only available mods
				inventoryTabEnabled = ModTabsConfig.inventoryTabEnabled;
				advancementsTabEnabled = ModTabsConfig.advancementsTabEnabled;
				ftbQuestsTabEnabled = ModTabsConfig.ftbQuestsTabEnabled;
				ftbTeamsTabEnabled = ModTabsConfig.ftbTeamsTabEnabled;
				mapAtlasesTabEnabled = ModTabsConfig.mapAtlasesTabEnabled;
				xaerosMapTabEnabled = ModTabsConfig.xaerosMapTabEnabled;
				pufferfishSkillsTabEnabled = ModTabsConfig.pufferfishSkillsTabEnabled;
				sophisticatedBackpacksTabEnabled = ModTabsConfig.sophisticatedBackpacksTabEnabled;
				travelersBackpackTabEnabled = ModTabsConfig.travelersBackpackTabEnabled;
				stickyInventoryTab = ModTabsConfig.stickyInventoryTab;

				// Load custom tabs settings
				customTabsEnabled = ModTabsConfig.customTabsEnabled;
				customTabsDebugLogging = ModTabsConfig.customTabsDebugLogging;

				// Load tab order overrides
				inventoryTabOrder = ModTabsConfig.inventoryTabOrder;
				advancementsTabOrder = ModTabsConfig.advancementsTabOrder;
				ftbQuestsTabOrder = ModTabsConfig.ftbQuestsTabOrder;
				ftbTeamsTabOrder = ModTabsConfig.ftbTeamsTabOrder;
				mapAtlasesTabOrder = ModTabsConfig.mapAtlasesTabOrder;
				xaerosMapTabOrder = ModTabsConfig.xaerosMapTabOrder;
				pufferfishSkillsTabOrder = ModTabsConfig.pufferfishSkillsTabOrder;
				sophisticatedBackpacksTabOrder = ModTabsConfig.sophisticatedBackpacksTabOrder;
				sophisticatedBackpacksPreferredSlot = ModTabsConfig.sophisticatedBackpacksPreferredSlot;
				travelersBackpackTabOrder = ModTabsConfig.travelersBackpackTabOrder;

				// Load tab display visibility settings
				inventoryTabDisplayVisibility = ModTabsConfig.inventoryTabDisplayVisibility;
				advancementsTabDisplayVisibility = ModTabsConfig.advancementsTabDisplayVisibility;
				ftbQuestsTabDisplayVisibility = ModTabsConfig.ftbQuestsTabDisplayVisibility;
				ftbTeamsTabDisplayVisibility = ModTabsConfig.ftbTeamsTabDisplayVisibility;
				mapAtlasesTabDisplayVisibility = ModTabsConfig.mapAtlasesTabDisplayVisibility;
				xaerosMapTabDisplayVisibility = ModTabsConfig.xaerosMapTabDisplayVisibility;
				pufferfishSkillsTabDisplayVisibility = ModTabsConfig.pufferfishSkillsTabDisplayVisibility;
				sophisticatedBackpacksTabDisplayVisibility = ModTabsConfig.sophisticatedBackpacksTabDisplayVisibility;
				travelersBackpackTabDisplayVisibility = ModTabsConfig.travelersBackpackTabDisplayVisibility;
			}
			catch (Exception e)
			{
				ModTabs.LOGGER.warn("An exception was caused trying to load the client config for Mod Tabs.");
				e.printStackTrace();
			}
		}
	}
}