package vodmordia.modtabs.utils;

import net.minecraft.client.gui.screens.Screen;

/**
 * Screen-typed wrapper around {@link ClassCache}. Returns the resolved class only when it
 * is actually a Screen subclass, so callers don't need to cast.
 */
public final class ScreenClassResolver {

    private ScreenClassResolver() {}

    /**
     * Resolve a screen class by name with caching. Returns null if the class is missing
     * or isn't a Screen subclass.
     */
    @SuppressWarnings("unchecked")
    public static Class<? extends Screen> resolveScreenClass(String className) {
        Class<?> clazz = ClassCache.resolve(className);
        if (clazz == null || !Screen.class.isAssignableFrom(clazz)) {
            return null;
        }
        return (Class<? extends Screen>) clazz;
    }

    /**
     * Try to resolve multiple possible screen class names, returning the first one found.
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
     * Check if a screen class exists. Cached.
     */
    public static boolean screenClassExists(String className) {
        return ClassCache.resolve(className) != null;
    }

    /**
     * Clear the underlying cache (useful for testing or dynamic class loading scenarios).
     */
    public static void clearCache() {
        ClassCache.clear();
    }
}
