package vodmordia.modtabs.utils;

//import com.illusivesoulworks.diet.api.type.IDietSuite;
//import com.illusivesoulworks.diet.common.data.suite.DietSuites;
//import com.mrcrayfish.backpacked.item.BackpackItem;
//import com.mrcrayfish.backpacked.platform.Services;
//import com.tiviacz.travelersbackpack.capability.AttachmentUtils;
//import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

import java.util.Collection;
import java.util.Set;

public class IntegrationUtils {
    public IntegrationUtils(){}

    public static int getBackpackWidth(PlayerEntity player) {
        // Backpacked integration temporarily disabled - mod is in active development
        return 0;
        /*if (!ModTabs.backpackedLoaded)
            return 0;

        ItemStack backpack = Services.BACKPACK.getBackpackStack(player);
        BackpackItem backpackItem = (BackpackItem)backpack.getItem();
        return 14 + Math.max(backpackItem.getColumnCount(), 9) * 18;*/
    }

    public static int getBackpackHeight(PlayerEntity player) {
        // Backpacked integration temporarily disabled - mod is in active development
        return 0;
        /*if (!ModTabs.backpackedLoaded)
            return 0;

        ItemStack backpack = Services.BACKPACK.getBackpackStack(player);
        BackpackItem backpackItem = (BackpackItem)backpack.getItem();
        return 114 + backpackItem.getRowCount() * 18;*/
    }

    public static int getDietHeight(PlayerEntity player) {
        // Diet mod is disabled - returning default height
        if (!ModIntegrationManager.isModLoaded(ModIntegration.DIET))
            return 0;

        // Diet mod functionality commented out until it's updated to Fabric 1.20.1
        // if (MinecraftClient.getInstance().world == null)
        //     return 0;
        // return ((Collection<?>) com.illusivesoulworks.diet.platform.Services.CAPABILITY.get(player)
        //         .map(
        //                 (tracker) -> (Set) DietSuites.getSuite(MinecraftClient.getInstance().world, tracker.getSuite()).map(IDietSuite::getGroups).orElse(Set.of()))
        //         .orElse(Set.of())).size() * 20 + 60;
        return 0;
    }

    public static int getTravelersBackpackWidth(PlayerEntity player) {
        if (!ModIntegrationManager.isModLoaded(ModIntegration.TRAVELERS_BACKPACK))
            return 0;

        try {
            // Try to access Traveler's Backpack via reflection for Fabric
            Class<?> attachmentUtilsClass = Class.forName("com.tiviacz.travelersbackpack.capability.AttachmentUtils");
            java.lang.reflect.Method getBackpackWrapperMethod = attachmentUtilsClass.getMethod("getBackpackWrapper", PlayerEntity.class);

            Object wrapper = getBackpackWrapperMethod.invoke(null, player);
            if (wrapper != null) {
                // Get storage and check slot count
                java.lang.reflect.Method getStorageMethod = wrapper.getClass().getMethod("getStorage");
                Object storage = getStorageMethod.invoke(wrapper);

                java.lang.reflect.Method getSlotsMethod = storage.getClass().getMethod("getSlots");
                int slotCount = (int) getSlotsMethod.invoke(storage);

                // Check if tanks are visible
                java.lang.reflect.Method tanksVisibleMethod = wrapper.getClass().getMethod("tanksVisible");
                boolean tanksVisible = (boolean) tanksVisibleMethod.invoke(wrapper);

                boolean wider = slotCount > 81;
                return wider ? (tanksVisible ? 256 : 212) : (tanksVisible ? 220 : 176);
            }
        } catch (Exception e) {
        }

        return 176; // Default width
    }

    public static int getTravelersBackpackHeight(PlayerEntity player) {
        if (!ModIntegrationManager.isModLoaded(ModIntegration.TRAVELERS_BACKPACK))
            return 0;

        try {
            // Try to access Traveler's Backpack via reflection for Fabric
            Class<?> attachmentUtilsClass = Class.forName("com.tiviacz.travelersbackpack.capability.AttachmentUtils");
            java.lang.reflect.Method getBackpackWrapperMethod = attachmentUtilsClass.getMethod("getBackpackWrapper", PlayerEntity.class);

            Object wrapper = getBackpackWrapperMethod.invoke(null, player);
            if (wrapper != null) {
                // Get storage and check slot count
                java.lang.reflect.Method getStorageMethod = wrapper.getClass().getMethod("getStorage");
                Object storage = getStorageMethod.invoke(wrapper);

                java.lang.reflect.Method getSlotsMethod = storage.getClass().getMethod("getSlots");
                int slotCount = (int) getSlotsMethod.invoke(storage);

                boolean wider = slotCount > 81;
                int rowSlots = wider ? 11 : 9;
                int rows = (int)Math.ceil((double)slotCount / (double)rowSlots);

                int slotsHeight = rows * 18;
                int playerInventoryHeight = 96;
                return 17 + slotsHeight + playerInventoryHeight;
            }
        } catch (Exception e) {
        }

        return 7 * 18 + 96 + 17; // Default height
    }
}