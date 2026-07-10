package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.api.tabs_menu.TabBase;
import vodmordia.modtabs.api.tabs_menu.TabRenderer;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.config.CustomTabDefinition;
import vodmordia.modtabs.utils.DynamicTextureLoader;
import vodmordia.modtabs.utils.ItemUseSimulator;
import vodmordia.modtabs.utils.PatchouliIntegration;

/**
 * Tab implementation for custom tabs loaded from JSON configuration
 */
public class CustomJsonTab extends TabBase {

    private final CustomTabDefinition definition;
    private final ItemStack iconItem;
    private final ItemStack fallbackItem;
    private final ItemStack patchouliBookItem;
    private final ResourceLocation customTexture;
    private final boolean useCustomTexture;

    public CustomJsonTab(CustomTabDefinition definition) {
        this.definition = definition;
        this.iconItem = createItemStack(definition.icon.item);
        this.fallbackItem = createItemStack(definition.icon.fallbackItem);

        // If this is a Patchouli book, try to get the proper book ItemStack
        if (definition.icon.patchouliBook != null && !definition.icon.patchouliBook.trim().isEmpty()) {
            ResourceLocation bookId = PatchouliIntegration.parseBookId(definition.icon.patchouliBook);
            if (bookId != null && PatchouliIntegration.isPatchouliLoaded()) {
                this.patchouliBookItem = PatchouliIntegration.getPatchouliBookStack(bookId);
            } else {
                this.patchouliBookItem = ItemStack.EMPTY;
            }
        } else {
            this.patchouliBookItem = ItemStack.EMPTY;
        }

        // Load custom texture if specified
        ResourceLocation loadedTexture = null;
        boolean customTextureLoaded = false;

        // Try loading from customTexturePath first (file-based)
        if (definition.icon.customTexturePath != null && !definition.icon.customTexturePath.trim().isEmpty()) {
            loadedTexture = DynamicTextureLoader.loadTextureFromFile(
                definition.icon.customTexturePath,
                "custom_tab_" + definition.tabId
            );
            if (loadedTexture != null) {
                customTextureLoaded = true;
                ModTabs.LOGGER.info("Loaded custom texture from file for tab " + definition.tabId);
            }
        }

        // Try loading from customTexture (resource-based) if file-based failed
        if (!customTextureLoaded && definition.icon.customTexture != null && !definition.icon.customTexture.trim().isEmpty()) {
            try {
                loadedTexture = ResourceLocation.parse(definition.icon.customTexture);
                // Validate the texture exists
                if (DynamicTextureLoader.validateResourceTexture(loadedTexture)) {
                    customTextureLoaded = true;
                    ModTabs.LOGGER.info("Loaded custom texture from resources for tab " + definition.tabId);
                } else {
                    loadedTexture = null;
                }
            } catch (Exception e) {
                ModTabs.LOGGER.error("Invalid customTexture ResourceLocation for tab " + definition.tabId + ": " + e.getMessage());
                loadedTexture = null;
            }
        }

        this.customTexture = loadedTexture;
        this.useCustomTexture = customTextureLoaded;
    }

    private ItemStack createItemStack(String itemId) {
        if (itemId == null || itemId.trim().isEmpty()) {
            return ItemStack.EMPTY;
        }

        try {
            ResourceLocation itemLocation = ResourceLocation.parse(itemId);
            Item item = BuiltInRegistries.ITEM.get(itemLocation);

            if (item != null && item != Items.AIR) {
                return new ItemStack(item);
            } else {
                ModTabs.LOGGER.warn("Item not found for custom tab " + definition.tabId + ": " + itemId);
                return ItemStack.EMPTY;
            }
        } catch (Exception e) {
            ModTabs.LOGGER.error("Error creating item stack for " + itemId + " in tab " + definition.tabId + ": " + e.getMessage());
            return ItemStack.EMPTY;
        }
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!isEnabled(player)) {
            return;
        }

