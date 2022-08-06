package io.github.aparx.challenges.looping.loadable.modules.loop.projectile;

import com.google.common.collect.EnumMultiset;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.challenges.looping.ChallengePlugin;
import io.github.aparx.challenges.looping.PluginConstants;
import io.github.aparx.challenges.looping.loadable.modules.block.CapturedStructure;
import io.github.aparx.challenges.looping.loadable.modules.loop.LoopEntity;
import io.github.aparx.challenges.looping.loadable.modules.loop.LoopModuleExtension;
import io.github.aparx.challenges.looping.loadable.modules.loop.MetadataWrapper;
import io.github.aparx.challenges.looping.scheduler.RelativeDuration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.UUID;

/**
 * @author aparx (Vinzent Zeband)
 * @version 22:27 CET, 03.08.2022
 * @since 1.0
 */
public class LoopProjectileEntity extends LoopEntity {

    public static boolean isZeroVector(Vector v) {
        if (v == null) return true;
        return v.getX() == 0 && v.getY() == 0 && v.getZ() == 0;
    }

    public static boolean isValidVector(Vector v) {
        if (v == null) return false;
        return !Double.isInfinite(v.getX()) && !Double.isNaN(v.getX())
                && !Double.isInfinite(v.getY()) && !Double.isNaN(v.getY())
                && !Double.isInfinite(v.getZ()) && !Double.isNaN(v.getZ());
    }

    /* LoopProjectileEntity implementation */

    public static final String KEY_TYPE = "type";
    public static final String KEY_HAS_GRAVITY = "hasGravity";
    public static final String KEY_INITIAL_VELOCITY = "initial_velocity";
    public static final String KEY_PROJECTILE_UUID = "projectile_UUID";
    public static final String KEY_PROJECTILE_LAND = "projectile_HitPos";

    public static final int NORMAL_ANIM_TIME = 40;

    private UUID projectileId;
    private WeakReference<Projectile> weakProjectile;
    private CapturedStructure blockReset;
    volatile private Vector hitPosition;
    volatile private Vector initialVelocity;
    volatile private EntityType type;
    volatile private Boolean hasNaturalGravity = null;

    public boolean backTransition = false;
    private int animTime = NORMAL_ANIM_TIME;

    public LoopProjectileEntity(
            final @NotNull ArmorStand entity,
            final @NotNull LoopModuleExtension<?> module) {
        super(entity, module, module.allocateMetadata(entity), 1);
    }

    @CanIgnoreReturnValue
    public boolean spawnProjectile(@NotNull Vector vector) {
        if (isInvalid()) return false;
        ArmorStand thisEntity = Objects.requireNonNull(getEntity());
        return spawnProjectile(thisEntity.getLocation(), vector);
    }

    @CanIgnoreReturnValue
    public boolean spawnProjectile(
            final @NotNull Location location,
            @Nullable Vector vector) {
        return spawnProjectile(location, vector, null);
    }

    @CanIgnoreReturnValue
    public boolean spawnProjectile(
            final @NotNull Location location,
            @Nullable Vector vector,
            Consumer<Projectile> action) {
        Projectile projectile = getProjectile();
        // Alter the current source if possible, and then apply
        final ProjectileSource latestSource = projectile == null
                ? null : projectile.getShooter();
        // Tries to correct `vector` if it is invalid
        vector = ensureNonnullVector(vector);
        if (!isValidVector(vector)) {
            vector = getInitialVelocity();
            if (!isValidVector(vector))
                vector = new Vector();
        }
        final Vector v = vector;
        if (!spawnProjectile(location, e -> {
            e.setVelocity(v);
            e.setShooter(latestSource);
            // Backwards transition animation specific
            e.setBounce(e.doesBounce() && !backTransition);
            e.setInvulnerable(backTransition);
            if (action != null) action.accept(e);
        })) return false;
        // Reapplies velocity onto the projectile if possible
        projectile = getProjectile();
        if (projectile == null) return false;
        projectile.setVelocity(v);
        return true;
    }

