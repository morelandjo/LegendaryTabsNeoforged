package vodmordia.modtabs.utils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
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
            patchouliLoaded = ModList.get().isLoaded(PATCHOULI_MOD_ID);
        }
        return patchouliLoaded;
    }

    /**
     * Open a Patchouli book by its ResourceLocation ID
     */
    public static boolean openBook(ResourceLocation bookId, Player player) {
        if (!isPatchouliLoaded()) {
            ModTabs.LOGGER.warn("Attempted to open Patchouli book but Patchouli is not loaded");
            return false;
        }

        try {
            // Use reflection to call Patchouli API since we don't want a hard dependency
            Class<?> apiClass = Class.forName("vazkii.patchouli.api.PatchouliAPI");
            Object apiInstance = apiClass.getMethod("get").invoke(null);

            // Call openBookGUI on client side
            if (player.level().isClientSide) {
                apiClass.getMethod("openBookGUI", ResourceLocation.class)
                        .invoke(apiInstance, bookId);
                ModTabs.LOGGER.debug("Opened Patchouli book: " + bookId + " (client-side)");
                return true;
            } else {
                // Server-side version would need ServerPlayer, but we're primarily client-side
                ModTabs.LOGGER.debug("Patchouli book opening attempted on server side - may not work correctly");
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
    public static ItemStack getPatchouliBookStack(ResourceLocation bookId) {
        if (!isPatchouliLoaded()) {
            if (vodmordia.modtabs.config.Config.Baked.customTabsDebugLogging) {
                ModTabs.LOGGER.info("Attempted to get Patchouli book ItemStack but Patchouli is not loaded");
            }
            return ItemStack.EMPTY;
        }

        if (vodmordia.modtabs.config.Config.Baked.customTabsDebugLogging) {
            ModTabs.LOGGER.info("Attempting to get Patchouli book ItemStack for: " + bookId);
        }

        try {
            // Use reflection to call Patchouli API
            Class<?> apiClass = Class.forName("vazkii.patchouli.api.PatchouliAPI");
            Object apiInstance = apiClass.getMethod("get").invoke(null);

            if (vodmordia.modtabs.config.Config.Baked.customTabsDebugLogging) {
                ModTabs.LOGGER.info("Successfully accessed Patchouli API");
                debugListRegisteredBooks(apiInstance, apiClass);
            }

            // Get the book ItemStack
            Object bookStack = apiClass.getMethod("getBookStack", ResourceLocation.class)
                    .invoke(apiInstance, bookId);

            if (bookStack instanceof ItemStack) {
                if (vodmordia.modtabs.config.Config.Baked.customTabsDebugLogging) {
                    ModTabs.LOGGER.info("Successfully created Patchouli book ItemStack for: " + bookId);
                }
                return (ItemStack) bookStack;
            } else {
                if (vodmordia.modtabs.config.Config.Baked.customTabsDebugLogging) {
                    ModTabs.LOGGER.info("getBookStack returned null or non-ItemStack for: " + bookId + " - will use fallback");
                }
            }

        } catch (ClassNotFoundException e) {
            ModTabs.LOGGER.error("Patchouli API not found - mod may not be loaded correctly");
        } catch (Exception e) {
            if (vodmordia.modtabs.config.Config.Baked.customTabsDebugLogging) {
                ModTabs.LOGGER.info("Failed to get Patchouli book ItemStack for " + bookId + ": " + e.getMessage() + " - will use fallback");
            } else {
                ModTabs.LOGGER.error("Failed to get Patchouli book ItemStack for " + bookId + ": " + e.getMessage());
            }
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
            ModTabs.LOGGER.debug("Could not check if item is Patchouli book - class not found");
            return false;
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Error checking if item is Patchouli book: " + e.getMessage());
            return false;
        }
    }

    /**
     * Extract book ID from a Patchouli book ItemStack
     */
    public static ResourceLocation getBookIdFromStack(ItemStack itemStack) {
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

                if (bookId instanceof ResourceLocation) {
                    return (ResourceLocation) bookId;
                }
            }

        } catch (Exception e) {
            ModTabs.LOGGER.debug("Error extracting book ID from Patchouli book: " + e.getMessage());
        }

        return null;
    }

    /**
     * Validate that a book ID exists in Patchouli's registry
     */
    public static boolean isValidBookId(ResourceLocation bookId) {
        if (!isPatchouliLoaded() || bookId == null) {
            return false;
        }

        try {
            // Try to get a book stack - if it's empty, the book doesn't exist
            ItemStack bookStack = getPatchouliBookStack(bookId);
            return !bookStack.isEmpty();

        } catch (Exception e) {
            ModTabs.LOGGER.debug("Error validating Patchouli book ID " + bookId + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Parse a book ID string into a ResourceLocation
     */
    public static ResourceLocation parseBookId(String bookIdString) {
        if (bookIdString == null || bookIdString.trim().isEmpty()) {
            return null;
        }

        try {
            return ResourceLocation.parse(bookIdString.trim());
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
            ModTabs.LOGGER.info("Found BookRegistry class: " + bookRegistryClass);

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
                            ModTabs.LOGGER.debug("Could not find books field in BookRegistry");
                        }
                    }
                }

                if (booksField != null) {
                    booksField.setAccessible(true);
                    Object booksMap = booksField.get(null);

                    if (booksMap != null) {
                        ModTabs.LOGGER.info("Books registry type: " + booksMap.getClass());

                        // Try to get keys if it's a map
                        if (booksMap instanceof java.util.Map) {
                            java.util.Set<?> keys = ((java.util.Map<?, ?>) booksMap).keySet();
                            ModTabs.LOGGER.info("Registered Patchouli books: " + keys);

                            // Also log the size
                            ModTabs.LOGGER.info("Number of registered books: " + ((java.util.Map<?, ?>) booksMap).size());
                        } else {
                            ModTabs.LOGGER.info("Books registry content: " + booksMap);
                        }
                    } else {
                        ModTabs.LOGGER.warn("Books registry is null");
                    }
                }

                // Also try to call static methods that might list books
                try {
                    java.lang.reflect.Method getAllBooksMethod = bookRegistryClass.getMethod("getAllBooks");
                    Object allBooks = getAllBooksMethod.invoke(null);
                    ModTabs.LOGGER.info("getAllBooks() result: " + allBooks);
                } catch (NoSuchMethodException e) {
                    ModTabs.LOGGER.debug("getAllBooks() method not found");
                }

            } catch (Exception e) {
                ModTabs.LOGGER.debug("Error accessing BookRegistry internals: " + e.getMessage());
            }

        } catch (ClassNotFoundException e) {
            ModTabs.LOGGER.debug("BookRegistry class not found");

            // Fallback: try the API method
            try {
                Object bookRegistry = apiClass.getMethod("getBookRegistry").invoke(apiInstance);
                if (bookRegistry != null) {
                    ModTabs.LOGGER.info("API getBookRegistry() result: " + bookRegistry);

                    // Try to get keys if it's a map-like object
                    try {
                        Object keys = bookRegistry.getClass().getMethod("keySet").invoke(bookRegistry);
                        ModTabs.LOGGER.info("Registry keys: " + keys);
                    } catch (Exception e2) {
                        ModTabs.LOGGER.debug("Could not get keys from registry");
                    }
                }
            } catch (Exception e2) {
                ModTabs.LOGGER.debug("API getBookRegistry() also failed: " + e2.getMessage());
            }
        }
    }
}