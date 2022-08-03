package io.github.aparx.challenges.looping.loadable.modules.loop.loops.tnt;

import io.github.aparx.challenges.looping.ChallengePlugin;
import io.github.aparx.challenges.looping.loadable.ListenerLoadable;
import io.github.aparx.challenges.looping.loadable.modules.loop.LoopEntityMetadata;
import io.github.aparx.challenges.looping.loadable.modules.loop.LoopModuleExtension;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

/**
 * @author aparx (Vinzent Zeband)
 * @version 08:00 CET, 02.08.2022
 * @since 1.0
 */
public class TNTLoopModule
        extends LoopModuleExtension<TNTLoopEntity>
        implements ListenerLoadable {

    public static final String META_KEY = "tnt_loop";

    public TNTLoopModule() {
        super(META_KEY);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onSpawn(EntitySpawnEvent event) {
        if (isPaused() || event.isCancelled()) return;
        if (event.getEntityType() != EntityType.PRIMED_TNT) return;
        if (!event.getLocation().getChunk().isLoaded()) return;
        // Check if the TNT is spawned because of a loop spawning it
        if (getLinkedEntityFrom(event.getEntity()) == null) {
            TNTLoopEntity entity = spawnAndRegister(
                    ChallengePlugin.getInstance(), event.getLocation());
            entity.linkEntityToThis(event.getEntity());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onExplode(EntityExplodeEvent event) {
        if (isPaused() || event.isCancelled()) return;
        if (event.getEntityType() != EntityType.PRIMED_TNT) return;
        // Check if the TNT is spawned because of a loop spawning it
        TNTLoopEntity owner = getLinkedEntityFrom(event.getEntity());
        if (owner == null) return;
        owner.updateResetBlocks(event.blockList());
    }

    @Override
    public LoopEntityMetadata allocateMetadata(ArmorStand armorStand) {
        return new LoopEntityMetadata(armorStand, getMetadataKey());
    }

    @Override
    public TNTLoopEntity allocateEntity(ArmorStand armorStand) {
        return new TNTLoopEntity(armorStand, this);
    }

}
