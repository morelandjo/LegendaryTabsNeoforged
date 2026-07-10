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
        try {
            // Create BackpackOpenMessage
            Class<?> messageClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.network.BackpackOpenMessage");
            Object message = messageClass.getDeclaredConstructor().newInstance();

            // Use SBPPacketHandler.sendToServer()
            Class<?> packetHandlerClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.network.SBPPacketHandler");
            java.lang.reflect.Method sendToServerMethod = packetHandlerClass.getMethod("sendToServer", Object.class);
            sendToServerMethod.invoke(null, message);

        } catch (Exception e) {
            vodmordia.modtabs.ModTabs.LOGGER.error("Failed to open Sophisticated Backpacks: " + e.getMessage());
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
        // Register the backpack screen with tabs
        vodmordia.modtabs.api.tabs_menu.ScreenRegistry.builder()
            .withStandardDimensions()
            .withPositioning(vodmordia.modtabs.api.tabs_menu.TabPositioning.GUI_RELATIVE)
            .registerAllTabs("net.p3pp3rf1y.sophisticatedbackpacks.client.gui.BackpackScreen");
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
