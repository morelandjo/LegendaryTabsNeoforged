package vodmordia.modtabs.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import vodmordia.modtabs.ModTabs;

/**
 * Client → server request to open the player's own equipped Reliable Backpack.
 *
 * No payload fields — the server uses {@code context.player()} to locate the
 * wearer's chest-slot backpack and runs the open logic itself.
 */
public record OpenReliableBackpackPayload() implements CustomPacketPayload {
    public static final Type<OpenReliableBackpackPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ModTabs.MOD_ID, "open_reliable_backpack"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenReliableBackpackPayload> STREAM_CODEC =
            StreamCodec.unit(new OpenReliableBackpackPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
