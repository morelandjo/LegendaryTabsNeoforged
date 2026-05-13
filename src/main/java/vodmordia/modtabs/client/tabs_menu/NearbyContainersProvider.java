package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import vodmordia.modtabs.api.tabs_menu.DynamicTabProvider;
import vodmordia.modtabs.api.tabs_menu.TabBase;
import vodmordia.modtabs.config.Config;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Discovers nearby container blocks around the player each screen-init and contributes one
 * {@link NearbyContainerTab} per pos. Detection is driven by
 * {@link BlockState#getMenuProvider}, so any block that opens a menu on right-click —
 * vanilla or modded — is picked up without a hardcoded list.
 */
public class NearbyContainersProvider implements DynamicTabProvider {

    @Override
    public void contribute(Player player, Screen screen, List<TabBase> out) {
        if (!Config.Baked.nearbyContainersTabEnabled) return;
        if (player == null || !player.isAlive()) return;
        Level level = player.level();
        if (level == null) return;

        int range = Math.max(1, Config.Baked.nearbyContainersTabRange);
        boolean sightCheck = Config.Baked.nearbyContainersTabRequireLineOfSight;

        Vec3 eye = player.getEyePosition();
        BlockPos origin = player.blockPosition();
        double rangeSq = (double) range * range;

        // Dedup set tracks the second half of double-chests once we accept the first half,
        // so we never emit two tabs for one logical chest.
        Set<BlockPos> claimedDoubleHalves = new HashSet<>();
        List<NearbyContainerTab> found = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -range; dy <= range; dy++) {
                for (int dz = -range; dz <= range; dz++) {
                    cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);

                    // Cheap distance reject before any state lookup.
                    Vec3 centre = new Vec3(cursor.getX() + 0.5, cursor.getY() + 0.5, cursor.getZ() + 0.5);
                    if (centre.distanceToSqr(eye) > rangeSq) continue;

                    BlockState state = level.getBlockState(cursor);
                    if (state.isAir()) continue;
                    // Any block that yields a MenuProvider on right-click is a container as
                    // far as we're concerned. Covers vanilla chests/barrels/shulker boxes/
                    // hoppers/dispensers/droppers/lecterns/brewing stands/etc. and any modded
                    // block that follows the standard right-click-to-open pattern.
                    if (state.getMenuProvider(level, cursor) == null) continue;

                    if (claimedDoubleHalves.contains(cursor)) continue;

                    // Double chests: drop the SECOND half so the player sees one tab per pair.
                    if (state.hasProperty(BlockStateProperties.CHEST_TYPE)) {
                        ChestType type = state.getValue(BlockStateProperties.CHEST_TYPE);
                        if (type == ChestType.LEFT || type == ChestType.RIGHT) {
                            BlockPos other = otherChestHalf(cursor, state, type);
                            if (claimedDoubleHalves.contains(other)) continue;
                            claimedDoubleHalves.add(other);
                        }
                    }

                    // Vanilla "chest is blocked" rule: opaque block (or cat) on top makes it
                    // un-openable. Honour the same gate so we don't emit a tab that produces
                    // no menu when clicked.
                    if (state.getBlock() instanceof ChestBlock
                            && ChestBlock.isChestBlockedAt(level, cursor)) {
                        continue;
                    }

                    if (sightCheck && !hasLineOfSight(level, player, eye, cursor)) {
                        continue;
                    }

                    found.add(new NearbyContainerTab(cursor.immutable()));
                }
            }
        }

        // Stable order: by encoded position. Without this, tabs would dance around as iter
        // order changes (it doesn't right now, but pre-sorting makes the contract explicit).
        found.sort(Comparator.comparingLong(t -> t.pos().asLong()));
        out.addAll(found);
    }

    private static BlockPos otherChestHalf(BlockPos pos, BlockState state, ChestType type) {
        Direction facing = state.getValue(ChestBlock.FACING);
        Direction toOther = (type == ChestType.LEFT) ? facing.getClockWise() : facing.getCounterClockWise();
        return pos.relative(toOther).immutable();
    }

    private static boolean hasLineOfSight(Level level, Player player, Vec3 eye, BlockPos target) {
        Vec3 targetCentre = new Vec3(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);
        BlockHitResult result = level.clip(new ClipContext(
                eye, targetCentre, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (result.getType() != HitResult.Type.BLOCK) return true;
        return result.getBlockPos().equals(target);
    }
}
