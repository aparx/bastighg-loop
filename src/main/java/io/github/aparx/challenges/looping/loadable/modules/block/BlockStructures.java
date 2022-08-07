package io.github.aparx.challenges.looping.loadable.modules.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author aparx (Vinzent Zeband)
 * @version 15:02 CET, 01.08.2022
 * @since 1.0
 */
public final class BlockStructures {

    private static final Map<Material, BlockFace[]> SPECIAL_LINKS = new HashMap<>();

    private static final BlockFace[] UP_FACE = new BlockFace[]{BlockFace.UP};
    private static final BlockFace[] DOWN_FACE = new BlockFace[]{BlockFace.DOWN};

    private static final BlockFace[] VERTICAL_DIRECTIONS
            = {BlockFace.UP, BlockFace.DOWN};

    private static final BlockFace[] HORIZONTAL_DIRECTIONS
            = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

    private static final BlockFace[] ALL_ONE_DIRECTIONS
            = {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH,
            BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

    /**
     * If a map's size exceeds this capacity, only vertical faces will be
     * tested in {@code scanAffectedBlocks}, and not any other face. This
     * is to avoid huge walls of non-solid structures to cause caching
     * issues over the long term.
     */
    private static final int MAXIMUM_MULTIDIR_SIZE = 15;

    static {
        SPECIAL_LINKS.put(Material.SAND, UP_FACE);
        SPECIAL_LINKS.put(Material.RED_SAND, UP_FACE);
        SPECIAL_LINKS.put(Material.GRAVEL, UP_FACE);
        SPECIAL_LINKS.put(Material.TALL_GRASS, UP_FACE);
        SPECIAL_LINKS.put(Material.SUGAR_CANE, UP_FACE);
        SPECIAL_LINKS.put(Material.TWISTING_VINES, UP_FACE);
        SPECIAL_LINKS.put(Material.CACTUS, UP_FACE);
        SPECIAL_LINKS.put(Material.BIG_DRIPLEAF, UP_FACE);
        SPECIAL_LINKS.put(Material.SMALL_DRIPLEAF, UP_FACE);
        SPECIAL_LINKS.put(Material.BAMBOO, UP_FACE);
        SPECIAL_LINKS.put(Material.KELP, UP_FACE);
        SPECIAL_LINKS.put(Material.VINE, DOWN_FACE);
    }

    public BlockStructures() {
        throw new AssertionError();
    }

    private static boolean isTypeReliantOnBlock(Material material) {
        if (material == null || material.isAir()) return false;
        if (!material.isSolid() && material.isBlock()) return true;
        // ...
        return SPECIAL_LINKS.containsKey(material);
    }

    private static boolean isBlocksAttachableTo(Block block) {
        return block.getType().isSolid();
    }

    @NotNull
    public static CapturedStructure getAffectedBlocks(
            final @NotNull Block center, boolean includeCenter) {
        return getAffectedBlocks(center, includeCenter, null);
    }

    @NotNull
    public static CapturedStructure getAffectedBlocks(
            final @NotNull Block center, boolean includeCenter,
            @Nullable Predicate<Block> additionalPredicate) {
        Map<Location, CapturedBlockData> outMap = new HashMap<>();
        scanAffectedBlocks(center, includeCenter, outMap, null, additionalPredicate);
        return CapturedStructure.of(outMap);
    }

    @NotNull
    public static void scanAffectedBlocks(
            @NotNull Block center, boolean includeCenter,
            @NotNull Map<Location, CapturedBlockData> out,
            @Nullable Block comingFrom,
            @Nullable Predicate<Block> includeTester) {
        // if above is fallable, include blocks above TODO
        // basedOnCenter = only blocks directly connected are included
        BlockFace[] targets = VERTICAL_DIRECTIONS;
        if (isBlocksAttachableTo(center)) {
            // Include all directions if blocks can be attached to `center`
            targets = ALL_ONE_DIRECTIONS;
        }
        if (includeCenter) {
            // Since the center does matter, we include it
            CapturedBlockData.capture(center).putInMap(out);
        }
        // If the center type is a specialized type, we only use the
        // directions that that special type requires (O(1) complexity)
        // Now we iterate over all our targets and get `center` relatives
        for (BlockFace face : targets) {
            Block fBlock = center.getRelative(face);
            if (fBlock.isLiquid()) continue;
            if (out.size() >= MAXIMUM_MULTIDIR_SIZE
                    && face != BlockFace.UP
                    && face != BlockFace.DOWN) {
                continue;
            }
            Location fPos = fBlock.getLocation();
            Material fType = fBlock.getType();
            if (comingFrom != null && fPos.equals(comingFrom.getLocation()))
                continue;
            if (out.containsKey(fPos)) continue;
            // Sort out types that are not reliant on a positioned block
            if (!isTypeReliantOnBlock(fType)) continue;
            if (includeTester != null && !includeTester.test(fBlock))
                continue;
            scanAffectedBlocks(fBlock, true, out, center, includeTester);
        }
    }

    public enum Direction {
        UP,
        DOWN
    }

}