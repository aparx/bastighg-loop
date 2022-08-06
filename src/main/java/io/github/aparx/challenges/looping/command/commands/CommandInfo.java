package io.github.aparx.challenges.looping.command.commands;

import io.github.aparx.challenges.looping.ChallengePlugin;
import io.github.aparx.challenges.looping.MessageConstants;
import io.github.aparx.challenges.looping.PluginMagics;
import io.github.aparx.challenges.looping.command.ChallengeCommand;
import io.github.aparx.challenges.looping.command.ChallengeExecutable;
import io.github.aparx.challenges.looping.loadable.modules.EntityLoopModule;
import io.github.aparx.challenges.looping.loadable.modules.SchedulerModule;
import io.github.aparx.challenges.looping.logger.DebugLogger;
import io.github.aparx.challenges.looping.scheduler.defaults.ChallengeScheduler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Objects;

import static io.github.aparx.challenges.looping.MessageConstants.NORMAL_PREFIX;

/**
 * @author aparx (Vinzent Zeband)
 * @version 18:42 CET, 04.08.2022
 * @since 1.0
 */
@ChallengeCommand(value = "cinfo")
public class CommandInfo extends ChallengeExecutable {

    public CommandInfo(ChallengePlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Please forgive my source here, it is just done in a couple
        // seconds to provide information
        assertPluginState(PluginMagics.PluginState.POST_LOAD);
        PluginMagics magics = ChallengePlugin.getMagics();
        EntityLoopModule loops = ChallengePlugin.getModules().getInstance(EntityLoopModule.class);
        SchedulerModule schedulers = ChallengePlugin.getSchedulers();
        ChallengeScheduler primaryScheduler = schedulers.getPrimaryScheduler();
        int childAmount = primaryScheduler.childAmount();
        sender.sendMessage(NORMAL_PREFIX + " §r§m----------§b§l Info§r §m----------");
        sender.sendMessage(NORMAL_PREFIX + " §rChallenge Status: §b§l" + magics.getGameState());
        sender.sendMessage(NORMAL_PREFIX + " §rTotal Attachments: "
                + getColorForCount(childAmount) + ChatColor.BOLD + childAmount);
        sender.sendMessage(NORMAL_PREFIX + " §rInstances (" + loops.getTotalInstances() + "):");
        loops.getLoops().forEach((c, m) -> {
            int count = m.getEntities().size();
            ChatColor color = getColorForCount(count);
            sender.sendMessage(NORMAL_PREFIX
                    + " §7§l>§r " + c.getSimpleName()
                    + ": " + color + ChatColor.BOLD + count);
        });
        sender.sendMessage(NORMAL_PREFIX + " §r§m-------------------------");
        return false;
    }

    private ChatColor getColorForCount(int count) {
        return count >= 3500 ? ChatColor.DARK_RED
                : (count >= 3000 ? ChatColor.RED
                : (count >= 2000 ? ChatColor.GOLD
                : (count >= 1000 ? ChatColor.YELLOW
                : (count == 0 ? ChatColor.GRAY
                : ChatColor.AQUA))));
    }

}
