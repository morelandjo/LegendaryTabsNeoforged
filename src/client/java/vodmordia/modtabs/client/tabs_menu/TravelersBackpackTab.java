package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import vodmordia.modtabs.api.tabs_menu.TabBase;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

public class TravelersBackpackTab extends TabBase {

    @Override
    public void openTargetScreen(PlayerEntity player) {
        try {
            // Check if wearing backpack (use OPEN_SCREEN action) or in inventory (use OPEN_BACKPACK action)
            Class<?> componentUtilsClass = Class.forName("com.tiviacz.travelersbackpack.component.ComponentUtils");
            java.lang.reflect.Method isWearingMethod = componentUtilsClass.getMethod("isWearingBackpack", net.minecraft.entity.player.PlayerEntity.class);
            boolean isWearing = (Boolean) isWearingMethod.invoke(null, player);

            Class<?> actionPacketClass = Class.forName("com.tiviacz.travelersbackpack.network.ServerboundActionTagPacket");
            java.lang.reflect.Method createPacketTagMethod = actionPacketClass.getDeclaredMethod("createPacketTag", int.class, Object[].class);

            Object compoundTag;
            if (isWearing) {
                // OPEN_SCREEN = 1 (for wearing backpack)
                compoundTag = createPacketTagMethod.invoke(null, 1, new Object[0]);
            } else {
                // OPEN_BACKPACK = 2 (for backpack in inventory)
                int backpackSlot = findBackpackSlot(player);
                if (backpackSlot == -1) return;
                compoundTag = createPacketTagMethod.invoke(null, 2, new Object[]{backpackSlot, false});
            }

            // Create and send packet
            Object packet = actionPacketClass.getDeclaredConstructor(compoundTag.getClass()).newInstance(compoundTag);
            java.lang.reflect.Method getPacketIdMethod = actionPacketClass.getMethod("getPacketId");
            Object packetId = getPacketIdMethod.invoke(packet);

            Class<?> packetByteBufsClass = Class.forName("net.fabricmc.fabric.api.networking.v1.PacketByteBufs");
            java.lang.reflect.Method createMethod = packetByteBufsClass.getMethod("create");
            Object byteBuf = createMethod.invoke(null);

            java.lang.reflect.Method encodeMethod = actionPacketClass.getMethod("encode", actionPacketClass, byteBuf.getClass());
            encodeMethod.invoke(packet, packet, byteBuf);

            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send((net.minecraft.util.Identifier) packetId, (net.minecraft.network.PacketByteBuf) byteBuf);
        } catch (Exception e) {
            // Silent fail - backpack mod not present or packet send failed
        }
    }

    private int findBackpackSlot(PlayerEntity player) {
        try {
            Class<?> backpackItemClass = Class.forName("com.tiviacz.travelersbackpack.items.TravelersBackpackItem");

            // Check main inventory (slots 0-35)
            for (int i = 0; i < player.getInventory().main.size(); i++) {
                ItemStack stack = player.getInventory().main.get(i);
                if (!stack.isEmpty() && backpackItemClass.isInstance(stack.getItem())) {
                    return i;
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return -1;
    }

    @Override
    public boolean isEnabled(PlayerEntity player) {
        return ModIntegrationManager.isModLoaded(ModIntegration.TRAVELERS_BACKPACK) && hasBackpack(player);
    }

    private boolean hasBackpack(PlayerEntity player) {
        try {
            Class<?> backpackItemClass = Class.forName("com.tiviacz.travelersbackpack.items.TravelersBackpackItem");

            // Check main inventory
            for (net.minecraft.item.ItemStack stack : player.getInventory().main) {
                if (!stack.isEmpty() && backpackItemClass.isInstance(stack.getItem())) {
                    return true;
                }
            }

            // Check armor slots
            for (net.minecraft.item.ItemStack stack : player.getInventory().armor) {
                if (!stack.isEmpty() && backpackItemClass.isInstance(stack.getItem())) {
                    return true;
                }
            }

            // Fallback to original AttachmentUtils check
            try {
                Class<?> attachmentUtilsClass = Class.forName("com.tiviacz.travelersbackpack.capability.AttachmentUtils");
                java.lang.reflect.Method isWearingBackpackMethod = attachmentUtilsClass.getMethod("isWearingBackpack", net.minecraft.entity.player.PlayerEntity.class);
                return (Boolean) isWearingBackpackMethod.invoke(null, player);
            } catch (Exception ex) {
                return false;
            }
        } catch (Exception e) {
            // If reflection fails, fall back to original method
            try {
                Class<?> attachmentUtilsClass = Class.forName("com.tiviacz.travelersbackpack.capability.AttachmentUtils");
                java.lang.reflect.Method isWearingBackpackMethod = attachmentUtilsClass.getMethod("isWearingBackpack", net.minecraft.entity.player.PlayerEntity.class);
                return (Boolean) isWearingBackpackMethod.invoke(null, player);
            } catch (Exception ex) {
                return false;
            }
        }
    }

    @Override
    public void initTabOnScreens() {
        // Register Traveler's Backpack screen with GUI-relative positioning
        vodmordia.modtabs.api.tabs_menu.ScreenRegistry.builder()
            .withStandardDimensions()
            .withPositioning(vodmordia.modtabs.api.tabs_menu.TabPositioning.GUI_RELATIVE)
            .registerAllTabs("com.tiviacz.travelersbackpack.client.screens.BackpackScreen");
    }

    @Override
    public void render(DrawContext gui, int x, int y, boolean hover) {
        // Try to get the actual backpack item for rendering
        Item backpackItem = getBackpackItem();
        if (backpackItem != null) {
            renderWithItem(gui, x, y, hover, new ItemStack(backpackItem));
        } else {
            vodmordia.modtabs.ModTabs.LOGGER.warn("TravelersBackpackTab using fallback leather chestplate - backpack item not found");
            // Fallback to leather backpack texture
            renderWithItem(gui, x, y, hover, new ItemStack(Items.LEATHER_CHESTPLATE));
        }
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        if (currentScreen == null) return false;

        // Check if current screen is a Traveler's Backpack screen
        String screenName = currentScreen.getClass().getName();
        return screenName.contains("travelersbackpack") && screenName.contains("screens");
    }

    @Override
    public Text getTooltip() {
        return Text.literal("Traveler's Backpack");
    }

    /**
     * Try to get the backpack item using reflection
     */
    private Item getBackpackItem() {
        try {
            Class<?> modItemsClass = Class.forName("com.tiviacz.travelersbackpack.init.ModItems");

            // Try common field names for backpack items
            String[] possibleFields = {
                "STANDARD_TRAVELERS_BACKPACK",
                "TRAVELERS_BACKPACK",
                "BACKPACK"
            };

            for (String fieldName : possibleFields) {
                try {
                    Object backpackSupplier = modItemsClass.getField(fieldName).get(null);
                    if (backpackSupplier instanceof Item) {
                        return (Item) backpackSupplier;
                    }

                    // Try get() method for Supplier types
                    try {
                        java.lang.reflect.Method getMethod = backpackSupplier.getClass().getMethod("get");
                        Object result = getMethod.invoke(backpackSupplier);
                        if (result instanceof Item) {
                            return (Item) result;
                        }
                    } catch (Exception e) {
                        // Continue to next field
                    }
                } catch (Exception e) {
                    // Field not found, continue
                }
            }
        } catch (Exception e) {
            // Class not found or other error
        }

        return null;
    }
}
