package vodmordia.modtabs.api.tabs_menu;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import vodmordia.modtabs.utils.ScreenClassResolver;

import java.util.function.Function;

/**
 * Centralized registry for screen-to-tab mappings with builder pattern
 */
public class ScreenRegistry {

    /**
     * Common screen dimensions
     */
    public static final Function<Player, Integer> STANDARD_WIDTH = (player) -> 176;
    public static final Function<Player, Integer> STANDARD_HEIGHT = (player) -> 166;
    public static final Function<Player, Integer> BODY_HEALTH_HEIGHT = (player) -> 183;
    public static final Function<Player, Integer> QUARK_BACKPACK_HEIGHT = (player) -> 224;
    public static final Function<Player, Integer> DIET_WIDTH = (player) -> 248;

    /**
     * Builder for registering multiple screens with the same configuration
     */
    public static class ScreenRegistrationBuilder {
        private Function<Player, Integer> widthFunction = STANDARD_WIDTH;
        private Function<Player, Integer> heightFunction = STANDARD_HEIGHT;
        private TabDisplayMode displayMode = TabDisplayMode.NORMAL;
        private TabPositioning positioning = TabPositioning.SCREEN_BOTTOM;
        private int offset = 0;

        public ScreenRegistrationBuilder withDimensions(Function<Player, Integer> width, Function<Player, Integer> height) {
            this.widthFunction = width;
            this.heightFunction = height;
            return this;
        }

        public ScreenRegistrationBuilder withStandardDimensions() {
            return withDimensions(STANDARD_WIDTH, STANDARD_HEIGHT);
        }

        public ScreenRegistrationBuilder withBodyHealthDimensions() {
            return withDimensions(STANDARD_WIDTH, BODY_HEALTH_HEIGHT);
        }

        public ScreenRegistrationBuilder withDietDimensions() {
            return withDimensions(DIET_WIDTH, STANDARD_HEIGHT);
        }

        public ScreenRegistrationBuilder withDisplayMode(TabDisplayMode displayMode) {
            this.displayMode = displayMode;
            return this;
        }

        public ScreenRegistrationBuilder inverted() {
            return withDisplayMode(TabDisplayMode.INVERTED);
        }

        public ScreenRegistrationBuilder withPositioning(TabPositioning positioning) {
            this.positioning = positioning;
            return this;
        }

        public ScreenRegistrationBuilder atTop() {
            return withPositioning(TabPositioning.SCREEN_TOP);
        }

        public ScreenRegistrationBuilder atBottom() {
            return withPositioning(TabPositioning.SCREEN_BOTTOM);
        }

        public ScreenRegistrationBuilder atRight() {
            return withPositioning(TabPositioning.SCREEN_RIGHT);
        }

        public ScreenRegistrationBuilder withOffset(int offset) {
            this.offset = offset;
            return this;
        }

        /**
         * Register all tabs for the specified screen classes
         */
        public void registerAllTabs(String... screenClassNames) {
            for (String className : screenClassNames) {
                Class<? extends Screen> screenClass = ScreenClassResolver.resolveScreenClass(className);
                if (screenClass != null) {
                    if (offset > 0) {
                        TabsMenu.registerScreenWithAllTabs(screenClass, widthFunction, heightFunction, displayMode, positioning, offset);
                    } else {
                        TabsMenu.registerScreenWithAllTabs(screenClass, widthFunction, heightFunction, displayMode, positioning);
                    }
                }
            }
        }

        /**
         * Register all tabs for the specified screen classes
         */
        public void registerAllTabs(Class<? extends Screen>... screenClasses) {
            for (Class<? extends Screen> screenClass : screenClasses) {
                if (offset > 0) {
                    TabsMenu.registerScreenWithAllTabs(screenClass, widthFunction, heightFunction, displayMode, positioning, offset);
                } else {
                    TabsMenu.registerScreenWithAllTabs(screenClass, widthFunction, heightFunction, displayMode, positioning);
                }
            }
        }

        /**
         * Register a specific tab for the specified screen classes
         */
        public void registerTab(TabBase tab, String... screenClassNames) {
            for (String className : screenClassNames) {
                Class<? extends Screen> screenClass = ScreenClassResolver.resolveScreenClass(className);
                if (screenClass != null) {
                    TabsMenu.addTabToScreen(tab, screenClass, widthFunction, heightFunction, offset);
                }
            }
        }

        /**
         * Register a specific tab for the specified screen classes
         */
        public void registerTab(TabBase tab, Class<? extends Screen>... screenClasses) {
            for (Class<? extends Screen> screenClass : screenClasses) {
                TabsMenu.addTabToScreen(tab, screenClass, widthFunction, heightFunction, offset);
            }
        }

        /**
         * Force register all tabs (override existing registrations)
         */
        public void forceRegisterAllTabs(String... screenClassNames) {
            for (String className : screenClassNames) {
                Class<? extends Screen> screenClass = ScreenClassResolver.resolveScreenClass(className);
                if (screenClass != null) {
                    TabsMenu.forceRegisterScreenWithAllTabs(screenClass, widthFunction, heightFunction, displayMode, positioning);
                }
            }
        }
    }

    /**
     * Start building a screen registration configuration
     */
    public static ScreenRegistrationBuilder builder() {
        return new ScreenRegistrationBuilder();
    }

    /**
     * Quick registration methods for common patterns
     */
    public static void registerStandardScreens(String... screenClassNames) {
        builder().withStandardDimensions().withPositioning(TabPositioning.GUI_RELATIVE).registerAllTabs(screenClassNames);
    }

    public static void registerStandardScreens(Class<? extends Screen>... screenClasses) {
        builder().withStandardDimensions().withPositioning(TabPositioning.GUI_RELATIVE).registerAllTabs(screenClasses);
    }

    public static void registerInvertedScreens(String... screenClassNames) {
        builder().withStandardDimensions().inverted().atTop().registerAllTabs(screenClassNames);
    }

    public static void registerInvertedScreens(Class<? extends Screen>... screenClasses) {
        builder().withStandardDimensions().inverted().atTop().registerAllTabs(screenClasses);
    }
}