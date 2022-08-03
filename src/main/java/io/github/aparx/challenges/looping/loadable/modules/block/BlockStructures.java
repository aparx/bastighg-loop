package io.github.aparx.challenges.looping.loadable.modules.block;

import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import javax.validation.constraints.NotNull;

/**
 * @author aparx (Vinzent Zeband)
 * @version 15:02 CET, 01.08.2022
 * @since 1.0
 */
public final class BlockStructures {

    public BlockStructures() {
        throw new AssertionError();
    }


    private static boolean isTypeConnectable(Material material) {
        if (material == null) return false;
        if (!material.isSolid() && material.isBlock()) return true;
        // ...
        return false;
    }

    @NotNull
    public static CapturedStructure getAffectedBlocks(
            final @NotNull Block center, boolean basedOnCenter) {
        // if above is fallable, include blocks above TODO
        CapturedStructure struct = CapturedStructure.EMPTY;
        if (basedOnCenter) {
            // Since the center does matter, we include it
            struct = struct.add(CapturedBlockData.capture(center));
        }
        return struct;
    }

    @NotNull
    public static CapturedStructure getAffectedBlocks(
            final @NotNull Location location,
            boolean includeCenter) {
        final World world = location.getWorld();
        Preconditions.checkNotNull(world);
        return getAffectedBlocks(world.getBlockAt(location), includeCenter);
    }


}