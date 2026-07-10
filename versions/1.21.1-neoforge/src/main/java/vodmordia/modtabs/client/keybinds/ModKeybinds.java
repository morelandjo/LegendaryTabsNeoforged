package vodmordia.modtabs.client.keybinds;

import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;

public class ModKeybinds {

    public static final String KEY_CATEGORY_MODTABS = "key.categories.modtabs";

    public static final KeyMapping TAB_CYCLE = new KeyMapping(
        "key.modtabs.tab_cycle",
        InputConstants.Type.KEYSYM,
        InputConstants.KEY_TAB,
        KEY_CATEGORY_MODTABS
    );
}