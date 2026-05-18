package dev.xylonity.bonsai.ghosts.configurations.tree;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.xylonity.bonsai.ghosts.registry.GhostsBlocks;
import dev.xylonity.bonsai.ghosts.registry.GhostsTrunkPlacerTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.function.BiConsumer;

public class HauntedTrunkPlacer extends StraightTrunkPlacer {

    public static final MapCodec<HauntedTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec((instance) -> trunkPlacerParts(instance).apply(instance, HauntedTrunkPlacer::new));

    private boolean hasSpawnedEye;

    public HauntedTrunkPlacer(int i, int j, int k) {
        super(i, j, k);
        this.hasSpawnedEye = false;
    }

    @Override
    protected @NotNull TrunkPlacerType<?> type() {
        return GhostsTrunkPlacerTypes.HAUNTED_TRUNK_PLACER.get();
    }

    @Override
    public @NotNull List<FoliagePlacer.FoliageAttachment> placeTrunk(@NotNull WorldGenLevel level, @NotNull BiConsumer<BlockPos, BlockState> blockSetter, @NotNull RandomSource random, int freeTreeHeight, BlockPos pos, @NotNull TreeConfiguration config) {
        this.hasSpawnedEye = false;
        placeBelowTrunkBlock(level, blockSetter, random, pos.below(), config);

        for (int i = 0; i < freeTreeHeight; ++i) {
            final BlockPos currentPosition = pos.above(i);

            if (random.nextFloat() < 0.10f && !hasSpawnedEye) {
                final Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                blockSetter.accept(currentPosition, GhostsBlocks.HAUNTED_EYE_LOG.get().defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, direction));
                this.hasSpawnedEye = true;
            }
            else {
                this.placeLog(level, blockSetter, random, currentPosition, config);
            }

        }

        return ImmutableList.of(new FoliagePlacer.FoliageAttachment(pos.above(freeTreeHeight), 0, false));
    }

}
