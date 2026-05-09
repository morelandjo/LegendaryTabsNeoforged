package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import vodmordia.modtabs.ModTabs;

import vodmordia.modtabs.api.tabs_menu.ConfigurableItemTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@TabConfig(configKey = "backpackedTab", defaultEnabled = true, defaultOrder = 0)
public class BackpackedTab extends ConfigurableItemTab {

    public BackpackedTab() {
        super(() -> getBackpackItem(), Config.Baked.backpackedTabCustomIcon, "backpacked");
    }

    private static ItemStack getBackpackItem() {
        try {
            // Try to get backpack item from Backpacked mod
            Class<?> itemsClass = Class.forName("com.mrcrayfish.backpacked.core.ModItems");
            Field backpackField = itemsClass.getField("BACKPACK");
            Object supplier = backpackField.get(null);
            Method getMethod = supplier.getClass().getMethod("get");
            Item backpackItem = (Item) getMethod.invoke(supplier);
            return new ItemStack(backpackItem);
        } catch (Exception e) {
            return new ItemStack(Items.BUNDLE); // Fallback to bundle
        }
    }

    @Override
    public void openTargetScreen(Player player) {
        if (Config.Baked.backpackedTabEnabled && player.level().isClientSide && hasBackpack(player)) {
            try {
                // Use reflection to access Backpacked's Network class
                Class<?> networkClass = Class.forName("com.mrcrayfish.backpacked.network.Network");
                Method getPlayMethod = networkClass.getMethod("getPlay");
                Object networkInstance = getPlayMethod.invoke(null);

                // Create MessageOpenBackpack instance
                Class<?> messageClass = Class.forName("com.mrcrayfish.backpacked.network.message.MessageOpenBackpack");
                Object message = messageClass.getDeclaredConstructor().newInstance();

                // Send the message to server
                Method sendToServerMethod = networkInstance.getClass().getMethod("sendToServer", Object.class);
                sendToServerMethod.invoke(networkInstance, message);

            } catch (Exception e) {
                // Silently ignore errors
            }
        }
    }

    @Override
    public boolean isEnabled(Player player) {
        return Config.Baked.backpackedTabEnabled && ModIntegrationManager.isModLoaded(ModIntegration.BACKPACKED) && hasBackpack(player);
    }

    private boolean hasBackpack(Player player) {
        try {
            Class<?> backpackItemClass = vodmordia.modtabs.utils.ClassCache.resolve(
                    vodmordia.modtabs.utils.ScreenClasses.BACKPACKED_ITEM);
            if (backpackItemClass == null) {
                return false;
            }

            // Check player's main inventory (hotbar + main inventory slots)
            for (ItemStack stack : player.getInventory().items) {
                if (!stack.isEmpty() && backpackItemClass.isInstance(stack.getItem())) {
                    return true;
                }
            }

            // Check armor slots (in case backpacks can be worn)
            for (ItemStack stack : player.getInventory().armor) {
                if (!stack.isEmpty() && backpackItemClass.isInstance(stack.getItem())) {
                    return true;
                }
            }

            // Check offhand
            ItemStack offhandStack = player.getOffhandItem();
            if (!offhandStack.isEmpty() && backpackItemClass.isInstance(offhandStack.getItem())) {
                return true;
            }

            // Also try the BackpackHelper method as backup
            try {
                Class<?> backpackHelperClass = Class.forName("com.mrcrayfish.backpacked.BackpackHelper");
                Method getBackpacksMethod = backpackHelperClass.getMethod("getBackpacks", Player.class);
                Object backpacksList = getBackpacksMethod.invoke(null, player);

                if (backpacksList != null) {
                    Method sizeMethod = backpacksList.getClass().getMethod("size");
                    int size = (Integer) sizeMethod.invoke(backpacksList);

                    if (size > 0) {
                        Method getMethod = backpacksList.getClass().getMethod("get", int.class);
                        for (int i = 0; i < size; i++) {
                            ItemStack backpack = (ItemStack) getMethod.invoke(backpacksList, i);
                            if (!backpack.isEmpty()) {
                                return true;
                            }
                        }
                    }
                }
            } catch (Exception helperException) {
                // Ignore
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        return vodmordia.modtabs.utils.ClassCache.isInstance(
                vodmordia.modtabs.utils.ScreenClasses.BACKPACKED_SCREEN, currentScreen);
    }

    @Override
    public Component getTooltip() {
        return Component.translatable("tooltip." + ModTabs.MOD_ID + ".tab.backpack.description");
    }

    @Override
    public void initTabOnScreens() {
        ScreenRegistry.builder()
            .withStandardDimensions()
            .inverted()
            .registerAllTabs(vodmordia.modtabs.utils.ScreenClasses.BACKPACKED_SCREEN);
    }
}
