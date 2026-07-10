package vodmordia.modtabs.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generic Class.forName cache. Each fully-qualified name is resolved at most once;
 * absent classes are remembered via a sentinel so repeated misses don't keep throwing
 * ClassNotFoundException.
 *
 * Used by hot paths (per-screen-init isEnabled/isCurrentlyUsed checks, per-inventory-item
 * isInstance scans) where the original Class.forName-on-every-call cost is measurable.
 */
public final class ClassCache {
    // Sentinel for "we tried and the class isn't on the classpath".
    private static final Class<?> MISSING = ClassCache.class;

    private static final Map<String, Class<?>> CACHE = new ConcurrentHashMap<>();

    private ClassCache() {}

    /**
     * Resolve a class by fully-qualified name. Returns null if the class is not present.
     */
    public static Class<?> resolve(String fqn) {
        Class<?> cached = CACHE.get(fqn);
        if (cached != null) {
            return cached == MISSING ? null : cached;
        }
        try {
            Class<?> c = Class.forName(fqn);
            CACHE.put(fqn, c);
            return c;
        } catch (ClassNotFoundException e) {
            CACHE.put(fqn, MISSING);
            return null;
        }
    }

    /**
     * Check whether {@code obj} is an instance of the named class. Cached.
     * Returns false if the class isn't loadable or {@code obj} is null.
     */
    public static boolean isInstance(String fqn, Object obj) {
        if (obj == null) return false;
        Class<?> c = resolve(fqn);
        return c != null && c.isInstance(obj);
    }

    /** Clear the cache. Intended for tests / dev hot-reload. */
    public static void clear() {
        CACHE.clear();
    }
}
