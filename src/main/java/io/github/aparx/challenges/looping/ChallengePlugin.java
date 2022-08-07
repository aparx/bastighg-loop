package io.github.aparx.challenges.looping;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.challenges.looping.command.CommandHandler;
import io.github.aparx.challenges.looping.command.commands.CommandInfo;
import io.github.aparx.challenges.looping.command.commands.CommandPause;
import io.github.aparx.challenges.looping.command.commands.CommandStart;
import io.github.aparx.challenges.looping.command.commands.CommandStop;
import io.github.aparx.challenges.looping.loadable.PluginLoadable;
import io.github.aparx.challenges.looping.loadable.modules.EntityLoopModule;
import io.github.aparx.challenges.looping.loadable.modules.SchedulerModule;
import io.github.aparx.challenges.looping.loadable.modules.PauseModule;
import io.github.aparx.challenges.looping.loadable.variants.ModuleManager;
import io.github.aparx.challenges.looping.logger.DebugLogger;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import javax.validation.constraints.NotNull;

import static io.github.aparx.challenges.looping.PluginMagics.GameState.*;
import static io.github.aparx.challenges.looping.PluginMagics.PluginState.*;

public final class ChallengePlugin extends JavaPlugin {

    private static ChallengePlugin runningInstance;

    @NotNull
    public static ChallengePlugin getInstance() {
        return Preconditions.checkNotNull(runningInstance);
    }

    @NotNull
    public static LoadableRegister<PluginLoadable> getLoadables() {
        return runningInstance.getMainRegister();
    }

    @NotNull
    public static ModuleManager getModules() {
        return runningInstance.getModuleManager();
    }

    @NotNull
    public static SchedulerModule getSchedulers() {
        return getModules().getInstance(SchedulerModule.class);
    }

    @NotNull
    public static PluginMagics getMagics() {
        return runningInstance.getPluginMagics();
    }

    public static boolean isLoadState(PluginMagics.PluginState state) {
        return runningInstance != null && getMagics().isLoadState(state);
    }

    public static boolean isGameState(PluginMagics.GameState state) {
        return runningInstance != null && getMagics().getGameState() == state;
    }

    public static boolean isGameImplyingStart() {
        return runningInstance != null && getMagics().isGameImplyingStart();
    }


    @NotNull
    public static DebugLogger getDebugLogger() {
        return getMagics().getDebugLogger();
    }

    /* Plugin implementation */

    @NotNull @Getter
    private final LoadableRegister<PluginLoadable> mainRegister;

    @NotNull @Getter
    private final PluginMagics pluginMagics;

    @NotNull @Getter
    private final ModuleManager moduleManager;

    @NotNull @Getter
    private final PluginConfig pluginConfig;

    @NotNull @Getter
    private CommandHandler commandHandler;

    public ChallengePlugin() {
        ChallengePlugin.runningInstance = this;
        // Allocate a new register before the events can be called
        this.mainRegister = new LoadableRegister<>(this);
        this.moduleManager = new ModuleManager(this);
        this.pluginMagics = new PluginMagics();
        this.pluginConfig = new PluginConfig(this);
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
            // Registers default loadables and modules
            moduleManager.registerDefaults(this);
            mainRegister.register(moduleManager);
            mainRegister.register(new PauseModule());
            // Registers all default commands
            commandHandler = new CommandHandler(this);
            commandHandler.add(new CommandPause(this));
            commandHandler.add(new CommandStart(this));
            commandHandler.add(new CommandStop(this));
            commandHandler.add(new CommandInfo(this));
        } catch (Throwable t) {
            t.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        } finally {
            logger.info(() -> "Pre-loading completed");
            pluginMagics.setState(POST_LOAD);
            updateGameState(pluginConfig.lastState.get());
        }
    }

    @Override
    public void onDisable() {
        if (!pluginMagics.isLoadState(POST_LOAD)) return;
        mainRegister.unregisterAll();
    }

    /**
     * Updates the challenge to given state and notifies underlying
     * loadables about the change, with additional evaluation.
     * <p>This method is called with the explicit intention of either,
     * starting, stopping or pausing the challenge. It is not called when
     * the plugin is temporarily disabled.
     *
     * @param newState the new state of the challenge
     * @return true if the state was changed
     */
    @CanIgnoreReturnValue
    public boolean updateGameState(PluginMagics.GameState newState) {
        Preconditions.checkArgument(isLoadState(POST_LOAD));
        final DebugLogger log = getDebugLogger();
        final PluginMagics pluginMagics = getPluginMagics();
        final PluginMagics.GameState nowState = pluginMagics.getGameState();
        if (newState == null || nowState == newState) return false;
        // Updates the general state within the data- and config object
        getPluginConfig().lastState.set(newState);
        pluginMagics.setGameState(newState);
        // Now it is actually determined what consequences the change has
        if (!newState.isPaused()) mainRegister.setPaused(false);
        if (nowState.isStopped() && newState.isPaused()) {
            // Since the new state is reliant on a start, but we have not
            // even instantiated or even loaded, we first need to boot
            return updateGameState(STARTED) && updateGameState(newState);
        }
        log.info(() -> "Status changed from %s to %s", nowState, newState);
        switch (newState) {
            /* Signals every loadable to pause */
            case PAUSED -> mainRegister.setPaused(true);
            /* Signals every loadable to stop */
            case STOPPED -> {
                mainRegister.unload(this);
                getModules().getInstance(EntityLoopModule.class).killAll();
            }
            /* Signals every loadable to start */
            case STARTED -> mainRegister.load(this);
            default -> {
                return false;
            }
        }
        return true;
    }

}
