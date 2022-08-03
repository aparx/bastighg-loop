package io.github.aparx.challenges.looping.loadable.modules.block;

import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockFromToEvent;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static io.github.aparx.challenges.looping.loadable.modules.block.BlockStructures.Direction.DOWN;
import static io.github.aparx.challenges.looping.loadable.modules.block.BlockStructures.Direction.UP;
import static org.bukkit.Material.*;

/**
 * @author aparx (Vinzent Zeband)
 * @version 11:13 CET, 01.08.2022
 * @since 1.0
 */
public final class BlockStructures {

    private static final Map<@NotNull Material, @NotNull Direction>
            LINKED_STRUCTS = new HashMap<>();

    static {
        LINKED_STRUCTS.put(TALL_GRASS, UP);
        LINKED_STRUCTS.put(SUGAR_CANE, UP);
        LINKED_STRUCTS.put(TWISTING_VINES, UP);
        LINKED_STRUCTS.put(CACTUS, UP);
        LINKED_STRUCTS.put(BIG_DRIPLEAF, UP);
        LINKED_STRUCTS.put(SMALL_DRIPLEAF, UP);
        LINKED_STRUCTS.put(BAMBOO, UP);
        LINKED_STRUCTS.put(KELP, UP);
        LINKED_STRUCTS.put(VINE, DOWN);
    }

    public BlockStructures() {
        throw new AssertionError();
    }

    public static CapturedStructure getEffectiveLinks(
            @NotNull Block block, boolean includeCenter) {
        return getEffectiveLinks(block.getLocation(), includeCenter);
    }

    public static CapturedStructure getEffectiveLinks(
            final @NotNull Location location,
            final boolean includeCenter) {
        return getEffectiveLinks(
                location.getWorld(), location.getBlockX(),
                location.getBlockY(), location.getBlockZ(),
                includeCenter);
    }

    public static CapturedStructure getEffectiveLinks(
            @NotNull World world, int centerX, int centerY, int centerZ,
            boolean includeCenter) {
        // Get all linked structures effected by the center
        CapturedStructure struct = CapturedStructure.EMPTY;
        if (includeCenter) {
            // Include center block as wanted
            Block block = world.getBlockAt(centerX, centerY, centerZ);
            struct = struct.add(CapturedBlockData.capture(block));
        }
        // TODO certain blocks like tall_grass might be added even tho
        //   they are located right next an unaffected block
        int hR = 1 /* horizontal radius */;
        int vR = 1 /* vertical radius */;
        for (int oX = -hR; oX <= hR; oX++) {
            for (int oY = -vR; oY <= vR; oY++) {
                for (int oZ = -hR; oZ <= hR; oZ++) {
                    if (oX == 0 && oY == 0 && oZ == 0) continue;
                    Block b = world.getBlockAt(centerX + oX, centerY + oY, centerZ + oZ);
                    struct = struct.add(getImmediateLinks(b));
                }
            }
        }
        return struct;
    }

    @NotNull
    public static CapturedStructure getImmediateLinksOrSelf(@NotNull Block block) {
        CapturedStructure struct = getImmediateLinks(block);
        if (struct == null || struct.isEmpty()) {
            // Include the block anyway if `struct` is not
            struct = CapturedStructure.of(CapturedBlockData.capture(block));
        }
        return struct;
    }

    @Nullable
    public static CapturedStructure getImmediateLinks(@NotNull Block block) {
        final Material material = block.getType();
        if (!LINKED_STRUCTS.containsKey(material))
            return null;
        Direction direction = LINKED_STRUCTS.get(material);
        Preconditions.checkNotNull(direction);
        return getConnectedTypes(block.getLocation(), material, direction);
    }


    @NotNull
    public static CapturedStructure getConnectedTypes(
            final @NotNull Location location,
            final @NotNull Material material,
            final @NotNull Direction linkDirection) {
        return getConnectedTypes(
                Preconditions.checkNotNull(location.getWorld()),
                location.getBlockX(), location.getBlockY(),
                location.getBlockZ(), material, linkDirection);
    }

    @NotNull
    public static CapturedStructure getConnectedTypes(
            @NotNull World world, int posX, int posY, int posZ,
            final @NotNull Material material,
            final @NotNull Direction linkDirection) {
        return getImmediateLinks(
                world, posX, posY, posZ,
                block -> block.getType() == material,
                linkDirection);
    }

    @NotNull
    public static CapturedStructure getImmediateLinks(
            final @NotNull Location location,
            final @NotNull Predicate<Block> blockPredicate,
            final @NotNull Direction linkDirection) {
        return getImmediateLinks(
                Preconditions.checkNotNull(location.getWorld()),
                location.getBlockX(), location.getBlockY(),
                location.getBlockZ(), blockPredicate, linkDirection);
    }

    @NotNull
    public static CapturedStructure getImmediateLinks(
            @NotNull World world, int posX, int posY, int posZ,
            final @NotNull Predicate<Block> blockPredicate,
            final @NotNull Direction linkDirection) {
        Preconditions.checkNotNull(blockPredicate);
        Preconditions.checkNotNull(linkDirection);
        ArrayList<CapturedBlockData> captures = new ArrayList<>(2);
        while (posY >= world.getMinHeight() && posY <= world.getMaxHeight()) {
            // Get the block at current position and try capturing it
            Block target = world.getBlockAt(posX, posY, posZ);
            if (!blockPredicate.test(target)) break;
            captures.add(CapturedBlockData.capture(target));
            // Move towards the vertical direction given
            if (linkDirection == UP) ++posY;
            else --posY;
        }
        captures.trimToSize();  // in case we did not exceed our capacity
        return CapturedStructure.of(captures);
    }

    public enum Direction {
        UP, DOWN
    }

}
