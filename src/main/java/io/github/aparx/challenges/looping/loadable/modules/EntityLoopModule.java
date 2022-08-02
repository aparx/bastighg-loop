package io.github.aparx.challenges.looping.loadable.modules;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.challenges.looping.LoadableRegister;
import io.github.aparx.challenges.looping.loadable.ChallengeModule;
import io.github.aparx.challenges.looping.loadable.ListenerLoadable;
import io.github.aparx.challenges.looping.loadable.modules.loop.LoopModule;
import io.github.aparx.challenges.looping.loadable.modules.loop.loops.tnt.TNTLoopModule;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.Plugin;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

/**
 * @author aparx (Vinzent Zeband)
 * @version 16:14 CET, 01.08.2022
 * @since 1.0
 */
public class EntityLoopModule
        extends ChallengeModule
        implements Listener {

    @SuppressWarnings("rawtypes")
    private final Map<Class<? extends LoopModule>, LoopModule<?>>
            LOOPS = new ConcurrentHashMap<>();

    @NotNull @Getter
    private final Plugin plugin;

    public EntityLoopModule(
            final @NotNull Plugin plugin) {
        this.plugin = Preconditions.checkNotNull(plugin);
    }

    public void registerDefaults() {
        register(new TNTLoopModule());
    }

    @SuppressWarnings("unchecked")
    public synchronized <T extends LoopModule<?>>
    T get(final @NotNull Class<T> ofType) {
        return (T) Preconditions.checkNotNull(LOOPS.get(ofType));
    }

    @CanIgnoreReturnValue
    @SuppressWarnings("rawtypes")
    public synchronized boolean unregister(
            final @NotNull Class<? extends LoopModule> module) {
        LoopModule<?> value = LOOPS.remove(module);
        if (value == null) return false;
        LoadableRegister.handleLoadAction(() -> value.unload(getPlugin()));
        return true;
    }

    @CanIgnoreReturnValue
    public synchronized boolean register(
            final @NotNull LoopModule<?> module) {
        LoopModule<?> mod = LOOPS.get(module.getClass());
        if (mod != null) return false;
        LOOPS.put(module.getClass(), module);
        LoadableRegister.handleLoadAction(() -> module.load(getPlugin()));
        return true;
    }

    @Override
    public void onLoad(Plugin plugin) throws Throwable {
        LOOPS.forEach((aClass, module) -> {
            // Load individual modules first, in case they were not
            // removed already within #onUnload() or added later
            LoadableRegister.handleLoadAction(() -> module.load(plugin));
        });
        registerDefaults();
    }

    @Override
    public void onUnload(Plugin plugin) throws Throwable {
        LOOPS.forEach((aClass, module) -> unregister(aClass));
    }

    /* Multi-Entity-Action methods */

    public void introduceEntities(@NotNull Iterator<? super ArmorStand> audience) {
        actionOnEntities(audience, LoopModule::introduceEntity);
    }

    public void invalidateEntities(@NotNull Iterator<? super ArmorStand> audience) {
        actionOnEntities(audience, LoopModule::invalidateEntity);
    }

    public synchronized void actionOnEntities(
            final @NotNull Iterator<? super ArmorStand> audience,
            final @NotNull BiPredicate<LoopModule<?>, ArmorStand> action) {
        while (audience.hasNext()) {
            final Object next = audience.next();
            if (!(next instanceof ArmorStand)) continue;
            final ArmorStand e = (ArmorStand) next;
            for (LoopModule<?> mod : LOOPS.values()) {
                if (action.test(mod, e)) break;
            }
        }
    }

    /* Chunk Event Handling */

    @EventHandler
    public synchronized void onChunkLoad(ChunkLoadEvent event) {
        Entity[] entities = event.getChunk().getEntities();
        if (ArrayUtils.isEmpty(entities)) return;
        introduceEntities(Arrays.stream(entities).iterator());
    }

    @EventHandler
    public synchronized void onChunkUnload(ChunkUnloadEvent event) {
        Entity[] entities = event.getChunk().getEntities();
        if (ArrayUtils.isEmpty(entities)) return;
        invalidateEntities(Arrays.stream(entities).iterator());
    }

}
