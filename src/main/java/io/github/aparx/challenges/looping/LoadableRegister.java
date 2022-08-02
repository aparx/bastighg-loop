package io.github.aparx.challenges.looping;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.challenges.looping.functional.ThrowableRunnable;
import io.github.aparx.challenges.looping.loadable.PluginLoadable;
import lombok.Getter;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * @author aparx (Vinzent Zeband)
 * @version 06:00 CET, 01.08.2022
 * @since 1.0
 */
public class LoadableRegister<T extends PluginLoadable> {

    @NotNull @Getter
    private final Plugin manager;

    // Map storing all our singleton objects associated to their origin
    private final Map<@NotNull Class<? extends T>, @NotNull T>
            table = new ConcurrentHashMap<>();

    public LoadableRegister(@NotNull Plugin manager) {
        this.manager = Preconditions.checkNotNull(manager);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public synchronized <_T extends T>
    _T getInstance(final @NotNull Class<_T> type) {
        // Lookup an instance associated with `type` and require nonnull
        var lookup = table.get(Preconditions.checkNotNull(type));
        return (_T) Preconditions.checkNotNull(lookup);
    }

    public synchronized boolean hasInstance(
            final @NotNull Class<? extends T> type) {
        return table.containsKey(Preconditions.checkNotNull(type));
    }

    @Nullable
    @CanIgnoreReturnValue
    @SuppressWarnings("unchecked")
    public synchronized T register(@NotNull T loadable) {
        // Unregister previous loadable and load-register given
        Preconditions.checkNotNull(loadable);
        Class<? extends T> decl = (Class<? extends T>) loadable.getClass();
        T predecessor = null;
        if (hasInstance(decl)) {
            // Unregister the last `decl`-associated instance first
            predecessor = unregister(decl);
        }
        table.put(decl, loadable);
        handleLoadAction(() -> loadable.load(getManager()));
        return predecessor;
    }

    @Nullable
    @CanIgnoreReturnValue
    public synchronized T unregister(
            final @NotNull Class<? extends T> type) {
        // Unregister with `type` associated loadable
        Preconditions.checkNotNull(type);
        if (!hasInstance(type)) return null;
        T target = table.remove(type);
        handleLoadAction(() -> target.unload(getManager()));
        return target;
    }

    public void unregisterAll() {
        for (T loadable : table.values()) {
            handleLoadAction(() -> loadable.unload(getManager()));
        }
    }

    public synchronized void forEach(
            @NotNull BiConsumer<Class<? extends T>, ? super T> action) {
        table.forEach(Preconditions.checkNotNull(action));
    }

    @NotNull
    public final Map<Class<? extends T>, T> getTable() {
        return table;
    }

    public static void handleLoadAction(@NotNull ThrowableRunnable action) {
        try {
            action.run();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

}
