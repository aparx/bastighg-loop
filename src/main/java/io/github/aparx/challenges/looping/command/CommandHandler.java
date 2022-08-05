package io.github.aparx.challenges.looping.command;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import javax.swing.text.html.Option;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * @author aparx (Vinzent Zeband)
 * @version 18:49 CET, 04.08.2022
 * @since 1.0
 */
public class CommandHandler implements CommandExecutor {

    @Getter
    private final Set<@NotNull ChallengeExecutable> commands
            = Collections.synchronizedSet(new HashSet<>());

    @NotNull @Getter
    private final Plugin plugin;

    public CommandHandler(@NotNull Plugin plugin) {
        this.plugin = Preconditions.checkNotNull(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Optional<ChallengeExecutable> match = nameMatch(label);
        if (match.isEmpty()) return false;
        try {
            final ChallengeExecutable executable = match.get();
            return executable.onCommand(sender, command, label, args);
        } catch (IllegalArgumentException e) {
            sender.sendMessage();
        }
        return true;
    }

    @NotNull
    public Optional<ChallengeExecutable> nameMatch(String label) {
        if (StringUtils.isEmpty(label))
            return Optional.empty();
        return commands.stream()
                .filter(e -> e.isNameMatching(label))
                .findFirst();
    }

    public boolean contains(@NotNull ChallengeExecutable executable) {
        return commands.contains(Preconditions.checkNotNull(executable));
    }

    @CanIgnoreReturnValue
    public boolean add(@NotNull ChallengeExecutable executable) {
        Preconditions.checkNotNull(executable);
        if (!commands.add(executable))
            return false;
        String label = executable.getCommandData().value();
        PluginCommand pluginCommand = Bukkit.getPluginCommand(label);
        Preconditions.checkNotNull(pluginCommand, "command not registered");
        pluginCommand.setExecutor(executable);
        return true;
    }

    @CanIgnoreReturnValue
    public boolean remove(@NotNull ChallengeExecutable executable) {
        Preconditions.checkNotNull(executable);
        if (!commands.remove(executable))
            return false;
        String label = executable.getCommandData().value();
        PluginCommand pluginCommand = Bukkit.getPluginCommand(label);
        Preconditions.checkNotNull(pluginCommand, "command not registered");
        pluginCommand.setExecutor(null);
        return true;
    }

}