package io.github.aparx.challenges.looping.loadable.modules;

import io.github.aparx.challenges.looping.ChallengePlugin;
import io.github.aparx.challenges.looping.loadable.ChallengeModule;
import io.github.aparx.challenges.looping.scheduler.AbstractTask;
import io.github.aparx.challenges.looping.scheduler.DelegatedTask;
import io.github.aparx.challenges.looping.scheduler.GameScheduler;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.concurrent.ThreadLocalRandom;

import static org.bukkit.event.entity.EntityDamageEvent.DamageCause.ENTITY_EXPLOSION;

/**
 * Module responsible for continuously damaging entities after being
 * damaged and respawning those, killed by a player.
 *
 * @author aparx (Vinzent Zeband)
 * @version 16:28 CET, 03.08.2022
 * @since 1.0
 */
public final class EntityDamageModule
        extends ChallengeModule
        implements Listener {

    /**
     * After {@code 100} iterations in which an entity is damaged because
     * it was hit by a player, the entity will die with high certainty.
     * <p>In order to keep performance high, the implementation of this is
     * inevitable. It is necessary, due to the amount of schedulers being
     * run simultaneously.
     */
    public static final int MAX_ENTITY_ITERATIONS = 50;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDead(EntityDeathEvent event) {
        if (isNonProcessableEventOrMoment(event)) return;
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player) return;
        Class<? extends Entity> type = entity.getType().getEntityClass();
        if (type == null) return;
        Player killer = entity.getKiller();
        if (killer == null) {
            // Include entities whose death is through an explosion
            var lastDamage = entity.getLastDamageCause();
            if (isNonProcessableEventOrMoment(lastDamage)) return;
            if (lastDamage.getCause() != ENTITY_EXPLOSION) return;
        }
        final Location location = entity.getLocation().clone();
        SchedulerModule module = ChallengePlugin.getSchedulers();
        GameScheduler scheduler = module.getPrimaryScheduler();
        scheduler.attach(AbstractTask.instantOfChallenge(DelegatedTask.ofStop(task -> {
            final World world = location.getWorld();
            if (world == null) return;
            Entity spawn = world.spawn(location, type, e -> {
                // TODO actually clone entity's NBT
            });
            // TODO move to an EffectPlayer instance
            final double maxHeight = spawn.getHeight() / 2;
            world.spawnParticle(Particle.CLOUD, location, 4, 0, maxHeight, 0, 0.03, null, false);
            world.spawnParticle(Particle.FLAME, location, 4, 0, maxHeight, 0, 0.02, null, false);
        })));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityHit(EntityDamageByEntityEvent event) {
        if (isNonProcessableEventOrMoment(event)) return;
        // We do not include player entities and non-player damagers
        Entity entity = event.getEntity();
        if (entity instanceof Player) return;
        if (entity instanceof LivingEntity damagee) {
            final var debugger = ChallengePlugin.getDebugLogger();
            Entity damager = event.getDamager();
            if (!(damager instanceof Player)) return;
            SchedulerModule module = ChallengePlugin.getSchedulers();
            GameScheduler scheduler = module.getPrimaryScheduler();
            // This way of doing it may not be working after stopping the
            // challenge (is this not the goal?)
            scheduler.attach(AbstractTask.instantOfChallenge(DelegatedTask.ofStop(task -> {
                if (event.isCancelled() || !entity.isValid()) return;
                // In case the entity cannot die for a longer period
                // of time, we just kill the entity to signal that
                if (task.getCallAmount() >= MAX_ENTITY_ITERATIONS
                        && ThreadLocalRandom.current().nextBoolean()) {
                    damagee.setHealth(0.0);
                    debugger.info(() -> "Forced entity death (itr:%d)",
                            task.getCallAmount());
                    return;
                }
                damagee.damage(event.getDamage(), damager);
            })));
        }
    }
}
