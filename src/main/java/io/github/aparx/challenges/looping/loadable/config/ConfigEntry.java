package io.github.aparx.challenges.looping.loadable.config;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * @author aparx (Vinzent Zeband)
 * @version 01:31 CET, 07.08.2022
 * @since 1.0
 */
public final class ConfigEntry<T> {

    @NotNull @Getter
    private final String key;

    @NotNull @Getter
    private final AbstractConfig config;

    @NotNull @Getter
    private final Class<T> type;

    @Getter
    private final T defaultValue;

    public ConfigEntry(
            final @NotNull AbstractConfig config,
            final @NotNull String keyName,
            final @NotNull Class<T> type,
            final T defaultValue) {
        this.key = Preconditions.checkNotNull(keyName);
        this.config = Preconditions.checkNotNull(config);
        this.defaultValue = defaultValue;
        this.type = Preconditions.checkNotNull(type);
    }

    @Nullable
    public T get() {
        return get(defaultValue);
    }

    @Nullable
    @SuppressWarnings({"unchecked", "rawtypes"})
    public T get(T def) {
        if (type.isEnum()) {
            return (T) getAsEnum((Class) type, (Enum) def);
        }
        // Now we get the actual object through our type
        FileConfiguration config = getConfig().getConfiguration();
        return config.getObject(getKey(), getType(), def);
    }

    public long getAsLong() {
        return getAsLong(0);
    }

    public int getAsInt() {
        return getAsInt(0);
    }

    public double getAsDouble() {
        return getAsDouble(0);
    }

    @Nullable
    public String getAsString(String def) {
        return getConfig().getConfiguration().getString(getKey(), def);
    }

    public long getAsLong(long def) {
        return getConfig().getConfiguration().getLong(getKey(), def);
    }

    public int getAsInt(int def) {
        return getConfig().getConfiguration().getInt(getKey(), def);
    }

    public double getAsDouble(double def) {
        return getConfig().getConfiguration().getDouble(getKey(), def);
    }

    @Nullable
    public String getAsString() {
        return getAsString(null);
    }

    @Nullable
    public <E extends Enum<E>>
    E getAsEnum(@NotNull Class<E> enumType, @Nullable E def) {
        Preconditions.checkNotNull(enumType);
        String enumName = getAsString();
        if (StringUtils.isEmpty(enumName))
            return def;
        try {
            return Enum.valueOf(enumType, enumName);
        } catch (Exception e) {
            return def;
        }
    }

    public void set() {
        set(get());
    }

    public void set(T newValue) {
        Object saveValue = newValue;
        if (newValue != null && type.isEnum()) {
            saveValue = ((Enum<?>) newValue).name();
        }
        AbstractConfig config = getConfig();
        config.getConfiguration().set(key, saveValue);
        config.save();
    }

}
