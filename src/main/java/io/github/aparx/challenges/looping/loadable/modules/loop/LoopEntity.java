package io.github.aparx.challenges.looping.loadable.modules.loop;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.challenges.looping.ChallengePlugin;
import io.github.aparx.challenges.looping.PluginConstants;
import io.github.aparx.challenges.looping.logger.DebugLogger;
import io.github.aparx.challenges.looping.scheduler.AbstractTask;
import io.github.aparx.challenges.looping.scheduler.GameScheduler;
import io.github.aparx.challenges.looping.scheduler.RelativeDuration;
import lombok.Getter;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.lang.ref.WeakReference;
import java.util.UUID;

import static io.github.aparx.challenges.looping.loadable.modules.loop.MetadataWrapper.KEY_CALL_AMOUNT;

/**
 * Weak-referenced {@code ArmorStand} entity decorator, being an
 * {@code AbstractTask}, using chunks and metadata to perform fast.
 *
 * @author aparx (Vinzent Zeband)
 * @version 07:32 CET, 02.08.2022
 * @since 1.0
 */
public abstract class LoopEntity extends AbstractTask {

    @NotNull @Getter
    @SuppressWarnings("rawtypes")
    private final LoopModuleExtension module;

    @NotNull
    private final WeakReference<ArmorStand> entityReference;

    // non-erasing identifiers of this armorstand
    private final int entityId;
    private final UUID entityUUID;

    @NotNull @Getter
    private MetadataWrapper metadata;

    public LoopEntity(
            final @NotNull ArmorStand entity,
            final @NotNull LoopModuleExtension<?> module,
            @NotNull MetadataWrapper metadata,
            final long certaintyUpdateSpeed) {
        // The smaller `certaintyUpdateSpeed` the higher the overall
        // performance of the entity
        super(RelativeDuration.ofSurvivor(certaintyUpdateSpeed));
        this.entityReference = new WeakReference<>(Preconditions.checkNotNull(entity));
        this.entityId = entity.getEntityId();
        this.entityUUID = entity.getUniqueId();
        this.module = Preconditions.checkNotNull(module);
        this.metadata = metadata;
        if (metadata != null) {
            setCallAmount(metadata.getLong(KEY_CALL_AMOUNT));
        }
    }

    // Event method called when the loop is done
    abstract protected void onLoop();

    abstract protected long getIntervalTime();

    @SuppressWarnings("unchecked")
    protected void onDied() {
        getModule().invalidateEntity(this);
    }

    protected void onIntroduce() {
        if (!PluginConstants.DEBUG_MODE) return;
        DebugLogger debugLogger = ChallengePlugin.getDebugLogger();
        debugLogger.info("Introducing %s (%s)",
                getClass().getSimpleName(), getUniqueId());
    }
    protected void onInvalidate() {
        if (!PluginConstants.DEBUG_MODE) return;
        DebugLogger debugLogger = ChallengePlugin.getDebugLogger();
        debugLogger.info("Invalidating %s (%s)",
                getClass().getSimpleName(), getUniqueId());
    }

    @Override
    protected void onUpdate() {
        final ArmorStand e = getEntity();
        if (metadata == null) return;
        // TODO moved e-null check from /\ to \/, bug causing?
        if (e == null || !e.isValid()) { onDied(); return; }
        // We do not do a reinterpreted cast to int, as it is unnecessary
        long steps = getStepsForLoop();
        long callAmount = getCallAmount();
        setSteps((int) (callAmount % steps));
        if (callAmount != 0 && getSteps() == 0) {
            loop(); // loops the entity
        }
    }

    /**
     * Does the actual loop, resetting all metadata.
     */
    public synchronized void loop() {
        onLoop();
        resetAttributes();
    }

    @CanIgnoreReturnValue
    public synchronized boolean tryUnload() {
        this.metadata = null;
        return true;
    }

    @CanIgnoreReturnValue
    public synchronized boolean tryReadLoad() {
        return metadata != null && metadata.hasBase();
    }

    @CanIgnoreReturnValue
    public synchronized boolean detachFromGameLoop(
            final @NotNull GameScheduler updater) {
        // Called when the entity's chunk is unloaded
        return updater.detach(this);
    }

    @CanIgnoreReturnValue
    public synchronized boolean attachToGameLoop(
            final @NotNull GameScheduler updater) {
        // Called when the entity's chunk is loaded
        if (isStarted()) return false;
        updater.attach(this);
        return true;
    }

    public void linkEntityToThis(Entity entity) {
        // "Notify" underlying events that the spawning entity is part
        // of this entity, thus do not use it as if a third party
        // had caused to spawn it
        new MetadataWrapper(entity, module.getMetadataKey())
                .set(LoopModuleExtension.KEY_UNIQUE_ID, getUniqueId());
    }

    public void setSteps(int n) {
        if (metadata == null) return;
        metadata.set("steps", n);
    }

    public int getSteps() {
        return metadata == null ? 0 : metadata.getInt("steps");
    }

    @Override
    public void setCallAmount(long callAmount) {
        super.setCallAmount(callAmount);
        if (isInvalid()) return;
        metadata.set(KEY_CALL_AMOUNT, callAmount);
    }

    public final UUID getUniqueId() {
        return entityUUID;
    }

    public final int getTemporaryId() {
        return entityId;
    }

    public long getStepsForLoop() {
        return getIntervalTime() / getDuration().getInterval();
    }

    public boolean isInvalid() {
        final ArmorStand entity = getEntity();
        return entity == null || !entity.isValid() || metadata == null;
    }

    @Nullable
    public ArmorStand requireEntity() {
        return Preconditions.checkNotNull(getEntity());
    }

    public ArmorStand getEntity() {
        return entityReference.get();
    }

    @Override
    protected void onStop() {
    }

    @Override
    protected void onStart() {
    }

    private void resetAttributes() {
        setSteps(0); // also done automatically through modulo
        setCallAmount(0);
    }

}