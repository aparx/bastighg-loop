package io.github.aparx.challenges.looping.scheduler;

import com.google.common.base.Preconditions;
import io.github.aparx.challenges.looping.PluginConstants;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.index.qual.NonNegative;

import javax.validation.constraints.NotNull;

/**
 * @author aparx (Vinzent Zeband)
 * @version 13:19 CET, 01.08.2022
 * @since 1.0
 */
public final class RelativeDuration {

    public static RelativeDuration INSTANT = ofInstant();
    public static RelativeDuration CHALLENGE_INTERVAL_SURVIVOR = ofSurvivor(PluginConstants.CHALLENGE_INTERVAL);
    public static RelativeDuration CHALLENGE_DELAY_INSTANT = ofInstant(PluginConstants.CHALLENGE_INTERVAL);
    public static RelativeDuration NODELAY_LASTING = ofSurvivor(0);

    /* TaskDuration factory methods */

    @NotNull
    public static RelativeDuration ofInstant() {
        return ofInstant(0);
    }

    @NotNull
    public static RelativeDuration ofInstant(
            final @NonNegative long delay) {
        return ofSurvivor(delay, -1, 1);
    }

    @NotNull
    public static RelativeDuration ofSurvivor(
            final @NonNegative long period) {
        return ofSurvivor(period, period);
    }

    @NotNull
    public static RelativeDuration ofSurvivor(
            final @NonNegative long delay,
            final long interval) {
        return ofSurvivor(delay, interval, -1);
    }

    @NotNull
    public static RelativeDuration ofSurvivor(
            final @NonNegative long delay,
            final long interval,
            final long callLimit) {
        return new RelativeDuration(delay, interval, callLimit);
    }

    /* TaskDuration implementation */

    @NonNegative @Getter
    private final long delay;

    @Getter
    private final long interval, callLimit;

    public RelativeDuration(
            final @NonNegative long delay,
            final long interval,
            final long callLimit) {
        Preconditions.checkArgument(delay >= 0);
        this.delay = delay;
        this.interval = interval;
        this.callLimit = callLimit;
    }

    @NotNull
    public BukkitTask createTask(
            final @NotNull Plugin plugin,
            final @NotNull Runnable action) {
        Preconditions.checkNotNull(action);
        BukkitScheduler scheduler = Bukkit.getScheduler();
        long delay = Math.max(getDelay(), 0);
        long period = Math.max(getInterval(), 0);
        return isSurvivor() ?
                scheduler.runTaskTimer(plugin, action, delay, period)
                : scheduler.runTaskLater(plugin, action, delay);
    }

    public boolean isMatchingCycle(@NonNegative long tick) {
        if (hasDelay() && tick < getDelay()) return false;
        if (!hasInterval()) return true;
        return tick % getInterval() == 0;
    }

    public boolean hasDelay() {
        return delay > 0;
    }

    public boolean hasInterval() {
        return interval > 0;
    }

    public boolean isCallLimited() {
        return callLimit >= 0;
    }

    public boolean isSurvivor() {
        return interval >= 0;
    }

}
