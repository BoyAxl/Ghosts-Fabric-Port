package dev.xylonity.bonsai.ghosts.configurations.tree;

import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.rootplacers.RootPlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.block.Blocks;

import java.util.Optional;

public class GhostsTreeConfigurationBuilder extends TreeConfiguration.TreeConfigurationBuilder {

    public GhostsTreeConfigurationBuilder(BlockStateProvider trunkProvider, TrunkPlacer trunkPlacer, BlockStateProvider foliageProvider, FoliagePlacer foliagePlacer, Optional<RootPlacer> rootPlacer, FeatureSize minimumSize) {
        super(trunkProvider, trunkPlacer, foliageProvider, foliagePlacer, rootPlacer, minimumSize, BlockStateProvider.simple(Blocks.DIRT));
    }

    public GhostsTreeConfigurationBuilder(BlockStateProvider trunkProvider, TrunkPlacer trunkPlacer, BlockStateProvider foliageProvider, FoliagePlacer foliagePlacer, FeatureSize minimumSize) {
        super(trunkProvider, trunkPlacer, foliageProvider, foliagePlacer, minimumSize);
    }

}
