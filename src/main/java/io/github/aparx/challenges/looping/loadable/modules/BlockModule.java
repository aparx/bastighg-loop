package io.github.aparx.challenges.looping.loadable.modules;

import io.github.aparx.challenges.looping.ChallengePlugin;
import io.github.aparx.challenges.looping.loadable.ChallengeModule;
import io.github.aparx.challenges.looping.loadable.ListenerLoadable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * @author aparx (Vinzent Zeband)
 * @version 07:30 CET, 01.08.2022
 * @since 1.0
 */
public class BlockModule
        extends ChallengeModule
        implements ListenerLoadable {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        SchedulerModule scheduler = ChallengePlugin.getScheduler();
    }

}
