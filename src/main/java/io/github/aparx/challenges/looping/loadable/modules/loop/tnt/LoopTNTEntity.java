package io.github.aparx.challenges.looping.loadable.modules.loop.tnt;

import io.github.aparx.challenges.looping.PluginConstants;
import io.github.aparx.challenges.looping.loadable.modules.BlockModule;
import io.github.aparx.challenges.looping.loadable.modules.block.CapturedStructure;
import io.github.aparx.challenges.looping.loadable.modules.loop.LoopEntity;
import io.github.aparx.challenges.looping.loadable.modules.loop.LoopModuleExtension;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.TNTPrimed;

import javax.validation.constraints.NotNull;

// TODO chunk bug, as soon as the chunk is unloaded but a primed
//  tnt is still running, it will destroy the blocks whose will not be reset

/**
 * @author aparx (Vinzent Zeband)
 * @version 08:00 CET, 02.08.2022
 * @since 1.0
 */
public class LoopTNTEntity extends LoopEntity {

    private boolean loopDone = false;

    private CapturedStructure blockCache;

    public static final long INTERVAL_TIME
            = PluginConstants.CHALLENGE_INTERVAL + 80 /* default fuse time */;

    public LoopTNTEntity(
            final @NotNull ArmorStand entity,
            final @NotNull LoopModuleExtension<?> module) {
        super(entity, module,
                module.allocateMetadata(entity),
                /* running 10 times in INTERVAL_TIME */
                INTERVAL_TIME / 10);
    }

    @Override
    public void onInvalidate() {
        super.onInvalidate();
        // Since we left the chunk, or this entity got invalidated
        // whilst we still have blocks to reset, we do them immediately.
        if (blockCache == null) return;
        blockCache.placeCapture();
        blockCache = null;
    }

    @Override
    protected long getIntervalTime() {
        return INTERVAL_TIME;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onDied() {
        getModule().invalidateEntity(this);
    }

    @Override
    protected synchronized void onLoop() {
        loopDone = true;
        if (blockCache != null) {
            // Replace the cached blocks
            blockCache.placeCapture();
            BlockModule.freeAll(blockCache.locationSet());
            blockCache = null;
        }
    }

    @Override
    protected synchronized void onUpdate() {
        ArmorStand entity = getEntity();
        if (entity == null) return;
        if (loopDone && getCallAmount() > 1) {
            loopDone = false;
            Location location = entity.getLocation();
            World world = location.getWorld();
            if (world == null) return;
            world.spawn(location, TNTPrimed.class, this::linkEntityToThis);
        }
        super.onUpdate();
    }

    public void setResetBlocks(CapturedStructure blockCache) {
        if (this.blockCache != null)
            this.blockCache.placeCapture();
        this.blockCache = blockCache;
        BlockModule.occupyAll(blockCache.locationSet());
    }

    @Override
    protected void onStart() {

    }

    @Override
    protected void onStop() {

    }
}
