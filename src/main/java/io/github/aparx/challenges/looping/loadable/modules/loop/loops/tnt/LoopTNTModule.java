package io.github.aparx.challenges.looping.loadable.modules.loop.loops.tnt;

import io.github.aparx.challenges.looping.ChallengePlugin;
import io.github.aparx.challenges.looping.loadable.ListenerLoadable;
import io.github.aparx.challenges.looping.loadable.modules.BlockModule;
import io.github.aparx.challenges.looping.loadable.modules.block.CapturedStructure;
import io.github.aparx.challenges.looping.loadable.modules.loop.MetadataWrapper;
import io.github.aparx.challenges.looping.loadable.modules.loop.LoopModuleExtension;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.util.UUID;

/**
 * @author aparx (Vinzent Zeband)
 * @version 08:00 CET, 02.08.2022
 * @since 1.0
 */
public class LoopTNTModule
        extends LoopModuleExtension<LoopTNTEntity>
        implements ListenerLoadable {

    public static final String META_KEY = "tnt_loop";

    public LoopTNTModule() {
        super(META_KEY);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onSpawn(EntitySpawnEvent event) {
        if (isPaused() || event.isCancelled()) return;
        if (event.getEntityType() != EntityType.PRIMED_TNT) return;
        if (!event.getLocation().getChunk().isLoaded()) return;
        // Check if the TNT is spawned because of a loop spawning it
        if (getLinkedEntityFrom(event.getEntity()) == null) {
            LoopTNTEntity entity = spawnAndRegister(
                    ChallengePlugin.getInstance(), event.getLocation());
            entity.linkEntityToThis(event.getEntity());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onExplode(EntityExplodeEvent event) {
        if (isPaused() || event.isCancelled()) return;
        if (event.getEntityType() != EntityType.PRIMED_TNT) return;
        // Check if the TNT is spawned because of a loop spawning it
        UUID uuid = getSupposedLinkage(event.getEntity());
        LoopTNTEntity entity = getEntity(uuid);
        if (uuid != null && entity == null) {
            event.setCancelled(true);
            event.blockList().clear();
            return;
        }
        // Update later in case owner is found
        if (entity == null) return;
        entity.setResetBlocks(CapturedStructure.of(event.blockList(), b -> {
            // We do not include other TNTs in the range of ours, due to
            // them automatically being registered as looped TNTs
            return b.getType() != Material.TNT;
        }));
    }

    @Override
    public MetadataWrapper allocateMetadata(ArmorStand armorStand) {
        return new MetadataWrapper(armorStand, getMetadataKey());
    }

    @Override
    public LoopTNTEntity allocateEntity(ArmorStand armorStand) {
        return new LoopTNTEntity(armorStand, this);
    }

}
