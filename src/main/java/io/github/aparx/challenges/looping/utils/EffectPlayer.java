package io.github.aparx.challenges.looping.utils;

import org.bukkit.Particle;
import org.bukkit.World;

import javax.validation.constraints.NotNull;

/**
 * Functional interface used to spawn a data placement particle.
 *
 * @author aparx (Vinzent Zeband)
 * @version 17:58 CET, 03.08.2022
 * @since 1.0
 */
@FunctionalInterface
public interface EffectPlayer {

    EffectPlayer BLOCK_CLOUD_PLAYER = (world, posX, posY, posZ) -> {
        // Spawns a new cloud particle horizontally centered
        world.spawnParticle(Particle.CLOUD, posX + .5, posY + 1, posZ + .5, 2, 0, 0, 0, 0, null, false);
    };

    void playParticles(@NotNull World world, int posX, int posY, int posZ);
}
