package io.github.aparx.challenges.looping.command.commands;

import io.github.aparx.challenges.looping.ChallengePlugin;
import io.github.aparx.challenges.looping.MessageConstants;
import io.github.aparx.challenges.looping.PluginMagics;
import io.github.aparx.challenges.looping.command.ChallengeCommand;
import io.github.aparx.challenges.looping.command.ChallengeExecutable;
import io.github.aparx.challenges.looping.exception.CommandErrorException;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
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
        PluginMagics.GameState gameState = magics.getGameState();
        if (gameState.isStarted()) {
            sender.sendMessage(MessageConstants.CHALLENGE_START_DUPLICATE);
            return true;
        }
        if (!instance.updateGameState(PluginMagics.GameState.STARTED)) {
            throw new CommandErrorException("Cannot start the game");
        }
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.sendMessage(MessageConstants.BROADCAST_CHALLENGE_START);
            if (gameState.isPaused())
                player.playSound(player, Sound.BLOCK_STONE_BUTTON_CLICK_OFF, 1f, 1f);
            else
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, .5f);
        });
        return true;
    }

}
