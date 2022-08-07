package io.github.aparx.challenges.looping.command.commands;

import io.github.aparx.challenges.looping.ChallengePlugin;
import io.github.aparx.challenges.looping.MessageConstants;
import io.github.aparx.challenges.looping.PluginMagics;
import io.github.aparx.challenges.looping.command.ChallengeCommand;
import io.github.aparx.challenges.looping.command.ChallengeExecutable;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * @author aparx (Vinzent Zeband)
 * @version 18:42 CET, 04.08.2022
 * @since 1.0
 */
@ChallengeCommand(value = "cpause")
public class CommandPause extends ChallengeExecutable {

    public CommandPause(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        assertPluginState(PluginMagics.PluginState.POST_LOAD);
        ChallengePlugin instance = ChallengePlugin.getInstance();
        PluginMagics magics = ChallengePlugin.getMagics();
        if (magics.isGameState(PluginMagics.GameState.PAUSED)) {
            sender.sendMessage(MessageConstants.CHALLENGE_PAUSE_DUPLICATE);
            return true;
        }
        if (!magics.isGameState(PluginMagics.GameState.STARTED)) {
            sender.sendMessage(MessageConstants.CHALLENGE_NOT_STARTED);
            return true;
        }
        if (instance.updateChallenge(PluginMagics.GameState.PAUSED)) {
            Bukkit.broadcastMessage(MessageConstants.BROADCAST_CHALLENGE_PAUSE);
        }
        return false;
    }

}
