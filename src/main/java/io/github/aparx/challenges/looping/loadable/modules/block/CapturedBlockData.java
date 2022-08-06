package io.github.aparx.challenges.looping.loadable.modules.block;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Map;

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

    @Nullable
    @CanIgnoreReturnValue
    public Object putInMap(Map<? super Location, ? super CapturedBlockData> target) {
        if (target == null) return null;
        return target.put(getLocation(), this);
    }

    @CanIgnoreReturnValue
    public boolean removeInMap(Map<? super Location, ? super CapturedBlockData> target) {
        if (target == null) return false;
        return target.remove(getLocation(), this);
    }

    @Override
    public String toString() {
        return "CapturedBlockData{" +
                "location=" + location +
                ", blockData=" + blockData +
                '}';
    }
}
