package io.github.aparx.challenges.looping.loadable;

import io.github.aparx.challenges.looping.functional.StatePauseable;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * @author aparx (Vinzent Zeband)
 * @version 07:26 CET, 01.08.2022
 * @since 1.0
 */
public abstract class ChallengeModule
        extends StatePauseable
        implements PluginLoadable {

    volatile private boolean isLoaded;

    /* Non-abstract abstract event methods */

    protected synchronized void onLoad(
            @NotNull Plugin plugin) throws Throwable {}
    protected synchronized void onUnload(
            @NotNull Plugin plugin) throws Throwable {}

    public synchronized final boolean isLoaded() {
        return isLoaded;
    }

    @Override
    public synchronized final void load(Plugin plugin) throws Throwable {
        if (isLoaded) return;
        isLoaded = true;
        // Register this listener if this is a listener
        if (this instanceof Listener o)
            Bukkit.getPluginManager().registerEvents(o, plugin);
        onLoad(plugin);
    }

    @Override
    public synchronized final void unload(Plugin plugin) throws Throwable {
        if (!isLoaded) return;
        isLoaded = false;
        // Unregister this listener if this is a listener
        if (this instanceof Listener o)
            HandlerList.unregisterAll(o);
        onUnload(plugin);
    }

    public boolean isNonProcessableEventOrMoment(@Nullable Object event) {
        return isPaused() || event instanceof Cancellable
                && ((Cancellable) event).isCancelled();
    }
}
