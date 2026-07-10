package vodmordia.modtabs.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import vodmordia.modtabs.ModTabs;

/**
 * Client → server request to open the player's own equipped Quark backpack.
 *
 * No payload fields — the server uses {@code context.player()} to locate the
 * wearer's chest-slot backpack and opens its {@link net.minecraft.world.MenuProvider}.
 */
public record OpenQuarkBackpackPayload() implements CustomPacketPayload {
    public static final Type<OpenQuarkBackpackPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ModTabs.MOD_ID, "open_quark_backpack"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenQuarkBackpackPayload> STREAM_CODEC =
            StreamCodec.unit(new OpenQuarkBackpackPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
