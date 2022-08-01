package io.github.aparx.challenges.looping;

import com.google.common.base.Preconditions;
import io.github.aparx.challenges.looping.loadable.PluginLoadable;
import io.github.aparx.challenges.looping.loadable.modules.SchedulerModule;
import io.github.aparx.challenges.looping.loadable.variants.MainLoadable;
import io.github.aparx.challenges.looping.loadable.variants.ModuleManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import javax.validation.constraints.NotNull;

import static io.github.aparx.challenges.looping.PluginMagics.State.POST_LOAD;
import static io.github.aparx.challenges.looping.PluginMagics.State.PRE_LOAD;

public final class ChallengePlugin extends JavaPlugin {

    private static ChallengePlugin runningInstance;

    @NotNull
    public static ChallengePlugin getInstance() {
        return Preconditions.checkNotNull(runningInstance);
    }

    @NotNull
    public static LoadableRegister<PluginLoadable> getLoadables() {
        return runningInstance.getLoadableRegister();
    }

    @NotNull
    public static ModuleManager getModules() {
        return runningInstance.getModuleManager();
    }

    @NotNull
    public static SchedulerModule getScheduler() {
        return getModules().getInstance(SchedulerModule.class);
    }

    @NotNull
    public static PluginMagics getMagics() {
        return runningInstance.getPluginMagics();
    }

    /* Plugin implementation */

    @NotNull @Getter
    private final LoadableRegister<PluginLoadable> loadableRegister;

    @NotNull @Getter
    private final PluginMagics pluginMagics;

    @NotNull @Getter
    private final ModuleManager moduleManager;

    public ChallengePlugin() {
        ChallengePlugin.runningInstance = this;
        // Allocate a new register before the events can be called
        this.loadableRegister = new LoadableRegister<>(this);
        this.moduleManager = new ModuleManager(this);
        this.pluginMagics = new PluginMagics();
    }

    @Override
    public void onEnable() {
        try {
            // Plugin startup logic
            pluginMagics.setState(PRE_LOAD);
            pluginMagics.setDebugMode(false);
            moduleManager.registerDefaults(this);
            loadableRegister.register(new MainLoadable());
        } catch (Throwable t) {
            t.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        } finally {
            pluginMagics.setState(POST_LOAD);
        }
    }

    @Override
    public void onDisable() {
        if (!pluginMagics.isState(POST_LOAD)) return;
        loadableRegister.unregisterAll();
    }

}
