package vodmordia.modtabs.api.tabs_menu;

/**
 * Configuration record for individual tabs containing enabled state and order
 */
public record TabConfiguration(boolean enabled, int order) {

    /**
     * Creates a TabConfiguration with enabled=true and order=0
     */
    public static TabConfiguration defaultConfig() {
        return new TabConfiguration(true, 0);
    }

    /**
     * Creates a TabConfiguration with specified order and enabled=true
     */
    public static TabConfiguration withOrder(int order) {
        return new TabConfiguration(true, order);
    }

    /**
     * Creates a disabled TabConfiguration
     */
    public static TabConfiguration disabled() {
        return new TabConfiguration(false, 0);
    }
}