    @CanIgnoreReturnValue
    public boolean spawnProjectile(
            final @NotNull Location location,
            Consumer<Projectile> action) {
        if (isInvalid()) return false;
        World world = location.getWorld();
        EntityType type = getType();
        if (world == null || type == null) return false;
        Class<? extends Entity> entityClass = type.getEntityClass();
        if (entityClass == null) return false;
        setProjectile((Projectile) world.spawn(location, entityClass, e -> {
            linkEntityToThis(e);
            setNaturalGravity(e.hasGravity());
            if (action != null) action.accept((Projectile) e);
        }));
        return true;
    }

    @Nullable
    public synchronized Projectile getProjectile() {
        if (weakProjectile == null) return null;
        // Get weak reference's projectile
        Projectile t = weakProjectile.get();
        if (t != null) return t;
        if (projectileId != null) {
            Entity entity = Bukkit.getEntity(projectileId);
            if (entity instanceof Projectile)
                setProjectile((Projectile) entity);
            if (weakProjectile != null)
                return weakProjectile.get();
        }
        weakProjectile = null;
        return null;
    }

    @Nullable
    public synchronized Vector getHitPosition() {
        if (hitPosition != null) return hitPosition;
        if (!isInvalid()) {
            MetadataWrapper metadata = getMetadata();
            Object object = metadata.getObject(KEY_PROJECTILE_LAND);
            if (object instanceof Vector v) return v;
        }
        return hitPosition = new Vector();
    }

    @NotNull
    public synchronized Vector getInitialVelocity() {
        if (isInvalid()) return ensureNonnullVector(initialVelocity);
        if (initialVelocity != null) return initialVelocity;
        final MetadataWrapper metadata = getMetadata();
        Object object = metadata.getObject(KEY_INITIAL_VELOCITY);
        if (!(object instanceof Vector)) object = null;
        return initialVelocity = ensureNonnullVector((Vector) object);
    }

    @Nullable
    public synchronized EntityType getType() {
        if (isInvalid()) return null;
        if (type != null) return type;
        final MetadataWrapper metadata = getMetadata();
        return type = metadata.getEnum(KEY_TYPE, EntityType.class, null);
    }

    public synchronized boolean hasNaturalGravity() {
        if (hasNaturalGravity != null || isInvalid())
            return hasNaturalGravity;
        MetadataWrapper metadata = getMetadata();
        if (metadata.contains(KEY_HAS_GRAVITY))
            return hasNaturalGravity = metadata.getBoolean(KEY_HAS_GRAVITY);
        return false;
    }

    public synchronized void setProjectile(Projectile projectile) {
        if (projectile == null) {
            this.weakProjectile = null;
            return;
        }
        this.weakProjectile = new WeakReference<>(projectile);
        this.projectileId = projectile.getUniqueId();
        if (isInvalid()) return;
        MetadataWrapper metadata = getMetadata();
        metadata.set(KEY_PROJECTILE_UUID, projectile.getUniqueId());
        metadata.set(KEY_HAS_GRAVITY, projectile.hasGravity());
        setEntityType(projectile.getType());
        linkEntityToThis(projectile);
    }

    public synchronized void setHitPosition(Vector vector) {
        hitPosition = ensureNonnullVector(vector);
        if (isInvalid()) return;
        MetadataWrapper metadata = getMetadata();
        metadata.set(KEY_PROJECTILE_LAND, hitPosition);
    }

    public synchronized void setInitialVelocity(Vector vector) {
        if (isInvalid()) return;
        vector = ensureNonnullVector(vector);
        final MetadataWrapper metadata = getMetadata();
        metadata.set(KEY_INITIAL_VELOCITY, vector);
    }

    public synchronized void setEntityType(EntityType type) {
        if (isInvalid()) return;
        final MetadataWrapper metadata = getMetadata();
        metadata.set(KEY_TYPE, type);
    }

