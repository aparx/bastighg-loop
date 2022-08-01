package io.github.aparx.challenges.looping.loadable.variants;

import io.github.aparx.challenges.looping.ChallengePlugin;
import io.github.aparx.challenges.looping.loadable.ListenerLoadable;
import io.github.aparx.challenges.looping.loadable.modules.BlockModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * @author aparx (Vinzent Zeband)
 * @version 06:27 CET, 01.08.2022
 * @since 1.0
 */
public final class MainLoadable implements ListenerLoadable {

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        var modules = ChallengePlugin.getModules();
        BlockModule instance = modules.getInstance(BlockModule.class);
    }
}
