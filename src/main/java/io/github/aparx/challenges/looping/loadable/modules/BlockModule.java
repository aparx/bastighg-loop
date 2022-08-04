package io.github.aparx.challenges.looping.loadable.modules;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.challenges.looping.ChallengePlugin;
import io.github.aparx.challenges.looping.loadable.ChallengeModule;
import io.github.aparx.challenges.looping.loadable.modules.block.BlockStructures;
import io.github.aparx.challenges.looping.loadable.modules.block.CapturedBlockData;
import io.github.aparx.challenges.looping.loadable.modules.block.CapturedStructure;
import io.github.aparx.challenges.looping.scheduler.AbstractTask;
import io.github.aparx.challenges.looping.scheduler.GameScheduler;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * @author aparx (Vinzent Zeband)
 * @version 07:30 CET, 01.08.2022
 * @since 1.0
 */
public final class BlockModule extends ChallengeModule implements Listener {

    /**
     * The initial capacity used within Sets registering positions of
     * block-events to prevent duplicate events to occur.
     */
    private static final int INITIAL_CAPACITY = 24;

    public static final Set<Location> OCCUPIED_BLOCKS
            = Collections.synchronizedSet(new HashSet<>(INITIAL_CAPACITY));

    /**
     * Places given {@code structure} after the globally specified
     * challenge-interval.
     *
     * @param structure the structure to be placed
     * @param event     the cancellable to re-check whether to cancel,
     *                  {@code nullable}
     */
    public static void lateStructurePlacement(
            final @NotNull CapturedStructure structure,
            final @Nullable Cancellable event) {
        if (structure.isEmpty()) return;
        if (!occupyAll(structure.locationSet())) return;
        SchedulerModule schedulerModule = ChallengePlugin.getScheduler();
        GameScheduler scheduler = schedulerModule.getMainScheduler();
        scheduler.attach(AbstractTask.instantOfChallenge(task -> {
            if (event != null && event.isCancelled()) return;
            structure.placeCapture(OCCUPIED_BLOCKS::remove);
        }));
    }

    public static boolean isOccupied(Location location) {
        return OCCUPIED_BLOCKS.contains(location);
    }

    @CanIgnoreReturnValue
    public static boolean occupy(Location location) {
        if (location == null) return false;
        return OCCUPIED_BLOCKS.add(location);
    }

    @CanIgnoreReturnValue
    public static boolean occupyAll(Collection<Location> blockList) {
        if (blockList == null || blockList.isEmpty()) return false;
        return OCCUPIED_BLOCKS.addAll(blockList);
    }

    @CanIgnoreReturnValue
    public static boolean free(Location location) {
        if (location == null) return false;
        return OCCUPIED_BLOCKS.remove(location);
    }

    @CanIgnoreReturnValue
    public static boolean freeAll(Collection<Location> blockList) {
        if (blockList == null || blockList.isEmpty()) return false;
        return OCCUPIED_BLOCKS.removeAll(blockList);
    }

    /* The actual event handlers */

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isNonProcessableEventOrMoment(event)) return;
        BlockState blockReplacedState = event.getBlockReplacedState();
        BlockData blockData = blockReplacedState.getBlockData();
        Location location = blockReplacedState.getLocation();
        var struct = BlockStructures.getAffectedBlocks(event.getBlock(), false);
        struct = struct.add(CapturedBlockData.capture(location, blockData));
        lateStructurePlacement(struct, event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (isNonProcessableEventOrMoment(event)) return;
        Block block = event.getBlock();
        // TODO how do multi-structures behave with this set?
        Location location = block.getLocation();
        lateStructurePlacement(BlockStructures.getAffectedBlocks(block, true), event);
    }

}
