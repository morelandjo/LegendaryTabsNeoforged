package vodmordia.modtabs.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.utils.ClassCache;
import vodmordia.modtabs.utils.ScreenClasses;

import java.util.function.Supplier;

/**
 * Server handler for {@link OpenQuarkBackpackPayload} (Forge 1.20.1).
 *
 * Replicates the {@code open} branch of Quark's own {@code HandleBackpackMessage}: the
 * Quark backpack is worn in the chest slot and is itself a {@link MenuProvider}, so we
 * open it via {@link NetworkHooks#openScreen} using the {@code BlockPos} variant Quark
 * uses (the backpack menu's client factory reads it). The carried (cursor) stack is
 * preserved across the menu swap so opening from a tab click never drops a held item.
 */
public class OpenQuarkBackpackHandler {

    public static void handle(OpenQuarkBackpackPayload payload, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer serverPlayer = ctx.getSender();
            if (serverPlayer == null) return;

            try {
                Class<?> backpackItemClass = ClassCache.resolve(ScreenClasses.QUARK_BACKPACK_ITEM);
                if (backpackItemClass == null) return;

                ItemStack chest = serverPlayer.getItemBySlot(EquipmentSlot.CHEST);
                if (chest.isEmpty() || !backpackItemClass.isInstance(chest.getItem())) return;
                if (!(chest.getItem() instanceof MenuProvider menuProvider)) return;
                if (serverPlayer.containerMenu == null) return;

                ItemStack holding = serverPlayer.containerMenu.getCarried().copy();
                serverPlayer.containerMenu.setCarried(ItemStack.EMPTY);
                NetworkHooks.openScreen(serverPlayer, menuProvider, serverPlayer.blockPosition());
                if (serverPlayer.containerMenu != null) {
                    serverPlayer.containerMenu.setCarried(holding);
                }
            } catch (Exception e) {
                ModTabs.LOGGER.debug("Failed to open Quark Backpack: " + e.getMessage());
            }
        });
        ctx.setPacketHandled(true);
    }
}
