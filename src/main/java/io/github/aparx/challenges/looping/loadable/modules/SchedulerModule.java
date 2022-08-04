package io.github.aparx.challenges.looping.loadable.modules;

import io.github.aparx.challenges.looping.ChallengePlugin;
import io.github.aparx.challenges.looping.PluginConstants;
import io.github.aparx.challenges.looping.loadable.ChallengeModule;
import io.github.aparx.challenges.looping.loadable.modules.loop.loops.tnt.LoopTNTModule;
import io.github.aparx.challenges.looping.logger.DebugLogger;
import io.github.aparx.challenges.looping.scheduler.AbstractTask;
import io.github.aparx.challenges.looping.scheduler.GameScheduler;
import io.github.aparx.challenges.looping.scheduler.RelativeDuration;
import io.github.aparx.challenges.looping.scheduler.defaults.ChallengeScheduler;
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
        mainScheduler = new ChallengeScheduler(plugin);
        mainScheduler.start();
        if (!PluginConstants.DEBUG_MODE) return;
        mainScheduler.attach(AbstractTask.delegate(RelativeDuration.ofSurvivor(20 * 5), task -> {
            EntityLoopModule instance = ChallengePlugin.getModules().getInstance(EntityLoopModule.class);
            DebugLogger debugLogger = ChallengePlugin.getDebugLogger();
            debugLogger.info("[Loop-Entity-Report]");
            instance.getLoops().forEach((c, m) -> {
                debugLogger.info("[%s] REPORTS [%d]", c.getSimpleName(), m.getEntities().size());
            });
        }));
    }

    @Override
    public void onUnload(Plugin plugin) throws Throwable {
        mainScheduler.stop();
    }

    public GameScheduler getMainScheduler() {
        return mainScheduler;
    }
}
