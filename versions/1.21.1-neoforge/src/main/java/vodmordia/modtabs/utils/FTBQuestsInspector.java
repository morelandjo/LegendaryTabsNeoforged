package vodmordia.modtabs.utils;

import net.minecraft.world.item.Item;

import java.lang.reflect.Method;

public class FTBQuestsInspector extends ModInspector {
    private static final FTBQuestsInspector INSTANCE = new FTBQuestsInspector();

    private FTBQuestsInspector() {
        super("FTB Quests");
    }

    public static void inspect() {
        INSTANCE.inspectModClasses();
    }

    public static Item tryGetBookItem() {
        Object item = INSTANCE.findBookItem();
        return item instanceof Item ? (Item) item : null;
    }

    @Override
    public void inspectModClasses() {
        // Pre-cache the book item lookup
        findBookItem();
        logCacheStats();
    }

    private Object findBookItem() {
        // Try to get FTB Quests book item: dev.ftb.mods.ftbquests.registry.ModItems.BOOK
        Object registrySupplier = findItem("dev.ftb.mods.ftbquests.registry.ModItems", "BOOK");
        if (registrySupplier != null) {
            try {
                // Call .get() method on Architectury's DeferredRegister$Entry
                Method getMethod = registrySupplier.getClass().getMethod("get");
                getMethod.setAccessible(true);
                return getMethod.invoke(registrySupplier);
            } catch (Exception e) {
                // Failed to get item from registry supplier
            }
        }
        return null;
    }
}
