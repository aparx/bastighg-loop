package io.github.aparx.challenges.looping;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.challenges.looping.functional.IStatePauseable;
import io.github.aparx.challenges.looping.functional.StatePauseable;
import io.github.aparx.challenges.looping.functional.ThrowableConsumer;
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
import java.util.function.Consumer;

/**
 * @author aparx (Vinzent Zeband)
 * @version 06:00 CET, 01.08.2022
 * @since 1.0
 */
public class LoadableRegister<T extends PluginLoadable>
        extends StatePauseable
        implements PluginLoadable {

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
        // Unload the target if already loaded
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

    /* Event implementations */

    @Override
    public synchronized void load(
            final @NotNull Plugin plugin) {
        Preconditions.checkNotNull(plugin);
        forEach((aClass, loadableModule) -> {
            // Omits the `load` notify to all modules
            handleLoadAction(() -> loadableModule.load(plugin));
        });
    }

    @Override
    public synchronized void unload(
            final @NotNull Plugin plugin) {
        Preconditions.checkNotNull(plugin);
        forEach((aClass, loadableModule) -> {
            // Omits the `unload` notify to all modules
            handleLoadAction(() -> loadableModule.unload(plugin));
        });
    }

    @Override
    protected void onPause() {
        notifyPauseUpdate(true);
    }

    @Override
    protected void onResume() {
        notifyPauseUpdate(false);
    }

    private synchronized void notifyPauseUpdate(boolean paused) {
        forEach((aClass, loadableModule) -> {
            if (!(loadableModule instanceof IStatePauseable)) return;
            ((IStatePauseable) loadableModule).setPaused(paused);
        });
    }

    public static void handleLoadAction(@NotNull ThrowableRunnable action) {
        try {
            action.run();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

}
