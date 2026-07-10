package vodmordia.modtabs.utils;

import net.minecraft.item.Item;

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
        // Try multiple possible locations for FTB Quests book item
        String[] possibleClasses = {
            "dev.ftb.mods.ftbquests.registry.ModItems",
            "dev.ftb.mods.ftbquests.item.FTBQuestsItems",
            "dev.ftb.mods.ftbquests.FTBQuests"
        };

        String[] possibleFields = {
            "BOOK",
            "QUEST_BOOK",
            "book"
        };

        for (String className : possibleClasses) {
            for (String fieldName : possibleFields) {
                Object registrySupplier = findItem(className, fieldName);
                if (registrySupplier != null) {
                    try {
                        // Try different access methods for Fabric

                        // Method 1: Direct item reference
                        if (registrySupplier instanceof Item) {
                            return registrySupplier;
                        }

                        // Method 2: Registry supplier with get() method
                        Method getMethod = registrySupplier.getClass().getMethod("get");
                        getMethod.setAccessible(true);
                        Object result = getMethod.invoke(registrySupplier);
                        if (result instanceof Item) {
                            return result;
                        }

                        // Method 3: Try value() method (some registry types)
                        try {
                            Method valueMethod = registrySupplier.getClass().getMethod("value");
                            valueMethod.setAccessible(true);
                            Object valueResult = valueMethod.invoke(registrySupplier);
                            if (valueResult instanceof Item) {
                                return valueResult;
                            }
                        } catch (Exception e2) {
                            // value() method doesn't exist, continue
                        }

                    } catch (Exception e) {
                        // Failed to get item from this supplier, try next
                        continue;
                    }
                }
            }
        }

        return null;
    }
}
