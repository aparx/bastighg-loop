package io.github.aparx.challenges.looping.loadable.modules;

import com.google.common.base.Preconditions;
import io.github.aparx.challenges.looping.ChallengePlugin;
import io.github.aparx.challenges.looping.loadable.ChallengeModule;
import io.github.aparx.challenges.looping.loadable.modules.block.BlockStructures;
import io.github.aparx.challenges.looping.loadable.modules.block.CapturedBlockData;
import io.github.aparx.challenges.looping.loadable.modules.block.CapturedStructure;
import io.github.aparx.challenges.looping.scheduler.AbstractTask;
import io.github.aparx.challenges.looping.scheduler.GameScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.HashSet;

/**
 * @author aparx (Vinzent Zeband)
 * @version 07:30 CET, 01.08.2022
 * @since 1.0
 */
public class BlockModule extends ChallengeModule implements Listener {

    private static final HashSet<Location> PLACED_BLOCKS = new HashSet<>(16);
    private static final HashSet<Location> DESTROYED_BLOCKS = new HashSet<>(16);

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
        SchedulerModule schedulerModule = ChallengePlugin.getScheduler();
        GameScheduler scheduler = schedulerModule.getMainScheduler();
        scheduler.attach(AbstractTask.instantOfChallenge(task -> {
            if (event != null && event.isCancelled()) return;
            structure.placeCapture(l -> {
                PLACED_BLOCKS.remove(l);
                DESTROYED_BLOCKS.remove(l);
            });
        }));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isNonProcessableEventOrMoment(event)) return;
        BlockState blockReplacedState = event.getBlockReplacedState();
        BlockData blockData = blockReplacedState.getBlockData();
        Location location = blockReplacedState.getLocation();
        if (!PLACED_BLOCKS.add(location)) return;
        if (DESTROYED_BLOCKS.contains(location)) return;
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
        if (PLACED_BLOCKS.contains(location)) return;
        if (!DESTROYED_BLOCKS.add(location)) return;
        lateStructurePlacement(BlockStructures.getAffectedBlocks(block, true), event);
    }

}
