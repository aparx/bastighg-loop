package io.github.aparx.challenges.looping.command.commands;

import io.github.aparx.challenges.looping.*;
import io.github.aparx.challenges.looping.command.ChallengeCommand;
import io.github.aparx.challenges.looping.command.ChallengeExecutable;
import io.github.aparx.challenges.looping.exception.CommandErrorException;
import io.github.aparx.challenges.looping.scheduler.defaults.ChallengeScheduler;
import io.github.aparx.challenges.looping.utils.DisplayUtils;
import io.github.aparx.challenges.looping.utils.TickUnit;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.*;

/**
 * @author aparx (Vinzent Zeband)
 * @version 18:42 CET, 04.08.2022
 * @since 1.0
 */
@ChallengeCommand(value = "cstop")
public class CommandStop extends ChallengeExecutable {

    public CommandStop(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        assertPluginState(PluginMagics.PluginState.POST_LOAD);
        ChallengePlugin instance = ChallengePlugin.getInstance();
        PluginMagics magics = ChallengePlugin.getMagics();
        PluginMagics.GameState gameState = magics.getGameState();
        if (gameState.isStopped()) {
            sender.sendMessage(MessageConstants.CHALLENGE_STOP_DUPLICATE);
            return true;
        }
        if (!magics.isGameImplyingStart()) {
            sender.sendMessage(MessageConstants.CHALLENGE_NOT_STARTED);
            return true;
        }
        final long t = instance.getPluginConfig().gameTicks.getAsLong();
        if (!instance.updateGameState(PluginMagics.GameState.STOPPED)) {
            throw new CommandErrorException("Cannot stop the game");
        }
        // Creates a new string having given game ticks as time
        final String timeString = DisplayUtils.createTimeString(t);
        final TextComponent timeComponent = new TextComponent("§c§l" + timeString);
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.sendMessage(String.format(MessageConstants.BROADCAST_CHALLENGE_STOP, timeString));
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, .5f);
            DisplayUtils.playActionbar(player, timeComponent, 120);
        });
        return true;
    }

}
