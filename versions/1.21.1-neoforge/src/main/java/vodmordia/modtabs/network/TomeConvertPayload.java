package vodmordia.modtabs.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import vodmordia.modtabs.ModTabs;

public record TomeConvertPayload(int tomeSlot, ItemStack selectedBook) implements CustomPacketPayload {
    public static final Type<TomeConvertPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModTabs.MOD_ID, "tome_convert"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TomeConvertPayload> STREAM_CODEC = StreamCodec.composite(
        net.minecraft.network.codec.ByteBufCodecs.VAR_INT,
        TomeConvertPayload::tomeSlot,
        ItemStack.STREAM_CODEC,
        TomeConvertPayload::selectedBook,
        TomeConvertPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
