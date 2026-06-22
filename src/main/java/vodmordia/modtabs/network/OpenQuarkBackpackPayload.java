package vodmordia.modtabs.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Client → server request to open the player's own equipped Quark backpack.
 *
 * Plain message class for Forge 1.20.1 SimpleChannel (mirrors {@link TomeConvertPayload}).
 * No fields — the server uses the sender to locate the chest-slot backpack.
 */
public class OpenQuarkBackpackPayload {

    public OpenQuarkBackpackPayload() {
    }

    public static void encode(OpenQuarkBackpackPayload msg, FriendlyByteBuf buf) {
        // No payload fields.
    }

    public static OpenQuarkBackpackPayload decode(FriendlyByteBuf buf) {
        return new OpenQuarkBackpackPayload();
    }
}
