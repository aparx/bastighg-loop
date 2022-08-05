package io.github.aparx.challenges.looping.loadable.modules;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.challenges.looping.loadable.ChallengeModule;
import io.github.aparx.challenges.looping.scheduler.GameScheduler;
import io.github.aparx.challenges.looping.scheduler.defaults.ChallengeScheduler;
import org.bukkit.plugin.Plugin;

import javax.imageio.stream.ImageInputStream;
import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author aparx (Vinzent Zeband)
 * @version 08:50 CET, 01.08.2022
 * @since 1.0
 */
public class SchedulerModule extends ChallengeModule {

    private final Map<@NotNull Integer, @NotNull GameScheduler>
            schedulerMap = new ConcurrentHashMap<>();

    private int primarySchedulerId;

    public synchronized void registerDefaults(Plugin plugin) {
        primarySchedulerId = register(new ChallengeScheduler(plugin));
    }

    @NotNull
    public synchronized ChallengeScheduler getPrimaryScheduler() {
        return (ChallengeScheduler) getScheduler(primarySchedulerId);
    }

    public synchronized boolean hasScheduler(int taskId) {
        return schedulerMap.containsKey(taskId);
    }

    @NotNull
    public synchronized GameScheduler getScheduler(int taskId) {
        return Preconditions.checkNotNull(schedulerMap.get(taskId));
    }

    @CanIgnoreReturnValue
    public synchronized boolean unregister(int taskId) {
        if (!hasScheduler(taskId)) return false;
        GameScheduler scheduler = getScheduler(taskId);
        if (!schedulerMap.remove(taskId, scheduler))
            return false;
        scheduler.stop();
        return true;
    }

    @CanIgnoreReturnValue
    public synchronized int register(@NotNull GameScheduler scheduler) {
        final int taskId = scheduler.getTaskId();
        if (hasScheduler(taskId))
            unregister(taskId);
        schedulerMap.put(taskId, scheduler);
        return taskId;
    }

    public synchronized void startAll() {
        schedulerMap.forEach((i, s) -> s.start());
    }

    public synchronized void stopAll() {
        schedulerMap.forEach((i, s) -> s.stop());
    }

    public synchronized void clear() {
        schedulerMap.forEach((i, s) -> unregister(i));
        schedulerMap.clear();   // in case something is not unregistered
    }

    @Override
    public synchronized void onLoad(Plugin plugin) throws Throwable {
        startAll();
    }

    @Override
    public synchronized void onUnload(Plugin plugin) throws Throwable {
        stopAll();
    }

    @Override
    protected synchronized void onResume() {
        schedulerMap.forEach((i, s) -> s.setPaused(false));
    }

    @Override
    protected synchronized void onPause() {
        schedulerMap.forEach((i, s) -> s.setPaused(true));
    }

}
