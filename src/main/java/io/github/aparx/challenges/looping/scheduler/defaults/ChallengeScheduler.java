package io.github.aparx.challenges.looping.scheduler.defaults;

import com.google.common.base.Preconditions;
import io.github.aparx.challenges.looping.ChallengePlugin;
import io.github.aparx.challenges.looping.PluginConfig;
import io.github.aparx.challenges.looping.scheduler.GameScheduler;
import io.github.aparx.challenges.looping.utils.DisplayUtils;
import io.github.aparx.challenges.looping.utils.TickUnit;
import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.plugin.Plugin;

import javax.validation.constraints.NotNull;

import static io.github.aparx.challenges.looping.PluginMagics.PluginState.POST_LOAD;

/**
 * @author aparx (Vinzent Zeband)
 * @version 17:03 CET, 04.08.2022
 * @since 1.0
 */
public final class ChallengeScheduler extends GameScheduler {

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
        if (gameTicks < TickUnit.SECOND.getTimeSecondFactor()) return;
        // We use an additional tick measurement, due to the nature
        // of pause-ability and #getTicksAlive()
        StringBuilder timeBuilder = new StringBuilder();
        if (isPaused()) {
            // Use gray and bold color font from now on
            timeBuilder.append(ChatColor.GRAY).append("⏸ ");
        } else {
            // Use aqua and bold color font from now on
            timeBuilder.append(ChatColor.AQUA);
        }
        timeBuilder.append(ChatColor.BOLD);
        DisplayUtils.appendTimeToBuilder(timeBuilder, gameTicks);
        var timeText = new TextComponent(timeBuilder.toString());
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, timeText);
        });
    }


}

