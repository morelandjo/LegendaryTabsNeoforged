package vodmordia.modtabs.utils;

//import com.illusivesoulworks.diet.api.type.IDietSuite;
//import com.illusivesoulworks.diet.common.data.suite.DietSuites;
//import com.mrcrayfish.backpacked.item.BackpackItem;
//import com.mrcrayfish.backpacked.platform.Services;
import com.tiviacz.travelersbackpack.capability.AttachmentUtils;
import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

import java.util.Collection;
import java.util.Set;

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

        BackpackWrapper wrapper = AttachmentUtils.getBackpackWrapper(player);
        if (wrapper != null) {
            int slotCount = wrapper.getStorage().getSlots();
            boolean wider = slotCount > 81;
            boolean tanksVisible = wrapper.tanksVisible();
            return wider ? (tanksVisible ? 256 : 212) : (tanksVisible ? 220 : 176);
        }
        return 176;
    }

    public static int getTravelersBackpackHeight(Player player) {
        if (!ModIntegrationManager.isModLoaded(ModIntegration.TRAVELERS_BACKPACK))
            return 0;

        BackpackWrapper wrapper = AttachmentUtils.getBackpackWrapper(player);
        if (wrapper != null) {
            int slotCount = wrapper.getStorage().getSlots();
            boolean wider = slotCount > 81;
            int rowSlots = wider ? 11 : 9;
            int rows = (int)Math.ceil((double)slotCount / (double)rowSlots);

            int slotsHeight = rows * 18;
            int playerInventoryHeight = 96;
            return 17 + slotsHeight + playerInventoryHeight;
        }
        return 7 * 18 + 96 + 17;
    }
}
