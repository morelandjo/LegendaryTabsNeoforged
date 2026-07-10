package vodmordia.modtabs.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.config.CustomTabDefinition;

/**
 * Utility class for simulating item interactions without requiring the item to be in hand
 */
public class ItemUseSimulator {

    /**
     * Execute the action defined in a custom tab definition
     */
    public static boolean executeAction(CustomTabDefinition definition, Player player) {
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
    public static boolean simulateItemUse(String itemId, Player player) {
        if (itemId == null || itemId.trim().isEmpty()) {
            return false;
        }

        try {
            ResourceLocation itemLocation = ResourceLocation.parse(itemId);
            Item item = BuiltInRegistries.ITEM.get(itemLocation);

            if (item == null || item == net.minecraft.world.item.Items.AIR) {
                ModTabs.LOGGER.warn("Item not found: " + itemId);
                return false;
            }

            ItemStack itemStack = new ItemStack(item);
            Level level = player.level();

            ModTabs.LOGGER.debug("Attempting to simulate use of item: " + itemId);

            // Strategy 1: Try to call the item's use method directly
            if (tryDirectItemUse(itemStack, level, player)) {
                return true;
            }

            // Strategy 2: Try to fire player interact event
            if (tryPlayerInteractEvent(itemStack, player)) {
                return true;
            }

            // Strategy 3: Try to simulate right-click interaction
            if (trySimulateRightClick(itemStack, player)) {
                return true;
            }

            ModTabs.LOGGER.debug("All item use simulation strategies failed for: " + itemId);
            return false;

        } catch (Exception e) {
            ModTabs.LOGGER.error("Error simulating item use for " + itemId + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Strategy 1: Try calling the item's use method directly
     */
    private static boolean tryDirectItemUse(ItemStack itemStack, Level level, Player player) {
        try {
            InteractionResultHolder<ItemStack> result = itemStack.getItem().use(level, player, InteractionHand.MAIN_HAND);

            if (result.getResult() == InteractionResult.SUCCESS ||
                result.getResult() == InteractionResult.CONSUME ||
                result.getResult() == InteractionResult.CONSUME_PARTIAL) {

                ModTabs.LOGGER.debug("Direct item use succeeded with result: " + result.getResult());
                return true;
            }
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Direct item use failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Strategy 2: Try firing a player interact event
     */
    private static boolean tryPlayerInteractEvent(ItemStack itemStack, Player player) {
        try {
            PlayerInteractEvent.RightClickItem event = new PlayerInteractEvent.RightClickItem(player, InteractionHand.MAIN_HAND);
            NeoForge.EVENT_BUS.post(event);

            if (!event.isCanceled()) {
                ModTabs.LOGGER.debug("Player interact event succeeded");
                return true;
            }
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Player interact event failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Strategy 3: Try to simulate right-click interaction using client hooks
     */
    private static boolean trySimulateRightClick(ItemStack itemStack, Player player) {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.gameMode != null) {
                // Store original item in hand
                ItemStack originalMainHand = player.getMainHandItem();

                // Temporarily set the item in main hand
                player.getInventory().items.set(player.getInventory().selected, itemStack);

                // Try to use the item
                InteractionResult result = minecraft.gameMode.useItem(player, InteractionHand.MAIN_HAND);

                // Restore original item
                player.getInventory().items.set(player.getInventory().selected, originalMainHand);

                if (result == InteractionResult.SUCCESS ||
                    result == InteractionResult.CONSUME ||
                    result == InteractionResult.CONSUME_PARTIAL) {

                    ModTabs.LOGGER.debug("Simulated right-click succeeded with result: " + result);
                    return true;
                }
            }
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Simulated right-click failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Try to open a screen by its class name using reflection
     */
    public static boolean openScreenByClassName(String screenClassName, Player player) {
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
                    screen = (Screen) screenClass.getConstructor(Player.class).newInstance(player);
                } catch (Exception e2) {
                    ModTabs.LOGGER.debug("Could not create screen with standard constructors: " + screenClassName);
                }
            }

            if (screen != null) {
                Minecraft.getInstance().setScreen(screen);
                ModTabs.LOGGER.debug("Successfully opened screen: " + screenClassName);
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
    public static boolean executeCommand(String command, Player player) {
        if (command == null || command.trim().isEmpty()) {
            return false;
        }

        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                // Remove leading slash if present
                String cleanCommand = command.startsWith("/") ? command.substring(1) : command;

                minecraft.player.connection.sendCommand(cleanCommand);
                ModTabs.LOGGER.debug("Executed command: " + cleanCommand);
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
    public static boolean simulateKeybind(String keybind, Player player) {
        if (keybind == null || keybind.trim().isEmpty()) {
            return false;
        }

        try {
            Minecraft minecraft = Minecraft.getInstance();

            // This is a simplified implementation - in a full implementation,
            // you would map keybind names to actual KeyMapping objects and trigger them
            ModTabs.LOGGER.debug("Keybind simulation not fully implemented: " + keybind);

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
    public static boolean openPatchouliBook(String bookIdString, Player player) {
        if (bookIdString == null || bookIdString.trim().isEmpty()) {
            return false;
        }

        try {
            ResourceLocation bookId = PatchouliIntegration.parseBookId(bookIdString);
            if (bookId == null) {
                ModTabs.LOGGER.warn("Invalid Patchouli book ID format: " + bookIdString);
                return false;
            }

            if (!PatchouliIntegration.isPatchouliLoaded()) {
                ModTabs.LOGGER.warn("Cannot open Patchouli book - Patchouli mod not loaded");
                return tryFallbackItemUse(bookId, player);
            }

            if (!PatchouliIntegration.isValidBookId(bookId)) {
                if (vodmordia.modtabs.config.Config.Baked.customTabsDebugLogging) {
                    ModTabs.LOGGER.info("Patchouli book not found in registry: " + bookId + ", using fallback item simulation");
                }
                return tryFallbackItemUse(bookId, player);
            }

            boolean success = PatchouliIntegration.openBook(bookId, player);
            if (success) {
                ModTabs.LOGGER.debug("Successfully opened Patchouli book: " + bookId);
                return true;
            } else {
                ModTabs.LOGGER.debug("Patchouli API failed, trying fallback item use");
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
    private static boolean tryFallbackItemUse(ResourceLocation bookId, Player player) {
        if (bookId == null) {
            return false;
        }

        // For Ars Nouveau specifically, try the worn_notebook item
        if ("ars_nouveau".equals(bookId.getNamespace()) && "worn_notebook".equals(bookId.getPath())) {
            if (vodmordia.modtabs.config.Config.Baked.customTabsDebugLogging) {
                ModTabs.LOGGER.info("Using fallback item simulation for Ars Nouveau worn notebook");
            }
            return simulateItemUse("ars_nouveau:worn_notebook", player);
        }

        // For other mods, try to use the book ID as an item ID
        // Many Patchouli books have the same ID for both the book registration and the item
        String itemId = bookId.toString();
        if (vodmordia.modtabs.config.Config.Baked.customTabsDebugLogging) {
            ModTabs.LOGGER.info("Using fallback item simulation with ID: " + itemId);
        }
        return simulateItemUse(itemId, player);
    }
}