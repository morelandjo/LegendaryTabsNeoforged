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

public class TravelersBackpackTab extends TabBase {

    @Override
    public void openTargetScreen(PlayerEntity player) {
        vodmordia.modtabs.ModTabs.LOGGER.info("TravelersBackpackTab.openTargetScreen called");
        try {
            // Try to get and use a traveler's backpack item
            Item backpackItem = getBackpackItem();
            vodmordia.modtabs.ModTabs.LOGGER.info("Got backpack item: {}", backpackItem != null ? backpackItem.toString() : "null");
            if (backpackItem != null) {
                try {
                    ItemStack backpackStack = new ItemStack(backpackItem);
                    // Use item interaction to open the backpack screen
                    vodmordia.modtabs.ModTabs.LOGGER.info("Attempting to use backpack item");
                    backpackStack.getItem().use(player.getWorld(), player, net.minecraft.util.Hand.MAIN_HAND);
                    return;
                } catch (Exception ex) {
                    vodmordia.modtabs.ModTabs.LOGGER.warn("Failed to use backpack item: " + ex.getMessage());
                    // Continue to reflection fallback
                }
            }

            // Fallback: Try to open backpack screen directly via reflection
            MinecraftClient minecraft = MinecraftClient.getInstance();
            Class<?> backpackScreenClass = Class.forName("com.tiviacz.travelersbackpack.client.screens.BackpackScreen");

            // This might require specific constructor parameters - for now, log the attempt
            vodmordia.modtabs.ModTabs.LOGGER.info("Trying to open Traveler's Backpack screen via reflection");

        } catch (Exception e) {
            // Log error for debugging
            vodmordia.modtabs.ModTabs.LOGGER.warn("Failed to open Traveler's Backpack screen: " + e.getMessage());
        }
    }

    @Override
    public boolean isEnabled(PlayerEntity player) {
        return ModIntegrationManager.isModLoaded(ModIntegration.TRAVELERS_BACKPACK) && hasBackpack(player);
    }

    private boolean hasBackpack(PlayerEntity player) {
        try {
            Class<?> backpackItemClass = Class.forName("com.tiviacz.travelersbackpack.items.TravelersBackpackItem");

            // Check main inventory
            for (net.minecraft.item.ItemStack stack : player.getInventory().main) {
                if (!stack.isEmpty() && backpackItemClass.isInstance(stack.getItem())) {
                    return true;
                }
            }

            // Check armor slots
            for (net.minecraft.item.ItemStack stack : player.getInventory().armor) {
                if (!stack.isEmpty() && backpackItemClass.isInstance(stack.getItem())) {
                    return true;
                }
            }

            // Fallback to original AttachmentUtils check
            try {
                Class<?> attachmentUtilsClass = Class.forName("com.tiviacz.travelersbackpack.capability.AttachmentUtils");
                java.lang.reflect.Method isWearingBackpackMethod = attachmentUtilsClass.getMethod("isWearingBackpack", net.minecraft.entity.player.PlayerEntity.class);
                return (Boolean) isWearingBackpackMethod.invoke(null, player);
            } catch (Exception ex) {
                return false;
            }
        } catch (Exception e) {
            // If reflection fails, fall back to original method
            try {
                Class<?> attachmentUtilsClass = Class.forName("com.tiviacz.travelersbackpack.capability.AttachmentUtils");
                java.lang.reflect.Method isWearingBackpackMethod = attachmentUtilsClass.getMethod("isWearingBackpack", net.minecraft.entity.player.PlayerEntity.class);
                return (Boolean) isWearingBackpackMethod.invoke(null, player);
            } catch (Exception ex) {
                return false;
            }
        }
    }

    @Override
    public void initTabOnScreens() {
        if (!ModIntegrationManager.isModLoaded(ModIntegration.TRAVELERS_BACKPACK)) return;

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
        // Try to get the actual backpack item for rendering
        Item backpackItem = getBackpackItem();
        if (backpackItem != null) {
            renderWithItem(gui, x, y, hover, new ItemStack(backpackItem));
        } else {
            vodmordia.modtabs.ModTabs.LOGGER.warn("TravelersBackpackTab using fallback leather chestplate - backpack item not found");
            // Fallback to leather backpack texture
            renderWithItem(gui, x, y, hover, new ItemStack(Items.LEATHER_CHESTPLATE));
        }
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        if (currentScreen == null) return false;

        // Check if current screen is a Traveler's Backpack screen
        String screenName = currentScreen.getClass().getName();
        return screenName.contains("travelersbackpack") && screenName.contains("screens");
    }

    @Override
    public Text getTooltip() {
        return Text.literal("Traveler's Backpack");
    }

    /**
     * Try to get the backpack item using reflection
     */
    private Item getBackpackItem() {
        try {
            Class<?> modItemsClass = Class.forName("com.tiviacz.travelersbackpack.init.ModItems");

            // Try common field names for backpack items
            String[] possibleFields = {
                "STANDARD_TRAVELERS_BACKPACK",
                "TRAVELERS_BACKPACK",
                "BACKPACK"
            };

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
            // Class not found or other error
        }

        return null;
    }
}
