package vodmordia.modtabs.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

/**
 * Client → server request to convert an Eccentric Tome in the player's inventory
 * back into the book the player selected from the tome screen.
 *
 * Plain message class for Forge 1.20.1 SimpleChannel. The 1.21.1 NeoForge
 * variant of this code used {@code CustomPacketPayload} + {@code StreamCodec};
 * neither exists on 1.20.1.
 */
public class TomeConvertPayload {

    private final int tomeSlot;
    private final ItemStack selectedBook;

    public TomeConvertPayload(int tomeSlot, ItemStack selectedBook) {
        this.tomeSlot = tomeSlot;
        this.selectedBook = selectedBook;
    }

    public int tomeSlot() {
        return tomeSlot;
    }

    public ItemStack selectedBook() {
        return selectedBook;
    }

    public static void encode(TomeConvertPayload msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.tomeSlot);
        buf.writeItem(msg.selectedBook);
    }

    public static TomeConvertPayload decode(FriendlyByteBuf buf) {
        int slot = buf.readVarInt();
        ItemStack stack = buf.readItem();
        return new TomeConvertPayload(slot, stack);
    }
}
