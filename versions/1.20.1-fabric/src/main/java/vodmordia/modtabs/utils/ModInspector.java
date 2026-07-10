package vodmordia.modtabs.utils;

import vodmordia.modtabs.ModTabs;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for mod inspectors that use reflection to find classes and items
 */
public abstract class ModInspector {

    protected final String modName;
    private final Map<String, Class<?>> classCache = new HashMap<>();
    private final Map<String, Object> itemCache = new HashMap<>();

    protected ModInspector(String modName) {
        this.modName = modName;
    }

    /**
     * Inspect mod classes - subclasses should override this
     */
    public abstract void inspectModClasses();

    /**
     * Try to find a class by name with caching
     */
    protected Class<?> findClass(String className) {
        if (classCache.containsKey(className)) {
            return classCache.get(className);
        }

        try {
            Class<?> clazz = Class.forName(className);
            classCache.put(className, clazz);
            return clazz;
        } catch (ClassNotFoundException e) {
            classCache.put(className, null);
            return null;
        }
    }

    /**
     * Try to find an item by accessing a static field
     */
    protected Object findItem(String className, String fieldName) {
        String cacheKey = className + "." + fieldName;
        if (itemCache.containsKey(cacheKey)) {
            return itemCache.get(cacheKey);
        }

        try {
            Class<?> clazz = findClass(className);
            if (clazz != null) {
                Field field = clazz.getField(fieldName);
                Object item = field.get(null);
                itemCache.put(cacheKey, item);
                
                return item;
            }
        } catch (Exception e) {
           
        }

        itemCache.put(cacheKey, null);
        return null;
    }

    /**
     * Try to find an item by calling a static method
     */
    protected Object findItemByMethod(String className, String methodName, Class<?>... parameterTypes) {
        String cacheKey = className + "." + methodName + "()";
        if (itemCache.containsKey(cacheKey)) {
            return itemCache.get(cacheKey);
        }

        try {
            Class<?> clazz = findClass(className);
            if (clazz != null) {
                Method method = clazz.getMethod(methodName, parameterTypes);
                Object item = method.invoke(null);
                itemCache.put(cacheKey, item);
                
                return item;
            }
        } catch (Exception e) {
           
        }

        itemCache.put(cacheKey, null);
        return null;
    }

    /**
     * Try multiple possible class names and return the first one found
     */
    protected Class<?> findFirstAvailableClass(String... classNames) {
        for (String className : classNames) {
            Class<?> clazz = findClass(className);
            if (clazz != null) {
                return clazz;
            }
        }
        return null;
    }

    /**
     * Try multiple possible field locations for an item
     */
    protected Object findFirstAvailableItem(String fieldName, String... classNames) {
        for (String className : classNames) {
            Object item = findItem(className, fieldName);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /**
     * Clear all cached results
     */
    protected void clearCache() {
        classCache.clear();
        itemCache.clear();
    }

    /**
     * Get information about cached results
     */
    protected void logCacheStats() {
       
    }
}