package vodmordia.modtabs.utils;

import net.minecraft.world.entity.player.Player;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

public class IntegrationUtils {
    public IntegrationUtils(){}

    public static int getBackpackWidth(Player player) {
        // Backpacked integration temporarily disabled - mod is in active development
        return 0;
        /*if (!ModTabs.backpackedLoaded)
            return 0;

        ItemStack backpack = Services.BACKPACK.getBackpackStack(player);
        BackpackItem backpackItem = (BackpackItem)backpack.getItem();
        return 14 + Math.max(backpackItem.getColumnCount(), 9) * 18;*/
    }

    public static int getBackpackHeight(Player player) {
        // Backpacked integration temporarily disabled - mod is in active development
        return 0;
        /*if (!ModTabs.backpackedLoaded)
            return 0;

        ItemStack backpack = Services.BACKPACK.getBackpackStack(player);
        BackpackItem backpackItem = (BackpackItem)backpack.getItem();
        return 114 + backpackItem.getRowCount() * 18;*/
    }

    public static int getDietHeight(Player player) {
        // Diet mod is disabled - returning default height
        if (!ModIntegrationManager.isModLoaded(ModIntegration.DIET))
            return 0;

        // Diet mod functionality commented out until it's updated to NeoForge 1.21.1
        // if (Minecraft.getInstance().level == null)
        //     return 0;
        // return ((Collection<?>) com.illusivesoulworks.diet.platform.Services.CAPABILITY.get(player)
        //         .map(
        //                 (tracker) -> (Set) DietSuites.getSuite(Minecraft.getInstance().level, tracker.getSuite()).map(IDietSuite::getGroups).orElse(Set.of()))
        //         .orElse(Set.of())).size() * 20 + 60;
        return 0;
    }

    public static int getTravelersBackpackWidth(Player player) {
        if (!ModIntegrationManager.isModLoaded(ModIntegration.TRAVELERS_BACKPACK))
            return 0;

        Object wrapper = getBackpackWrapper(player);
        if (wrapper != null) {
            try {
                int slotCount = ((Integer) wrapper.getClass().getMethod("getStorage").invoke(wrapper)
                        .getClass().getMethod("getSlots").invoke(wrapper.getClass().getMethod("getStorage").invoke(wrapper)));
                boolean wider = slotCount > 81;
                boolean tanksVisible = (Boolean) wrapper.getClass().getMethod("tanksVisible").invoke(wrapper);
                return wider ? (tanksVisible ? 256 : 212) : (tanksVisible ? 220 : 176);
            } catch (Exception ignored) {
            }
        }
        return 176;
    }

    public static int getTravelersBackpackHeight(Player player) {
        if (!ModIntegrationManager.isModLoaded(ModIntegration.TRAVELERS_BACKPACK))
            return 0;

        Object wrapper = getBackpackWrapper(player);
        if (wrapper != null) {
            try {
                Object storage = wrapper.getClass().getMethod("getStorage").invoke(wrapper);
                int slotCount = (Integer) storage.getClass().getMethod("getSlots").invoke(storage);
                boolean wider = slotCount > 81;
                int rowSlots = wider ? 11 : 9;
                int rows = (int)Math.ceil((double)slotCount / (double)rowSlots);

                int slotsHeight = rows * 18;
                int playerInventoryHeight = 96;
                return 17 + slotsHeight + playerInventoryHeight;
            } catch (Exception ignored) {
            }
        }
        return 7 * 18 + 96 + 17;
    }

    /**
     * Resolve the player's Travelers Backpack wrapper via reflection.
     *
     * <p>The 1.21.1 NeoForge build uses {@code AttachmentUtils.getBackpackWrapper(player)}, which
     * may not be the right entry point on the 1.20.1 Forge build of the mod (it historically used
     * a Forge {@link net.minecraftforge.common.capabilities.Capability Capability} on the player).
     * Try the attachment-style call first; if that class isn't there, fall back to {@code null} —
     * the callers default to vanilla-sized layout when the wrapper is missing.
     */
    private static Object getBackpackWrapper(Player player) {
        try {
            Class<?> attachmentUtilsClass = Class.forName("com.tiviacz.travelersbackpack.capability.AttachmentUtils");
            return attachmentUtilsClass.getMethod("getBackpackWrapper", Player.class).invoke(null, player);
        } catch (Exception e) {
            return null;
        }
    }
}
