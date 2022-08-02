package io.github.aparx.challenges.looping.loadable.modules.loop.loops.tnt;

import io.github.aparx.challenges.looping.ChallengePlugin;
import io.github.aparx.challenges.looping.loadable.ListenerLoadable;
import io.github.aparx.challenges.looping.loadable.modules.loop.LoopEntityMetadata;
import io.github.aparx.challenges.looping.loadable.modules.loop.LoopModule;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.util.EnumSet;

/**
 * @author aparx (Vinzent Zeband)
 * @version 08:00 CET, 02.08.2022
 * @since 1.0
 */
public class TNTLoopModule
        extends LoopModule<TNTLoopEntity>
        implements ListenerLoadable {

    public static final String META_KEY = "tnt_loop";

    private static final EnumSet<Material> TNT_PRIME_CAUSES;

    static {
        TNT_PRIME_CAUSES = EnumSet.noneOf(Material.class);
        TNT_PRIME_CAUSES.add(Material.FLINT_AND_STEEL);
        TNT_PRIME_CAUSES.add(Material.FIRE_CHARGE);
        // TODO
    }

    public TNTLoopModule() {
        super(META_KEY);
    }


    @EventHandler
    public void onSpawn(EntitySpawnEvent event) {
        if (event.getEntityType() == EntityType.PRIMED_TNT) {
            spawnAndRegister(ChallengePlugin.getInstance(), event.getLocation());
        }
    }

    @Override
    public LoopEntityMetadata allocateMetadata(ArmorStand armorStand) {
        return new LoopEntityMetadata(armorStand, getMetadataKey());
    }

    @Override
    public TNTLoopEntity allocateEntity(ArmorStand armorStand) {
        return new TNTLoopEntity(armorStand, this);
    }

    private boolean isPrimingItem(Material material) {
        return material.isItem() && TNT_PRIME_CAUSES.contains(material);
    }

}
