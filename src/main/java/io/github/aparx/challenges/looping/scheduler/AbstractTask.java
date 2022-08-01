package io.github.aparx.challenges.looping.scheduler;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.challenges.looping.functional.StatePauseable;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author aparx (Vinzent Zeband)
 * @version 15:28 CET, 01.08.2022
 * @since 1.0
 */
public abstract class AbstractTask extends StatePauseable {

    /* AbstractTask factory method */

    @NotNull
    public static AbstractTask delegate(
            final @NotNull RelativeDuration duration,
            final @NotNull DelegatedTask<? super AbstractTask> delegate) {
        Preconditions.checkNotNull(delegate);
        return new AbstractTask(duration) {
            @Override
            protected void onUpdate() {
                delegate.onUpdate(this);
            }

            @Override
            protected void onStart() {
                delegate.onStart(this);
            }

            @Override
            protected void onStop() {
                delegate.onStop(this);
            }
        };
    }

    /* AbstractTask implementation */

    @Getter
    private final int taskId;

    @NotNull @Getter @Setter
    private RelativeDuration duration;

    @Getter @Setter
    private long ticksAlive, callAmount;

    private boolean isStarted;


    public AbstractTask(@NotNull RelativeDuration duration) {
        this(ThreadLocalRandom.current().nextInt(), duration);
    }

    public AbstractTask(
            final int taskId,
            final @NotNull RelativeDuration duration) {
        this.taskId = taskId;
        this.duration = Preconditions.checkNotNull(duration);
    }

    /* Abstract event methods */

    protected void onUpdate() {
    }

    abstract protected void onStart();

    abstract protected void onStop();

    /* Method implementations */

    @CanIgnoreReturnValue
    public synchronized final boolean start() {
        if (isStarted()) return false;
        isStarted = true;
        onStart();
        // Return true if the task is really started
        return isStarted();
    }

    @CanIgnoreReturnValue
    public synchronized final boolean stop() {
        if (!isStarted()) return false;
        isStarted = false;
        onStop();
        return !isStarted();
    }

    public synchronized final void updateTask() {
        if (!isStarted() || isPaused()) return;
        if (!hasCallsLeft()) {
            stop();
            return;
        }
        if (!duration.isMatchingCycle(++ticksAlive)) return;
        // We use methods to give the possibility of overriding the
        // behaviour. Scheduled for change, due to pass by value cost. TODO
        setCallAmount(1 + getCallAmount());
        onUpdate(); // notify implementation about update
    }

    public synchronized boolean isStarted() {
        return isStarted;
    }

    public synchronized boolean hasCallsLeft() {
        if (!duration.isCallLimited()) return true;
        return duration.getCallLimit() > getCallAmount();
    }


}
