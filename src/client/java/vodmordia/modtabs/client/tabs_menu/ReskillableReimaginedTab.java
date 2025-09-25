package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import vodmordia.modtabs.api.tabs_menu.TabBase;

public class ReskillableReimaginedTab extends TabBase {

    @Override
    public void openTargetScreen(PlayerEntity player) {
        // TODO: Implement screen opening
    }

    @Override
    public boolean isEnabled(PlayerEntity player) {
        return false; // Stub - disabled by default
    }

    @Override
    public void initTabOnScreens() {
        // TODO: Initialize on screens
    }

    @Override
    public void render(DrawContext gui, int x, int y, boolean hover) {
        // TODO: Implement rendering
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        return false; // TODO: Implement current screen detection
    }

    @Override
    public Text getTooltip() {
        return Text.literal("ReskillableReimaginedTab");
    }
}
