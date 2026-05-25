package vodmordia.modtabs.api.tabs_menu;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import vodmordia.modtabs.utils.ScreenClassResolver;

import java.util.function.Function;

/**
 * Centralized registry for screen-to-tab mappings.
 *
 * Tabs declare only the host GUI's nominal dimensions and display mode (normal vs.
 * inverted background). Where the bar actually sits is decided per-screen by the
 * layout JSON ({@code anchor + offsetX/offsetY}), not at registration time.
 */
public class ScreenRegistry {

    public static final Function<Player, Integer> STANDARD_WIDTH = (player) -> 176;
    public static final Function<Player, Integer> STANDARD_HEIGHT = (player) -> 166;
    public static final Function<Player, Integer> BODY_HEALTH_HEIGHT = (player) -> 183;
    public static final Function<Player, Integer> QUARK_BACKPACK_HEIGHT = (player) -> 224;
    public static final Function<Player, Integer> DIET_WIDTH = (player) -> 248;

    public static class ScreenRegistrationBuilder {
        private Function<Player, Integer> widthFunction = STANDARD_WIDTH;
        private Function<Player, Integer> heightFunction = STANDARD_HEIGHT;
        private TabDisplayMode displayMode = TabDisplayMode.NORMAL;

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

        public void registerAllTabs(String... screenClassNames) {
            for (String className : screenClassNames) {
                Class<? extends Screen> screenClass = ScreenClassResolver.resolveScreenClass(className);
                if (screenClass != null) {
                    TabsMenu.registerScreenWithAllTabs(screenClass, widthFunction, heightFunction, displayMode);
                }
            }
        }

        @SafeVarargs
        public final void registerAllTabs(Class<? extends Screen>... screenClasses) {
            for (Class<? extends Screen> screenClass : screenClasses) {
                TabsMenu.registerScreenWithAllTabs(screenClass, widthFunction, heightFunction, displayMode);
            }
        }

        public void registerTab(TabBase tab, String... screenClassNames) {
            for (String className : screenClassNames) {
                Class<? extends Screen> screenClass = ScreenClassResolver.resolveScreenClass(className);
                if (screenClass != null) {
                    TabsMenu.addTabToScreen(tab, screenClass, widthFunction, heightFunction, 0);
                }
            }
        }

        @SafeVarargs
        public final void registerTab(TabBase tab, Class<? extends Screen>... screenClasses) {
            for (Class<? extends Screen> screenClass : screenClasses) {
                TabsMenu.addTabToScreen(tab, screenClass, widthFunction, heightFunction, 0);
            }
        }

        public void forceRegisterAllTabs(String... screenClassNames) {
            for (String className : screenClassNames) {
                Class<? extends Screen> screenClass = ScreenClassResolver.resolveScreenClass(className);
                if (screenClass != null) {
                    TabsMenu.forceRegisterScreenWithAllTabs(screenClass, widthFunction, heightFunction, displayMode);
                }
            }
        }
    }

    public static ScreenRegistrationBuilder builder() {
        return new ScreenRegistrationBuilder();
    }

    public static void registerStandardScreens(String... screenClassNames) {
        builder().withStandardDimensions().registerAllTabs(screenClassNames);
    }

    @SafeVarargs
    public static void registerStandardScreens(Class<? extends Screen>... screenClasses) {
        builder().withStandardDimensions().registerAllTabs(screenClasses);
    }

    public static void registerInvertedScreens(String... screenClassNames) {
        builder().withStandardDimensions().inverted().registerAllTabs(screenClassNames);
    }

    @SafeVarargs
    public static void registerInvertedScreens(Class<? extends Screen>... screenClasses) {
        builder().withStandardDimensions().inverted().registerAllTabs(screenClasses);
    }
}
