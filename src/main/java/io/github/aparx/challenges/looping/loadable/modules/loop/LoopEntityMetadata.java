package io.github.aparx.challenges.looping.loadable.modules.loop;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.challenges.looping.ChallengePlugin;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.LazyMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

/**
 * @author aparx (Vinzent Zeband)
 * @version 12:16 CET, 02.08.2022
 * @since 1.0
 */
public class LoopEntityMetadata {

    @NotNull
    private final Entity entity;

    @NotNull @Getter
    private final String baseMetadataKey;

    public LoopEntityMetadata(
            final @NotNull Entity entity,
            final @NotNull String baseKey) {
        this.entity = Preconditions.checkNotNull(entity);
        this.baseMetadataKey = Preconditions.checkNotNull(baseKey);
    }

    @CanIgnoreReturnValue
    public final MetadataValue set(String key, Object value) {
        if (!containsExact(getBaseMetadataKey())) {
            writeExact(getBaseMetadataKey(), true);
        }
        return writeExact(createPath(key), value);
    }

    public final boolean hasBase() {
        return containsExact(getBaseMetadataKey());
    }

    public final boolean contains(String key) {
        return containsExact(createPath(key));
    }

    @CanIgnoreReturnValue
    public final Optional<MetadataValue> get(String key) {
        List<MetadataValue> metadata = entity.getMetadata(createPath(key));
        if (metadata.isEmpty()) return Optional.empty();
        return Optional.ofNullable(metadata.get(0));
    }

    public final Object getObject(String key) {
        final Optional<MetadataValue> metadataValue = get(key);
        return metadataValue.map(MetadataValue::value).orElse(null);
    }

    public final int getInt(String key) {
        final Optional<MetadataValue> metadataValue = get(key);
        return metadataValue.map(MetadataValue::asInt).orElse(0);
    }

    public final boolean getBoolean(String key) {
        final Optional<MetadataValue> metadataValue = get(key);
        return metadataValue.map(MetadataValue::asBoolean).orElse(false);
    }

    public final double getDouble(String key) {
        final Optional<MetadataValue> metadataValue = get(key);
        return metadataValue.map(MetadataValue::asDouble).orElse(0.0);
    }

    public final long getLong(String key) {
        final Optional<MetadataValue> metadataValue = get(key);
        return metadataValue.map(MetadataValue::asLong).orElse(0L);
    }

    @NotNull
    public String createPath(String path) {
        if (StringUtils.isEmpty(path))
            return getBaseMetadataKey();
        return getBaseMetadataKey() + '.' + path;
    }

    protected Plugin getOwningPlugin() {
        return ChallengePlugin.getInstance();
    }

    private MetadataValue writeExact(String exactPath, Object object) {
        var insert = new FixedMetadataValue(getOwningPlugin(), object);
        entity.setMetadata(exactPath, insert);
        return insert;
    }

    private boolean containsExact(String exactPath) {
        return entity.hasMetadata(exactPath);
    }

}
