package io.github.aparx.challenges.looping.loadable.modules.block;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import javax.validation.constraints.NotNull;

/**
 * @author aparx (Vinzent Zeband)
 * @version 10:51 CET, 01.08.2022
 * @since 1.0
 */
public final class CapturedBlockData {

    /* Class factory methods */

    @NotNull
    public static CapturedBlockData capture(@NotNull Block block) {
        return capture(block.getLocation(), block.getBlockData());
    }

    @NotNull
    public static CapturedBlockData reference(@NotNull Block block) {
        return reference(block.getLocation(), block.getBlockData());
    }

    @NotNull
    public static CapturedBlockData capture(
            final @NotNull Location location,
            final @NotNull BlockData blockData) {
        // TODO should actually the location be cloned as well?
        return reference(location, blockData.clone());
    }

    @NotNull
    public static CapturedBlockData reference(
            final @NotNull Location location,
            final @NotNull BlockData blockData) {
        return new CapturedBlockData(location, blockData);
    }

    /* Class implementation */

    @NotNull @Getter
    private final Location location;

    @NotNull @Getter
    private final BlockData blockData;

    CapturedBlockData(@NotNull Location location, @NotNull BlockData blockData) {
        this.location = Preconditions.checkNotNull(location);
        this.blockData = Preconditions.checkNotNull(blockData);
    }

}
