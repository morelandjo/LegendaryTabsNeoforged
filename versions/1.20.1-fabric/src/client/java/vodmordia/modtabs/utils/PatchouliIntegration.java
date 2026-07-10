package vodmordia.modtabs.utils;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import vodmordia.modtabs.ModTabs;

/**
 * Integration utility for Patchouli mod functionality.
 * Provides methods to work with Patchouli books programmatically.
 */
public class PatchouliIntegration {

    private static final String PATCHOULI_MOD_ID = "patchouli";
    private static Boolean patchouliLoaded = null;

    /**
     * Check if Patchouli mod is loaded
     */
    public static boolean isPatchouliLoaded() {
        if (patchouliLoaded == null) {
            patchouliLoaded = FabricLoader.getInstance().isModLoaded(PATCHOULI_MOD_ID);
        }
        return patchouliLoaded;
    }

    /**
     * Open a Patchouli book by its Identifier ID
     */
    public static boolean openBook(Identifier bookId, PlayerEntity player) {
        if (!isPatchouliLoaded()) {
            ModTabs.LOGGER.warn("Attempted to open Patchouli book but Patchouli is not loaded");
            return false;
        }

        try {
            // Use reflection to call Patchouli API since we don't want a hard dependency
            Class<?> apiClass = Class.forName("vazkii.patchouli.api.PatchouliAPI");
            Object apiInstance = apiClass.getMethod("get").invoke(null);

            // Call openBookGUI on client side
            if (player.getWorld().isClient) {
                apiClass.getMethod("openBookGUI", Identifier.class)
                        .invoke(apiInstance, bookId);
                return true;
            } else {
                // Server-side version would need ServerPlayer, but we're primarily client-side
                return false;
            }

        } catch (ClassNotFoundException e) {
            ModTabs.LOGGER.error("Patchouli API not found - mod may not be loaded correctly");
            return false;
        } catch (Exception e) {
            ModTabs.LOGGER.error("Failed to open Patchouli book " + bookId + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Get a properly configured ItemStack for a Patchouli book
     */
    public static ItemStack getPatchouliBookStack(Identifier bookId) {
        if (!isPatchouliLoaded()) {
            return ItemStack.EMPTY;
        }


        try {
            // Use reflection to call Patchouli API
            Class<?> apiClass = Class.forName("vazkii.patchouli.api.PatchouliAPI");
            Object apiInstance = apiClass.getMethod("get").invoke(null);


            // Get the book ItemStack
            Object bookStack = apiClass.getMethod("getBookStack", Identifier.class)
                    .invoke(apiInstance, bookId);

            if (bookStack instanceof ItemStack) {
                return (ItemStack) bookStack;
            }

        } catch (ClassNotFoundException e) {
            ModTabs.LOGGER.error("Patchouli API not found - mod may not be loaded correctly");
        } catch (Exception e) {
            ModTabs.LOGGER.error("Failed to get Patchouli book ItemStack for " + bookId + ": " + e.getMessage());
        }

        return ItemStack.EMPTY;
    }

    /**
     * Check if an ItemStack is a Patchouli book
     */
    public static boolean isPatchouliBook(ItemStack itemStack) {
        if (!isPatchouliLoaded() || itemStack.isEmpty()) {
            return false;
        }

        try {
            // Check if the item is an instance of ItemModBook
            Class<?> itemModBookClass = Class.forName("vazkii.patchouli.common.item.ItemModBook");
            return itemModBookClass.isInstance(itemStack.getItem());

        } catch (ClassNotFoundException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extract book ID from a Patchouli book ItemStack
     */
    public static Identifier getBookIdFromStack(ItemStack itemStack) {
        if (!isPatchouliLoaded() || !isPatchouliBook(itemStack)) {
            return null;
        }

        try {
            // Use ItemModBook.getBook(stack).id to get the book ID
            Class<?> itemModBookClass = Class.forName("vazkii.patchouli.common.item.ItemModBook");
            Object bookInstance = itemModBookClass.getMethod("getBook", ItemStack.class)
                    .invoke(null, itemStack);

            if (bookInstance != null) {
                // Get the id field from the book instance
                Class<?> bookClass = bookInstance.getClass();
                Object bookId = bookClass.getField("id").get(bookInstance);

                if (bookId instanceof Identifier) {
                    return (Identifier) bookId;
                }
            }

        } catch (Exception e) {
        }

        return null;
    }

    /**
     * Validate that a book ID exists in Patchouli's registry
     */
    public static boolean isValidBookId(Identifier bookId) {
        if (!isPatchouliLoaded() || bookId == null) {
            return false;
        }

        try {
            // Try to get a book stack - if it's empty, the book doesn't exist
            ItemStack bookStack = getPatchouliBookStack(bookId);
            return !bookStack.isEmpty();

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Parse a book ID string into an Identifier
     */
    public static Identifier parseBookId(String bookIdString) {
        if (bookIdString == null || bookIdString.trim().isEmpty()) {
            return null;
        }

        try {
            return new Identifier(bookIdString.trim());
        } catch (Exception e) {
            ModTabs.LOGGER.warn("Invalid book ID format: " + bookIdString);
            return null;
        }
    }


    /**
     * Debug method to list all registered books in Patchouli
     */
    private static void debugListRegisteredBooks(Object apiInstance, Class<?> apiClass) {
        try {
            // Try to access the book registry through the BookRegistry class directly
            Class<?> bookRegistryClass = Class.forName("vazkii.patchouli.common.book.BookRegistry");

            // Try to access static fields or methods that might contain the registry
            try {
                // Look for common static field names like BOOKS, books, registry, etc.
                java.lang.reflect.Field booksField = null;
                try {
                    booksField = bookRegistryClass.getDeclaredField("books");
                } catch (NoSuchFieldException e1) {
                    try {
                        booksField = bookRegistryClass.getDeclaredField("BOOKS");
                    } catch (NoSuchFieldException e2) {
                        try {
                            booksField = bookRegistryClass.getDeclaredField("registry");
                        } catch (NoSuchFieldException e3) {
                        }
                    }
                }

                if (booksField != null) {
                    booksField.setAccessible(true);
                    Object booksMap = booksField.get(null);

                    if (booksMap != null) {

                        // Try to get keys if it's a map
                        if (booksMap instanceof java.util.Map) {
                            java.util.Set<?> keys = ((java.util.Map<?, ?>) booksMap).keySet();

                            // Also log the size
                        } else {
                        }
                    } else {
                        ModTabs.LOGGER.warn("Books registry is null");
                    }
                }

                // Also try to call static methods that might list books
                try {
                    java.lang.reflect.Method getAllBooksMethod = bookRegistryClass.getMethod("getAllBooks");
                    Object allBooks = getAllBooksMethod.invoke(null);
                } catch (NoSuchMethodException e) {
                }

            } catch (Exception e) {
            }

        } catch (ClassNotFoundException e) {

            // Fallback: try the API method
            try {
                Object bookRegistry = apiClass.getMethod("getBookRegistry").invoke(apiInstance);
                if (bookRegistry != null) {

                    // Try to get keys if it's a map-like object
                    try {
                        Object keys = bookRegistry.getClass().getMethod("keySet").invoke(bookRegistry);
                    } catch (Exception e2) {
                    }
                }
            } catch (Exception e2) {
            }
        }
    }
}