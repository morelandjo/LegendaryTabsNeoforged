package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import vodmordia.modtabs.api.tabs_menu.IntegrationItemTab;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;
import vodmordia.modtabs.utils.ClassCache;
import vodmordia.modtabs.utils.IntegrationUtils;
import vodmordia.modtabs.utils.ScreenClasses;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@TabConfig(configKey = "travelersBackpackTab", defaultEnabled = true, defaultOrder = 0)
public class TravelersBackpackTab extends IntegrationItemTab {
    private static final String BACKPACK_ITEM_FQN = "com.tiviacz.travelersbackpack.items.TravelersBackpackItem";

    // Layout uses bespoke width/height functions from IntegrationUtils, so we override
    // initTabOnScreens; the spec still drives isEnabled / isCurrentlyUsed / tooltip.
    private static final TabSpec SPEC = new TabSpec(
            "travelersBackpackTab",
            ModIntegration.TRAVELERS_BACKPACK,
            () -> Config.Baked.travelersBackpackTabEnabled,
            "travelersBackpack",
            "backpack",
            TabSpec.Layout.guiRelative(),
            new String[] { ScreenClasses.TRAVELERS_BACKPACK_SCREEN },
            new String[] { ScreenClasses.TRAVELERS_BACKPACK_SCREEN }
    );

    public TravelersBackpackTab() {
        super(SPEC, TravelersBackpackTab::getStandardBackpackItem, Config.Baked.travelersBackpackTabCustomIcon);
    }

    private static ItemStack getStandardBackpackItem() {
        try {
            Class<?> modItemsClass = Class.forName("com.tiviacz.travelersbackpack.init.ModItems");
            Field standardBackpackField = modItemsClass.getDeclaredField("STANDARD_TRAVELERS_BACKPACK");
            standardBackpackField.setAccessible(true);
            Object deferredItem = standardBackpackField.get(null);
            Method getMethod = deferredItem.getClass().getMethod("get");
            return new ItemStack((Item) getMethod.invoke(deferredItem));
        } catch (Exception e) {
            return new ItemStack(Items.LEATHER_CHESTPLATE);
        }
    }

    @Override
    public boolean isEnabled(Player player) {
        return super.isEnabled(player) && hasBackpack(player);
    }

    private boolean hasBackpack(Player player) {
        Class<?> backpackItemClass = ClassCache.resolve(BACKPACK_ITEM_FQN);
        if (backpackItemClass == null) return false;

        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && backpackItemClass.isInstance(stack.getItem())) return true;
        }
        for (ItemStack stack : player.getInventory().armor) {
            if (!stack.isEmpty() && backpackItemClass.isInstance(stack.getItem())) return true;
        }

        if (ModIntegrationManager.isModLoaded(ModIntegration.CURIOS) && hasInCurios(player, backpackItemClass)) {
            return true;
        }
        return isWearingBackpackViaAttachments(player);
    }

    private boolean hasInCurios(Player player, Class<?> backpackItemClass) {
        try {
            Class<?> curiosAPI = ClassCache.resolve("top.theillusivec4.curios.api.CuriosApi");
            if (curiosAPI == null) return false;
            Method getCuriosInventory = curiosAPI.getDeclaredMethod("getCuriosInventory", net.minecraft.world.entity.LivingEntity.class);
            Object optionalCurios = getCuriosInventory.invoke(null, player);
            Method isPresentMethod = optionalCurios.getClass().getDeclaredMethod("isPresent");
            if (!(Boolean) isPresentMethod.invoke(optionalCurios)) return false;

            Object curiosInventory = optionalCurios.getClass().getDeclaredMethod("get").invoke(optionalCurios);
            Method findCurios = curiosInventory.getClass().getDeclaredMethod("findCurios", java.util.function.Predicate.class);

            java.util.function.Predicate<ItemStack> predicate = stack ->
                    !stack.isEmpty() && backpackItemClass.isInstance(stack.getItem());

            @SuppressWarnings("unchecked")
            java.util.List<Object> results = (java.util.List<Object>) findCurios.invoke(curiosInventory, predicate);
            return !results.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isWearingBackpackViaAttachments(Player player) {
        try {
            Class<?> attachmentUtilsClass = ClassCache.resolve("com.tiviacz.travelersbackpack.capability.AttachmentUtils");
            if (attachmentUtilsClass == null) return false;
            Method isWearingBackpack = attachmentUtilsClass.getMethod("isWearingBackpack", Player.class);
            return (Boolean) isWearingBackpack.invoke(null, player);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void initTabOnScreens() {
        Class<?> screenClass = ClassCache.resolve(ScreenClasses.TRAVELERS_BACKPACK_SCREEN);
        if (screenClass == null) return;
        @SuppressWarnings("unchecked")
        Class<? extends net.minecraft.client.gui.screens.Screen> typed =
                (Class<? extends net.minecraft.client.gui.screens.Screen>) screenClass;
        ScreenRegistry.builder()
                .withDimensions(IntegrationUtils::getTravelersBackpackWidth, IntegrationUtils::getTravelersBackpackHeight)
                .registerAllTabs(typed);
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.travelersBackpackTabEnabled) return;
        try {
            Class<?> actionPacketClass = Class.forName("com.tiviacz.travelersbackpack.network.ServerboundActionTagPacket");
            Method createMethod = actionPacketClass.getDeclaredMethod("create", int.class, Object[].class);
            // OPEN_SCREEN (1) only opens a worn backpack — the server handler bails when
            // AttachmentUtils.isWearingBackpack is false. For inventory backpacks we send
            // OPEN_BACKPACK (2) with (slotIndex, fromHotbar=false). The fromHotbar=false
            // form skips the `allowOpeningFromSlot` config gate (which defaults to false in
            // TB's server config), so the open happens regardless of where the backpack sits.
            if (isWearingBackpackViaAttachments(player)) {
                createMethod.invoke(null, 1, new Object[0]);
                return;
            }
            int slot = findBackpackInventorySlot(player);
            if (slot >= 0) {
                createMethod.invoke(null, 2, new Object[]{ slot, false });
            }
        } catch (Exception e) {
            if (player instanceof ServerPlayer serverPlayer) {
                openServerSide(serverPlayer);
            }
        }
    }

    /** Returns the inventory slot of the first backpack found, or -1 if none. */
    private int findBackpackInventorySlot(Player player) {
        Class<?> backpackItemClass = ClassCache.resolve(BACKPACK_ITEM_FQN);
        if (backpackItemClass == null) return -1;
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (!stack.isEmpty() && backpackItemClass.isInstance(stack.getItem())) {
                return i;
            }
        }
        return -1;
    }

    private void openServerSide(ServerPlayer serverPlayer) {
        try {
            Class<?> backpackContainerClass = Class.forName("com.tiviacz.travelersbackpack.inventory.BackpackContainer");
            Class<?> attachmentUtilsClass = Class.forName("com.tiviacz.travelersbackpack.capability.AttachmentUtils");
            Class<?> referenceClass = Class.forName("com.tiviacz.travelersbackpack.util.Reference");

            Object wearingBackpack = attachmentUtilsClass.getMethod("getWearingBackpack", Player.class)
                    .invoke(null, serverPlayer);
            int wearableScreenId = referenceClass.getField("WEARABLE_SCREEN_ID").getInt(null);
            backpackContainerClass.getMethod("openBackpack", ServerPlayer.class, ItemStack.class, int.class)
                    .invoke(null, serverPlayer, wearingBackpack, wearableScreenId);
        } catch (Exception ignored) {}
    }
}
