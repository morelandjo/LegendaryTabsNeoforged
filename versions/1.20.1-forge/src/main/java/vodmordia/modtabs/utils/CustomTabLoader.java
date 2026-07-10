package vodmordia.modtabs.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.config.CustomTabDefinition;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Utility class for loading custom tab definitions from JSON files
 */
public class CustomTabLoader {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final String CUSTOM_TABS_FOLDER = "custom_tabs";
    private static List<CustomTabDefinition> cachedDefinitions = null;

    /**
     * Load all custom tab definitions from the config/modtabs/custom_tabs/ directory
     */
    public static List<CustomTabDefinition> loadCustomTabs() {
        if (cachedDefinitions != null) {
            return cachedDefinitions;
        }

        List<CustomTabDefinition> definitions = new ArrayList<>();
        Path customTabsPath = getCustomTabsPath();

        try {
            // Create the directory if it doesn't exist
            if (!Files.exists(customTabsPath)) {
                Files.createDirectories(customTabsPath);
                ModTabs.LOGGER.info("Created custom tabs directory: " + customTabsPath);

                // Create an example file if directory was just created
                createExampleFile(customTabsPath);
            }

            // Load all JSON files from the directory
            try (Stream<Path> files = Files.walk(customTabsPath)) {
                files.filter(path -> path.toString().endsWith(".json"))
                     .forEach(path -> {
                         try {
                             CustomTabDefinition definition = loadTabDefinition(path);
                             if (definition != null && definition.isValid()) {
                                 definitions.add(definition);
                                 ModTabs.LOGGER.info("Loaded custom tab: " + definition.tabId + " from " + path.getFileName());
                             } else if (definition != null) {
                                 ModTabs.LOGGER.warn("Invalid custom tab definition in file: " + path.getFileName());
                             }
                         } catch (Exception e) {
                             ModTabs.LOGGER.error("Error loading custom tab from " + path.getFileName() + ": " + e.getMessage());
                         }
                     });
            }

        } catch (IOException e) {
            ModTabs.LOGGER.error("Error accessing custom tabs directory: " + e.getMessage());
        }

        cachedDefinitions = definitions;
        ModTabs.LOGGER.info("Loaded " + definitions.size() + " custom tab definitions");
        return definitions;
    }

