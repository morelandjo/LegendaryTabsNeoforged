package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import vodmordia.modtabs.api.tabs_menu.TabBase;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;

public class InventoryTab extends TabBase {

    @Override
    public void openTargetScreen(PlayerEntity player) {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        minecraft.setScreen(new InventoryScreen(player));
    }

    @Override
    public boolean isEnabled(PlayerEntity player) {
        return true; // Always enabled
    }

    @Override
    public void initTabOnScreens() {
        // Register this tab for common container screens
        try {
            TabsMenu.addPendingRegistration(() -> {
                // Inventory screen
                TabsMenu.registerScreenForTabs(InventoryScreen.class, this);

                // Other common screens that should show inventory tab
                try {
                    Class<?> creativeScreen = Class.forName("net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen");
                    TabsMenu.registerScreenForTabs((Class<? extends Screen>) creativeScreen, this);
                } catch (ClassNotFoundException e) {
                    // Creative screen not found, skip
                }
            });
        } catch (Exception e) {
            // Ignore registration errors
        }
    }

    @Override
    public void render(DrawContext gui, int x, int y, boolean hover) {
        // Render with chest icon
        renderWithItem(gui, x, y, hover, new ItemStack(Items.CHEST));
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        return currentScreen instanceof InventoryScreen;
    }

    @Override
    public Text getTooltip() {
        return Text.translatable("container.inventory");
    }
}