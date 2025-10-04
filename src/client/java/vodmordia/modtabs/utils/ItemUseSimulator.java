package vodmordia.modtabs.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.config.CustomTabDefinition;

/**
 * Utility class for simulating item interactions without requiring the item to be in hand
 */
public class ItemUseSimulator {

    /**
     * Execute the action defined in a custom tab definition
     */
    public static boolean executeAction(CustomTabDefinition definition, PlayerEntity player) {
        if (definition.action == null) {
            return false;
        }

        switch (definition.action.type) {
            case "use_item":
                return simulateItemUse(definition.action.item, player);
            case "open_screen":
                return openScreenByClassName(definition.action.screenClass, player);
            case "run_command":
                return executeCommand(definition.action.command, player);
            case "keybind":
                return simulateKeybind(definition.action.keybind, player);
            case "open_patchouli_book":
                return openPatchouliBook(definition.action.bookId, player);
            default:
                ModTabs.LOGGER.warn("Unknown action type: " + definition.action.type);
                return false;
        }
    }

    /**
     * Simulate using an item by trying multiple strategies
     */
    public static boolean simulateItemUse(String itemId, PlayerEntity player) {
        if (itemId == null || itemId.trim().isEmpty()) {
            return false;
        }

        try {
            Identifier itemLocation = new Identifier(itemId);
            Item item = Registries.ITEM.get(itemLocation);

            if (item == null || item == Items.AIR) {
                ModTabs.LOGGER.warn("Item not found: " + itemId);
                return false;
            }

            ItemStack itemStack = new ItemStack(item);
            World world = player.getWorld();


            // Strategy 1: Try to call the item's use method directly
            if (tryDirectItemUse(itemStack, world, player)) {
                return true;
            }

            // Strategy 2: Try to simulate right-click interaction
            if (trySimulateRightClick(itemStack, player)) {
                return true;
            }

            return false;

        } catch (Exception e) {
            ModTabs.LOGGER.error("Error simulating item use for " + itemId + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Strategy 1: Try calling the item's use method directly
     */
    private static boolean tryDirectItemUse(ItemStack itemStack, World world, PlayerEntity player) {
        try {
            TypedActionResult<ItemStack> result = itemStack.getItem().use(world, player, Hand.MAIN_HAND);

            if (result.getResult() == ActionResult.SUCCESS ||
                result.getResult() == ActionResult.CONSUME ||
                result.getResult() == ActionResult.CONSUME_PARTIAL) {

                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * Strategy 2: Try to simulate right-click interaction using client
     */
    private static boolean trySimulateRightClick(ItemStack itemStack, PlayerEntity player) {
        try {
            MinecraftClient minecraft = MinecraftClient.getInstance();
            if (minecraft.interactionManager != null) {
                // Store original item in hand
                ItemStack originalMainHand = player.getMainHandStack();

                // Temporarily set the item in main hand
                player.getInventory().setStack(player.getInventory().selectedSlot, itemStack);

                // Try to use the item
                ActionResult result = minecraft.interactionManager.interactItem(player, Hand.MAIN_HAND);

                // Restore original item
                player.getInventory().setStack(player.getInventory().selectedSlot, originalMainHand);

                if (result == ActionResult.SUCCESS ||
                    result == ActionResult.CONSUME ||
                    result == ActionResult.CONSUME_PARTIAL) {

                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * Try to open a screen by its class name using reflection
     */
    public static boolean openScreenByClassName(String screenClassName, PlayerEntity player) {
        if (screenClassName == null || screenClassName.trim().isEmpty()) {
            return false;
        }

        try {
            Class<?> screenClass = Class.forName(screenClassName);

            // Try different constructor patterns commonly used by mod screens
            Screen screen = null;

            // Try no-arg constructor
            try {
                screen = (Screen) screenClass.getConstructor().newInstance();
            } catch (Exception e) {
                // Try constructor with player parameter
                try {
                    screen = (Screen) screenClass.getConstructor(PlayerEntity.class).newInstance(player);
                } catch (Exception e2) {
                }
            }

            if (screen != null) {
                MinecraftClient.getInstance().setScreen(screen);
                return true;
            }

        } catch (ClassNotFoundException e) {
            ModTabs.LOGGER.warn("Screen class not found: " + screenClassName);
        } catch (Exception e) {
            ModTabs.LOGGER.error("Error opening screen " + screenClassName + ": " + e.getMessage());
        }

        return false;
    }

    /**
     * Execute a client command
     */
    public static boolean executeCommand(String command, PlayerEntity player) {
        if (command == null || command.trim().isEmpty()) {
            return false;
        }

        try {
            MinecraftClient minecraft = MinecraftClient.getInstance();
            if (minecraft.player != null) {
                // Remove leading slash if present
                String cleanCommand = command.startsWith("/") ? command.substring(1) : command;

                minecraft.player.networkHandler.sendCommand(cleanCommand);
                return true;
            }
        } catch (Exception e) {
            ModTabs.LOGGER.error("Error executing command " + command + ": " + e.getMessage());
        }

        return false;
    }

    /**
     * Simulate a keybind press (basic implementation)
     */
    public static boolean simulateKeybind(String keybind, PlayerEntity player) {
        if (keybind == null || keybind.trim().isEmpty()) {
            return false;
        }

        try {
            MinecraftClient minecraft = MinecraftClient.getInstance();

            // This is a simplified implementation - in a full implementation,
            // you would map keybind names to actual KeyBinding objects and trigger them

            // For now, just return false to indicate it's not implemented
            return false;

        } catch (Exception e) {
            ModTabs.LOGGER.error("Error simulating keybind " + keybind + ": " + e.getMessage());
        }

        return false;
    }

    /**
     * Open a Patchouli book by its book ID, with fallback to item use
     */
    public static boolean openPatchouliBook(String bookIdString, PlayerEntity player) {
        if (bookIdString == null || bookIdString.trim().isEmpty()) {
            return false;
        }

        try {
            Identifier bookId = PatchouliIntegration.parseBookId(bookIdString);
            if (bookId == null) {
                ModTabs.LOGGER.warn("Invalid Patchouli book ID format: " + bookIdString);
                return false;
            }

            if (!PatchouliIntegration.isPatchouliLoaded()) {
                ModTabs.LOGGER.warn("Cannot open Patchouli book - Patchouli mod not loaded");
                return tryFallbackItemUse(bookId, player);
            }

            if (!PatchouliIntegration.isValidBookId(bookId)) {
                return tryFallbackItemUse(bookId, player);
            }

            boolean success = PatchouliIntegration.openBook(bookId, player);
            if (success) {
                return true;
            } else {
                return tryFallbackItemUse(bookId, player);
            }

        } catch (Exception e) {
            ModTabs.LOGGER.error("Error opening Patchouli book " + bookIdString + ": " + e.getMessage());
            return tryFallbackItemUse(PatchouliIntegration.parseBookId(bookIdString), player);
        }
    }

    /**
     * Fallback method to try using the book item directly when Patchouli API fails
     */
    private static boolean tryFallbackItemUse(Identifier bookId, PlayerEntity player) {
        if (bookId == null) {
            return false;
        }

        // For Ars Nouveau specifically, try the worn_notebook item
        if ("ars_nouveau".equals(bookId.getNamespace()) && "worn_notebook".equals(bookId.getPath())) {
            return simulateItemUse("ars_nouveau:worn_notebook", player);
        }

        // For other mods, try to use the book ID as an item ID
        // Many Patchouli books have the same ID for both the book registration and the item
        String itemId = bookId.toString();
        return simulateItemUse(itemId, player);
    }
}