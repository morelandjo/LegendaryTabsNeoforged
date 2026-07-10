package vodmordia.modtabs.utils;

import net.minecraft.item.Item;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ArsElixirumInspector {
    private static Item cachedGlassCauldronItem = null;
    private static boolean searchAttempted = false;

    /**
     * Attempts to get the Ars Elixirum glass cauldron item via reflection (cached)
     */
    public static Item tryGetGlassCauldronItem() {
        // Return cached result if already attempted
        if (searchAttempted) {
            return cachedGlassCauldronItem;
        }

        searchAttempted = true;

        try {
            // Get Ars Elixirum glass cauldron item: dev.obscuria.elixirum.registry.ElixirumItems.GLASS_CAULDRON
            Class<?> itemsClass = Class.forName("dev.obscuria.elixirum.registry.ElixirumItems");
            Field itemField = itemsClass.getField("GLASS_CAULDRON");
            Object registryObject = itemField.get(null);

            // Get item from Fragmentum Deferred object (uses 'value' method, not 'get')
            Method valueMethod = registryObject.getClass().getMethod("value");
            valueMethod.setAccessible(true); // Bypass module access restrictions
            Object item = valueMethod.invoke(registryObject);

            if (item instanceof Item) {
                cachedGlassCauldronItem = (Item) item;
                return cachedGlassCauldronItem;
            }
        } catch (Exception e) {
            // Silently fail and use fallback
        }
        return null; // Will use brewing stand fallback
    }
}