package vodmordia.modtabs.utils;

import net.minecraft.world.item.Item;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ArsElixirumInspector {
    private static Item cachedGlassCauldronItem = null;
    private static boolean searchAttempted = false;

    /**
     * Attempts to get the Ars Elixirum glass cauldron item via reflection (cached)
     */
    public static Item tryGetGlassCauldronItem() {
        if (searchAttempted) {
            return cachedGlassCauldronItem;
        }
        searchAttempted = true;

        try {
            Class<?> itemsClass = Class.forName("dev.obscuria.elixirum.registry.ElixirumItems");
            Field itemField = itemsClass.getField("GLASS_CAULDRON");
            Object registryObject = itemField.get(null);

            // Fragmentum's Deferred uses 'value' on NeoForge 1.21.x.
            Method valueMethod = registryObject.getClass().getMethod("value");
            valueMethod.setAccessible(true);
            Object item = valueMethod.invoke(registryObject);

            if (item instanceof Item) {
                cachedGlassCauldronItem = (Item) item;
                return cachedGlassCauldronItem;
            }
        } catch (Throwable ignored) {
        }
        return null;
    }
}