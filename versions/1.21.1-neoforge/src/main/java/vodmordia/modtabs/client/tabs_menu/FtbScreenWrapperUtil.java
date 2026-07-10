package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.gui.screens.Screen;

import java.lang.reflect.Field;

/** Reflective access to {@code dev.ftb.mods.ftblibrary.ui.ScreenWrapper.wrappedGui},
 *  used by FtbQuestsTab/FtbTeamsTab to distinguish which of the two is the "home" tab
 *  on the shared ScreenWrapper. */
final class FtbScreenWrapperUtil {
    private FtbScreenWrapperUtil() {}

    private static Field wrappedGuiField;
    private static boolean reflectFailed;

    static String getWrappedGuiClassName(Screen screen) {
        if (reflectFailed || screen == null) return null;
        try {
            if (wrappedGuiField == null) {
                wrappedGuiField = screen.getClass().getDeclaredField("wrappedGui");
                wrappedGuiField.setAccessible(true);
            }
            Object wrapped = wrappedGuiField.get(screen);
            return wrapped == null ? null : wrapped.getClass().getName();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            reflectFailed = true;
            return null;
        }
    }
}
