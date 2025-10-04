package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.api.tabs_menu.TabBase;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

public class MapAtlasesTab extends TabBase {

    @Override
    public void openTargetScreen(PlayerEntity player) {

        try {
            // Try to get and use a map atlas item
            Item atlasItem = getAtlasItem();

            if (atlasItem != null) {
                try {
                    ItemStack atlasStack = new ItemStack(atlasItem);
                    // Use item interaction to open the atlas screen
                    atlasStack.getItem().use(player.getWorld(), player, net.minecraft.util.Hand.MAIN_HAND);
                    return;
                } catch (Exception ex) {
                }
            }

            // Fallback: Try to open atlas overview screen directly via reflection
            try {
                MinecraftClient minecraft = MinecraftClient.getInstance();
                Class<?> atlasScreenClass = Class.forName("pepjebs.mapatlases.client.screen.AtlasOverviewScreen");

                // Try different constructor approaches
                try {
                    Object atlasScreen = atlasScreenClass.getConstructor().newInstance();
                    minecraft.setScreen((net.minecraft.client.gui.screen.Screen) atlasScreen);
                    return;
                } catch (Exception e1) {
                }

            } catch (Exception e) {
            }

        } catch (Exception e) {
            // Log error for debugging
            vodmordia.modtabs.ModTabs.LOGGER.warn("Failed to open Map Atlases screen: " + e.getMessage());
        }

    }

    @Override
    public boolean isEnabled(PlayerEntity player) {
        return ModIntegrationManager.isModLoaded(ModIntegration.MAP_ATLASES) && hasMapAtlas(player);
    }

    private boolean hasMapAtlas(PlayerEntity player) {
        try {
            // Try Map Atlases access utility method first
            Class<?> mapAtlasesAccessUtilsClass = Class.forName("pepjebs.mapatlases.utils.MapAtlasesAccessUtils");
            java.lang.reflect.Method getAtlasMethod = mapAtlasesAccessUtilsClass.getMethod("getAtlasFromPlayerByConfig", net.minecraft.entity.player.PlayerEntity.class);
            net.minecraft.item.ItemStack atlas = (net.minecraft.item.ItemStack) getAtlasMethod.invoke(null, player);

            Class<?> mapAtlasItemClass = Class.forName("pepjebs.mapatlases.item.MapAtlasItem");
            return mapAtlasItemClass.isInstance(atlas.getItem());
        } catch (Exception e) {
            // Fallback: Check player inventory manually
            try {
                Class<?> mapAtlasItemClass = Class.forName("pepjebs.mapatlases.item.MapAtlasItem");

                // Check main inventory
                for (net.minecraft.item.ItemStack stack : player.getInventory().main) {
                    if (!stack.isEmpty() && mapAtlasItemClass.isInstance(stack.getItem())) {
                        return true;
                    }
                }

                // Check offhand
                if (!player.getOffHandStack().isEmpty() && mapAtlasItemClass.isInstance(player.getOffHandStack().getItem())) {
                    return true;
                }

            } catch (Exception ex) {
                // If reflection fails completely, return false
            }
        }

        return false;
    }

    @Override
    public void initTabOnScreens() {
        // Register Map Atlases screen with inverted tabs at the top
        vodmordia.modtabs.api.tabs_menu.ScreenRegistry.registerInvertedScreens("pepjebs.mapatlases.client.screen.AtlasOverviewScreen");
    }

    @Override
    public void render(DrawContext gui, int x, int y, boolean hover) {
        // Try to get the actual atlas item for rendering
        Item atlasItem = getAtlasItem();
        if (atlasItem != null) {
            renderWithItem(gui, x, y, hover, new ItemStack(atlasItem));
        } else {
            // Fallback to filled map
            renderWithItem(gui, x, y, hover, new ItemStack(Items.FILLED_MAP));
        }
    }

    @Override
    protected void renderInverted(DrawContext gui, int x, int y, boolean hover) {
        // Try to get the actual atlas item for rendering
        Item atlasItem = getAtlasItem();
        ItemStack stack = atlasItem != null ? new ItemStack(atlasItem) : new ItemStack(Items.FILLED_MAP);

        vodmordia.modtabs.api.tabs_menu.TabRenderer.builder()
            .withBackground()
            .withItemIcon(stack, 5, 3)
            .render(gui, x, y, hover, true);
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        if (currentScreen == null) return false;

        // Check if current screen is a Map Atlases screen
        String screenName = currentScreen.getClass().getName();
        return screenName.contains("mapatlases") && screenName.contains("screen");
    }

    @Override
    public Text getTooltip() {
        return Text.literal("Map Atlases");
    }

    /**
     * Try to get the atlas item using reflection
     */
    private Item getAtlasItem() {
        try {
            // Map Atlases stores the item as a Supplier<MapAtlasItem>
            Class<?> modClass = Class.forName("pepjebs.mapatlases.MapAtlasesMod");
            Object atlasSupplier = modClass.getField("MAP_ATLAS").get(null);

            // Call get() on the Supplier to get the actual item
            java.lang.reflect.Method getMethod = atlasSupplier.getClass().getMethod("get");
            Object result = getMethod.invoke(atlasSupplier);

            if (result instanceof Item) {
                return (Item) result;
            }
        } catch (Exception e) {
            // Reflection failed, try fallback
        }

        return null;
    }
}
