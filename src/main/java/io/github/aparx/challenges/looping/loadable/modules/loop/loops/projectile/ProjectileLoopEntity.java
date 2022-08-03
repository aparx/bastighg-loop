package io.github.aparx.challenges.looping.loadable.modules.loop.loops.projectile;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.challenges.looping.PluginConstants;
import io.github.aparx.challenges.looping.loadable.modules.loop.LoopEntity;
import io.github.aparx.challenges.looping.loadable.modules.loop.LoopEntityMetadata;
import io.github.aparx.challenges.looping.loadable.modules.loop.LoopModuleExtension;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * @author aparx (Vinzent Zeband)
 * @version 14:40 CET, 02.08.2022
 * @since 1.0
 */
public class ProjectileLoopEntity extends LoopEntity {

    public static final String KEY_TYPE = "type";
    public static final String KEY_VELOCITY = "velocity";
    public static final String KEY_PROJECTILE_UUID = "projUUID";

    public static final int INTERPOLATE_AMOUNT = 30;

    public static final long INTERVAL_TIME
            = PluginConstants.CHALLENGE_INTERVAL;

    private boolean bringBack = false;

    private Projectile projectile;

    private static final int RESET_ANIMATION_TIMER = 20;

    private int animationTimer;

    private Location hitLocation;

    public ProjectileLoopEntity(
            ArmorStand entity, LoopModuleExtension<?> module) {
        super(entity, module, module.allocateMetadata(entity), 1);
    }

    @NotNull
    public final Vector getForceVector() {
        LoopEntityMetadata metadata = getMetadata();
        if (metadata == null) return new Vector();
        return metadata.getObject(KEY_VELOCITY);
    }

    @Nullable
    public final Projectile getProjectile() {
        if (projectile != null || isInvalid())
            return projectile;
        LoopEntityMetadata meta = getMetadata();
        UUID uuid = meta.getObject(KEY_PROJECTILE_UUID);
        if (uuid == null) {
            shootNewProjectile(getForceVector());
            return projectile;
        }
        Entity target = Bukkit.getEntity(uuid);
        if (!(target instanceof Projectile)) return null;
        return projectile = (Projectile) target;
    }

    @Nullable
    public final EntityType getEntityType(EntityType def) {
        if (isInvalid()) return def;
        return getMetadata().getEnum(KEY_TYPE, EntityType.class, def);
    }

    @CanIgnoreReturnValue
    public final boolean setForceVector(@Nullable Vector vector) {
        if (isInvalid()) return false;
        LoopEntityMetadata metadata = getMetadata();
        metadata.set(KEY_VELOCITY, ensureNonnullVector(vector));
        return metadata.contains(KEY_VELOCITY);
    }

    @CanIgnoreReturnValue
    public final boolean setProjectile(@Nullable Projectile projectile) {
        if (isInvalid()) return false;
        this.projectile = projectile;
        if (projectile != null) {
            linkEntityToThis(projectile);
        }
        LoopEntityMetadata metadata = getMetadata();
        metadata.set(KEY_PROJECTILE_UUID, projectile == null
                ? null : projectile.getUniqueId());
        metadata.set(KEY_TYPE, projectile == null
                ? null : projectile.getType());
        return metadata.contains(KEY_PROJECTILE_UUID);
    }

    public void shootNewProjectile(Vector vector) {
        if (isInvalid()) return;
        shootNewProjectile(getEntityReference().getLocation(), vector);
    }

    public void shootNewProjectile(
            final Location location,
            @Nullable Vector vector) {
        shootNewProjectile(location, vector, null);
    }

    public void shootNewProjectile(
            final Location location,
            @Nullable Vector vector,
            @Nullable Consumer<Projectile> action) {
        shootNewProjectile(location, e -> {
            // Apply the velocity first
            applyVelocity(e, vector);
            if (action != null) action.accept(e);
        });
    }

    public void shootNewProjectile(
            final Location location,
            @Nullable Consumer<Projectile> action) {
        World world = location.getWorld();
        if (world == null) return;
        EntityType type = getEntityType(null);
        if (type == null) return;
        Class<? extends Entity> entityClass = type.getEntityClass();
        if (entityClass == null) return;
        setProjectile((Projectile) world.spawn(location, entityClass, e -> {
            linkEntityToThis(e);
            if (action != null) action.accept((Projectile) e);
        }));
    }

    public void applyVelocity(final Projectile projectile, @Nullable Vector vector) {
        if (projectile == null) return;
        projectile.setVelocity(ensureNonnullVector(vector));
    }

    @Override
    protected void onLoop() {
        // calculate all points between both the projectile and the entity
        if (isInvalid()) return;
        // notify the onUpdate() of the current state
        bringBack = true;
        if (projectile == null) return;
        if (hitLocation == null) return;
        projectile.remove();
        // f(x) = (x + 0.08) * 0.98 -> f(x) = (x - 0.02) / 1.02 ?
        ArmorStand entity = getEntityReference();
        Location endPos = entity.getLocation();
        Location hitPos = hitLocation.clone();
        final Vector initial = getForceVector();
        // First we copy the initial and just get the x- and z-coordinates
        Vector velocity = initial.clone();
        velocity.rotateAroundY(Math.toRadians(180f)).setY(0);
        double lxz = velocity.length();
        // Now we manipulate the y-velocity
        // TODO not working yet, prefer the old variant?
        velocity.setY((-initial.getY() + Math.pow(lxz, 0.08)) / Math.pow(1.02, lxz));
        shootNewProjectile(hitPos, velocity);
        projectile.setRotation(endPos.getYaw(), endPos.getPitch());
    }

    public void setHitLocation(Location hitLocation) {
        this.hitLocation = hitLocation;
    }

    @Override
    protected void onUpdate() {
        if (!bringBack || isInvalid()) {
            super.onUpdate();
            return;
        }
        // After we looped, we do not want automatic loop causes,
        // thus we count manually in the speed required.
        ArmorStand entity = getEntityReference();
        Projectile projectile = getProjectile();
        if (projectile == null || !projectile.isValid()) {
            // Causes the loop to be done
            if (projectile != null)
                projectile.remove();
            this.projectile = null;
            this.bringBack = false;
            this.animationTimer = RESET_ANIMATION_TIMER;
            setCallAmount(0);
            shootNewProjectile(getForceVector());
            return;
        }
        Location target = entity.getLocation();
        Location current = projectile.getLocation();
        double distance = target.distance(current);
        if (distance <= .5 || --animationTimer <= 0) {
            projectile.remove();
        }
    }

    @Override
    public synchronized boolean tryReadLoad() {
        if (!super.tryReadLoad()) return false;
        projectile = getProjectile();
        return true;
    }

    @Override
    public synchronized boolean tryUnload() {
        if (!super.tryUnload()) return false;
        if (projectile != null) {
            projectile.remove();
        }
        projectile = null;
        return true;
    }

    @Override
    protected long getIntervalTime() {
        return INTERVAL_TIME;
    }

    private static Vector ensureNonnullVector(Vector vector) {
        return vector == null ? new Vector() : vector;
    }

}
