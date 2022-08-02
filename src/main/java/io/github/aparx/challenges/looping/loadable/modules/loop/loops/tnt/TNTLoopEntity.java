package io.github.aparx.challenges.looping.loadable.modules.loop.loops.tnt;

import io.github.aparx.challenges.looping.loadable.modules.loop.LoopEntity;
import io.github.aparx.challenges.looping.loadable.modules.loop.LoopEntityMetadata;
import io.github.aparx.challenges.looping.loadable.modules.loop.LoopModule;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * @author aparx (Vinzent Zeband)
 * @version 08:00 CET, 02.08.2022
 * @since 1.0
 */
public class TNTLoopEntity extends LoopEntity {

    private boolean loopDone = false;

    public TNTLoopEntity(
            final @NotNull ArmorStand entity,
            final @NotNull LoopModule<?> module) {
        super(entity, module, module.allocateMetadata(entity));
    }


    @Override
    protected void onLoop() {
        loopDone = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onDied() {
        getModule().invalidateEntity(this);
    }

    @Override
    protected void onUpdate() {
        if (loopDone && getCallAmount() > 0) {
            // TODO spawn tnt
            loopDone = false;
        }
        super.onUpdate();
    }

    @Override
    protected void onStart() {

    }

    @Override
    protected void onStop() {

    }
}
