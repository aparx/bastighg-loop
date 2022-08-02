package io.github.aparx.challenges.looping.loadable.modules;

import io.github.aparx.challenges.looping.ChallengePlugin;
import io.github.aparx.challenges.looping.loadable.ChallengeModule;
import io.github.aparx.challenges.looping.loadable.ListenerLoadable;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;

/**
 * @author aparx (Vinzent Zeband)
 * @version 07:30 CET, 01.08.2022
 * @since 1.0
 */
public class BlockModule extends ChallengeModule implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isEventInvalid(event)) return;
        SchedulerModule module = ChallengePlugin.getScheduler();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (isEventInvalid(event)) return;
    }

    private boolean isEventInvalid(Event event) {
        return isPaused() || event instanceof Cancellable
                && ((Cancellable) event).isCancelled();
    }

}
