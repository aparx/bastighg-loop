package io.github.aparx.challenges.looping.loadable;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

/**
 * @author aparx (Vinzent Zeband)
 * @version 06:28 CET, 01.08.2022
 * @see Listener
 * @see PluginLoadable
 * @since 1.0
 */
public interface ListenerLoadable extends Listener, PluginLoadable {

    @Override
    default void load(Plugin plugin) throws Throwable {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    default void unload(Plugin plugin) throws Throwable {
        HandlerList.unregisterAll(this);
    }

}