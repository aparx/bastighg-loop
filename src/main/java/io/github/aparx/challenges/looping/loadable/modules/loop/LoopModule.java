package io.github.aparx.challenges.looping.loadable.modules.loop;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.challenges.looping.ChallengePlugin;
import io.github.aparx.challenges.looping.PluginConstants;
import io.github.aparx.challenges.looping.loadable.ChallengeModule;
import io.github.aparx.challenges.looping.loadable.modules.SchedulerModule;
import io.github.aparx.challenges.looping.scheduler.GameScheduler;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author aparx (Vinzent Zeband)
 * @version 08:13 CET, 02.08.2022
 * @since 1.0
 */
public abstract class LoopModule<T extends LoopEntity>
        extends ChallengeModule {

    @NotNull
    private final Map<Integer, T> entities = new ConcurrentHashMap<>();

    @NotNull @Getter
    private final String metadataKey;

    public LoopModule(final @NotNull String metadataKey) {
        this.metadataKey = Preconditions.checkNotNull(metadataKey);
    }

    abstract public LoopEntityMetadata allocateMetadata(@NotNull ArmorStand armorStand);

    abstract public T allocateEntity(@NotNull ArmorStand armorStand);

    @NotNull
    public final Map<Integer, T> getEntities() {
        return entities;
    }

    @CanIgnoreReturnValue
    public synchronized boolean introduceEntity(ArmorStand entity) {
        // Called to try to introduce an armorstand-entity
        if (entity == null) return false;
        if (!entity.hasMetadata(getMetadataKey()))
            return false;
        T newEntity = allocateEntity(entity);
        if (!newEntity.tryReadLoad()) return false;
        return introduceEntity(newEntity);
    }

    public synchronized boolean invalidateEntity(ArmorStand entity) {
        // Called to try to invalidate an armorstand-entity
        if (entity == null) return false;
        if (!entities.containsKey(entity.getEntityId()))
            return false;
        T decorator = entities.get(entity.getEntityId());
        if (decorator == null || !decorator.tryUnload()) return false;
        return invalidateEntity(decorator);
    }

    @CanIgnoreReturnValue
    public synchronized boolean spawnAndRegister(
            final @NotNull Plugin plugin,
            final @NotNull Location location) {
        ArmorStand entity = spawnNewEntity(plugin, location);
        return introduceEntity(allocateEntity(entity));
    }

    @CanIgnoreReturnValue
    public synchronized boolean introduceEntity(@NotNull T entity) {
        // `entity` is (re-)introduced, thus attached to the game-loop
        // again and stored in temporary memory
        registerEntity(entity);
        //Bukkit.broadcastMessage("§bintroduce " + entity);
        return entity.attachToGameLoop(getScheduler());
    }

    @CanIgnoreReturnValue
    public synchronized boolean invalidateEntity(@NotNull T entity) {
        // `entity` is going to be detached and unregistered,
        // clearing overall memory and giving the GC the ability
        // to cleanup
        if (!unregisterEntity(entity)) return false;
        //Bukkit.broadcastMessage("§cinvalidate " + entity);
        return entity.detachFromGameLoop(getScheduler());
    }

    @CanIgnoreReturnValue
    public synchronized boolean registerEntity(@NotNull T entity) {
        if (entities.containsKey(entity.getEntityId()))
            return false;
        entities.put(entity.getEntityId(), entity);
        return true;
    }

    @CanIgnoreReturnValue
    public synchronized boolean unregisterEntity(@NotNull T entity) {
        return entities.remove(entity.getEntityId(), entity);
    }

    @NotNull
    public ArmorStand spawnNewEntity(
            final @NotNull Plugin plugin,
            final @NotNull Location location) {
        // Spawns a new armorstand at given location
        Preconditions.checkNotNull(location);
        World world = location.getWorld();
        Preconditions.checkNotNull(world);
        ArmorStand e = (ArmorStand) world.spawnEntity(location, EntityType.ARMOR_STAND);
        if (PluginConstants.DEBUG_MODE) {
            e.setCustomNameVisible(true);
        }
        e.setInvisible(true);
        e.setInvulnerable(true);
        e.setAbsorptionAmount(Double.MAX_VALUE);
        e.setGravity(false);
        e.setSmall(true);
        e.setCollidable(false);
        e.setMetadata(getMetadataKey(), new FixedMetadataValue(plugin, true));
        return e;
    }

    /* Internal event overloads */

    @Override
    protected synchronized void onLoad(Plugin plugin) {
        plugin.getServer().getWorlds().forEach(world -> {
            // Gets all armorstand-entities and introduces them one by one
            world.getEntitiesByClass(ArmorStand.class).forEach(this::introduceEntity);
        });
    }

    @Override
    protected synchronized void onUnload(Plugin plugin) {
        entities.forEach((k, e) -> invalidateEntity(e));
    }

    @Override
    protected synchronized void onPause() {
        entities.forEach((k, e) -> e.setPaused(true));
    }

    @Override
    protected synchronized void onResume() {
        entities.forEach((k, e) -> e.setPaused(false));
    }

    @NotNull
    private GameScheduler getScheduler() {
        SchedulerModule module = ChallengePlugin.getScheduler();
        return module.getMainScheduler();
    }
}