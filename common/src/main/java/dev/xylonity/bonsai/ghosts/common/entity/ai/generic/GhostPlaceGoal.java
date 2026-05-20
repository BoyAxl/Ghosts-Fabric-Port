package dev.xylonity.bonsai.ghosts.common.entity.ai.generic;

import dev.xylonity.bonsai.ghosts.common.entity.ghost.GhostEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import java.util.EnumSet;
import java.util.function.Predicate;

public class GhostPlaceGoal extends Goal {

    private final GhostEntity ghost;
    private final Predicate<ItemStack> placeables;
    private final Predicate<BlockState> preference;
    private final int minLight;
    private final int retryCooldown;
    private final double approachSpeed;

    private int nextTryTick;
    private int lastRepathTick;
    private final double maxOwnerDrift = 6.0D * 6.0D;

    @Nullable
    private BlockPos targetPlacePos;

    @Nullable
    private BlockPos lastPlacedPos;

    public GhostPlaceGoal(GhostEntity ghost, Predicate<ItemStack> placeables, Predicate<BlockState> pref, int lightThreshold, int retry, double speed) {
        this.ghost = ghost;
        this.placeables = placeables;
        this.preference = pref;
        this.minLight = lightThreshold;
        this.retryCooldown = Math.max(5, retry);
        this.approachSpeed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (ghost.level().isClientSide()) {
            return false;
        }
        if (ghost.getMainInteraction() != 1) {
            return false;
        }
        if (ghost.isInSittingPose() || ghost.isPassenger()) {
            return false;
        }
        if (ghost.tickCount < nextTryTick) {
            return false;
        }

        LivingEntity owner = ghost.getOwner();
        if (owner == null) {
            return false;
        }

        ItemStack heldStack = ghost.getHoldItem();
        if (!isPlaceableBlockItem(heldStack)) {
            return false;
        }

        if (lastPlacedPos != null) {
            int manhattanDistance = owner.blockPosition().distManhattan(lastPlacedPos);
            int minOwnerDistFromLast = 4;
            if (manhattanDistance < minOwnerDistFromLast) {
                return false;
            }

        }

        BlockItem blockItem = (BlockItem) heldStack.getItem();
        BlockPos found = findDarkPlacePosNearOwner(ghost.level(), owner, blockItem);
        if (found == null) {
            return false;
        }

        targetPlacePos = found.immutable();

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (ghost.level().isClientSide()) {
            return false;
        }
        if (ghost.getMainInteraction() != 1) {
            return false;
        }
        if (targetPlacePos == null) {
            return false;
        }

        LivingEntity owner = ghost.getOwner();
        if (owner == null) {
            return false;
        }
        if (owner.position().distanceToSqr(Vec3.atCenterOf(targetPlacePos)) > maxOwnerDrift) {
            return false;
        }

        ItemStack heldStack = ghost.getHoldItem();
        if (!isPlaceableBlockItem(heldStack)) {
            return false;
        }

        BlockItem blockItem = (BlockItem) heldStack.getItem();

        return isValidPlacement(ghost.level(), targetPlacePos, blockItem);
    }

    @Override
    public void start() {
        lastRepathTick = ghost.tickCount;

        if (targetPlacePos == null) {
            return;
        }

        moveToTarget();
    }

    @Override
    public void stop() {
        ghost.getNavigation().stop();
        targetPlacePos = null;

        int desired = ghost.tickCount + retryCooldown;
        if (nextTryTick < desired) {
            nextTryTick = desired;
        }
        
    }

    @Override
    public void tick() {
        if (targetPlacePos == null) {
            return;
        }

        LivingEntity owner = ghost.getOwner();
        if (owner == null) {
            stop();
            return;
        }

        if (!ghost.level().getBlockState(targetPlacePos).isAir()) {
            stop();
            return;
        }

        Vec3 center = Vec3.atCenterOf(targetPlacePos);
        if (owner.position().distanceToSqr(center) > maxOwnerDrift) {
            stop();
            return;
        }

        double distSquare = ghost.position().distanceToSqr(center);

        if (distSquare <= (3.0D * 3.0D)) {
            ghost.getLookControl().setLookAt(center.x, center.y, center.z);
        }

        double placeDistanceSquare = 1.4D * 1.4D;
        if (distSquare <= placeDistanceSquare) {
            ghost.getNavigation().stop();

            if (canSee(targetPlacePos)) {
                if (placeNow(targetPlacePos)) {
                    stop();
                }

            }

            return;
        }

        int ticksPerRepath = 12;
        if (ghost.tickCount - lastRepathTick >= ticksPerRepath) {
            lastRepathTick = ghost.tickCount;
            moveToTarget();
        }

    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private void moveToTarget() {
        if (targetPlacePos == null) {
            return;
        }

        Vec3 movePos = Vec3.atCenterOf(targetPlacePos).add(0.0D, 0.1D, 0.0D);
        ghost.getNavigation().moveTo(movePos.x, movePos.y, movePos.z, approachSpeed);
    }

    private boolean isPlaceableBlockItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (!placeables.test(stack)) {
            return false;
        }

        return stack.getItem() instanceof BlockItem;
    }

    private boolean isValidPlacement(Level level, BlockPos placePos, BlockItem blockItem) {
        return getValidPlacementState(level, placePos, blockItem) != null;
    }

