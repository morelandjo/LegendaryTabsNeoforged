package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
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

@TabConfig(configKey = "arsNouveauTab", defaultEnabled = true, defaultOrder = 0)
public class ArsNouveauTab extends ConfigurableItemTab {

    public ArsNouveauTab() {
        super(() -> getArchmageSpellbook(), Config.Baked.arsNouveauTabCustomIcon, "arsNouveau");
    }

    private static ItemStack getArchmageSpellbook() {
        // Try to get Ars Nouveau's Archmage Spellbook item via reflection, fallback to vanilla item
        try {
            Class<?> itemsRegistryClass = Class.forName("com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry");
            Field archmageSpellbookField = itemsRegistryClass.getField("ARCHMAGE_SPELLBOOK");
            Object registryWrapper = archmageSpellbookField.get(null);
            Method getMethod = registryWrapper.getClass().getMethod("get");
            Item archmageSpellbook = (Item) getMethod.invoke(registryWrapper);
            return new ItemStack(archmageSpellbook);
        } catch (Exception e) {
            // Fallback to vanilla item
            return new ItemStack(Items.ENCHANTED_BOOK);
        }
    }

    @Override
    public void openTargetScreen(Player player) {
        try {
            // Find any spellbook in player's inventory
            ItemStack spellbookStack = findSpellbookInInventory(player);
            if (spellbookStack.isEmpty()) {
                return; // No spellbook found
            }

            // Temporarily put spellbook in hand for constructor, then restore
            ItemStack originalMainHand = player.getMainHandItem().copy();
            boolean needsRestore = true;

            try {
                // Temporarily set the spellbook in main hand for the constructor
                player.getInventory().setItem(player.getInventory().selected, spellbookStack);

                // Create GuiSpellBook - now the constructor will find the spellbook
                Class<?> guiSpellBookClass = Class.forName("com.hollingsworth.arsnouveau.client.gui.book.GuiSpellBook");
                Object guiInstance = guiSpellBookClass.getDeclaredConstructor(InteractionHand.class)
                    .newInstance(InteractionHand.MAIN_HAND);

                // Restore original item in hand
                player.getInventory().setItem(player.getInventory().selected, originalMainHand);
                needsRestore = false;

                // Open the GUI
                net.minecraft.client.Minecraft.getInstance().setScreen((net.minecraft.client.gui.screens.Screen) guiInstance);

            } finally {
                // Make sure we restore the original item even if something goes wrong
                if (needsRestore) {
                    player.getInventory().setItem(player.getInventory().selected, originalMainHand);
                }
            }

        } catch (Exception e) {
            // Silently ignore errors - mod may not be available
        }
    }

    @Override
    public boolean isEnabled(Player player) {
        return Config.Baked.arsNouveauTabEnabled && ModIntegrationManager.isModLoaded(ModIntegration.ARS_NOUVEAU) && hasSpellbook(player);
    }

    private boolean hasSpellbook(Player player) {
        try {
            // Check main inventory and hotbar
            for (ItemStack stack : player.getInventory().items) {
                if (!stack.isEmpty() && isSpellbook(stack)) {
                    return true;
                }
            }

            // Check offhand
            ItemStack offhandStack = player.getOffhandItem();
            if (!offhandStack.isEmpty() && isSpellbook(offhandStack)) {
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

                        java.util.function.Predicate<ItemStack> spellbookPredicate = stack ->
                            !stack.isEmpty() && isSpellbook(stack);

                        @SuppressWarnings("unchecked")
                        java.util.List<Object> curioResults = (java.util.List<Object>) findCuriosMethod.invoke(curiosInventory, spellbookPredicate);

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

    private InteractionHand getSpellbookHand(Player player) {
        // Check main hand first
        ItemStack mainHandStack = player.getMainHandItem();
        if (!mainHandStack.isEmpty() && isSpellbook(mainHandStack)) {
            return InteractionHand.MAIN_HAND;
        }

        // Check offhand
        ItemStack offhandStack = player.getOffhandItem();
        if (!offhandStack.isEmpty() && isSpellbook(offhandStack)) {
            return InteractionHand.OFF_HAND;
        }

        // No spellbook in hands
        return null;
    }

    private ItemStack findSpellbookInInventory(Player player) {
        // Check hands first
        if (isSpellbook(player.getMainHandItem())) {
            return player.getMainHandItem();
        }
        if (isSpellbook(player.getOffhandItem())) {
            return player.getOffhandItem();
        }

        // Check inventory
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && isSpellbook(stack)) {
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

                    java.util.function.Predicate<ItemStack> spellbookPredicate = stack ->
                        !stack.isEmpty() && isSpellbook(stack);

                    @SuppressWarnings("unchecked")
                    java.util.List<Object> curioResults = (java.util.List<Object>) findCuriosMethod.invoke(curiosInventory, spellbookPredicate);

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

    private boolean isSpellbook(ItemStack stack) {
        return vodmordia.modtabs.utils.ClassCache.isInstance(
                vodmordia.modtabs.utils.ScreenClasses.ARS_NOUVEAU_SPELLBOOK_ITEM, stack.getItem());
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        return vodmordia.modtabs.utils.ClassCache.isInstance(
                vodmordia.modtabs.utils.ScreenClasses.ARS_NOUVEAU_SPELLBOOK_GUI, currentScreen);
    }

    @Override
    public Component getTooltip() {
        return Component.translatable("tooltip." + ModTabs.MOD_ID + ".tab.ars_nouveau.description");
    }

    @Override
    public void initTabOnScreens() {
        ScreenRegistry.builder()
            .withStandardDimensions()
            .inverted()
            .registerAllTabs(vodmordia.modtabs.utils.ScreenClasses.ARS_NOUVEAU_SPELLBOOK_GUI);
    }
}