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
@ChallengeCommand(value = "cstart")
public class CommandStart extends ChallengeExecutable {

    public CommandStart(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        assertPluginState(PluginMagics.PluginState.POST_LOAD);
        ChallengePlugin instance = ChallengePlugin.getInstance();
        PluginMagics magics = ChallengePlugin.getMagics();
        if (magics.isGameState(PluginMagics.GameState.STARTED)) {
            sender.sendMessage(MessageConstants.CHALLENGE_START_DUPLICATE);
            return true;
        }
        if (instance.updateChallenge(PluginMagics.GameState.STARTED)) {
            Bukkit.broadcastMessage(MessageConstants.BROADCAST_CHALLENGE_START);
        }
        return true;
    }

}
