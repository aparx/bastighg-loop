package io.github.aparx.challenges.looping.loadable.modules.loop;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.challenges.looping.PluginConstants;
import io.github.aparx.challenges.looping.scheduler.AbstractTask;
import io.github.aparx.challenges.looping.scheduler.GameScheduler;
import io.github.aparx.challenges.looping.scheduler.RelativeDuration;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * @author aparx (Vinzent Zeband)
 * @version 07:32 CET, 02.08.2022
 * @since 1.0
 */
public abstract class LoopEntity extends AbstractTask {

    @NotNull @Getter
    @SuppressWarnings("rawtypes")
    private final LoopModule module;

    private final int entityId;

    @NotNull @Getter
    private ArmorStand entity;

    @NotNull @Getter
    private LoopEntityMetadata metadata;

    public LoopEntity(
            final @NotNull ArmorStand entity,
            final @NotNull LoopModule<?> module,
            @NotNull LoopEntityMetadata metadata) {
        super(RelativeDuration.ofSurvivor(PluginConstants.LOOP_ENTITY_UPDATE));
        this.entity = Preconditions.checkNotNull(entity);
        this.entityId = entity.getEntityId();
        this.module = Preconditions.checkNotNull(module);
        this.metadata = metadata;
    }

    // Event method called when the loop is done
    abstract protected void onLoop();

    @SuppressWarnings("unchecked")
    protected void onDied() {
        getModule().invalidateEntity(this);
    }

    @Override
    protected void onUpdate() {
        final ArmorStand e = getEntity();
        if (e == null || metadata == null) return;
        if (!e.isValid()) { onDied(); return; }
        // We do not do a reinterpreted cast to int, as it is unnecessary
        long steps = PluginConstants.CHALLENGE_INTERVAL / getDuration().getInterval();
        int newSteps = (int) (getCallAmount() % (1 + steps));
        metadata.set("steps", newSteps);
        if (newSteps >= steps) {
            loop(); // loops the entity
        }
    }

    /**
     * Does the actual loop, resetting all metadata.
     */
    public synchronized void loop() {
        if (metadata == null) return;
        metadata.set("steps", 0);
        onLoop();
    }

    @CanIgnoreReturnValue
    public synchronized boolean tryUnload() {
        this.entity = null;
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

    public final int getEntityId() {
        return entityId;
    }

    @Override
    protected void onStop() {}

    @Override
    protected void onStart() {}
}