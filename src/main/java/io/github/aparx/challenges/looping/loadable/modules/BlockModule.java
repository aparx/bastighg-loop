package io.github.aparx.challenges.looping.loadable.modules;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.challenges.looping.ChallengePlugin;
import io.github.aparx.challenges.looping.loadable.ChallengeModule;
import io.github.aparx.challenges.looping.loadable.modules.block.BlockStructures;
import io.github.aparx.challenges.looping.loadable.modules.block.CapturedBlockData;
import io.github.aparx.challenges.looping.loadable.modules.block.CapturedStructure;
import io.github.aparx.challenges.looping.scheduler.AbstractTask;
import io.github.aparx.challenges.looping.scheduler.DelegatedTask;
import io.github.aparx.challenges.looping.scheduler.GameScheduler;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.function.Predicate;

/**
 * @author aparx (Vinzent Zeband)
 * @version 07:30 CET, 01.08.2022
 * @since 1.0
 */
public final class BlockModule extends ChallengeModule implements Listener {

    /**
     * The initial capacity used within sets registering positions of
     * block-events to prevent duplicate events to occur.
     */
    public static final int INITIAL_OCCUPANT_CAPACITY = 24;

    public static final Set<Location> OCCUPIED_BLOCKS
            = Collections.synchronizedSet(new HashSet<>(INITIAL_OCCUPANT_CAPACITY));

    public static final Predicate<Block> EXCLUDE_OCCUPIED_BLOCKS
            = block -> block != null && !isOccupied(block.getLocation());

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
            final @Nullable Cancellable event,
            boolean dropItems) {
        if (structure.isEmpty()) return;
        if (!occupyAll(structure.locationSet())) return;
        SchedulerModule schedulerModule = ChallengePlugin.getSchedulers();
        GameScheduler scheduler = schedulerModule.getPrimaryScheduler();
        scheduler.attach(AbstractTask.instantOfChallenge(DelegatedTask.ofStop(task -> {
            if (event != null && event.isCancelled()) return;
            structure.placeCapture((e, willDataBeUpdated) -> {
                final Location location = e.getLocation();
                OCCUPIED_BLOCKS.remove(location);
                if (!willDataBeUpdated || !dropItems) return;
                final World world = location.getWorld();
                if (world == null) return;
                BlockData current = world.getBlockData(location);
                Material material = current.getMaterial();
                if (material == Material.AIR) return;
                world.dropItemNaturally(location, new ItemStack(material));
            });
        })));
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
        var struct = BlockStructures.getAffectedBlocks(
                event.getBlock(), false, EXCLUDE_OCCUPIED_BLOCKS);
        struct = struct.add(CapturedBlockData.capture(location, blockData));
        lateStructurePlacement(struct, event, shouldDropBlocks(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (isNonProcessableEventOrMoment(event)) return;
        // TODO how do multi-structures behave with this set?
        var struct = BlockStructures.getAffectedBlocks(
                event.getBlock(), true, EXCLUDE_OCCUPIED_BLOCKS);
        lateStructurePlacement(struct, event, false);
    }

    public boolean shouldDropBlocks(Player player) {
        if (player == null) return false;
        return player.getGameMode() == GameMode.SURVIVAL;
    }

    @Override
    protected synchronized void onUnload(Plugin plugin) throws Throwable {
        OCCUPIED_BLOCKS.clear();
    }
}
