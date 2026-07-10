package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.resources.ResourceLocation;
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
import vodmordia.modtabs.config.BackpackSlot;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@TabConfig(configKey = "sophisticatedBackpacksTab", defaultEnabled = true, defaultOrder = 0)
public class SophisticatedBackpacksTab extends ConfigurableItemTab {

    public SophisticatedBackpacksTab() {
        super(() -> getBackpackItem(), Config.Baked.sophisticatedBackpacksTabCustomIcon, "sophisticatedBackpacks");
    }

    private static ItemStack getBackpackItem() {
        try {
            Class<?> itemsClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems");
            Field backpackField = itemsClass.getField("BACKPACK");
            Object supplier = backpackField.get(null);
            Method getMethod = supplier.getClass().getMethod("get");
            Item backpackItem = (Item) getMethod.invoke(supplier);
            return new ItemStack(backpackItem);
        } catch (Exception e) {
            return new ItemStack(Items.BUNDLE);
        }
    }

    @Override
    public void openTargetScreen(Player player) {
        try {
            Class<?> packetDistributorClass = Class.forName("net.neoforged.neoforge.network.PacketDistributor");
            Class<?> payloadClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.network.BackpackOpenPayload");
            Class<?> backpackItemClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem");

            // Always find a specific backpack and send the explicit (slot, "", handlerName)
            // form, even with BackpackSlot.DEFAULT. The empty-payload path defers to SB's
            // priority system, which respects the `allowOpeningFromSlot` server config — that
            // setting is false by default, so main-inventory backpacks never open from the
            // tab click. Specifying the slot bypasses that gate.
            ItemStack target = getBackpackFromPreferredSlot(player, backpackItemClass);
            if (target == null || target.isEmpty()) {
                target = findFirstBackpack(player, backpackItemClass);
            }

            Object payload;
            if (target != null && !target.isEmpty()) {
                int slotIndex = getSlotIndexForBackpack(player, target);
                if (slotIndex != -1) {
                    String handlerName;
                    String identifier = "";
                    int adjustedSlotIndex = slotIndex;
                    if (slotIndex <= 8) {
                        handlerName = "main";
                    } else if (slotIndex == 40) {
                        handlerName = "offhand";
                        adjustedSlotIndex = 0;
                    } else if (slotIndex >= 36 && slotIndex <= 39) {
                        handlerName = "armor";
                        adjustedSlotIndex = 0;
                    } else {
                        handlerName = "main";
                    }
                    payload = payloadClass.getDeclaredConstructor(int.class, String.class, String.class)
                            .newInstance(adjustedSlotIndex, identifier, handlerName);
                } else {
                    // Curios slot — no inventory index. Let SB's Curios integration resolve it.
                    payload = payloadClass.getDeclaredConstructor().newInstance();
                }
            } else {
                payload = payloadClass.getDeclaredConstructor().newInstance();
            }

            // Find the sendToServer method with 2 parameters
            for (Method method : packetDistributorClass.getDeclaredMethods()) {
                if (method.getName().equals("sendToServer") && method.getParameterCount() == 2) {
                    Class<?>[] paramTypes = method.getParameterTypes();
                    // Create empty array for the second parameter
                    Object[] emptyArray = (Object[]) java.lang.reflect.Array.newInstance(paramTypes[1].getComponentType(), 0);
                    method.invoke(null, payload, emptyArray);
                    return;
                }
            }

        } catch (Exception e) {
        }
    }

