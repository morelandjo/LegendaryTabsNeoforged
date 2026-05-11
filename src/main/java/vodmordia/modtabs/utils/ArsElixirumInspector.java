package vodmordia.modtabs.utils;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ArsElixirumInspector {
    private static Item cachedGlassCauldronItem = null;
    private static boolean searchAttempted = false;

    /**
     * Attempts to get the Ars Elixirum glass cauldron item via reflection (cached).
     * Fragmentum's DeferredItem implements ItemLike on 1.20.1 Forge, so the cleanest path
     * is to cast and call asItem(). Other versions have used get() or value() — both are
     * tried as fallbacks.
     */
    public static Item tryGetGlassCauldronItem() {
        if (searchAttempted) {
            return cachedGlassCauldronItem;
        }
        searchAttempted = true;

        try {
            Class<?> itemsClass;
            try {
                itemsClass = Class.forName("dev.obscuria.elixirum.common.registry.ElixirumItems");
            } catch (ClassNotFoundException ignored) {
                itemsClass = Class.forName("dev.obscuria.elixirum.registry.ElixirumItems");
            }
            Field itemField = itemsClass.getField("GLASS_CAULDRON");
            Object registryObject = itemField.get(null);
            if (registryObject == null) return null;

            Item resolved = extractItem(registryObject);
            if (resolved != null) {
                cachedGlassCauldronItem = resolved;
                return cachedGlassCauldronItem;
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static Item extractItem(Object deferred) {
        if (deferred instanceof ItemLike itemLike) {
            try {
                Item asItem = itemLike.asItem();
                if (asItem != null) return asItem;
            } catch (Throwable ignored) {
            }
        }
        // 1.20.1 Fragmentum's Deferred exposes get(); 1.21.1 NeoForge Fragmentum exposes value().
        for (String methodName : new String[]{"get", "value"}) {
            try {
                Method m = deferred.getClass().getMethod(methodName);
                m.setAccessible(true);
                Object result = m.invoke(deferred);
                if (result instanceof Item item) return item;
            } catch (Throwable ignored) {
            }
        }
        return null;
    }
}