package io.github.aparx.challenges.looping.loadable.variants;

import com.google.common.base.Preconditions;
import io.github.aparx.challenges.looping.ChallengePlugin;
import io.github.aparx.challenges.looping.LoadableRegister;
import io.github.aparx.challenges.looping.PluginMagics;
import io.github.aparx.challenges.looping.functional.IStatePauseable;
import io.github.aparx.challenges.looping.loadable.ChallengeModule;
import io.github.aparx.challenges.looping.loadable.PluginLoadable;
import io.github.aparx.challenges.looping.loadable.modules.BlockModule;
import io.github.aparx.challenges.looping.loadable.modules.EntityLoopModule;
import io.github.aparx.challenges.looping.loadable.modules.SchedulerModule;
import io.github.aparx.challenges.looping.loadable.modules.EntityDamageModule;
import org.bukkit.plugin.Plugin;

import javax.validation.constraints.NotNull;

import static io.github.aparx.challenges.looping.PluginMagics.PluginState.PRE_LOAD;

/**
 * @author aparx (Vinzent Zeband)
 * @version 07:32 CET, 01.08.2022
 * @since 1.0
 */
public class ModuleManager extends LoadableRegister<ChallengeModule> {

    public ModuleManager(Plugin manager) {
        super(manager);
    }

    public synchronized void registerDefaults(@NotNull Plugin plugin) {
        Preconditions.checkNotNull(plugin);
        final PluginMagics magics = ChallengePlugin.getMagics();
        Preconditions.checkArgument(magics.isState(PRE_LOAD));
        register(new SchedulerModule());
        getInstance(SchedulerModule.class).registerDefaults(plugin);
        register(new BlockModule());
        register(new EntityLoopModule(plugin));
        register(new EntityDamageModule());
    }

}
