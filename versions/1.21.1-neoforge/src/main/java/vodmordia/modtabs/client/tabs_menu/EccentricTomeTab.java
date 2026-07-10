package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.ConfigurableItemTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@TabConfig(configKey = "eccentricTomeTab", defaultEnabled = true, defaultOrder = 0)
public class EccentricTomeTab extends ConfigurableItemTab {

    public EccentricTomeTab() {
        super(() -> getCurrentTomeOrBookItem(), Config.Baked.eccentricTomeTabCustomIcon, "eccentricTome");
    }

    // Returns the tome if unconverted, or the converted book if converted
    private static ItemStack getCurrentTomeOrBookItem() {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return getTomeItem();
        }

        try {
            // Check if player has a converted book (non-tome eccentric tome item)
            ItemStack convertedBook = findConvertedBook(player);
            if (!convertedBook.isEmpty()) {
                return convertedBook;
            }

            // Otherwise return the regular tome
            return getTomeItem();
        } catch (Exception e) {
            return getTomeItem();
        }
    }

    private static ItemStack getTomeItem() {
        // Try to get Eccentric Tome's tome item via reflection, fallback to vanilla item
        try {
            Class<?> eccentricTomeClass = Class.forName("website.eccentric.tome.EccentricTome");
            Field tomeField = eccentricTomeClass.getField("TOME");
            Object registryObject = tomeField.get(null);
            Method getMethod = registryObject.getClass().getMethod("get");
            Item tomeItem = (Item) getMethod.invoke(registryObject);
            return new ItemStack(tomeItem);
        } catch (Exception e) {
            // Fallback to vanilla item
            return new ItemStack(Items.BOOK);
        }
    }

    // Find a book that was converted from the tome
    private static ItemStack findConvertedBook(Player player) {
        try {
            // Check inventory for items that are NOT tomes but ARE from eccentric tome
            for (ItemStack stack : player.getInventory().items) {
                if (!stack.isEmpty() && isConvertedFromTome(stack)) {
                    return stack;
                }
            }

            // Check offhand
            ItemStack offhand = player.getOffhandItem();
            if (!offhand.isEmpty() && isConvertedFromTome(offhand)) {
                return offhand;
            }
        } catch (Exception e) {
            // Ignore
        }
        return ItemStack.EMPTY;
    }

    // Check if an item was converted from the tome (has tome data but isn't a tome)
    private static boolean isConvertedFromTome(ItemStack stack) {
        try {
            // If it's a tome itself, return false
            if (isTome(stack)) {
                return false;
            }

            // Check if it has eccentric tome data components (meaning it was converted)
            Class<?> tomeUtilsClass = Class.forName("website.eccentric.tome.TomeUtils");
            Method getModsBooksMethod = tomeUtilsClass.getMethod("getModsBooks", ItemStack.class);
            Object modsBooks = getModsBooksMethod.invoke(null, stack);

            // If it has mods books data, it was converted from tome
            return modsBooks != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void openTargetScreen(Player player) {
        try {
            // Check if player has a converted book
            ItemStack convertedBook = findConvertedBook(player);
            if (!convertedBook.isEmpty()) {
                // Open the converted book's screen
                openConvertedBookScreen(player, convertedBook);
                return;
            }

            // Otherwise, find and open tome
            ItemStack tomeStack = findTomeInInventory(player);
            if (tomeStack.isEmpty()) {
                return; // No tome found
            }

            // Create original TomeScreen
            Class<?> tomeScreenClass = Class.forName("website.eccentric.tome.client.TomeScreen");
            Object originalTomeScreen = tomeScreenClass.getDeclaredConstructor(ItemStack.class)
                .newInstance(tomeStack);

            // Initialize the original screen properly (set minecraft instance)
            Method initMethod = Screen.class.getDeclaredMethod("init", net.minecraft.client.Minecraft.class, int.class, int.class);
            initMethod.setAccessible(true);
            var minecraft = Minecraft.getInstance();
            initMethod.invoke(originalTomeScreen, minecraft, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());

            // Create a wrapper that delegates to original but overrides mouseClicked
            TomeScreenWrapper wrapper = new TomeScreenWrapper((Screen) originalTomeScreen, player);

            // Open the GUI
            Minecraft.getInstance().setScreen(wrapper);

        } catch (Exception e) {
            ModTabs.LOGGER.error("Error opening TomeScreen", e);
        }
    }

    private void openConvertedBookScreen(Player player, ItemStack bookStack) {
        try {
            // Use the book item's use() method to open its screen
            bookStack.getItem().use(player.level(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
        } catch (Exception e) {
            ModTabs.LOGGER.error("Error opening converted book screen", e);
        }
    }

    @Override
    public boolean isEnabled(Player player) {
        boolean configEnabled = Config.Baked.eccentricTomeTabEnabled;
        boolean modLoaded = ModIntegrationManager.isModLoaded(ModIntegration.ECCENTRIC_TOME);
        boolean hasTheTome = hasTome(player);
        boolean hasConvertedBook = !findConvertedBook(player).isEmpty();

        // Show tab if player has either the tome or a converted book
        return configEnabled && modLoaded && (hasTheTome || hasConvertedBook);
    }

    private boolean hasTome(Player player) {
        try {
            // Check main inventory and hotbar
            for (ItemStack stack : player.getInventory().items) {
                if (!stack.isEmpty() && isTome(stack)) {
                    return true;
                }
            }

            // Check offhand
            ItemStack offhandStack = player.getOffhandItem();
            if (!offhandStack.isEmpty() && isTome(offhandStack)) {
                return true;
            }

            // Check Curios if available
            if (ModIntegrationManager.isModLoaded(ModIntegration.CURIOS)) {
                try {
                    Class<?> curiosAPIClass = Class.forName("top.theillusivec4.curios.api.CuriosApi");
                    Method getCuriosInventoryMethod = curiosAPIClass.getDeclaredMethod("getCuriosInventory", net.minecraft.world.entity.LivingEntity.class);
                    Object optionalCuriosInventory = getCuriosInventoryMethod.invoke(null, player);

                    Method isPresentMethod = optionalCuriosInventory.getClass().getDeclaredMethod("isPresent");
                    boolean isPresent = (Boolean) isPresentMethod.invoke(optionalCuriosInventory);

                    if (isPresent) {
                        Method getMethod = optionalCuriosInventory.getClass().getDeclaredMethod("get");
                        Object curiosInventory = getMethod.invoke(optionalCuriosInventory);

                        Method findCuriosMethod = curiosInventory.getClass().getDeclaredMethod("findCurios", java.util.function.Predicate.class);

                        java.util.function.Predicate<ItemStack> tomePredicate = stack ->
                            !stack.isEmpty() && isTome(stack);

                        @SuppressWarnings("unchecked")
                        java.util.List<Object> curioResults = (java.util.List<Object>) findCuriosMethod.invoke(curiosInventory, tomePredicate);

                        if (!curioResults.isEmpty()) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                    // Silently ignore Curios errors
                }
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private ItemStack findTomeInInventory(Player player) {
        // Check hands first
        if (isTome(player.getMainHandItem())) {
            return player.getMainHandItem();
        }
        if (isTome(player.getOffhandItem())) {
            return player.getOffhandItem();
        }

        // Check inventory
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && isTome(stack)) {
                return stack;
            }
        }

        // Check Curios if available
        if (ModIntegrationManager.isModLoaded(ModIntegration.CURIOS)) {
            try {
                Class<?> curiosAPIClass = Class.forName("top.theillusivec4.curios.api.CuriosApi");
                Method getCuriosInventoryMethod = curiosAPIClass.getDeclaredMethod("getCuriosInventory", net.minecraft.world.entity.LivingEntity.class);
                Object optionalCuriosInventory = getCuriosInventoryMethod.invoke(null, player);

                Method isPresentMethod = optionalCuriosInventory.getClass().getDeclaredMethod("isPresent");
                boolean isPresent = (Boolean) isPresentMethod.invoke(optionalCuriosInventory);

                if (isPresent) {
                    Method getMethod = optionalCuriosInventory.getClass().getDeclaredMethod("get");
                    Object curiosInventory = getMethod.invoke(optionalCuriosInventory);

                    Method findCuriosMethod = curiosInventory.getClass().getDeclaredMethod("findCurios", java.util.function.Predicate.class);

                    java.util.function.Predicate<ItemStack> tomePredicate = stack ->
                        !stack.isEmpty() && isTome(stack);

                    @SuppressWarnings("unchecked")
                    java.util.List<Object> curioResults = (java.util.List<Object>) findCuriosMethod.invoke(curiosInventory, tomePredicate);

                    if (!curioResults.isEmpty()) {
                        // Get the first result and extract the ItemStack
                        Object curioResult = curioResults.get(0);
                        Method getStackMethod = curioResult.getClass().getDeclaredMethod("stack");
                        return (ItemStack) getStackMethod.invoke(curioResult);
                    }
                }
            } catch (Exception e) {
                // Silently ignore Curios errors
            }
        }

        return ItemStack.EMPTY;
    }

    private static boolean isTome(ItemStack stack) {
        return vodmordia.modtabs.utils.ClassCache.isInstance(vodmordia.modtabs.utils.ScreenClasses.ECCENTRIC_TOME_ITEM, stack.getItem());
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        return vodmordia.modtabs.utils.ClassCache.isInstance(vodmordia.modtabs.utils.ScreenClasses.ECCENTRIC_TOME_SCREEN, currentScreen);
    }

    @Override
    public Component getTooltip() {
        return Component.translatable("tooltip." + ModTabs.MOD_ID + ".tab.eccentric_tome.description");
    }

    @Override
    public void initTabOnScreens() {
        ScreenRegistry.builder()
            .withStandardDimensions()
            .inverted()
            .registerAllTabs("website.eccentric.tome.client.TomeScreen");
    }

    // Wrapper that delegates to original TomeScreen but overrides mouseClicked for custom conversion
    private static class TomeScreenWrapper extends Screen {
        private final Screen originalTomeScreen;
        private final Player player;

        public TomeScreenWrapper(Screen originalTomeScreen, Player player) {
            super(Component.empty());
            this.originalTomeScreen = originalTomeScreen;
            this.player = player;
        }

        @Override
        protected void init() {
            super.init();
            // Make sure the original screen is also initialized
            try {
                // Call the protected init() method from Screen class
                Method initMethod = Screen.class.getDeclaredMethod("init", net.minecraft.client.Minecraft.class, int.class, int.class);
                initMethod.setAccessible(true);
                var minecraft = Minecraft.getInstance();
                initMethod.invoke(originalTomeScreen, minecraft, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
                ModTabs.LOGGER.info("Successfully initialized original TomeScreen");
            } catch (Exception e) {
                ModTabs.LOGGER.error("Error initializing original screen", e);
            }
        }

        @Override
        public boolean mouseClicked(double x, double y, int button) {
            if (button == 0) { // LEFT_CLICK
                try {
                    ModTabs.LOGGER.info("Left click detected at x={}, y={}", x, y);

                    // Get the selected book by calling mouseClicked and checking state
                    boolean originalResult = originalTomeScreen.mouseClicked(x, y, button);
                    ModTabs.LOGGER.info("Original mouseClicked result: {}", originalResult);

                    Field bookField = originalTomeScreen.getClass().getDeclaredField("book");
                    bookField.setAccessible(true);
                    ItemStack selectedBook = (ItemStack) bookField.get(originalTomeScreen);

                    ModTabs.LOGGER.info("Selected book: {} (isEmpty: {})", selectedBook, selectedBook == null ? "null" : selectedBook.isEmpty());

                    if (selectedBook != null && !selectedBook.isEmpty()) {
                        ModTabs.LOGGER.info("Converting tome to selected book: {}", selectedBook.getItem());
                        // Convert the tome to the selected book
                        sendConvertMessage(selectedBook);
                        // Screen is closed inside sendConvertMessage
                        return true;
                    }

                    return originalResult;
                } catch (Exception e) {
                    ModTabs.LOGGER.error("Error in custom mouseClicked", e);
                }
            }

            return originalTomeScreen.mouseClicked(x, y, button);
        }

        private void sendConvertMessage(ItemStack selectedBook) {
            try {
                ModTabs.LOGGER.info("sendConvertMessage called with selectedBook: {}", selectedBook);

                // Find the tome in the player's inventory
                EccentricTomeTab tab = new EccentricTomeTab();
                int tomeSlot = findTomeSlot(player);

                if (tomeSlot == -1) {
                    ModTabs.LOGGER.warn("Tome not found in inventory");
                    return;
                }

                ModTabs.LOGGER.info("Found tome in slot: {}", tomeSlot);

                // Try to create a custom network packet to send to server
                // First, try to find network-related classes
                try {
                    // Try to find what networking system they use
                    String[] networkClasses = {
                        "website.eccentric.tome.network.ConvertMessage",
                        "website.eccentric.tome.network.ConvertPayload",
                        "website.eccentric.tome.ConvertMessage",
                        "website.eccentric.tome.ConvertPayload",
                        "website.eccentric.tome.network.TomeConvertPacket",
                        "website.eccentric.tome.packet.ConvertPacket"
                    };

                    Class<?> convertMessageClass = null;
                    for (String className : networkClasses) {
                        try {
                            convertMessageClass = Class.forName(className);
                            ModTabs.LOGGER.info("Found network class: {}", className);
                            break;
                        } catch (ClassNotFoundException e) {
                            // Continue
                        }
                    }

                    if (convertMessageClass == null) {
                        ModTabs.LOGGER.info("No network message class found, NeoForge version must use different approach");

                        // Since there's no network packet, Eccentric Tome NeoForge must handle conversion differently
                        // Let's check TomeItem for use() method that might trigger conversion
                        ItemStack tomeStack = tab.findTomeInInventory(player);
                        Class<?> tomeItemClass = tomeStack.getItem().getClass();

                        ModTabs.LOGGER.info("Checking TomeItem for interaction methods:");
                        for (Method m : tomeItemClass.getDeclaredMethods()) {
                            if (m.getName().contains("use") || m.getName().contains("interact") ||
                                m.getName().contains("click") || m.getName().contains("convert")) {
                                ModTabs.LOGGER.info("  - {} (params: {})", m.getName(), java.util.Arrays.toString(m.getParameterTypes()));
                            }
                        }

                        // They might be using item right-click to open screen then converting internally
                        // Let's just use TomeUtils directly but send the result to server properly
                        Class<?> tomeUtilsClass = Class.forName("website.eccentric.tome.TomeUtils");
                        Method convertMethod = tomeUtilsClass.getMethod("convert", ItemStack.class, ItemStack.class);
                        ItemStack convertedBook = (ItemStack) convertMethod.invoke(null, tomeStack, selectedBook);

                        ModTabs.LOGGER.info("Converted tome to: {}", convertedBook);

                        // Replace in inventory and let Minecraft's sync handle it
                        replaceTomeInInventory(tab, convertedBook);
                        ModTabs.LOGGER.info("Replaced tome in inventory with converted book");

                    } else {
                        // Use ModTabs custom packet to handle conversion
                        ModTabs.LOGGER.info("Sending ModTabs TomeConvertPayload to server...");

                        vodmordia.modtabs.network.TomeConvertPayload payload =
                            new vodmordia.modtabs.network.TomeConvertPayload(tomeSlot, selectedBook);

                        net.neoforged.neoforge.network.PacketDistributor.sendToServer(payload);

                        ModTabs.LOGGER.info("Sent TomeConvertPayload: slot={}, book={}", tomeSlot, selectedBook);
                    }

                } catch (Exception e) {
                    ModTabs.LOGGER.error("Failed to convert tome", e);
                }

                // Close screen
                Minecraft.getInstance().setScreen(null);
                ModTabs.LOGGER.info("Closed TomeScreen");

            } catch (Exception e) {
                ModTabs.LOGGER.error("Failed to convert tome", e);
            }
        }

        private int findTomeSlot(Player player) {
            EccentricTomeTab tab = new EccentricTomeTab();

            // Check main hand (slot depends on selected hotbar slot)
            if (tab.isTome(player.getMainHandItem())) {
                return player.getInventory().selected; // Hotbar slot
            }

            // Check offhand
            if (tab.isTome(player.getOffhandItem())) {
                return 40; // Offhand slot
            }

            // Check inventory
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty() && tab.isTome(stack)) {
                    return i;
                }
            }

            return -1; // Not found
        }

        private void replaceTomeInInventory(EccentricTomeTab tab, ItemStack convertedBook) {
            boolean replaced = false;

            // Check main hand
            if (tab.isTome(player.getMainHandItem())) {
                player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, convertedBook);
                replaced = true;
                ModTabs.LOGGER.info("Replaced tome in main hand");
            }
            // Check offhand
            else if (tab.isTome(player.getOffhandItem())) {
                player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, convertedBook);
                replaced = true;
                ModTabs.LOGGER.info("Replaced tome in offhand");
            }
            // Check inventory
            else {
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack slotStack = player.getInventory().getItem(i);
                    if (!slotStack.isEmpty() && tab.isTome(slotStack)) {
                        player.getInventory().setItem(i, convertedBook);
                        replaced = true;
                        ModTabs.LOGGER.info("Replaced tome in slot {}", i);
                        break;
                    }
                }
            }

            if (!replaced) {
                ModTabs.LOGGER.warn("Could not find tome to replace in inventory");
            }
        }

        // Delegate all other methods to the original screen
        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            try {
                originalTomeScreen.render(guiGraphics, mouseX, mouseY, partialTick);
            } catch (Exception e) {
                ModTabs.LOGGER.error("Error in original screen render", e);
            }
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return originalTomeScreen.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean isPauseScreen() {
            return originalTomeScreen.isPauseScreen();
        }

        @Override
        public void onClose() {
            originalTomeScreen.onClose();
            super.onClose();
        }
    }
}