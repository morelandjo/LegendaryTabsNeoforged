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

    /**
     * Mirrors the B key (KEY_BACKPACK) handler in {@code ClientEvents}:
     * {@code Network.getPlay().sendToServer(new MessageOpenBackpack())}.
     * {@code MessageOpenBackpack()} (no-arg) sets {@code backpackIndex = -1} — the "use
     * currently selected backpack" sentinel that {@code ServerPlayHandler.handleOpenBackpack}
     * resolves server-side via {@code BackpackHelper.getSelectedBackpackIndex(player)}.
     *
     * {@code sendToServer}'s parameter type is {@code com.mrcrayfish.framework.network.message.IMessage}
     * — NOT {@code Object} — so {@code getMethod("sendToServer", Object.class)} throws
     * {@code NoSuchMethodException}. Resolve {@code IMessage} explicitly and look up the
     * method with the correct signature.
     */
    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.backpackedTabEnabled || !player.level().isClientSide || !hasBackpack(player)) return;
        try {
            Class<?> networkClass = Class.forName("com.mrcrayfish.backpacked.network.Network");
            Object networkInstance = networkClass.getMethod("getPlay").invoke(null);

            Class<?> messageClass = Class.forName("com.mrcrayfish.backpacked.network.message.MessageOpenBackpack");
            Object message = messageClass.getDeclaredConstructor().newInstance();

            Class<?> iMessageClass = Class.forName("com.mrcrayfish.framework.network.message.IMessage");
            Method sendToServer = networkClass.getMethod("getPlay").getReturnType()
                    .getMethod("sendToServer", iMessageClass);
            sendToServer.invoke(networkInstance, message);
        } catch (Exception e) {
            ModTabs.LOGGER.warn("[Backpacked] open failed: {} - {}", e.getClass().getName(), e.getMessage());
        }
    }

    @Override
    public boolean isEnabled(Player player) {
        return Config.Baked.backpackedTabEnabled && ModIntegrationManager.isModLoaded(ModIntegration.BACKPACKED) && hasBackpack(player);
    }

    /**
     * Backpacked 3.x can hold a backpack in three places, all of which mean "show the tab":
     *
     * 1. Equipped (equipable slots). Detected via {@code ModSyncedDataKeys.COSMETIC_PROPERTIES}
     *    — an {@code Optional<…Properties>} that's present iff a backpack is equipped. This
     *    is the same signal {@code BackpackLayer.render} uses to draw the on-back model. We
     *    use it instead of {@code getBackpacks(player)} because {@code equipBackpack} mutates
     *    the {@code BACKPACKS} NonNullList in place without re-calling {@code SyncedDataKey.setValue},
     *    so the framework's dirty-tracking misses it and the client-side list reads as all
     *    empty even when a backpack is equipped server-side. COSMETIC_PROPERTIES is set via
     *    a separate path that does sync correctly.
     * 2. Stored in {@code BACKPACKS[i]}. {@code BackpackHelper.getFirstBackpackStack}
     *    walks the equipable list — covers the case where a future Backpacked build fixes
     *    the in-place-mutation sync bug and starts populating the synced list on equip.
     * 3. Bare item in hotbar / main inventory / armor / offhand. Pre-equip state
     *    (player picked up a backpack but hasn't right-clicked to equip), or with the
     *    equipable feature disabled in config (legacy single-use-from-inventory mode).
     */
    private boolean hasBackpack(Player player) {
        Class<?> backpackItemClass = vodmordia.modtabs.utils.ClassCache.resolve(
                vodmordia.modtabs.utils.ScreenClasses.BACKPACKED_ITEM);
        if (backpackItemClass == null) return false;

        try {
            Class<?> keysClass = Class.forName("com.mrcrayfish.backpacked.core.ModSyncedDataKeys");
            Field cosmeticField = keysClass.getField("COSMETIC_PROPERTIES");
            Object syncedKey = cosmeticField.get(null);
            Method getValue = syncedKey.getClass().getMethod("getValue", net.minecraft.world.entity.Entity.class);
            Object value = getValue.invoke(syncedKey, player);
            if (value instanceof java.util.Optional && ((java.util.Optional<?>) value).isPresent()) return true;
        } catch (Exception ignored) {
        }

        try {
            Class<?> helperClass = Class.forName("com.mrcrayfish.backpacked.BackpackHelper");
            Method getFirst = helperClass.getMethod("getFirstBackpackStack", Player.class);
            ItemStack equipped = (ItemStack) getFirst.invoke(null, player);
            if (equipped != null && !equipped.isEmpty()) return true;
        } catch (Exception ignored) {
        }

        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && backpackItemClass.isInstance(stack.getItem())) return true;
        }
        for (ItemStack stack : player.getInventory().armor) {
            if (!stack.isEmpty() && backpackItemClass.isInstance(stack.getItem())) return true;
        }
        ItemStack offhand = player.getOffhandItem();
        return !offhand.isEmpty() && backpackItemClass.isInstance(offhand.getItem());
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
