package io.github.aparx.challenges.looping.loadable.modules;

import com.google.common.base.Preconditions;
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
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * @author aparx (Vinzent Zeband)
 * @version 07:30 CET, 01.08.2022
 * @since 1.0
 */
public class BlockModule extends ChallengeModule implements Listener {

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
            structure.placeCapture();
        }));
    }

    private static boolean shouldBeIgnored(Player player) {
        // TODO
        //return player.getGameMode() != GameMode.CREATIVE;
        return false;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isNonProcessableEventOrMoment(event)) return;
        if (shouldBeIgnored(event.getPlayer())) return;
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
        if (shouldBeIgnored(event.getPlayer())) return;
        lateStructurePlacement(BlockStructures.getAffectedBlocks(
                event.getBlock(), true), event);
    }

}
