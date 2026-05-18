package dev.xylonity.bonsai.ghosts.common.entity.ai.smallghost;

import dev.xylonity.bonsai.ghosts.common.entity.ghost.SmallGhostEntity;
import dev.xylonity.bonsai.ghosts.common.entity.variant.SmallGhostVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public class SmallGhostFindBurrowGoal extends MoveToBlockGoal {

    private final SmallGhostEntity ghost;

    public SmallGhostFindBurrowGoal(SmallGhostEntity ghost, double speed, int searchRange, int verticalSearchRange) {
        super(ghost, speed, searchRange, verticalSearchRange);
        this.ghost = ghost;
    }

    @Override
    public boolean canUse() {
        return wantsToBurrow() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return wantsToBurrow() && super.canContinueToUse();
    }

    @Override
    public void start() {
        ghost.noPhysics = false;
        super.start();
    }

    @Override
    protected boolean isValidTarget(LevelReader level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return SmallGhostEntity.isBurrowGround(state) && level.getBlockState(pos.above()).isAir();
    }

    private boolean wantsToBurrow() {
        return !ghost.level().isClientSide()
                && ghost.getVariant() == SmallGhostVariant.PLANT
                && ghost.getHoldItem().isEmpty()
                && !ghost.getIsSleeping()
                && !ghost.level().isBrightOutside();
    }

}
