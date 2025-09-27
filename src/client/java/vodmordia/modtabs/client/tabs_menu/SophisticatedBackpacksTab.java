package vodmordia.modtabs.client.tabs_menu;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import vodmordia.modtabs.api.tabs_menu.SimpleItemTab;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class SophisticatedBackpacksTab extends SimpleItemTab {

    public SophisticatedBackpacksTab() {
        super(() -> getBackpackItem());
    }

    @Override
    public void openTargetScreen(PlayerEntity player) {
        vodmordia.modtabs.ModTabs.LOGGER.info("SophisticatedBackpacksTab.openTargetScreen() called");

        // Debug: Try to find available classes
        String[] possiblePackageNames = {
            "net.p3pp3rf1y.sophisticatedbackpacks.network",
            "net.p3pp3rf1y.sophisticatedbackpacks.common.network",
            "net.p3pp3rf1y.sophisticatedbackpacks.client.network",
            "net.p3pp3rf1y.sophisticatedbackpacks.fabric.network"
        };

        String[] possibleClassNames = {
            "BackpackOpenPayload",
            "OpenBackpackPayload",
            "BackpackOpenMessage",
            "OpenBackpackMessage",
            "BackpackMessage"
        };

        vodmordia.modtabs.ModTabs.LOGGER.info("Debugging: Searching for Sophisticated Backpacks network classes...");

        Class<?> foundPayloadClass = null;
        String foundClassName = null;

        // Try to find the correct payload class
        for (String packageName : possiblePackageNames) {
            for (String className : possibleClassNames) {
                String fullClassName = packageName + "." + className;
                try {
                    Class<?> testClass = Class.forName(fullClassName);
                    vodmordia.modtabs.ModTabs.LOGGER.info("Found class: " + fullClassName);
                    if (foundPayloadClass == null) {
                        foundPayloadClass = testClass;
                        foundClassName = fullClassName;
                    }
                } catch (ClassNotFoundException e) {
                    // Continue searching
                }
            }
        }

        if (foundPayloadClass == null) {
            vodmordia.modtabs.ModTabs.LOGGER.warn("No Sophisticated Backpacks network payload class found!");
            return;
        }

        vodmordia.modtabs.ModTabs.LOGGER.info("Using payload class: " + foundClassName);

        try {
            // Try to find PacketDistributor or alternative networking
            Class<?> packetDistributorClass = null;
            String[] packetDistributorPackages = {
                "net.p3pp3rf1y.sophisticatedcore.network.PacketDistributor",
                "net.p3pp3rf1y.sophisticatedbackpacks.network.PacketDistributor",
                "net.p3pp3rf1y.sophisticatedcore.network.NetworkHandler",
                "net.p3pp3rf1y.sophisticatedbackpacks.network.NetworkHandler",
                "net.p3pp3rf1y.sophisticatedbackpacks.network.SBPNetworking"
            };

            for (String packetClassName : packetDistributorPackages) {
                try {
                    packetDistributorClass = Class.forName(packetClassName);
                    vodmordia.modtabs.ModTabs.LOGGER.info("Found networking class: " + packetClassName);
                    break;
                } catch (ClassNotFoundException e) {
                    vodmordia.modtabs.ModTabs.LOGGER.info("Class not found: " + packetClassName);
                }
            }

            if (packetDistributorClass == null) {
                vodmordia.modtabs.ModTabs.LOGGER.warn("No networking class found! Trying Fabric networking directly...");

                // Try direct Fabric networking as fallback
                try {
                    // Use Fabric's ClientPlayNetworking directly
                    ClientPlayNetworking.send(
                        Identifier.of("sophisticatedbackpacks", "open_backpack"),
                        new PacketByteBuf(io.netty.buffer.Unpooled.buffer())
                    );
                    vodmordia.modtabs.ModTabs.LOGGER.info("Sent via direct Fabric networking");
                    return;
                } catch (Exception e) {
                    vodmordia.modtabs.ModTabs.LOGGER.warn("Direct Fabric networking failed: " + e.getMessage());
                }
                return;
            }

            // Try different constructor patterns
            Object payload = null;

            // Pattern 1: No-args constructor
            try {
                payload = foundPayloadClass.getDeclaredConstructor().newInstance();
                vodmordia.modtabs.ModTabs.LOGGER.info("Created payload with no-args constructor");
            } catch (Exception e) {
                vodmordia.modtabs.ModTabs.LOGGER.info("No-args constructor failed: " + e.getMessage());
            }

            // Pattern 2: Three-args constructor (int, String, String)
            if (payload == null) {
                try {
                    payload = foundPayloadClass.getDeclaredConstructor(int.class, String.class, String.class).newInstance(-1, "", "");
                    vodmordia.modtabs.ModTabs.LOGGER.info("Created payload with (int, String, String) constructor");
                } catch (Exception e) {
                    vodmordia.modtabs.ModTabs.LOGGER.info("Three-args constructor failed: " + e.getMessage());
                }
            }

            // Pattern 3: Single int constructor
            if (payload == null) {
                try {
                    payload = foundPayloadClass.getDeclaredConstructor(int.class).newInstance(-1);
                    vodmordia.modtabs.ModTabs.LOGGER.info("Created payload with (int) constructor");
                } catch (Exception e) {
                    vodmordia.modtabs.ModTabs.LOGGER.info("Single int constructor failed: " + e.getMessage());
                }
            }

            if (payload == null) {
                vodmordia.modtabs.ModTabs.LOGGER.warn("Could not create payload instance with any constructor pattern!");
                return;
            }

            // Send it via PacketDistributor.sendToServer()
            Method sendToServerMethod = packetDistributorClass.getMethod("sendToServer", Object.class);
            sendToServerMethod.invoke(null, payload);

            vodmordia.modtabs.ModTabs.LOGGER.info("Successfully sent payload to server");
            return;

        } catch (Exception e) {
            vodmordia.modtabs.ModTabs.LOGGER.warn("Failed to send payload: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Find the slot number where the backpack is located
     */
    private int findBackpackSlot(PlayerEntity player) {
        try {
            Class<?> backpackItemClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem");

            // Check main inventory (slots 0-35 in the main inventory)
            for (int i = 0; i < player.getInventory().main.size(); i++) {
                ItemStack stack = player.getInventory().main.get(i);
                if (!stack.isEmpty() && backpackItemClass.isInstance(stack.getItem())) {
                    return i;
                }
            }

            // Check armor slots (slots 36-39)
            for (int i = 0; i < player.getInventory().armor.size(); i++) {
                ItemStack stack = player.getInventory().armor.get(i);
                if (!stack.isEmpty() && backpackItemClass.isInstance(stack.getItem())) {
                    return 36 + i; // Armor slots start at 36
                }
            }

            // Check offhand (slot 40)
            ItemStack offhandStack = player.getOffHandStack();
            if (!offhandStack.isEmpty() && backpackItemClass.isInstance(offhandStack.getItem())) {
                return 40;
            }

        } catch (Exception e) {
            // If reflection fails, return -1
        }

        return -1; // No backpack found
    }

    /**
     * Find the first backpack in the player's inventory
     */
    private ItemStack findPlayerBackpack(PlayerEntity player) {
        try {
            Class<?> backpackItemClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem");

            // Check main inventory
            for (ItemStack stack : player.getInventory().main) {
                if (!stack.isEmpty() && backpackItemClass.isInstance(stack.getItem())) {
                    return stack;
                }
            }

            // Check armor slots
            for (ItemStack stack : player.getInventory().armor) {
                if (!stack.isEmpty() && backpackItemClass.isInstance(stack.getItem())) {
                    return stack;
                }
            }

            // Check offhand
            ItemStack offhandStack = player.getOffHandStack();
            if (!offhandStack.isEmpty() && backpackItemClass.isInstance(offhandStack.getItem())) {
                return offhandStack;
            }

        } catch (Exception e) {
            // If reflection fails, return null
        }

        return null;
    }

    @Override
    public boolean isEnabled(PlayerEntity player) {
        return ModIntegrationManager.isModLoaded(ModIntegration.SOPHISTICATED_BACKPACKS) && hasBackpack(player);
    }

    private boolean hasBackpack(PlayerEntity player) {
        try {
            Class<?> backpackItemClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem");

            // Check main inventory (using correct Fabric method)
            for (ItemStack stack : player.getInventory().main) {
                if (!stack.isEmpty() && backpackItemClass.isInstance(stack.getItem())) {
                    return true;
                }
            }

            // Check armor slots
            for (ItemStack stack : player.getInventory().armor) {
                if (!stack.isEmpty() && backpackItemClass.isInstance(stack.getItem())) {
                    return true;
                }
            }

            // Check offhand
            ItemStack offhandStack = player.getOffHandStack();
            if (!offhandStack.isEmpty() && backpackItemClass.isInstance(offhandStack.getItem())) {
                return true;
            }

        } catch (Exception e) {
            // If reflection fails, return false
        }

        return false;
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
    public boolean isCurrentlyUsed(Screen currentScreen) {
        try {
            Class<?> screenClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.client.gui.BackpackScreen");
            return screenClass.isInstance(currentScreen);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public Text getTooltip() {
        return Text.literal("Sophisticated Backpacks");
    }

    /**
     * Try to get the backpack item using reflection
     * Handles both NeoForge (supplier pattern) and Fabric (direct item pattern)
     */
    private static ItemStack getBackpackItem() {
        try {
            Class<?> itemsClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems");
            Field backpackField = itemsClass.getField("BACKPACK");
            Object backpackFieldValue = backpackField.get(null);

            // Check if this is a direct Item (Fabric pattern)
            if (backpackFieldValue instanceof Item) {
                return new ItemStack((Item) backpackFieldValue);
            }

            // Otherwise try supplier pattern (NeoForge pattern)
            try {
                Method getMethod = backpackFieldValue.getClass().getMethod("get");
                Item backpackItem = (Item) getMethod.invoke(backpackFieldValue);
                return new ItemStack(backpackItem);
            } catch (NoSuchMethodException e) {
                // If it's not a supplier and not an Item, log for debugging
                vodmordia.modtabs.ModTabs.LOGGER.warn("BACKPACK field is neither Item nor Supplier: " + backpackFieldValue.getClass().getName());
                return new ItemStack(Items.BUNDLE);
            }

        } catch (Exception e) {
            // Log the actual error for debugging
            vodmordia.modtabs.ModTabs.LOGGER.warn("Failed to get Sophisticated Backpacks item via reflection: " + e.getMessage());
            return new ItemStack(Items.BUNDLE);
        }
    }
}
