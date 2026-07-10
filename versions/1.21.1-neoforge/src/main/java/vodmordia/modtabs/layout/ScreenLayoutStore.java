package vodmordia.modtabs.layout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import vodmordia.modtabs.ModTabs;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
        // 3-tier lookup: user config → bundled resource → hard-coded default. The user file
        // is the source of truth once a player has tweaked anything; the bundled JSON is
        // the mod's curated default per-screen layout (Phase 2). Hard-coded fallback covers
        // unrecognized screens (e.g. third-party-loaded screens with no shipping JSON).
        ScreenLayout loaded = loadFromDisk(fqn);
        if (loaded == null) {
            loaded = loadFromResource(fqn);
        }
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

    /**
     * Reads a bundled default JSON shipped at {@code assets/modtabs/screen-layouts/<fqn>.json}.
     * Returns null if the file isn't present, the resource manager isn't ready yet, or parsing
     * fails — callers fall through to a hard-coded {@link ScreenLayout} default.
     *
     * Resource path uses lowercase + {@code $} → {@code _} because ResourceLocation paths
     * are restricted to {@code [a-z0-9_/.-]}; FQNs only ever introduce uppercase or {@code $}
     * (for inner classes), so this is a lossless flattening.
     */
    private static ScreenLayout loadFromResource(String fqn) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getResourceManager() == null) {
            return null;
        }
        ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(
                ModTabs.MOD_ID, "screen-layouts/" + resourceSanitize(fqn) + ".json");
        Optional<Resource> res = mc.getResourceManager().getResource(loc);
        if (res.isEmpty()) {
            return null;
        }
        try (InputStream in = res.get().open()) {
            String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            ScreenLayout parsed = GSON.fromJson(json, ScreenLayout.class);
            return parsed != null ? parsed : new ScreenLayout();
        } catch (Exception e) {
            ModTabs.LOGGER.warn("Failed to load bundled layout " + fqn + ": " + e.getMessage());
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

    /**
     * ResourceLocation paths only accept {@code [a-z0-9_/.-]}, so we lowercase and flatten
     * {@code $} (inner-class separator) to {@code _}. The mapping is unambiguous because
     * FQNs only differ from resource-path-safe strings on case and {@code $}.
     */
    private static String resourceSanitize(String fqn) {
        return fqn.toLowerCase().replace('$', '_');
    }
}
