package io.github.aparx.challenges.looping.scheduler.defaults;

import com.google.common.base.Preconditions;
import io.github.aparx.challenges.looping.ChallengePlugin;
import io.github.aparx.challenges.looping.PluginConfig;
import io.github.aparx.challenges.looping.scheduler.GameScheduler;
import io.github.aparx.challenges.looping.utils.TickUnit;
import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import javax.validation.constraints.NotNull;

import static io.github.aparx.challenges.looping.PluginMagics.PluginState.POST_LOAD;

/**
 * @author aparx (Vinzent Zeband)
 * @version 17:03 CET, 04.08.2022
 * @since 1.0
 */
public final class ChallengeScheduler extends GameScheduler {

    @NotNull
    public static String createTimeString(final long gameTicks) {
        StringBuilder builder = new StringBuilder();
        appendTimeToBuilder(builder, gameTicks);
        return builder.toString();
    }

    public static void appendTimeToBuilder(
            final @NotNull StringBuilder timeBuilder,
            final long gameTicks) {
        Preconditions.checkNotNull(timeBuilder);
        long sec = TickUnit.TICK.convert(gameTicks, TickUnit.SECOND, true);
        long min = TickUnit.TICK.convert(gameTicks, TickUnit.MINUTE, true);
        long hours = TickUnit.TICK.convert(gameTicks, TickUnit.HOUR, true);
        long days = TickUnit.TICK.convert(gameTicks, TickUnit.DAY, false);
        timeToString(timeBuilder, days, hours, 'd');
        timeToString(timeBuilder, hours, min, 'h');
        timeToString(timeBuilder, min, sec, 'm');
        timeToString(timeBuilder, Math.max(sec, 1), 0, 's');
    }

    private static void timeToString(
            @NotNull StringBuilder outBuilder,
            long displayTime, long successorTime,
            char displayUnit, ChatColor... displayColors) {
        if (displayTime <= 0) return;
        if (displayColors != null) {
            for (ChatColor c : displayColors) {
                outBuilder.append(c);
            }
        }
        outBuilder.append(displayTime).append(displayUnit);
        if (successorTime <= 0) return;
        outBuilder.append(' ');
    }

    /* ChallengeScheduler implementation */

    @Getter
    private long gameTicks = 0;

    public ChallengeScheduler(Plugin plugin) {
        super(plugin);
    }

    /**
     * Updates the internal {@code gameTicks} counter to the specified
     * long-value and saves the new value into the main configuration.
     *
     * @param newTicks the new tick amount
     * @see ChallengePlugin#getPluginConfig()
     */
    public void saveGameTicks(long newTicks) {
        this.gameTicks = newTicks;
        if (ChallengePlugin.isLoadState(POST_LOAD)) {
            ChallengePlugin plugin = ChallengePlugin.getInstance();
            PluginConfig config = plugin.getPluginConfig();
            config.gameTicks.set(newTicks);
        }
    }

    @Override
    protected boolean isActuallyPaused() {
        // This scheduler is never paused updating
        return false;
    }

    @Override
    protected void onScheduleStart() {
        if (!ChallengePlugin.isLoadState(POST_LOAD)) return;
        ChallengePlugin plugin = ChallengePlugin.getInstance();
        PluginConfig config = plugin.getPluginConfig();
        saveGameTicks(config.gameTicks.getAsLong(gameTicks));
    }

    @Override
    protected void onScheduleStop() {
        if (!ChallengePlugin.isGameImplyingStart()) {
            // We only reset the game ticks if the plugin was not just
            // reloaded, but someone intentionally and explicitly stopped
            // the challenge
            saveGameTicks(0);
        }
    }

    @Override
    protected synchronized void onUpdate() {
        if (!isPaused()) {
            super.onUpdate();   // update all tasks
            ++gameTicks;
        }
        // Saves the current tick amount twice every second
        if (gameTicks % 10 == 0) saveGameTicks(gameTicks);
        // We use an additional tick measurement, due to the nature
        // of pause-ability and #getTicksAlive()
        StringBuilder timeBuilder = new StringBuilder();
        if (isPaused()) {
            // Use gray and bold color font from now on
            timeBuilder.append(ChatColor.GRAY).append("⏸ ");
        } else {
            // Use aqua and bold color font from now on
            timeBuilder.append(ChatColor.AQUA).append(ChatColor.BOLD);
        }
        appendTimeToBuilder(timeBuilder, gameTicks);
        final String timeString = timeBuilder.toString();
        if (StringUtils.isEmpty(ChatColor.stripColor(timeString))) return;
        var timeText = new TextComponent(timeString);
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, timeText);
        });
    }


}

