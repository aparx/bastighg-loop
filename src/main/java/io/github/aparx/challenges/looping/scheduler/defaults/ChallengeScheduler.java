package io.github.aparx.challenges.looping.scheduler.defaults;

import io.github.aparx.challenges.looping.ChallengePlugin;
import io.github.aparx.challenges.looping.PluginConstants;
import io.github.aparx.challenges.looping.loadable.modules.EntityLoopModule;
import io.github.aparx.challenges.looping.logger.DebugLogger;
import io.github.aparx.challenges.looping.scheduler.AbstractTask;
import io.github.aparx.challenges.looping.scheduler.DelegatedTask;
import io.github.aparx.challenges.looping.scheduler.GameScheduler;
import io.github.aparx.challenges.looping.scheduler.RelativeDuration;
import io.github.aparx.challenges.looping.utils.TickUnit;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import javax.swing.text.StyledEditorKit;
import javax.validation.constraints.NotNull;

/**
 * @author aparx (Vinzent Zeband)
 * @version 17:03 CET, 04.08.2022
 * @since 1.0
 */
public final class ChallengeScheduler extends GameScheduler {

    private static final long SECOND_ELAPSE = 20;

    private long ticksNonPaused = 0;

    public ChallengeScheduler(Plugin plugin) {
        super(plugin);
    }

    @Override
    protected boolean isActuallyPaused() {
        // This scheduler is never paused updating
        return false;
    }

    @Override
    protected void onScheduleStop() {
        ticksNonPaused = 0;
    }

    @Override
    protected synchronized void onUpdate() {
        if (!isPaused()) {
            super.onUpdate();   // update all tasks
            ++ticksNonPaused;
        }
        // We use an additional tick measurement, due to the nature
        // of pause-ability and #getTicksAlive()
        long ticksRan = ticksNonPaused;
        long sec = TickUnit.TICK.convert(ticksRan, TickUnit.SECOND, true);
        long min = TickUnit.TICK.convert(ticksRan, TickUnit.MINUTE, true);
        long hours = TickUnit.TICK.convert(ticksRan, TickUnit.HOUR, true);
        StringBuilder timeBuilder = new StringBuilder();
        if (isPaused()) {
            // Use gray and bold color font from now on
            timeBuilder.append(ChatColor.GRAY).append("⏸ ");
        } else {
            // Use aqua and bold color font from now on
            timeBuilder.append(ChatColor.AQUA).append(ChatColor.BOLD);
        }
        timeToString(timeBuilder, hours, min, 'h');
        timeToString(timeBuilder, min, sec, 'm');
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

