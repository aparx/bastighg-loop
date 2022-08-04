package io.github.aparx.challenges.looping.scheduler.defaults;

import io.github.aparx.challenges.looping.scheduler.GameScheduler;
import io.github.aparx.challenges.looping.utils.TickUnit;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import javax.validation.constraints.NotNull;

/**
 * @author aparx (Vinzent Zeband)
 * @version 17:03 CET, 04.08.2022
 * @since 1.0
 */
public final class ChallengeScheduler extends GameScheduler {

    private static final long SECOND_ELAPSE = 20;

    public ChallengeScheduler(Plugin plugin) {
        super(plugin);
    }

    @Override
    protected synchronized void onUpdate() {
        super.onUpdate();   // update all tasks
        long ticksRan = getTicksAlive();
        long sec = TickUnit.TICK.convert(ticksRan, TickUnit.SECOND, true);
        long min = TickUnit.TICK.convert(ticksRan, TickUnit.MINUTE, true);
        long hours = TickUnit.TICK.convert(ticksRan, TickUnit.HOUR, true);
        StringBuilder timeBuilder = new StringBuilder();
        timeToString(timeBuilder, hours, min, 'h', ChatColor.AQUA, ChatColor.BOLD);
        timeToString(timeBuilder, min, sec, 'm', ChatColor.AQUA, ChatColor.BOLD);
        timeToString(timeBuilder, sec, 0, 's', ChatColor.GRAY);
        var timeText = new TextComponent(timeBuilder.toString());
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, timeText);
        });
    }

    private static void timeToString(
            @NotNull StringBuilder outBuilder,
            long displayTime, long successorTime,
            char displayUnit, ChatColor... displayColors) {
        if (displayTime > 0) {
            if (displayColors != null) {
                for (ChatColor c : displayColors) {
                    outBuilder.append(c);
                }
            }
            outBuilder.append(displayTime).append(displayUnit);
        }
        if (successorTime > 0) {
            outBuilder.append(' ');
        }
    }

}