    public synchronized void setNaturalGravity(boolean hasGravity) {
        this.hasNaturalGravity = hasGravity;
        if (isInvalid()) return;
        MetadataWrapper metadata = getMetadata();
        metadata.set(KEY_HAS_GRAVITY, hasGravity);
    }

    public void setBlockReset(@Nullable CapturedStructure blockReset) {
        this.blockReset = blockReset;
    }

    public void forceInitialSpawn(@Nullable Projectile projectile) {
        animTime = 0;
        backTransition = false;
        if (projectile != null)
            projectile.remove();
        spawnProjectile(getInitialVelocity());
        setCallAmount(0);
    }

    @Override
    protected void onInvalidate() {
        super.onInvalidate();
        if (blockReset != null) {
            blockReset.placeCapture();
            blockReset = null;
        }
        // Remove the back-animated projectile when invalidated
        if (!backTransition) return;
        Projectile projectile = getProjectile();
        if (projectile != null && projectile.isValid()) {
            projectile.remove();
        }
    }

    @Override
    protected void onLoop() {
        setCallAmount(0);
        backTransition = true;
        animTime = NORMAL_ANIM_TIME;
        if (blockReset != null) {
            blockReset.placeCapture();
            blockReset = null;
        }
        Projectile projectile = getProjectile();
        if (projectile != null) {
            projectile.remove();
        }
        if (isInvalid()) return;
        ArmorStand entity = getEntity();
        World world = entity.getWorld();
        Location thisPos = entity.getLocation();
        Vector hitVec = getHitPosition();
        if (hitVec == null) return;
        Vector velocity = thisPos.toVector().subtract(hitVec).normalize();
        if (!isValidVector(velocity)) {
            velocity = getInitialVelocity().clone().multiply(-1);
        }
        spawnProjectile(hitVec.toLocation(world), velocity);
        if ((projectile = getProjectile()) == null) return;
        projectile.setGravity(false);
    }

    @Override
    protected void onUpdate() {
        if (!backTransition || isInvalid()) {
            super.onUpdate();   // update first before doing the removal
            // TODO: projectile removal after it passes the hit-position
            // The current code is commented out, since it is working,
            // but not flawlessly due to the updating timer and speed of
            // velocity, not able to fully detect the distance. In case it
            // is required at one time, this code can simply be optimized
            // to the way it is required.
            /*if (backTransition || isInvalid() || !isStarted()) return;
            Projectile projectile = getProjectile();
            if (projectile == null || !projectile.isValid()) return;
            Vector hitPos = getHitPosition();
            if (!isValidVector(hitPos)) return;
            World world = projectile.getWorld();
            Location hitLoc = hitPos.toLocation(world);
            Location thisLoc = getEntity().getLocation();
            Location thatLoc = projectile.getLocation();
            if (thatLoc.distance(hitLoc) <= 2) {
                float yaw = thisLoc.getYaw(), pitch = thisLoc.getPitch();
                projectile.setVelocity(new Vector());
                projectile.setGravity(false);
                projectile.setRotation(yaw, pitch);
            }*/
            return;
        }
        ArmorStand thisEntity = getEntity();
        Projectile projectile = getProjectile();
        if (projectile == null
                || !projectile.isValid()
                || --animTime <= 0) {
            forceInitialSpawn(projectile);
            return;
        }
        Location thisLocation = thisEntity.getLocation();
        Location thatLocation = projectile.getLocation();
        double distance = thatLocation.distance(thisLocation);
        if (distance <= 1.2) {
            animTime = 0; // forces a loop next iteration
        }
    }

    @Override
    protected long getIntervalTime() {
        return PluginConstants.CHALLENGE_INTERVAL;
    }

    private static Vector ensureNonnullVector(Vector vector) {
        return vector == null ? new Vector() : vector;
    }

}
