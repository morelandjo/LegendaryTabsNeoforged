package vodmordia.modtabs.utils;

import net.minecraft.client.gui.screen.Screen;
import vodmordia.modtabs.ModTabs;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility for resolving screen classes with caching to reduce reflection overhead
 */
public class ScreenClassResolver {
    private static final Map<String, Class<? extends Screen>> classCache = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> failureCache = new ConcurrentHashMap<>();

    /**
     * Resolve a screen class by name with caching
     */
    @SuppressWarnings("unchecked")
    public static Class<? extends Screen> resolveScreenClass(String className) {
        // Check cache first
        if (classCache.containsKey(className)) {
            return classCache.get(className);
        }

        // Check failure cache to avoid repeated failed lookups
        if (failureCache.getOrDefault(className, false)) {
            return null;
        }

        try {
            Class<?> clazz = Class.forName(className);
            if (Screen.class.isAssignableFrom(clazz)) {
                Class<? extends Screen> screenClass = (Class<? extends Screen>) clazz;
                classCache.put(className, screenClass);
                return screenClass;
            } else {
                failureCache.put(className, true);
                return null;
            }
        } catch (ClassNotFoundException e) {
            failureCache.put(className, true);
            return null;
        }
    }

    /**
     * Try to resolve multiple possible screen class names, returning the first one found
     */
    public static Class<? extends Screen> resolveFirstAvailable(String... classNames) {
        for (String className : classNames) {
            Class<? extends Screen> screenClass = resolveScreenClass(className);
            if (screenClass != null) {
                return screenClass;
            }
        }
        return null;
    }

    /**
     * Check if a screen class exists without caching the result
     */
    public static boolean screenClassExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Clear the cache (useful for testing or dynamic class loading scenarios)
     */
    public static void clearCache() {
        classCache.clear();
        failureCache.clear();
    }
}