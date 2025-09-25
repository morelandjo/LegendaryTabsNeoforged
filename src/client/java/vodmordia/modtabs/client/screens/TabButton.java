package vodmordia.modtabs.client.screens;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import vodmordia.modtabs.api.tabs_menu.TabBase;
import vodmordia.modtabs.api.tabs_menu.TabDisplayMode;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;
import vodmordia.modtabs.config.Config;

/**
 * Button widget that renders and handles clicks for a tab
 */
public class TabButton extends ButtonWidget {

    private final TabBase tab;
    private final PlayerEntity player;
    private final TabDisplayMode displayMode;

    public TabButton(TabBase tab, PlayerEntity player, int x, int y) {
        this(tab, player, x, y, TabDisplayMode.NORMAL);
    }

    public TabButton(TabBase tab, PlayerEntity player, int x, int y, TabDisplayMode displayMode) {
        super(x, y, TabBase.TAB_WIDTH, TabBase.TAB_HEIGHT, Text.empty(),
              button -> handlePress((TabButton) button), DEFAULT_NARRATION_SUPPLIER);
        this.tab = tab;
        this.player = player;
        this.displayMode = displayMode;
    }

    private static void handlePress(TabButton button) {
        if (button.tab != null && button.player != null) {
            if (Config.Baked.enableDebugLogging) {
                button.tab.getClass().getSimpleName();
            }

            TabsMenu.openTabScreen(button.tab, button.player);
        }
    }

    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (tab == null) return;

        boolean isHovered = isMouseOver(mouseX, mouseY);
        boolean isInverted = displayMode == TabDisplayMode.INVERTED;

        // Use the tab's render method
        tab.render(context, getX(), getY(), isHovered, displayMode);
    }

    public Text getMessage() {
        return tab != null ? tab.getTooltip() : Text.empty();
    }

    public TabBase getTab() {
        return tab;
    }

    public TabDisplayMode getDisplayMode() {
        return displayMode;
    }

    /**
     * Check if this tab is currently active (showing the screen it represents)
     */
    public boolean isCurrentlyUsed() {
        try {
            net.minecraft.client.MinecraftClient minecraft = net.minecraft.client.MinecraftClient.getInstance();
            if (minecraft.currentScreen != null && tab != null) {
                return tab.isCurrentlyUsed(minecraft.currentScreen);
            }
        } catch (Exception e) {
            // Ignore errors in checking current usage
        }
        return false;
    }
}