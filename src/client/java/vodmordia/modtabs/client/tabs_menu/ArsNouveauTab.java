package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import vodmordia.modtabs.api.tabs_menu.TabBase;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;
import vodmordia.modtabs.utils.ItemUseSimulator;

public class ArsNouveauTab extends TabBase {

    @Override
    public void openTargetScreen(PlayerEntity player) {
        // Try to use the worn notebook item to open the Ars Nouveau book
        ItemUseSimulator.simulateItemUse("ars_nouveau:worn_notebook", player);
    }

    @Override
    public boolean isEnabled(PlayerEntity player) {
        return ModIntegrationManager.isModLoaded(ModIntegration.ARS_NOUVEAU);
    }

    @Override
    public void initTabOnScreens() {
        if (!ModIntegrationManager.isModLoaded(ModIntegration.ARS_NOUVEAU)) return;

        TabsMenu.addPendingRegistration(() -> {
            try {
                TabsMenu.registerScreenForTabs(net.minecraft.client.gui.screen.ingame.InventoryScreen.class, this);
            } catch (Exception e) {
                // Registration failed
            }
        });
    }

    @Override
    public void render(DrawContext gui, int x, int y, boolean hover) {
        // Try to get the worn notebook item for rendering
        try {
            ItemStack wornNotebook = new ItemStack(Registries.ITEM.get(new Identifier("ars_nouveau", "worn_notebook")));
            if (!wornNotebook.isEmpty() && wornNotebook.getItem() != Items.AIR) {
                renderWithItem(gui, x, y, hover, wornNotebook);
                return;
            }
        } catch (Exception e) {
            // Item not found
        }

        // Fallback to enchanted book
        renderWithItem(gui, x, y, hover, new ItemStack(Items.ENCHANTED_BOOK));
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        if (currentScreen == null) return false;
        String screenName = currentScreen.getClass().getName();
        return screenName.contains("ars_nouveau") || screenName.contains("com.hollingsworth.arsnouveau");
    }

    @Override
    public Text getTooltip() {
        return Text.literal("Ars Nouveau");
    }
}