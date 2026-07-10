package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import vodmordia.modtabs.api.tabs_menu.TabBase;
import vodmordia.modtabs.config.CustomTabDefinition;

public class CustomJsonTab extends TabBase {

    private final CustomTabDefinition definition;

    public CustomJsonTab(CustomTabDefinition definition) {
        this.definition = definition;
    }

    @Override
    public void openTargetScreen(PlayerEntity player) {
        if (definition == null || definition.action == null) return;

        try {
            vodmordia.modtabs.utils.ItemUseSimulator.executeAction(definition, player);
        } catch (Exception e) {
            vodmordia.modtabs.ModTabs.LOGGER.error("Failed to execute custom tab action for {}: {}",
                definition.tabId, e.getMessage());
        }
    }

    @Override
    public boolean isEnabled(PlayerEntity player) {
        if (definition == null || !definition.enabled) return false;

        // Check if required mods are loaded
        if (definition.requiredMods != null && definition.requiredMods.length > 0) {
            for (String modId : definition.requiredMods) {
                if (!vodmordia.modtabs.integration.ModIntegrationManager.isModLoaded(modId)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void initTabOnScreens() {
        if (!isEnabled(null)) return;

        vodmordia.modtabs.api.tabs_menu.TabsMenu.addPendingRegistration(() -> {
            try {
                // Register for inventory screen by default
                vodmordia.modtabs.api.tabs_menu.TabsMenu.registerScreenForTabs(
                    net.minecraft.client.gui.screen.ingame.InventoryScreen.class, this);
            } catch (Exception e) {
                // Registration failed
            }
        });
    }

    @Override
    public void render(DrawContext gui, int x, int y, boolean hover) {
        if (definition == null || definition.icon == null) {
            // Fallback rendering
            renderWithItem(gui, x, y, hover, new net.minecraft.item.ItemStack(net.minecraft.item.Items.BOOK));
            return;
        }

        try {
            // Try to render with the specified item
            String itemId = definition.icon.item != null ? definition.icon.item : definition.icon.fallbackItem;
            if (itemId != null) {
                net.minecraft.item.Item item = net.minecraft.registry.Registries.ITEM.get(new net.minecraft.util.Identifier(itemId));
                if (item != null && item != net.minecraft.item.Items.AIR) {
                    renderWithItem(gui, x, y, hover, new net.minecraft.item.ItemStack(item));
                    return;
                }
            }

            // Try Patchouli book rendering if specified
            if (definition.icon.patchouliBook != null) {
                net.minecraft.util.Identifier bookId = vodmordia.modtabs.utils.PatchouliIntegration.parseBookId(definition.icon.patchouliBook);
                if (bookId != null) {
                    net.minecraft.item.ItemStack bookStack = vodmordia.modtabs.utils.PatchouliIntegration.getPatchouliBookStack(bookId);
                    if (!bookStack.isEmpty()) {
                        renderWithItem(gui, x, y, hover, bookStack);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            // Error in custom rendering, fall back
        }

        // Final fallback
        renderWithItem(gui, x, y, hover, new net.minecraft.item.ItemStack(net.minecraft.item.Items.BOOK));
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        return false; // TODO: Implement current screen detection
    }

    @Override
    public Text getTooltip() {
        return definition != null ? Text.literal(definition.tooltip) : Text.literal("Custom Tab");
    }
}
