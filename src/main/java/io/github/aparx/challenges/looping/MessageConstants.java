package io.github.aparx.challenges.looping;

import org.bukkit.ChatColor;

/**
 * @author aparx (Vinzent Zeband)
 * @version 12:00 CET, 05.08.2022
 * @since 1.0
 */
public final class MessageConstants {

    private MessageConstants() {
        throw new AssertionError();
    }

    public static final String NORMAL_PREFIX = "§b§l[" + PluginConstants.PLUGIN_NAME + ']';
    public static final String UNCOLORED_PREFIX = ChatColor.stripColor(NORMAL_PREFIX);
    public static final String ERROR_PREFIX = "§c§l" + UNCOLORED_PREFIX + "§c";

    public static final String BROADCAST_CHALLENGE_START
            = NORMAL_PREFIX + "§r Die Challenge ist jetzt §b§lgestartet§r.";

    public static final String BROADCAST_CHALLENGE_PAUSE
            = NORMAL_PREFIX + "§r Die Challenge ist §7§lpausiert§r.";

    public static final String BROADCAST_CHALLENGE_RESUME
            = NORMAL_PREFIX + "§r Die Challenge ist jetzt wieder §b§laktiv§r.";

    public static final String BROADCAST_CHALLENGE_STOP
            = NORMAL_PREFIX + "§r Die Challenge ist jetzt §c§lbeendet§r.";

    public static final String CHALLENGE_NOT_STARTED
            = ERROR_PREFIX + " Die Challenge ist nicht gestartet.";

    public static final String CHALLENGE_START_DUPLICATE
            = ERROR_PREFIX + " Die Challenge ist bereits gestartet!";

    public static final String CHALLENGE_PAUSE_DUPLICATE
            = ERROR_PREFIX + " Die Challenge ist bereits pausiert! §7§o(Meintest du \"/start\"?)";

    public static final String CHALLENGE_STOP_DUPLICATE
            = ERROR_PREFIX + " Die Challenge ist nicht gestartet! §7§o(Meintest du \"/start\"?)";

    public static final String CHALLENGE_ACTION_PAUSE
            = ChatColor.RED + "Diese Aktion ist während der Pause ausgeschaltet!";


}
