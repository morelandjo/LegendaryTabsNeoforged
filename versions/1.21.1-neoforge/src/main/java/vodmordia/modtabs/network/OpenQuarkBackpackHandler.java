package vodmordia.modtabs.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.utils.ClassCache;
import vodmordia.modtabs.utils.ScreenClasses;

/**
 * Server handler for {@link OpenQuarkBackpackPayload}.
 *
 * Replicates the {@code open} branch of Quark's own {@code HandleBackpackMessage}: the
 * Quark backpack is worn in the chest slot and is itself a {@link MenuProvider}, so we
 * just open its menu. The carried (cursor) stack is preserved across the menu swap so
 * opening from a tab click never drops a held item — same dance Quark does.
 */
public class OpenQuarkBackpackHandler {

    public static void handle(OpenQuarkBackpackPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;

            try {
                Class<?> backpackItemClass = ClassCache.resolve(ScreenClasses.QUARK_BACKPACK_ITEM);
                if (backpackItemClass == null) return;

                ItemStack chest = serverPlayer.getItemBySlot(EquipmentSlot.CHEST);
                if (chest.isEmpty() || !backpackItemClass.isInstance(chest.getItem())) return;
                if (!(chest.getItem() instanceof MenuProvider menuProvider)) return;
                if (serverPlayer.containerMenu == null) return;

                ItemStack holding = serverPlayer.containerMenu.getCarried().copy();
                serverPlayer.containerMenu.setCarried(ItemStack.EMPTY);
                serverPlayer.openMenu(menuProvider);
                if (serverPlayer.containerMenu != null) {
                    serverPlayer.containerMenu.setCarried(holding);
                }
            } catch (Exception e) {
                ModTabs.LOGGER.debug("Failed to open Quark Backpack: " + e.getMessage());
            }
        });
    }
}
