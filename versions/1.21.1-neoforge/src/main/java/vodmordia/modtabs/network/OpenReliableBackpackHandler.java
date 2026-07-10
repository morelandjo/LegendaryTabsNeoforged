package vodmordia.modtabs.network;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.utils.ClassCache;
import vodmordia.modtabs.utils.ScreenClasses;

import java.lang.reflect.Constructor;

/**
 * Server handler for {@link OpenReliableBackpackPayload}.
 *
 * Replicates the body of {@code EntityInteractionEvents.onEntityInteract} from the
 * Reliable Backpacks mod minus the {@code isBehind} proximity check, so the player
 * can open their own equipped backpack from a tab click. The mod's
 * {@code BackpackItemContainer} is used so persistence ({@code setChanged} → component)
 * and animation packets ({@code startOpen}/{@code stopOpen}) all work as the mod intends.
 *
 * If the backpack is in the player's inventory but not equipped, this handler swaps
 * it into the chest slot (mirroring vanilla {@code Equipable.swapWithEquipmentSlot})
 * before opening, so the tab works whether the player has the backpack worn or not.
 */
public class OpenReliableBackpackHandler {

    public static void handle(OpenReliableBackpackPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;

            try {
                Class<?> backpackItemClass = ClassCache.resolve(ScreenClasses.RELIABLE_BACKPACKS_ITEM);
                Class<?> containerClass = ClassCache.resolve(ScreenClasses.RELIABLE_BACKPACKS_CONTAINER);
                if (backpackItemClass == null || containerClass == null) return;

                ItemStack equipped = ensureBackpackEquipped(serverPlayer, backpackItemClass);
                if (equipped.isEmpty()) return;

                Constructor<?> ctor = containerClass.getDeclaredConstructor(LivingEntity.class, Player.class);
                Object containerObj = ctor.newInstance(serverPlayer, serverPlayer);
                if (!(containerObj instanceof SimpleContainer container)) return;

                // Hydrate from DataComponents.CONTAINER (mirrors onEntityInteract).
                if (!equipped.has(DataComponents.CONTAINER)) {
                    equipped.set(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
                }
                ItemContainerContents contents = equipped.get(DataComponents.CONTAINER);
                if (contents != null) {
                    contents.copyInto(container.getItems());
                }

                serverPlayer.openMenu(new SimpleMenuProvider(
                        (id, inv, p) -> new ShulkerBoxMenu(id, inv, container),
                        Component.translatable("container.backpack")
                ));
            } catch (Exception e) {
                ModTabs.LOGGER.debug("Failed to open Reliable Backpack: " + e.getMessage());
            }
        });
    }

    /**
     * Make sure a Reliable Backpacks backpack is in the chest slot. If one is in the
     * inventory or offhand, swap it with whatever is currently in the chest slot.
     * Returns the equipped backpack stack, or empty if no backpack was found anywhere.
     */
    private static ItemStack ensureBackpackEquipped(ServerPlayer player, Class<?> backpackItemClass) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!chest.isEmpty() && backpackItemClass.isInstance(chest.getItem())) {
            return chest;
        }

        Inventory inventory = player.getInventory();

        // Look in the 36 main inventory slots (hotbar + main).
        for (int i = 0; i < inventory.items.size(); i++) {
            ItemStack stack = inventory.items.get(i);
            if (!stack.isEmpty() && backpackItemClass.isInstance(stack.getItem())) {
                inventory.items.set(i, chest);
                player.setItemSlot(EquipmentSlot.CHEST, stack);
                return stack;
            }
        }

        // Offhand fallback.
        ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty() && backpackItemClass.isInstance(offhand.getItem())) {
            player.setItemSlot(EquipmentSlot.OFFHAND, chest);
            player.setItemSlot(EquipmentSlot.CHEST, offhand);
            return offhand;
        }

        return ItemStack.EMPTY;
    }
}
