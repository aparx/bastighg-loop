package io.github.aparx.challenges.looping;

import com.google.common.base.Preconditions;
import io.github.aparx.challenges.looping.loadable.PluginLoadable;
import io.github.aparx.challenges.looping.loadable.modules.SchedulerModule;
import io.github.aparx.challenges.looping.loadable.variants.MainLoadable;
import io.github.aparx.challenges.looping.loadable.variants.ModuleManager;
import io.github.aparx.challenges.looping.logger.DebugLogger;
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

    @NotNull
    public static DebugLogger getDebugLogger() {
        return getMagics().getDebugLogger();
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
        final DebugLogger logger = DebugLogger.of(
                getLogger(), pluginMagics::isDebugMode);
        try {
            // Plugin startup logic
            pluginMagics.setState(PRE_LOAD);
            pluginMagics.setDebugMode(PluginConstants.DEBUG_MODE);
            pluginMagics.setDebugLogger(logger);
            logger.info(() -> "Starting plugin in debug mode");
            moduleManager.registerDefaults(this);
            loadableRegister.register(moduleManager);
            loadableRegister.register(new MainLoadable());
        } catch (Throwable t) {
            t.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        } finally {
            logger.info(() -> "Pre-loading completed");
            pluginMagics.setState(POST_LOAD);
        }
    }

    @Override
    public void onDisable() {
        if (!pluginMagics.isState(POST_LOAD)) return;
        loadableRegister.unregisterAll();
    }

}