        try {
            boolean success = ItemUseSimulator.executeAction(definition, player);
            if (!success) {
                ModTabs.LOGGER.debug("Failed to execute action for custom tab: " + definition.tabId);
            }
        } catch (Exception e) {
            ModTabs.LOGGER.error("Error executing action for custom tab " + definition.tabId + ": " + e.getMessage());
        }
    }

    @Override
    public boolean isEnabled(Player player) {
        // Check if custom tabs are enabled globally
        if (!Config.Baked.customTabsEnabled) {
            return false;
        }

        // Check if this specific tab is enabled
        if (!definition.enabled) {
            return false;
        }

        // Check required mods
        if (definition.requiredMods != null && definition.requiredMods.length > 0) {
            for (String modId : definition.requiredMods) {
                if (!ModList.get().isLoaded(modId)) {
                    return false;
                }
            }
        }

        // If using custom texture, always return true (texture already validated)
        if (useCustomTexture) {
            return true;
        }

        // Otherwise check if the icon item exists (use fallback if main item doesn't exist)
        return !getCurrentIconItem().isEmpty();
    }

    @Override
    public void initTabOnScreens() {
        // Register this tab to appear on standard screens
        // Custom tabs will appear on inventory screen and other standard screens by default
        ScreenRegistry.registerStandardScreens(
            net.minecraft.client.gui.screens.inventory.InventoryScreen.class
        );
    }

    @Override
    public void render(GuiGraphics gui, int x, int y, boolean hover) {
        if (useCustomTexture && customTexture != null) {
            // Render with custom texture
            TabRenderer.builder()
                .withBackground()
                .withTextureIcon(customTexture, 5, 4, 16, 16)
                .render(gui, x, y, hover, false);
        } else {
            // Render with item icon
            ItemStack renderItem = getCurrentIconItem();
            float scale = definition.icon.scale;

            TabRenderer.builder()
                .withBackground()
                .withItemIcon(renderItem, 5, 4, scale)
                .render(gui, x, y, hover, false);
        }
    }

    @Override
    protected void renderInverted(GuiGraphics gui, int x, int y, boolean hover) {
        if (useCustomTexture && customTexture != null) {
            // Render with custom texture
            TabRenderer.builder()
                .withBackground()
                .withTextureIcon(customTexture, 5, 4, 16, 16)
                .render(gui, x, y, hover, true);
        } else {
            // Render with item icon
            ItemStack renderItem = getCurrentIconItem();
            float scale = definition.icon.scale;

            TabRenderer.builder()
                .withBackground()
                .withItemIcon(renderItem, 5, 4, scale)
                .render(gui, x, y, hover, true);
        }
    }

    /**
     * Get the current icon item, prioritizing Patchouli book, then main item, then fallback
     */
    private ItemStack getCurrentIconItem() {
        // Prioritize Patchouli book if available
        if (!patchouliBookItem.isEmpty()) {
            return patchouliBookItem;
        }

        // Use regular icon item if available
        if (!iconItem.isEmpty()) {
            return iconItem;
        }

        // Fall back to fallback item
        if (!fallbackItem.isEmpty()) {
            return fallbackItem;
        }

        // Ultimate fallback
        return new ItemStack(Items.BOOK);
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        // Custom tabs are generally not considered "currently used" unless
        // we can determine the specific screen they open
        // This prevents the tab from being highlighted when on unknown screens
        return false;
    }

    @Override
    public Component getTooltip() {
        if (definition.tooltip != null && !definition.tooltip.trim().isEmpty()) {
            return Component.literal(definition.tooltip);
        } else {
            return Component.literal("Custom Tab: " + definition.tabId);
        }
    }

    @Override
    public int getOverrideOrder() {
        return definition.order;
    }

    /**
     * Get the underlying tab definition
     */
    public CustomTabDefinition getDefinition() {
        return definition;
    }

    /**
     * Get the tab ID
     */
    public String getTabId() {
        return definition.tabId;
    }

    @Override
    public String toString() {
        return "CustomJsonTab{" +
                "tabId='" + definition.tabId + '\'' +
                ", enabled=" + definition.enabled +
                ", order=" + definition.order +
                ", useCustomTexture=" + useCustomTexture +
                '}';
    }
}
