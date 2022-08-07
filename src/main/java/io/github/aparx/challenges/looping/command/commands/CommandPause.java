package io.github.aparx.challenges.looping.command.commands;

import io.github.aparx.challenges.looping.ChallengePlugin;
import io.github.aparx.challenges.looping.MessageConstants;
import io.github.aparx.challenges.looping.PluginMagics;
import io.github.aparx.challenges.looping.command.ChallengeCommand;
import io.github.aparx.challenges.looping.command.ChallengeExecutable;
import io.github.aparx.challenges.looping.command.CommandHandler;
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
        PluginMagics.GameState gameState = magics.getGameState();
        if (gameState.isPaused()) {
            final ChallengePlugin plugin = ChallengePlugin.getInstance();
            final CommandHandler commands = plugin.getCommandHandler();
            Bukkit.dispatchCommand(sender, commands.getByType(CommandStart.class)
                    .orElseThrow(this::newCannotFindCommandException)
                    .getCommandData().value());
            return true;
        }
        if (!gameState.isImplyingStart()) {
            sender.sendMessage(MessageConstants.CHALLENGE_NOT_STARTED);
            return true;
        }
        if (!instance.updateGameState(PluginMagics.GameState.PAUSED)) {
            throw new CommandErrorException("Cannot pause the game");
        }
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.sendMessage(MessageConstants.BROADCAST_CHALLENGE_PAUSE);
            player.playSound(player, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, .5f);
        });
        return false;
    }

    private CommandErrorException newCannotFindCommandException() {
        return new CommandErrorException("Cannot find start command");
    }

}
