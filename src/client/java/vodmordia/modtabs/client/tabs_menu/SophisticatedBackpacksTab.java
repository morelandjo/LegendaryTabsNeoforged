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

public class SophisticatedBackpacksTab extends TabBase {

    @Override
    public void openTargetScreen(PlayerEntity player) {
        try {
            // Try to get and use a sophisticated backpack item
            Item backpackItem = getBackpackItem();
            if (backpackItem != null) {
                try {
                    ItemStack backpackStack = new ItemStack(backpackItem);
                    // Use item interaction to open the backpack screen
                    backpackStack.getItem().use(player.getWorld(), player, net.minecraft.util.Hand.MAIN_HAND);
                    return;
                } catch (Exception ex) {
                    // Continue to reflection fallback
                }
            }

            // Fallback: Try to open backpack screen directly via reflection
            MinecraftClient minecraft = MinecraftClient.getInstance();
            Class<?> backpackScreenClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.client.gui.BackpackScreen");

            // This might require specific constructor parameters - for now, log the attempt
            vodmordia.modtabs.ModTabs.LOGGER.info("Trying to open Sophisticated Backpacks screen via reflection");

        } catch (Exception e) {
            // Log error for debugging
            vodmordia.modtabs.ModTabs.LOGGER.warn("Failed to open Sophisticated Backpacks screen: " + e.getMessage());
        }
    }

    @Override
    public boolean isEnabled(PlayerEntity player) {
        return ModIntegrationManager.isModLoaded(ModIntegration.SOPHISTICATED_BACKPACKS);
    }

    @Override
    public void initTabOnScreens() {
        if (!ModIntegrationManager.isModLoaded(ModIntegration.SOPHISTICATED_BACKPACKS)) return;

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
        // Try to get the actual sophisticated backpack item for rendering
        Item backpackItem = getBackpackItem();
        if (backpackItem != null) {
            renderWithItem(gui, x, y, hover, new ItemStack(backpackItem));
        } else {
            // Fallback to bundle (closest vanilla equivalent)
            renderWithItem(gui, x, y, hover, new ItemStack(Items.BUNDLE));
        }
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        if (currentScreen == null) return false;

        // Check if current screen is a Sophisticated Backpacks screen
        String screenName = currentScreen.getClass().getName();
        return screenName.contains("sophisticatedbackpacks") && screenName.contains("gui");
    }

    @Override
    public Text getTooltip() {
        return Text.literal("Sophisticated Backpacks");
    }

    /**
     * Try to get the backpack item using reflection
     */
    private Item getBackpackItem() {
        try {
            // Try to access the item from the mod's items class
            String[] possibleClasses = {
                "net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems",
                "net.p3pp3rf1y.sophisticatedbackpacks.items.ModItems",
                "net.p3pp3rf1y.sophisticatedbackpacks.SophisticatedBackpacks"
            };

            String[] possibleFields = {
                "BACKPACK",
                "SOPHISTICATED_BACKPACK",
                "LEATHER_BACKPACK"
            };

            for (String className : possibleClasses) {
                try {
                    Class<?> modItemsClass = Class.forName(className);

                    for (String fieldName : possibleFields) {
                        try {
                            Object backpackSupplier = modItemsClass.getField(fieldName).get(null);
                            if (backpackSupplier instanceof Item) {
                                return (Item) backpackSupplier;
                            }

                            // Try get() method for Supplier types
                            try {
                                java.lang.reflect.Method getMethod = backpackSupplier.getClass().getMethod("get");
                                Object result = getMethod.invoke(backpackSupplier);
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
                } catch (Exception e) {
                    // Class not found, continue
                }
            }
        } catch (Exception e) {
            // Error in reflection
        }

        return null;
    }
}
