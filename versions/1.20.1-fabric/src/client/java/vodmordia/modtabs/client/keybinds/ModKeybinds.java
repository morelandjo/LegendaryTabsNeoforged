package vodmordia.modtabs.client.keybinds;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ModKeybinds {

    public static final String KEY_CATEGORY_MODTABS = "key.categories.modtabs";

    public static final KeyBinding TAB_CYCLE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.modtabs.tab_cycle",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_TAB,
        KEY_CATEGORY_MODTABS
    ));

    public static void register() {
        // Keybindings are registered during field initialization via KeyBindingHelper
    }
}
