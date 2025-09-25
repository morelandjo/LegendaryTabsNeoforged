package vodmordia.modtabs.api.tabs_menu;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for managing tab order and registration
 */
public class TabRegistry {
    private static final Map<TabBase, Integer> tabOrders = new HashMap<>();
    private static int defaultOrder = 0;

    /**
     * Register a tab with a specific order
     */
    public static void registerTab(TabBase tab, int order) {
        tabOrders.put(tab, order);
    }

    /**
     * Get the order for a tab
     */
    public static int getTabOrder(TabBase tab) {
        return tabOrders.getOrDefault(tab, defaultOrder++);
    }

    /**
     * Clear all registered tabs
     */
    public static void clear() {
        tabOrders.clear();
        defaultOrder = 0;
    }
}