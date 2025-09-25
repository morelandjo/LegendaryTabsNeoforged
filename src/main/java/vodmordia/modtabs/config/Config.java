package vodmordia.modtabs.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class Config extends MidnightConfig {
    @Entry(category = "general", name = "enableDebugLogging")
    public static boolean enableDebugLogging = false;

    @Entry(category = "general", name = "customTabsDebugLogging")
    public static boolean customTabsDebugLogging = false;

    @Entry(category = "general", name = "customTabsEnabled")
    public static boolean customTabsEnabled = true;

    @Entry(category = "general", name = "tabDisplayMode")
    public static String tabDisplayMode = "AUTO";

    @Entry(category = "general", name = "tabOrder")
    public static int tabOrder = 0;

    public static class Baked {
        public static boolean enableDebugLogging = false;
        public static boolean customTabsDebugLogging = false;
        public static boolean customTabsEnabled = true;
        public static String tabDisplayMode = "AUTO";
        public static int tabOrder = 0;

        public static void bake() {
            enableDebugLogging = Config.enableDebugLogging;
            customTabsDebugLogging = Config.customTabsDebugLogging;
            customTabsEnabled = Config.customTabsEnabled;
            tabDisplayMode = Config.tabDisplayMode;
            tabOrder = Config.tabOrder;
        }

        public static void bakeClient() {
            bake();
        }
    }
}