    private boolean placeNow(BlockPos placePos) {
        ItemStack heldStack = ghost.getHoldItem();
        if (!isPlaceableBlockItem(heldStack)) {
            return false;
        }

        BlockItem blockItem = (BlockItem) heldStack.getItem();
        BlockState placementState = getValidPlacementState(ghost.level(), placePos, blockItem);
        if (placementState == null) {
            return false;
        }

        ghost.triggerAnim("torch_place_controller", "torch_place");

        ghost.level().setBlock(placePos, placementState, 3);
        heldStack.shrink(1);

        if (heldStack.isEmpty()) {
            ghost.setHoldItem(ItemStack.EMPTY);
        }

        lastPlacedPos = placePos.immutable();
        nextTryTick = ghost.tickCount + retryCooldown;

        return true;
    }

    private boolean canSee(BlockPos placePos) {
        Vec3 targetPosition = Vec3.atCenterOf(placePos);
        HitResult hitResult = ghost.level().clip(
                new ClipContext(
                        ghost.getEyePosition(),
                        targetPosition,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        ghost
                )

        );

        if (hitResult.getType() == HitResult.Type.MISS) {
            return true;
        }
        if (hitResult instanceof BlockHitResult blockHitResult) {
            return blockHitResult.getBlockPos().equals(placePos);
        }

        return false;
    }

    @Nullable
    private BlockPos findDarkPlacePosNearOwner(Level level, LivingEntity owner, BlockItem blockItem) {
        Vec3 forward = owner.getDeltaMovement();
        forward = new Vec3(forward.x, 0.0D, forward.z);
        if (forward.lengthSqr() < 0.0005D) {
            Vec3 look = owner.getLookAngle();
            forward = new Vec3(look.x, 0.0D, look.z);
        }
        if (forward.lengthSqr() > 0.0001D) {
            forward = forward.normalize();
        }

        BlockPos bestPosition = null;
        double bestScore = Double.POSITIVE_INFINITY;

        BlockPos centerPosition = owner.blockPosition();
        int searchRadius = 4;
        int searchBelow = 3;
        int searchAbove = blockItem.getBlock().defaultBlockState().hasProperty(LanternBlock.HANGING) ? 4 : 0;
        for (int dx = -searchRadius; dx <= searchRadius; dx++) {
            for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                for (int dy = -searchBelow; dy <= searchAbove; dy++) {
                    BlockPos placePosition = centerPosition.offset(dx, dy, dz);
                    BlockState placementState = getValidPlacementState(level, placePosition, blockItem);
                    if (placementState == null) {
                        continue;
                    }

                    int light = level.getMaxLocalRawBrightness(placePosition);
                    Vec3 toPosition = Vec3.atCenterOf(placePosition).subtract(owner.position());
                    Vec3 toPositionFlat = new Vec3(toPosition.x, 0.0D, toPosition.z);
                    double distanceToPosition = toPositionFlat.length();

                    double behindPenalty = 0D;
                    if (forward.lengthSqr() > 0.0001D && toPositionFlat.lengthSqr() > 0.0001D) {
                        double normalized = forward.dot(toPositionFlat.normalize());
                        behindPenalty = (1.0D - normalized) * 2.0D;
                    }

                    double score = (light * 1000.0D) + (distanceToPosition * 25.0D) + (behindPenalty * 200.0D);
                    if (isHangingLantern(placementState)) {
                        score -= 500.0D;
                    }

                    if (score < bestScore) {
                        bestScore = score;
                        bestPosition = placePosition;
                    }
                }

            }

        }

        return bestPosition;
    }

    private boolean isHangingLantern(BlockState state) {
        return state.hasProperty(LanternBlock.HANGING) && state.getValue(LanternBlock.HANGING);
    }

    @Nullable
    private BlockState getValidPlacementState(Level level, BlockPos placePos, BlockItem blockItem) {
        if (!level.getBlockState(placePos).isAir()) {
            return null;
        }

        if (lastPlacedPos != null) {
            int dx = Math.abs(placePos.getX() - lastPlacedPos.getX());
            int dy = Math.abs(placePos.getY() - lastPlacedPos.getY());
            int dz = Math.abs(placePos.getZ() - lastPlacedPos.getZ());
            if (dx <= 1 && dy <= 1 && dz <= 1) {
                return null;
            }

        }

        if (level.getMaxLocalRawBrightness(placePos) > minLight) {
            return null;
        }

        BlockState defaultState = blockItem.getBlock().defaultBlockState();
        if (defaultState.hasProperty(LanternBlock.HANGING)) {
            BlockState hangingState = defaultState.setValue(LanternBlock.HANGING, true);
            if (hasPreferredSupport(level, placePos.above()) && hangingState.canSurvive(level, placePos)) {
                return hangingState;
            }

            BlockState standingState = defaultState.setValue(LanternBlock.HANGING, false);
            if (hasPreferredSupport(level, placePos.below()) && standingState.canSurvive(level, placePos)) {
                return standingState;
            }

            return null;
        }

        if (!hasPreferredSupport(level, placePos.below())) {
            return null;
        }

        return defaultState.canSurvive(level, placePos) ? defaultState : null;
    }

    private boolean hasPreferredSupport(Level level, BlockPos supportPos) {
        BlockState supportState = level.getBlockState(supportPos);

        return !supportState.isAir() && preference.test(supportState);
    }

}
