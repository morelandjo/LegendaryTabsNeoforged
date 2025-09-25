package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
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
                    // Continue to reflection fallback
                }
            }

            // Fallback: Try to open atlas overview screen directly via reflection
            MinecraftClient minecraft = MinecraftClient.getInstance();
            Class<?> atlasScreenClass = Class.forName("pepjebs.mapatlases.client.screen.AtlasOverviewScreen");

            // This might require specific constructor parameters - for now, log the attempt
            vodmordia.modtabs.ModTabs.LOGGER.info("Trying to open Map Atlases screen via reflection");

        } catch (Exception e) {
            // Log error for debugging
            vodmordia.modtabs.ModTabs.LOGGER.warn("Failed to open Map Atlases screen: " + e.getMessage());
        }
    }

    @Override
    public boolean isEnabled(PlayerEntity player) {
        return ModIntegrationManager.isModLoaded(ModIntegration.MAP_ATLASES);
    }

    @Override
    public void initTabOnScreens() {
        if (!ModIntegrationManager.isModLoaded(ModIntegration.MAP_ATLASES)) return;

        TabsMenu.addPendingRegistration(() -> {
            // Register for common screens
            try {
                TabsMenu.registerScreenForTabs(net.minecraft.client.gui.screen.ingame.InventoryScreen.class, this);

                // Try to register for other common container screens
                String[] screenClasses = {
                    "net.minecraft.client.gui.screen.ingame.GenericContainerScreen",
                    "net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen",
                    "net.minecraft.client.gui.screen.ingame.ChestScreen"
                };

                for (String className : screenClasses) {
                    try {
                        Class<?> screenClass = Class.forName(className);
                        TabsMenu.registerScreenForTabs((Class<? extends Screen>) screenClass, this);
                    } catch (ClassNotFoundException e) {
                        // Screen class not found, continue
                    }
                }
            } catch (Exception e) {
                // Registration failed
            }
        });
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
            // Map Atlases uses a different registration pattern
            // Try to access the item directly from the mod class
            Class<?> modClass = Class.forName("pepjebs.mapatlases.MapAtlasesMod");

            // Try common field names for atlas items
            String[] possibleFields = {
                "MAP_ATLAS",
                "ATLAS_ITEM"
            };

            for (String fieldName : possibleFields) {
                try {
                    Object atlasSupplier = modClass.getField(fieldName).get(null);
                    if (atlasSupplier instanceof Item) {
                        return (Item) atlasSupplier;
                    }

                    // Try get() method for Supplier types
                    try {
                        java.lang.reflect.Method getMethod = atlasSupplier.getClass().getMethod("get");
                        Object result = getMethod.invoke(atlasSupplier);
                        if (result instanceof Item) {
                            return (Item) result;
                        }
                    } catch (Exception e) {
                        // Continue to next field
                    }
                } catch (Exception e) {
                    // Field not found, continue
                }
            }

            // Also try getting the item from registry lookup
            try {
                // Try to get the item via registry lookup
                Class<?> registryClass = Class.forName("net.minecraft.util.registry.Registry");
                java.lang.reflect.Method getMethod = registryClass.getMethod("get", net.minecraft.util.Identifier.class);
                Object registry = registryClass.getField("ITEM").get(null);

                net.minecraft.util.Identifier atlasId = new net.minecraft.util.Identifier("map_atlases", "atlas");
                Object result = getMethod.invoke(registry, atlasId);
                if (result instanceof Item) {
                    return (Item) result;
                }
            } catch (Exception e) {
                // Registry lookup failed
            }

        } catch (Exception e) {
            // Class not found or other error
        }

        return null;
    }
}
