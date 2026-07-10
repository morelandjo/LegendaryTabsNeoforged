package vodmordia.modtabs.utils;

import net.minecraft.world.item.Item;
import vodmordia.modtabs.ModTabs;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class FTBTeamsInspector {
    private static Item cachedTeamItem = null;
    private static boolean searchAttempted = false;

    public static void inspectModClasses() {
        try {
            // Try to find FTB Teams items registry
            Class<?> itemsClass = Class.forName("dev.ftb.mods.ftbteams.registry.ModItems");
            Field[] fields = itemsClass.getFields();

            for (Field field : fields) {
                try {
                    Object fieldValue = field.get(null);

                    if (fieldValue != null) {
                        Method[] methods = fieldValue.getClass().getMethods();
                        for (Method method : methods) {
                            if (method.getName().equals("get") && method.getParameterCount() == 0) {
                                
                            }
                        }
                    }
                } catch (Exception e) {
                    
                }
            }
        } catch (ClassNotFoundException e) {
            

            // Try other potential locations
            String[] possibleClasses = {
                "dev.ftb.mods.ftbteams.item.ModItems",
                "dev.ftb.mods.ftbteams.FTBTeamsItems",
                "dev.ftb.mods.ftbteams.registry.FTBTeamsItems"
            };

            for (String className : possibleClasses) {
                try {
                    Class<?> itemsClass = Class.forName(className);

                    Field[] fields = itemsClass.getFields();
                    for (Field field : fields) {
                       
                    }
                } catch (ClassNotFoundException ex) {
                    
                }
            }
        } catch (Exception e) {
           
        }
    }

    /**
     * Attempts to get an FTB Teams item via reflection (cached)
     */
    public static Item tryGetTeamItem() {
        // Return cached result if already attempted
        if (searchAttempted) {
            return cachedTeamItem;
        }

        searchAttempted = true;

        // Try common item names that might exist
        String[] possibleItems = {"TEAM_ICON", "TEAM_ITEM", "TEAMS", "BANNER", "TEAM_BOOK", "TEAM_COMPASS"};
        String[] possibleClasses = {
            "dev.ftb.mods.ftbteams.registry.ModItems",
            "dev.ftb.mods.ftbteams.item.ModItems",
            "dev.ftb.mods.ftbteams.FTBTeamsItems",
            "dev.ftb.mods.ftbteams.neoforge.ModItems",
            "dev.ftb.mods.ftbteams.neoforge.FTBTeamsItems",
            "dev.ftb.mods.ftbteams.neoforge.Items",
            "dev.ftb.mods.ftblibrary.items.ModItems",
            "dev.ftb.mods.ftblibrary.registry.ModItems",
            "dev.ftb.mods.ftblibrary.FTBLibraryItems"
        };

        for (String className : possibleClasses) {
            for (String itemName : possibleItems) {
                try {
                    Class<?> itemsClass = Class.forName(className);
                    Field itemField = itemsClass.getField(itemName);
                    Object registryObject = itemField.get(null);

                    // Try to call .get() method (similar to FTB Quests pattern)
                    Method getMethod = registryObject.getClass().getMethod("get");
                    getMethod.setAccessible(true); // Bypass module access restrictions
                    Object item = getMethod.invoke(registryObject);

                    if (item instanceof Item) {
                        cachedTeamItem = (Item) item;
                        return cachedTeamItem;
                    }
                } catch (Exception e) {
                    // Continue trying other combinations
                }
            }
        }
        return null; // Will use fallback
    }
}
