package io.github.aparx.challenges.looping.scheduler;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author aparx (Vinzent Zeband)
 * @version 15:23 CET, 01.08.2022
 * @since 1.0
 */
public class GameScheduler extends AbstractTask {

    @NotNull @Getter
    private final Plugin plugin;

    volatile private BukkitTask task;

    @NotNull
    private final Map<@NotNull Integer, @NotNull AbstractTask>
            children = new ConcurrentHashMap<>();

    public GameScheduler(
            final @NotNull Plugin plugin) {
        this(plugin, RelativeDuration.NODELAY_LASTING);
    }

    public GameScheduler(
            final @NotNull Plugin plugin,
            final @NotNull RelativeDuration duration) {
        super(duration);
        this.plugin = Preconditions.checkNotNull(plugin);
    }

    /* Abstract non-abstract event methods */

    protected void onScheduleStart() {}
    protected void onScheduleStop() {}

    /* Method implementations */

    @Override
    public synchronized boolean isStarted() {
        return super.isStarted() && task != null;
    }

    @Override
    protected synchronized void onUpdate() {
        // Notifies all the children of the updates
        children.forEach((id, task) -> {
            if (!task.isStarted()) {
                // detaches task as it is stopped
                detach(task);
                return;
            }
            task.updateTask();
        });
    }

    @Override
    protected synchronized final void onStart() {
        if (isStarted()) return;
        task = RelativeDuration.NODELAY_LASTING
                .createTask(getPlugin(), this::updateTask);
        // Starts all in-memory children in case they are not started yet
        children.forEach((id, task) -> task.start());
        onScheduleStart();
    }

    @Override
    protected synchronized final void onStop() {
        if (!isStarted()) return;
        task.cancel();
        task = null;
        children.forEach((id, task) -> task.stop());
        onScheduleStop();
    }

    /* Child-operations */

    @CanIgnoreReturnValue
    public synchronized boolean detach(
            final @NotNull AbstractTask task) {
        // Delegates call with `task` to #detach(int)
        return detach(task.getTaskId());
    }

    @CanIgnoreReturnValue
    public synchronized boolean detach(int taskId) {
        AbstractTask detached = children.remove(taskId);
        if (detached == null) return false;
        detached.stop();    // stops the old task
        return true;
    }

    public synchronized void attach(
            final @NotNull RelativeDuration duration,
            final @NotNull DelegatedTask<AbstractTask> delegate) {
        Preconditions.checkNotNull(delegate);
        attach(AbstractTask.delegate(duration, delegate));
    }

    public synchronized void attach(
            final @NotNull AbstractTask task) {
        detach(task.getTaskId());   // remove the predecessor
        children.put(task.getTaskId(), task);
        if (isStarted()) task.start();
    }
}