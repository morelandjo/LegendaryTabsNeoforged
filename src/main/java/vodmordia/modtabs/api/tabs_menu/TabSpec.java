package vodmordia.modtabs.api.tabs_menu;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

import java.util.function.BooleanSupplier;
import java.util.function.Function;

/**
 * Declarative description of an integration tab. Captures the boilerplate that's
 * the same across most tabs (config check, mod-loaded check, tooltip key, screen
 * registration) so subclasses only need to implement the variable part — usually
 * just {@code openTargetScreen}.
 *
 * @param configKey         matches {@code @TabConfig(configKey = ...)}; informational
 * @param mod               integration whose presence is required, or {@code null} for vanilla tabs
 * @param enabledFlag       supplier for the per-tab enable flag, e.g. {@code () -> Config.Baked.fooTabEnabled}
 * @param iconKey           IconResolver lookup key for custom-icon config (third arg to ConfigurableIconTab)
 * @param tooltipKey        translation key suffix; full key is {@code tooltip.modtabs.tab.<tooltipKey>.description}
 * @param layout            how/where to position the tab bar on the screen
 * @param currentScreenFqns FQNs of screens that mean "this tab is currently active" (so cycling
 *                          skips it). Empty array = never current. Supports multiple variants
 *                          for tabs whose mod renders more than one screen class.
 * @param screenFqns        screens the tab attaches to; empty array = no auto-registration
 */
public record TabSpec(
        String configKey,
        ModIntegration mod,
        BooleanSupplier enabledFlag,
        String iconKey,
        String tooltipKey,
        Layout layout,
        String[] currentScreenFqns,
        String[] screenFqns
) {
    private static final String[] NONE = new String[0];

    /** Convenience: spec with no current-screen check. */
    public static TabSpec withoutCurrentScreen(
            String configKey, ModIntegration mod, BooleanSupplier enabledFlag,
            String iconKey, String tooltipKey, Layout layout, String... screenFqns) {
        return new TabSpec(configKey, mod, enabledFlag, iconKey, tooltipKey, layout, NONE, screenFqns);
    }
    public boolean modLoaded() {
        return mod == null || ModIntegrationManager.isModLoaded(mod);
    }

    public Component buildTooltip() {
        return Component.translatable("tooltip." + ModTabs.MOD_ID + ".tab." + tooltipKey + ".description");
    }

    /**
     * Layout describes both dimensions (width/height of the host GUI) and where the
     * tab bar sits relative to it (bottom, top, GUI-relative).
     */
    public record Layout(boolean inverted, Position position, Dimensions dimensions) {

        public enum Position {
            GUI_RELATIVE(TabPositioning.GUI_RELATIVE),
            SCREEN_TOP(TabPositioning.SCREEN_TOP),
            SCREEN_BOTTOM(TabPositioning.SCREEN_BOTTOM),
            SCREEN_RIGHT(TabPositioning.SCREEN_RIGHT);

            public final TabPositioning value;
            Position(TabPositioning v) { this.value = v; }
        }

        /**
         * Common host-screen sizes. Backed by fixed Function<Player,Integer> instances so
         * we don't allocate a new lambda on every screen-init.
         */
        public enum Dimensions {
            STANDARD(176, 166),
            BODY_HEALTH(176, 183),
            DIET(248, 166),
            QUARK_BACKPACK(176, 224);

            public final Function<Player, Integer> width;
            public final Function<Player, Integer> height;

            Dimensions(int w, int h) {
                this.width = p -> w;
                this.height = p -> h;
            }
        }

        public TabDisplayMode displayMode() {
            return inverted ? TabDisplayMode.INVERTED : TabDisplayMode.NORMAL;
        }

        // Common preset shortcuts
        public static Layout invertedTop() { return new Layout(true, Position.SCREEN_TOP, Dimensions.STANDARD); }
        public static Layout standardBottom() { return new Layout(false, Position.SCREEN_BOTTOM, Dimensions.STANDARD); }
        public static Layout guiRelative() { return new Layout(false, Position.GUI_RELATIVE, Dimensions.STANDARD); }
        public static Layout guiRelativeInverted() { return new Layout(true, Position.GUI_RELATIVE, Dimensions.STANDARD); }
        public static Layout screenRight() { return new Layout(false, Position.SCREEN_RIGHT, Dimensions.STANDARD); }
    }
}
