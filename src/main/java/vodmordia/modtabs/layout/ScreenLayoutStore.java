package vodmordia.modtabs.layout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.gui.screens.Screen;
import vodmordia.modtabs.ModTabs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Reads and writes per-screen {@link ScreenLayout} JSON files at
 * {@code config/modtabs/screen-layouts/<screen.fqn>.json}.
 *
 * Layouts are cached in memory after first load so the per-frame positioning
 * code can read them without touching the disk.
 */
public class ScreenLayoutStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FOLDER = "screen-layouts";
    private static final Map<String, ScreenLayout> CACHE = new HashMap<>();

    private ScreenLayoutStore() {}

    public static ScreenLayout get(Class<? extends Screen> screenClass) {
        return get(screenClass.getName());
    }

    public static ScreenLayout get(String fqn) {
        ScreenLayout cached = CACHE.get(fqn);
        if (cached != null) {
            return cached;
        }
        ScreenLayout loaded = loadFromDisk(fqn);
        if (loaded == null) {
            loaded = new ScreenLayout();
        }
        CACHE.put(fqn, loaded);
        return loaded;
    }

    public static void save(String fqn, ScreenLayout layout) {
        CACHE.put(fqn, layout);
        Path path = pathFor(fqn);
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(layout), StandardCharsets.UTF_8);
        } catch (IOException e) {
            ModTabs.LOGGER.error("Failed to save screen layout for " + fqn + ": " + e.getMessage());
        }
    }

    public static void reset(String fqn) {
        CACHE.remove(fqn);
        try {
            Files.deleteIfExists(pathFor(fqn));
        } catch (IOException e) {
            ModTabs.LOGGER.error("Failed to delete screen layout for " + fqn + ": " + e.getMessage());
        }
    }

    public static void invalidateCache() {
        CACHE.clear();
    }

    private static ScreenLayout loadFromDisk(String fqn) {
        Path path = pathFor(fqn);
        if (!Files.exists(path)) {
            return null;
        }
        try {
            String json = Files.readString(path, StandardCharsets.UTF_8);
            ScreenLayout parsed = GSON.fromJson(json, ScreenLayout.class);
            return parsed != null ? parsed : new ScreenLayout();
        } catch (Exception e) {
            ModTabs.LOGGER.warn("Failed to load screen layout " + fqn + ": " + e.getMessage());
            return null;
        }
    }

    private static Path pathFor(String fqn) {
        return ModTabs.modConfigPath.resolve(FOLDER).resolve(sanitize(fqn) + ".json");
    }

    /**
     * Strip filesystem-hostile characters from a FQN. Dots, dollars, and underscores
     * (the only special chars Java FQNs use) all survive unchanged.
     */
    private static String sanitize(String fqn) {
        return fqn.replaceAll("[^a-zA-Z0-9._$-]", "_");
    }
}
