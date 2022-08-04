package io.github.aparx.challenges.looping.loadable.modules.loop;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.challenges.looping.ChallengePlugin;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author aparx (Vinzent Zeband)
 * @version 12:16 CET, 02.08.2022
 * @since 1.0
 */
public final class MetadataWrapper {

    public static final String KEY_CALL_AMOUNT = "l_callAmount";

    @NotNull
    private final Entity entity;

    @NotNull @Getter
    private final String baseMetadataKey;
    public MetadataWrapper(
            final @NotNull Entity entity,
            final @NotNull String baseKey) {
        this.entity = Preconditions.checkNotNull(entity);
        this.baseMetadataKey = Preconditions.checkNotNull(baseKey);
    }

    @CanIgnoreReturnValue
    public void set(String key, Object value) {
        if (!containsExact(getBaseMetadataKey())) {
            writeExact(getBaseMetadataKey(), true);
        }
        writeExact(createPath(key), value);
    }

    public boolean hasBase() {
        return containsExact(getBaseMetadataKey());
    }

    public boolean contains(String key) {
        return containsExact(createPath(key));
    }

    @SuppressWarnings("unchecked")
    public <E extends Enum<E>>
    E getEnum(String key, Class<E> type, E defaultValue) {
        Optional<MetadataValue> metadataValue = get(key);
        if (metadataValue.isEmpty()) return defaultValue;
        MetadataValue value = metadataValue.get();
        final Object obj = value.value();
        if (type.isInstance(obj))
            return (E) obj;
        try {
            return Enum.valueOf(type, Objects.toString(obj));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getObject(String key) {
        final Optional<MetadataValue> metadataValue = get(key);
        return (T) metadataValue.map(MetadataValue::value).orElse(null);
    }

    @CanIgnoreReturnValue
    public Optional<MetadataValue> get(String key) {
        List<MetadataValue> metadata = entity.getMetadata(createPath(key));
        if (metadata.isEmpty()) return Optional.empty();
        return Optional.ofNullable(metadata.get(0));
    }

    public int getInt(String key) {
        final Optional<MetadataValue> metadataValue = get(key);
        return metadataValue.map(MetadataValue::asInt).orElse(0);
    }

    public boolean getBoolean(String key) {
        final Optional<MetadataValue> metadataValue = get(key);
        return metadataValue.map(MetadataValue::asBoolean).orElse(false);
    }

    public double getDouble(String key) {
        final Optional<MetadataValue> metadataValue = get(key);
        return metadataValue.map(MetadataValue::asDouble).orElse(0.0);
    }

    public long getLong(String key) {
        final Optional<MetadataValue> metadataValue = get(key);
        return metadataValue.map(MetadataValue::asLong).orElse(0L);
    }

    public String concatPath(String path, String secondary) {
        return path + '.' + secondary;
    }

    @NotNull
    public String createPath(String path) {
        if (StringUtils.isEmpty(path))
            return getBaseMetadataKey();
        return getBaseMetadataKey() + '.' + path;
    }

    @NotNull
    public String createPath(String... paths) {
        StringBuilder builder = new StringBuilder();
        if (!ArrayUtils.isEmpty(paths)) {
            for (int i = 0; i < paths.length; i++) {
                builder.append(paths[i]);
                if (i != paths.length - 1)
                    builder.append('.');
            }
        }
        return createPath(builder.toString());
    }

    @NotNull
    public Plugin getOwningPlugin() {
        return ChallengePlugin.getInstance();
    }

    private void writeExact(String exactPath, Object object) {
        if (object instanceof Enum<?>) {
            object = Objects.toString(object);
        }
        if (object == null) {
            entity.removeMetadata(exactPath, getOwningPlugin());
            return;
        }
        var insert = new FixedMetadataValue(getOwningPlugin(), object);
        entity.setMetadata(exactPath, insert);
    }

    private boolean containsExact(String exactPath) {
        return entity.hasMetadata(exactPath);
    }

}
