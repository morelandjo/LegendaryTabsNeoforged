package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.SimpleTextureTab;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;
import vodmordia.modtabs.config.Config;

public class InventoryTab extends SimpleTextureTab {
    private static final Identifier INVENTORY_ICON = new Identifier(ModTabs.MOD_ID, "textures/gui/inventory.png");

    public InventoryTab() {
        super(INVENTORY_ICON);
    }

    @Override
    public void openTargetScreen(PlayerEntity player) {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        minecraft.setScreen(new InventoryScreen(player));
    }

    @Override
    public boolean isEnabled(PlayerEntity player) {
        return Config.Baked.inventoryTabEnabled;
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
    public boolean isCurrentlyUsed(Screen currentScreen) {
        return currentScreen instanceof InventoryScreen;
    }

    @Override
    public Text getTooltip() {
        return Text.translatable("tooltip." + ModTabs.MOD_ID + ".tab.inventory.description");
    }
}