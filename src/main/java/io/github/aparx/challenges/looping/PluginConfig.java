package io.github.aparx.challenges.looping;

import io.github.aparx.challenges.looping.loadable.config.AbstractConfig;
import io.github.aparx.challenges.looping.loadable.config.ConfigEntry;
import org.bukkit.plugin.Plugin;

import javax.validation.constraints.NotNull;

/**
 * @author aparx (Vinzent Zeband)
 * @version 01:29 CET, 07.08.2022
 * @since 1.0
 */
public class PluginConfig extends AbstractConfig {

    @NotNull
    public final ConfigEntry<PluginMagics.GameState> lastState
            = createEntry("game.lastState", PluginMagics.GameState.class);

    public final ConfigEntry<Long> gameTicks
            = createEntry("game.lastTicks", Long.class);

    public PluginConfig(
            final @NotNull Plugin plugin) {
        super(plugin, plugin.getConfig());
    }

    @Override
    public void save() {
        getPlugin().saveConfig();
    }

}
