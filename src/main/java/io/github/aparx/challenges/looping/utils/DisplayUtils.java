package io.github.aparx.challenges.looping.utils;

import com.google.common.base.Preconditions;
import io.github.aparx.challenges.looping.ChallengePlugin;
import io.github.aparx.challenges.looping.PluginConstants;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.function.Consumer;

/**
 * @author aparx (Vinzent Zeband)
 * @version 03:49 CET, 07.08.2022
 * @since 1.0
 */
public final class DisplayUtils {

    public static final int ACTIONBAR_DEFAULT_TIME = 2 * 20;

    static final TickUnit[] DEFAULT_SEQUENTIAL_UNITS
            = {TickUnit.DAY, TickUnit.HOUR, TickUnit.MINUTE, TickUnit.SECOND};

    private DisplayUtils() {
        throw new AssertionError();
    }

    /* Actionbar utilities */

    public static void playActionbar(Player player, TextComponent component, int ticks) {
        Preconditions.checkNotNull(player);
        Preconditions.checkNotNull(component);
        new BukkitRunnable() {
            int ticksLeft = 1 + (ticks / ACTIONBAR_DEFAULT_TIME);

            @Override
            public void run() {
                if (!player.isOnline() || --ticksLeft <= 0) {
                    cancel();
                    return;
                }
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
            }
        }.runTaskTimer(ChallengePlugin.getInstance(), 0, ACTIONBAR_DEFAULT_TIME);
    }

    /* Time to string utilities */

    @NotNull
    public static String createTimeString(long baseTime) {
        return createTimeString(baseTime, DEFAULT_SEQUENTIAL_UNITS);
    }

    @NotNull
    public static String createTimeString(
            final long baseTime,
            final @Nullable TickUnit... units) {
        return createTimeString(TickUnit.TICK, baseTime, units);
    }

    @NotNull
    public static String createTimeString(
            final @NotNull TickUnit from,
            final long baseTime,
            final @Nullable TickUnit... units) {
        if (ArrayUtils.isEmpty(units))
            return StringUtils.EMPTY;
        StringBuilder builder = new StringBuilder();
        appendTimeToBuilder(builder, from, baseTime, units);
        return builder.toString();
    }

    public static void appendTimeToBuilder(
            final @NotNull StringBuilder timeBuilder,
            final long baseTime) {
        appendTimeToBuilder(timeBuilder, baseTime, DEFAULT_SEQUENTIAL_UNITS);
    }

    public static void appendTimeToBuilder(
            final @NotNull StringBuilder timeBuilder,
            final long baseTime,
            TickUnit... units) {
        appendTimeToBuilder(timeBuilder, TickUnit.TICK, baseTime, units);
    }

    public static void appendTimeToBuilder(
            final @NotNull StringBuilder timeBuilder,
            final @NotNull TickUnit from,
            final long baseTime,
            TickUnit... units) {
        Preconditions.checkNotNull(timeBuilder);
        if (ArrayUtils.isEmpty(units)) return;
        for (int i = 0; i < units.length; ++i) {
            if (units[i] == null) continue;
            boolean isEnd = 1 + i == units.length;
            boolean isNextEnd = 2 + i == units.length;
            long t = from.convert(baseTime, units[i], i != 0);
            long n = isEnd ? 0 : from.convert(baseTime, units[1 + i], true);
            timeToString(timeBuilder, t, isEnd,
                    !isEnd && (n > 0 || isNextEnd),
                    units[i].getDisplayChar());
        }
    }

    private static void timeToString(
            @NotNull StringBuilder outBuilder,
            long displayTime, boolean displayNull,
            boolean hasSuccessor,
            char displayUnit, ChatColor... displayColors) {
        if (displayTime < 0 || !displayNull && displayTime == 0) return;
        if (displayColors != null) {
            for (ChatColor c : displayColors) {
                outBuilder.append(c);
            }
        }
        outBuilder.append(displayTime).append(displayUnit);
        if (hasSuccessor) outBuilder.append(' ');
    }

}
