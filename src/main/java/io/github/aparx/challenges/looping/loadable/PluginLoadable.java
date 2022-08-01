package io.github.aparx.challenges.looping.loadable;

import org.bukkit.plugin.Plugin;

import javax.validation.constraints.NotNull;

/**
 * @author aparx (Vinzent Zeband)
 * @version 06:00 CET, 01.08.2022
 * @since 1.0
 */
public interface PluginLoadable {

    /**
     * Loads this loadable entry.
     *
     * @param plugin The plugin causing the loading.
     * @throws Throwable if an error occurred
     */
    void load(@NotNull Plugin plugin) throws Throwable;

    /**
     * Unloads this loadable entry.
     *
     * @param plugin The plugin causing the unloading.
     * @throws Throwable if an error occurred
     */
    void unload(@NotNull Plugin plugin) throws Throwable;

}
