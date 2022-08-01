package io.github.aparx.challenges.looping.loadable.variants;

import com.google.common.base.Preconditions;
import io.github.aparx.challenges.looping.ChallengePlugin;
import io.github.aparx.challenges.looping.LoadableRegister;
import io.github.aparx.challenges.looping.PluginMagics;
import io.github.aparx.challenges.looping.loadable.ChallengeModule;
import io.github.aparx.challenges.looping.loadable.PluginLoadable;
import io.github.aparx.challenges.looping.loadable.modules.BlockModule;
import io.github.aparx.challenges.looping.loadable.modules.SchedulerModule;
import org.bukkit.plugin.Plugin;

import javax.validation.constraints.NotNull;

import static io.github.aparx.challenges.looping.PluginMagics.State.PRE_LOAD;

/**
 * @author aparx (Vinzent Zeband)
 * @version 07:32 CET, 01.08.2022
 * @since 1.0
 */
public class ModuleManager
        extends LoadableRegister<ChallengeModule>
        implements PluginLoadable {

    public ModuleManager(Plugin manager) {
        super(manager);
    }

    @Override
    public synchronized void load(Plugin plugin) throws Throwable {
        Preconditions.checkNotNull(plugin);
        // Omits the `load` notify to all modules
        forEach((aClass, challengeModule) -> {
            // Handles the load action for us
            handleLoadAction(() -> challengeModule.load(plugin));
        });
    }

    @Override
    public synchronized void unload(Plugin plugin) throws Throwable {
        Preconditions.checkNotNull(plugin);
        // Omits the `unload` notify to all modules
        forEach((aClass, challengeModule) -> {
            // Handles the load action for us
            handleLoadAction(() -> challengeModule.unload(plugin));
        });
    }

    public synchronized void registerDefaults(@NotNull Plugin plugin) {
        Preconditions.checkNotNull(plugin);
        final PluginMagics magics = ChallengePlugin.getMagics();
        Preconditions.checkArgument(magics.isState(PRE_LOAD));
        register(new SchedulerModule());
        register(new BlockModule());
    }

}
