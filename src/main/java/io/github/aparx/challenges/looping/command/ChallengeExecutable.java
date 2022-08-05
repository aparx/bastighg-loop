package io.github.aparx.challenges.looping.command;

import com.google.common.base.Preconditions;
import io.github.aparx.challenges.looping.ChallengePlugin;
import io.github.aparx.challenges.looping.PluginMagics;
import lombok.Getter;
import org.bukkit.command.CommandExecutor;

import javax.validation.constraints.NotNull;

/**
 * @author aparx (Vinzent Zeband)
 * @version 18:42 CET, 04.08.2022
 * @since 1.0
 */
public abstract class ChallengeExecutable implements CommandExecutor {

    @NotNull @Getter
    private final ChallengePlugin plugin;

    @NotNull @Getter
    private final ChallengeCommand commandData;

    public ChallengeExecutable(@NotNull ChallengePlugin plugin) {
        this.plugin = Preconditions.checkNotNull(plugin);
        var annotation = getClass().getAnnotation(ChallengeCommand.class);
        Preconditions.checkNotNull(annotation, "Command is missing annotation");
        this.commandData = Preconditions.checkNotNull(annotation);
    }

    public boolean isNameMatching(@NotNull String str) {
        return str.equalsIgnoreCase(commandData.value());
    }

    protected void assertPluginState(PluginMagics.PluginState state) {
        if (!ChallengePlugin.isPluginState(state)) {
            throw new IllegalArgumentException("Plugin has not finished loading or cannot be loaded");
        }
    }

}