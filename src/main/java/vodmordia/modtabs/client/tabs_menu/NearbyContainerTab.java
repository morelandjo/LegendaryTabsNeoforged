package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import vodmordia.modtabs.api.tabs_menu.ScaledItemTab;

import java.util.Objects;

/**
 * One transient tab per nearby container block (chest, barrel, shulker box, modded
 * inventories, …). The icon is the block's item form; the click action sends a synthetic
 * right-click to the server via {@code MultiPlayerGameMode.useItemOn}, which is the same
 * path the vanilla "look at chest and right-click" produces — the server handles
 * permissions, menu construction, and the open packet uniformly.
 *
 * <p>Tabs are rebuilt each screen-init by {@link NearbyContainersProvider}; we don't hold
 * a chunk reference beyond the {@link BlockPos} so a tab going stale (player walks away,
 * block is broken) just means the next init produces a different set.
 */
public class NearbyContainerTab extends ScaledItemTab {

    private final BlockPos blockPos;

    public NearbyContainerTab(BlockPos blockPos) {
        // Defer icon resolution to render time — the block at this pos can change (someone
        // breaks the chest mid-frame), and we want the rendered icon to track that until the
        // next init cycle drops the stale tab.
        super(() -> resolveIcon(blockPos), 1.0f);
        this.blockPos = blockPos.immutable();
    }

    public BlockPos pos() {
        return blockPos;
    }

    private static ItemStack resolveIcon(BlockPos pos) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return new ItemStack(Items.CHEST);
        BlockState state = level.getBlockState(pos);
        ItemStack stack = new ItemStack(state.getBlock().asItem());
        if (stack.isEmpty()) {
            // Some technical blocks don't have an item form (e.g. END_PORTAL_FRAME, command
            // blocks, certain modded machines). Fall back to a chest so the bar still shows
            // something rather than an invisible slot.
            return new ItemStack(Items.CHEST);
        }
        return stack;
    }

    @Override
    public void openTargetScreen(Player player) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.gameMode == null || mc.player == null
                || mc.getConnection() == null) {
            return;
        }

        // Bail if the tracked block is no longer a container — the tick-driven refresh
        // will catch up and drop this tab on the next pass, but if the click lands in
        // the meantime we must not close the player's current menu just to send a
        // useItemOn against air.
        BlockState currentState = mc.level.getBlockState(blockPos);
        if (currentState.isAir() || currentState.getMenuProvider(mc.level, blockPos) == null) {
            return;
        }

        // Close whatever menu the player has open (player inventory, or a previously-opened
        // chest) before triggering the new use-on. Without this, the server can briefly hold
        // two menus and the next click desyncs slot ids.
        AbstractContainerMenu currentMenu = mc.player.containerMenu;
        if (currentMenu != null) {
            mc.getConnection().send(new ServerboundContainerClosePacket(currentMenu.containerId));
        }

        // Synthetic hit on the centre of the target block. Direction.UP is a harmless choice —
        // the server's BlockBehaviour.use* path doesn't care about the face for container opens.
        BlockHitResult hit = new BlockHitResult(
                Vec3.atCenterOf(blockPos), Direction.UP, blockPos, false);
        mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
    }

    @Override
    public boolean isEnabled(Player player) {
        // Dynamic provider already filtered by range / menu-provider presence, and tab
        // instances are rebuilt every init, so by the time we get here the tab is valid
        // for this frame's tab bar.
        return true;
    }

    @Override
    public void initTabOnScreens() {
        // No-op: nearby tabs are contributed at runtime by NearbyContainersProvider rather
        // than statically registered to a fixed list of screens.
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        // We don't track which BlockPos owns the player's currently-open menu yet (vanilla
        // ChestMenu doesn't carry pos), so always leave the tab clickable. Re-clicking the
        // active container is a no-op on the server.
        return false;
    }

    @Override
    public Component getTooltip() {
        Level level = Minecraft.getInstance().level;
        if (level == null) return Component.empty();
        BlockEntity be = level.getBlockEntity(blockPos);
        if (be instanceof net.minecraft.world.Nameable nameable && nameable.hasCustomName()) {
            return nameable.getCustomName();
        }
        return level.getBlockState(blockPos).getBlock().getName();
    }

    @Override
    public int getOverrideOrder() {
        // No @TabConfig annotation, so TabRegistry returns 0 → these would sort alphabetically
        // among static tabs by class name. The provider appends after the sort, so this only
        // matters for cycleToNextTab; returning 0 keeps that consistent.
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NearbyContainerTab other)) return false;
        return blockPos.equals(other.blockPos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockPos);
    }
}