    private ItemStack findFirstBackpack(Player player, Class<?> backpackItemClass) {
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && backpackItemClass.isInstance(stack.getItem())) {
                return stack;
            }
        }
        ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty() && backpackItemClass.isInstance(offhand.getItem())) {
            return offhand;
        }
        for (ItemStack stack : player.getInventory().armor) {
            if (!stack.isEmpty() && backpackItemClass.isInstance(stack.getItem())) {
                return stack;
            }
        }
        if (ModIntegrationManager.isModLoaded(ModIntegration.CURIOS)) {
            ItemStack curio = findBackpackInCurios(player, backpackItemClass);
            if (curio != null) return curio;
        }
        return ItemStack.EMPTY;
    }

    private ItemStack findBackpackInCurios(Player player, Class<?> backpackItemClass) {
        try {
            Class<?> curiosAPIClass = Class.forName("top.theillusivec4.curios.api.CuriosApi");
            Method getCuriosInventory = curiosAPIClass.getDeclaredMethod("getCuriosInventory", net.minecraft.world.entity.LivingEntity.class);
            Object holder = getCuriosInventory.invoke(null, player);
            if (holder == null) return null;
            // 1.21.1 NeoForge returns Optional; orElse(null) keeps the call uniform with the
            // 1.20.1 Forge build, which returns LazyOptional (also exposes orElse).
            Method orElse = holder.getClass().getMethod("orElse", Object.class);
            Object curiosInventory = orElse.invoke(holder, (Object) null);
            if (curiosInventory == null) return null;
            Method findCurios = curiosInventory.getClass().getDeclaredMethod("findCurios", java.util.function.Predicate.class);
            java.util.function.Predicate<ItemStack> predicate = stack ->
                    !stack.isEmpty() && backpackItemClass.isInstance(stack.getItem());
            @SuppressWarnings("unchecked")
            java.util.List<Object> results = (java.util.List<Object>) findCurios.invoke(curiosInventory, predicate);
            if (results == null || results.isEmpty()) return null;
            Object first = results.get(0);
            Method stackMethod = first.getClass().getDeclaredMethod("stack");
            return (ItemStack) stackMethod.invoke(first);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean isEnabled(Player player) {
        return Config.Baked.sophisticatedBackpacksTabEnabled && ModIntegrationManager.isModLoaded(ModIntegration.SOPHISTICATED_BACKPACKS) && hasBackpack(player);
    }

    private ItemStack getBackpackFromPreferredSlot(Player player, Class<?> backpackItemClass) {
        BackpackSlot preferredSlot = Config.Baked.sophisticatedBackpacksPreferredSlot;

        if (preferredSlot == BackpackSlot.DEFAULT) {
            return null; // Use default behavior
        }

        try {
            switch (preferredSlot) {
                case QUICKBAR_1:
                case QUICKBAR_2:
                case QUICKBAR_3:
                case QUICKBAR_4:
                case QUICKBAR_5:
                case QUICKBAR_6:
                case QUICKBAR_7:
                case QUICKBAR_8:
                case QUICKBAR_9:
                    int hotbarIndex = preferredSlot.ordinal() - 1; // Convert QUICKBAR_1 (ordinal 1) to slot 0

                    if (hotbarIndex < 0 || hotbarIndex >= 9) {
                        break;
                    }

                    ItemStack hotbarStack = player.getInventory().items.get(hotbarIndex);

                    if (!hotbarStack.isEmpty() && backpackItemClass.isInstance(hotbarStack.getItem())) {
                        return hotbarStack;
                    }
                    break;

                case OFFHAND:
                    ItemStack offhandStack = player.getOffhandItem();
                    if (!offhandStack.isEmpty() && backpackItemClass.isInstance(offhandStack.getItem())) {
                        return offhandStack;
                    }
                    break;

                case MAIN_INVENTORY:
                    for (ItemStack stack : player.getInventory().items) {
                        if (!stack.isEmpty() && backpackItemClass.isInstance(stack.getItem())) {
                            return stack;
                        }
                    }
                    break;

                case ARMOR:
                    for (ItemStack stack : player.getInventory().armor) {
                        if (!stack.isEmpty() && backpackItemClass.isInstance(stack.getItem())) {
                            return stack;
                        }
                    }
                    break;

                case CURIOS:
                    if (ModIntegrationManager.isModLoaded(ModIntegration.CURIOS)) {
                        ItemStack curio = findBackpackInCurios(player, backpackItemClass);
                        if (curio != null) return curio;
                    }
                    break;

                case DEFAULT:
                    // Handled at method entry — fall through is unreachable but Java requires it.
                    break;
            }
        } catch (Exception e) {
            // Silently ignore errors
        }

        return null;
    }

    private int getSlotIndexForBackpack(Player player, ItemStack targetBackpack) {
        // Try to find the slot index of the target backpack
        try {
            // Check hotbar first (slots 0-8)
            for (int i = 0; i < 9; i++) {
                if (player.getInventory().items.get(i) == targetBackpack) {
                    return i;
                }
            }

            // Check main inventory (slots 9-35)
            for (int i = 9; i < player.getInventory().items.size(); i++) {
                if (player.getInventory().items.get(i) == targetBackpack) {
                    return i;
                }
            }

            // Check armor slots (slots 36-39)
            for (int i = 0; i < player.getInventory().armor.size(); i++) {
                if (player.getInventory().armor.get(i) == targetBackpack) {
                    return 36 + i;
                }
            }

            // Check offhand (slot 40)
            if (player.getOffhandItem() == targetBackpack) {
                return 40;
            }

        } catch (Exception e) {
            // Silently ignore errors
        }

        return -1; // Not found
    }

    private boolean hasBackpack(Player player) {
        try {
            Class<?> backpackItemClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem");

            // Check main inventory
            for (ItemStack stack : player.getInventory().items) {
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

            // Check Curios if available
            if (ModIntegrationManager.isModLoaded(ModIntegration.CURIOS)) {
                if (findBackpackInCurios(player, backpackItemClass) != null) return true;
            }

            return false;
        } catch (Exception e) {
            return false;
        }
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
    public Component getTooltip() {
        return Component.translatable("tooltip." + ModTabs.MOD_ID + ".tab.sophisticated_backpacks.description");
    }

    @Override
    public void initTabOnScreens() {
        ScreenRegistry.builder()
            .withStandardDimensions()
            .registerAllTabs("net.p3pp3rf1y.sophisticatedbackpacks.client.gui.BackpackScreen");
    }

}