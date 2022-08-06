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
import io.github.aparx.challenges.looping.loadable.modules.ImmuneModule;
import io.github.aparx.challenges.looping.loadable.variants.ModuleManager;
import io.github.aparx.challenges.looping.logger.DebugLogger;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import javax.validation.constraints.NotNull;

import static io.github.aparx.challenges.looping.MessageConstants.*;
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

    public static boolean isPluginState(PluginMagics.PluginState state) {
        return runningInstance != null && getMagics().isState(state);
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
    private CommandHandler commandHandler;

    public ChallengePlugin() {
        ChallengePlugin.runningInstance = this;
        // Allocate a new register before the events can be called
        this.mainRegister = new LoadableRegister<>(this);
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
            // Registers default loadables and modules
            moduleManager.registerDefaults(this);
            mainRegister.register(moduleManager);
            mainRegister.register(new ImmuneModule());
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
            if (pluginMagics.isDebugMode()) {
                // Starts the plugin in started due to the debug mode
                updateChallenge(STARTED);
            }
        }
    }

    @Override
    public void onDisable() {
        if (!pluginMagics.isState(POST_LOAD)) return;
        mainRegister.unregisterAll();
    }

    /**
     * Updates the challenge to given state and notifies underlying
     * loadables about the change, with additional evaluation.
     * <p>This method is called with the explicit intention of either,
     * starting, stopping or pausing the challenge. It is not called when
     * the plugin is temporarily disabled.
     *
     * @param state the new state of the challenge
     * @return true if the state was changed
     */
    @CanIgnoreReturnValue
    public boolean updateChallenge(PluginMagics.GameState state) {
        Preconditions.checkNotNull(state);
        PluginMagics pluginMagics = getPluginMagics();
        PluginMagics.GameState current = pluginMagics.getGameState();
        pluginMagics.setGameState(state);
        if (current == state) return false;
        if (current == PAUSED) {
            mainRegister.setPaused(false);
            if (state == STARTED) {
                Bukkit.broadcastMessage(BROADCAST_CHALLENGE_RESUME);
                return true;
            }
        }
        switch (state) {
            /* Signals every loadable to pause */
            case PAUSED -> {
                mainRegister.setPaused(true);
                Bukkit.broadcastMessage(BROADCAST_CHALLENGE_PAUSE);
            }
            /* Signals every loadable to stop */
            case STOPPED -> {
                mainRegister.unload(this);
                explicitStop();
                Bukkit.broadcastMessage(BROADCAST_CHALLENGE_STOP);
            }
            /* Signals every loadable to start */
            case STARTED -> {
                mainRegister.load(this);
                Bukkit.broadcastMessage(BROADCAST_CHALLENGE_START);
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    private void explicitStop() {
        getModules().getInstance(EntityLoopModule.class).killAll();
    }


}
