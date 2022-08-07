package io.github.aparx.challenges.looping.loadable.config;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * @author aparx (Vinzent Zeband)
 * @version 01:33 CET, 07.08.2022
 * @since 1.0
 */
public abstract class AbstractConfig {

    @NotNull @Getter
    private final Plugin plugin;

    @NotNull @Getter
    private final FileConfiguration configuration;

    public AbstractConfig(Plugin plugin, FileConfiguration configuration) {
        this.plugin = Preconditions.checkNotNull(plugin);
        this.configuration = Preconditions.checkNotNull(configuration);
    }

    abstract public void save();

    @NotNull
    public <T> ConfigEntry<T> createEntry(
            final @NotNull String key,
            final @NotNull Class<T> valueType) {
        return createEntry(key, valueType, null);
    }

    @NotNull
    public <T> ConfigEntry<T> createEntry(
            final @NotNull String key,
            final @NotNull Class<T> valueType,
            final @Nullable T defaultValue) {
        return new ConfigEntry<>(this, key, valueType, defaultValue);
    }

}
