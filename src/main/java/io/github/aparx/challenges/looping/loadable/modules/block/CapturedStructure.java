package io.github.aparx.challenges.looping.loadable.modules.block;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A decorator containing a collection of {@code CapturedBlockData}, used
 * to capture multiple blocks and replace them at a later timer.
 *
 * @author aparx (Vinzent Zeband)
 * @version 10:50 CET, 01.08.2022
 * @since 1.0
 */
public final class CapturedStructure {

    public static final CapturedStructure EMPTY = of(List.of());

    @NotNull
    public static CapturedStructure of(
            final @NotNull CapturedBlockData blockData) {
        return of(blockData, EffectPlayer.CLOUD_PLAYER);
    }

    @NotNull
    public static CapturedStructure of(
            final @NotNull CapturedBlockData blockData,
            final @Nullable EffectPlayer effectPlayer) {
        // Allocate a new immutable list off of given data
        return of(List.of(blockData), effectPlayer);
    }

    @NotNull
    public static CapturedStructure of(
            final @NotNull Collection<@NotNull CapturedBlockData> blockData) {
        return of(blockData, EffectPlayer.CLOUD_PLAYER);
    }

    @NotNull
    public static CapturedStructure of(
            final @NotNull Collection<@NotNull CapturedBlockData> blockData,
            final @Nullable EffectPlayer effectPlayer) {
        return new CapturedStructure(blockData, effectPlayer);
    }

    /* BlockResetStruct class implementation */

    @NotNull
    private final Collection<@NotNull CapturedBlockData> blockData;

    @Nullable @Getter @Setter
    private EffectPlayer effectPlayer;

    CapturedStructure(
            final @NotNull Collection<@NotNull CapturedBlockData> blockData,
            final @Nullable EffectPlayer effectPlayer) {
        this.blockData = Preconditions.checkNotNull(blockData);
        this.effectPlayer = effectPlayer;
    }

    public boolean isEmpty() {
        return blockData.isEmpty();
    }

    /* passive operations */

    public void placeCapture() {
        placeCapture(true);
    }

    public void placeCapture(boolean playEffect) {
        placeCapture(0, 0, 0, playEffect);
    }

    public void placeCapture(
            int offsetX, int offsetY, int offsetZ,
            boolean playEffect) {
        for (CapturedBlockData data : blockData) {
            if (data == null) continue;
            Location location = data.getLocation();
            int posX = location.getBlockX() + offsetX;
            int posY = location.getBlockY() + offsetY;
            int posZ = location.getBlockZ() + offsetZ;
            BlockData newData = data.getBlockData();
            World world = location.getWorld();
            if (world == null) continue;
            // We skip if the current data is already our desired data
            BlockData current = world.getBlockData(posX, posY, posZ);
            if (newData.equals(current)) continue;
            // We update the block data at given position to the capture
            world.setBlockData(posX, posY, posZ, newData);
            if (playEffect && effectPlayer != null) {
                // Now we play the visual effect as it is wanted behaviour
                effectPlayer.playParticle(world, posX, posY, posZ);
            }
        }
    }

    /* non-mutating operations */

    @NotNull
    public CapturedStructure add(@Nullable CapturedBlockData blockData) {
        if (blockData == null) return this;
        ArrayList<CapturedBlockData> dataCopy
                = new ArrayList<>(this.blockData);
        dataCopy.add(blockData);
        return of(dataCopy);
    }

    @NotNull
    public CapturedStructure add(@Nullable CapturedStructure other) {
        if (other == null || other.isEmpty()) return this;
        return add(other.blockData);
    }

    @NotNull
    public CapturedStructure add(@Nullable Collection<@NotNull CapturedBlockData> blockData) {
        if (blockData == null || blockData.isEmpty()) return this;
        CapturedStructure newStruct = new CapturedStructure(
                new ArrayList<>(this.blockData), effectPlayer);
        newStruct.blockData.addAll(blockData);
        return newStruct;
    }

    @Override
    public String toString() {
        return "CapturedStructure{" +
                "blockData=" + blockData +
                ", effectPlayer=" + effectPlayer +
                '}';
    }

    /**
     * Functional interface used to spawn a data placement particle.
     */
    @FunctionalInterface
    public interface EffectPlayer {

        EffectPlayer CLOUD_PLAYER = (world, posX, posY, posZ) -> {
            // Spawns a new cloud particle horizontally centered
            world.spawnParticle(Particle.CLOUD, posX + .5, posY + 1, posZ + .5, 2, 0, 0, 0, 0, null, false);
        };

        void playParticle(@NotNull World world, int posX, int posY, int posZ);
    }

}
