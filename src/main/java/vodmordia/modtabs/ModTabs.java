package vodmordia.modtabs;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vodmordia.modtabs.integration.ModIntegrationManager;
import eu.midnightdust.lib.config.MidnightConfig;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.config.ModTabsConfig;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ModTabs implements ModInitializer
{
    public static final String MOD_ID = "modtabs";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Path configPath = FabricLoader.getInstance().getConfigDir();
    public static Path modConfigPath = Paths.get(configPath.toAbsolutePath().toString(), "modtabs");

    @Override
    public void onInitialize()
    {
        LOGGER.info("Initializing Mod Tabs");

        // Initialize MidnightConfig
        try {
            MidnightConfig.init(MOD_ID, ModTabsConfig.class);
            LOGGER.info("MidnightConfig initialized successfully");
        } catch (Exception e) {
            LOGGER.warn("Failed to initialize MidnightConfig: " + e.getMessage());
        }

        // Initialize mod integration manager
        ModIntegrationManager.detectLoadedMods();

        LOGGER.info("Mod Tabs initialized successfully");
    }

}