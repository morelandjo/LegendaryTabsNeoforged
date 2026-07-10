package vodmordia.modtabs.utils;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import vodmordia.modtabs.ModTabs;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility for dynamically loading PNG textures from the file system
 * and registering them with Minecraft's texture manager.
 */
public class DynamicTextureLoader {

    private static final Map<String, ResourceLocation> loadedTextures = new HashMap<>();
    private static final String DYNAMIC_TEXTURE_PREFIX = "modtabs_dynamic";

    /**
     * Load a texture from a file path and register it with Minecraft's texture manager.
     * The path should be relative to the game directory (e.g., "config/modtabs/icons/my_icon.png")
     *
     * @param filePath The file path relative to the game directory
     * @param textureId A unique identifier for this texture
     * @return The ResourceLocation of the loaded texture, or null if loading failed
     */
    public static ResourceLocation loadTextureFromFile(String filePath, String textureId) {
        // Check if we've already loaded this texture
        if (loadedTextures.containsKey(textureId)) {
            return loadedTextures.get(textureId);
        }

        try {
            // Resolve the path relative to the game directory
            Path gameDir = Paths.get(".").toAbsolutePath().normalize();
            Path texturePath = gameDir.resolve(filePath).normalize();

            // Verify the file exists
            if (!Files.exists(texturePath)) {
                ModTabs.LOGGER.warn("Custom texture file not found: " + texturePath);
                return null;
            }

            // Verify the file is a PNG
            if (!filePath.toLowerCase().endsWith(".png")) {
                ModTabs.LOGGER.warn("Custom texture must be a PNG file: " + filePath);
                return null;
            }

            // Load the image using NativeImage
            NativeImage nativeImage;
            try (FileInputStream fis = new FileInputStream(texturePath.toFile())) {
                nativeImage = NativeImage.read(fis);
            }

            // Create a DynamicTexture from the NativeImage
            DynamicTexture dynamicTexture = new DynamicTexture(nativeImage);

            // Create a unique ResourceLocation for this texture
            // ResourceLocations must be lowercase, so convert textureId to lowercase
            ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(
                ModTabs.MOD_ID,
                DYNAMIC_TEXTURE_PREFIX + "/" + textureId.toLowerCase()
            );

            // Register the texture with Minecraft's texture manager
            Minecraft.getInstance().getTextureManager().register(textureLocation, dynamicTexture);

            // Cache the ResourceLocation
            loadedTextures.put(textureId, textureLocation);

            ModTabs.LOGGER.info("Successfully loaded custom texture: " + filePath + " as " + textureLocation);
            return textureLocation;

        } catch (IOException e) {
            ModTabs.LOGGER.error("Failed to load custom texture from " + filePath + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Validate that a ResourceLocation can be loaded from the resource pack system.
     * This is used for "customTexture" paths that reference textures in resource packs.
     *
     * @param textureLocation The ResourceLocation to validate
     * @return true if the texture exists, false otherwise
     */
    public static boolean validateResourceTexture(ResourceLocation textureLocation) {
        try {
            // Check if the texture exists in the resource manager
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.getResourceManager().getResource(textureLocation).isPresent()) {
                return true;
            } else {
                ModTabs.LOGGER.warn("Custom texture not found in resources: " + textureLocation);
                return false;
            }
        } catch (Exception e) {
            ModTabs.LOGGER.warn("Error validating custom texture: " + textureLocation + " - " + e.getMessage());
            return false;
        }
    }

    /**
     * Clear all loaded dynamic textures from memory.
     * This should be called when reloading custom tabs.
     */
    public static void clearLoadedTextures() {
        for (Map.Entry<String, ResourceLocation> entry : loadedTextures.entrySet()) {
            try {
                Minecraft.getInstance().getTextureManager().release(entry.getValue());
            } catch (Exception e) {
                ModTabs.LOGGER.warn("Failed to release texture: " + entry.getValue());
            }
        }
        loadedTextures.clear();
        ModTabs.LOGGER.info("Cleared all dynamically loaded textures");
    }

    /**
     * Get the icons directory path for custom textures.
     * Creates the directory if it doesn't exist.
     *
     * @return The path to the icons directory
     */
    public static Path getIconsDirectory() {
        Path iconsPath = Paths.get(ModTabs.modConfigPath.toString(), "icons");
        try {
            if (!Files.exists(iconsPath)) {
                Files.createDirectories(iconsPath);
                ModTabs.LOGGER.info("Created custom icons directory: " + iconsPath);
            }
        } catch (IOException e) {
            ModTabs.LOGGER.error("Failed to create icons directory: " + e.getMessage());
        }
        return iconsPath;
    }
}
