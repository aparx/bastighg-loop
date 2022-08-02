package io.github.aparx.challenges.looping.loadable.modules;

import io.github.aparx.challenges.looping.loadable.ChallengeModule;
import io.github.aparx.challenges.looping.scheduler.AbstractTask;
import io.github.aparx.challenges.looping.scheduler.GameScheduler;
import io.github.aparx.challenges.looping.scheduler.RelativeDuration;
import org.bukkit.plugin.Plugin;

/**
 * @author aparx (Vinzent Zeband)
 * @version 08:50 CET, 01.08.2022
 * @since 1.0
 */
public class SchedulerModule extends ChallengeModule {

    private GameScheduler mainScheduler;

    @Override
    public void onLoad(Plugin plugin) throws Throwable {
        mainScheduler = new GameScheduler(plugin);
        mainScheduler.start();
    }

    @Override
    public void onUnload(Plugin plugin) throws Throwable {
        mainScheduler.stop();
    }

    public GameScheduler getMainScheduler() {
        return mainScheduler;
    }
}