    /**
     * Load a single tab definition from a JSON file
     */
    private static CustomTabDefinition loadTabDefinition(Path path) {
        try {
            String content = Files.readString(path);
            return GSON.fromJson(content, CustomTabDefinition.class);
        } catch (IOException e) {
            ModTabs.LOGGER.error("Failed to read file " + path.getFileName() + ": " + e.getMessage());
            return null;
        } catch (JsonSyntaxException e) {
            ModTabs.LOGGER.error("Invalid JSON syntax in " + path.getFileName() + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Get the path to the custom tabs directory
     */
    private static Path getCustomTabsPath() {
        return Paths.get(ModTabs.modConfigPath.toString(), CUSTOM_TABS_FOLDER);
    }

    /**
     * Create example JSON files to help users understand the format
     */
    private static void createExampleFile(Path customTabsPath) {
        try {
            // Create basic example
            CustomTabDefinition example = new CustomTabDefinition();
            example.tabId = "example_guidebook";
            example.enabled = false; // Disabled by default so it doesn't interfere
            example.tooltip = "Example Guidebook Tab";
            example.order = 100;

            example.icon = new CustomTabDefinition.IconDefinition();
            example.icon.item = "minecraft:written_book";
            example.icon.fallbackItem = "minecraft:book";

            example.action = new CustomTabDefinition.ActionDefinition();
            example.action.type = "use_item";
            example.action.item = "minecraft:written_book";

            example.requiredMods = new String[]{"examplemod"};

            String exampleJson = GSON.toJson(example);
            Path exampleFile = customTabsPath.resolve("example_tab.json");

            Files.writeString(exampleFile, exampleJson);
            ModTabs.LOGGER.info("Created example custom tab file: " + exampleFile);

            // Create Patchouli example
            CustomTabDefinition patchouliExample = new CustomTabDefinition();
            patchouliExample.tabId = "example_patchouli_book";
            patchouliExample.enabled = false; // Disabled by default
            patchouliExample.tooltip = "Example Patchouli Book";
            patchouliExample.order = 101;

            patchouliExample.icon = new CustomTabDefinition.IconDefinition();
            patchouliExample.icon.item = "patchouli:guide_book";
            patchouliExample.icon.fallbackItem = "minecraft:book";
            patchouliExample.icon.patchouliBook = "examplemod:guide";

            patchouliExample.action = new CustomTabDefinition.ActionDefinition();
            patchouliExample.action.type = "open_patchouli_book";
            patchouliExample.action.bookId = "examplemod:guide";

            patchouliExample.requiredMods = new String[]{"patchouli", "examplemod"};

            String patchouliJson = GSON.toJson(patchouliExample);
            Path patchouliFile = customTabsPath.resolve("example_patchouli_book.json");

            Files.writeString(patchouliFile, patchouliJson);
            ModTabs.LOGGER.info("Created example Patchouli book tab file: " + patchouliFile);

            // Create custom texture example (file-based)
            CustomTabDefinition customTextureExample = new CustomTabDefinition();
            customTextureExample.tabId = "example_custom_texture";
            customTextureExample.enabled = false;
            customTextureExample.tooltip = "Example Custom Texture Tab";
            customTextureExample.order = 102;

            customTextureExample.icon = new CustomTabDefinition.IconDefinition();
            customTextureExample.icon.customTexturePath = "config/modtabs/icons/my_custom_icon.png";
            customTextureExample.icon.fallbackItem = "minecraft:compass";

            customTextureExample.action = new CustomTabDefinition.ActionDefinition();
            customTextureExample.action.type = "use_item";
            customTextureExample.action.item = "minecraft:compass";

            customTextureExample.requiredMods = new String[]{};

            String customTextureJson = GSON.toJson(customTextureExample);
            Path customTextureFile = customTabsPath.resolve("example_custom_texture.json");

            Files.writeString(customTextureFile, customTextureJson);
            ModTabs.LOGGER.info("Created example custom texture tab file: " + customTextureFile);

            // Create custom texture example (resource-based)
            CustomTabDefinition resourceTextureExample = new CustomTabDefinition();
            resourceTextureExample.tabId = "example_resource_texture";
            resourceTextureExample.enabled = false;
            resourceTextureExample.tooltip = "Example Resource Texture Tab";
            resourceTextureExample.order = 103;

            resourceTextureExample.icon = new CustomTabDefinition.IconDefinition();
            resourceTextureExample.icon.customTexture = "modtabs:textures/gui/inventory.png";
            resourceTextureExample.icon.fallbackItem = "minecraft:map";

            resourceTextureExample.action = new CustomTabDefinition.ActionDefinition();
            resourceTextureExample.action.type = "use_item";
            resourceTextureExample.action.item = "minecraft:map";

            resourceTextureExample.requiredMods = new String[]{};

            String resourceTextureJson = GSON.toJson(resourceTextureExample);
            Path resourceTextureFile = customTabsPath.resolve("example_resource_texture.json");

            Files.writeString(resourceTextureFile, resourceTextureJson);
            ModTabs.LOGGER.info("Created example resource texture tab file: " + resourceTextureFile);

        } catch (IOException e) {
            ModTabs.LOGGER.error("Failed to create example files: " + e.getMessage());
        }
    }

    /**
     * Reload custom tab definitions (clears cache)
     */
    public static void reload() {
        cachedDefinitions = null;
        ModTabs.LOGGER.info("Custom tab definitions cache cleared");
    }

    /**
     * Save a custom tab definition to a JSON file
     */
    public static boolean saveCustomTab(CustomTabDefinition definition) {
        if (!definition.isValid()) {
            ModTabs.LOGGER.warn("Cannot save invalid tab definition: " + definition.tabId);
            return false;
        }

        try {
            Path customTabsPath = getCustomTabsPath();
            if (!Files.exists(customTabsPath)) {
                Files.createDirectories(customTabsPath);
            }

            Path tabFile = customTabsPath.resolve(definition.tabId + ".json");
            String json = GSON.toJson(definition);
            Files.writeString(tabFile, json);

            ModTabs.LOGGER.info("Saved custom tab: " + definition.tabId);
            reload(); // Clear cache so changes are picked up
            return true;

        } catch (IOException e) {
            ModTabs.LOGGER.error("Failed to save custom tab " + definition.tabId + ": " + e.getMessage());
            return false;
        }
    }
}