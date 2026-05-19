package dev.xylonity.bonsai.ghosts.common.entity.ai.smallghost;

import dev.xylonity.bonsai.ghosts.common.entity.ghost.SmallGhostEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import java.util.EnumSet;

public class SmallGhostFindBurrowGoal extends Goal {

    private static final double BURROW_CENTER_DISTANCE = 0.18D;
    private static final int BURROW_SEARCH_INTERVAL = 20;

    private final SmallGhostEntity ghost;
    private final double speed;
    private final int searchRange;
    private final int verticalSearchRange;

    @Nullable
    private BlockPos targetBurrowPos;
    private int nextSearchTick;

    public SmallGhostFindBurrowGoal(SmallGhostEntity ghost, double speed, int searchRange, int verticalSearchRange) {
        this.ghost = ghost;
        this.speed = speed;
        this.searchRange = searchRange;
        this.verticalSearchRange = verticalSearchRange;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!ghost.canStartBurrow() || ghost.tickCount < nextSearchTick) {
            return false;
        }

        targetBurrowPos = findNearestBurrowPos();
        if (targetBurrowPos == null) {
            nextSearchTick = ghost.tickCount + BURROW_SEARCH_INTERVAL;
            return false;
        }

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return targetBurrowPos != null
                && ghost.canStartBurrow()
                && ghost.canUseBurrow(targetBurrowPos);
    }

    @Override
    public void start() {
        ghost.noPhysics = true;
        ghost.getNavigation().stop();
    }

    @Override
    public void stop() {
        targetBurrowPos = null;

        if (!ghost.getIsSleeping()) {
            ghost.noPhysics = false;
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (targetBurrowPos == null) {
            return;
        }

        Vec3 center = Vec3.atBottomCenterOf(targetBurrowPos.above());
        ghost.getLookControl().setLookAt(center.x, center.y, center.z);

        if (isAtBurrowCenter(center)) {
            ghost.tryStartBurrowAt(targetBurrowPos);
            return;
        }

        steerTo(center);
    }

    @Nullable
    private BlockPos findNearestBurrowPos() {
        BlockPos origin = ghost.blockPosition();
        BlockPos min = origin.offset(-searchRange, -verticalSearchRange, -searchRange);
        BlockPos max = origin.offset(searchRange, verticalSearchRange, searchRange);

        BlockPos nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (!ghost.canUseBurrow(pos)) {
                continue;
            }

            double distance = Vec3.atBottomCenterOf(pos.above()).distanceToSqr(ghost.position());
            if (distance < nearestDistance) {
                nearest = pos.immutable();
                nearestDistance = distance;
            }
        }

        return nearest;
    }

    private boolean isAtBurrowCenter(Vec3 center) {
        double dx = ghost.getX() - center.x;
        double dy = ghost.getY() - center.y;
        double dz = ghost.getZ() - center.z;
        return dx * dx + dy * dy + dz * dz <= BURROW_CENTER_DISTANCE * BURROW_CENTER_DISTANCE;
    }

    private void steerTo(Vec3 target) {
        Vec3 delta = target.subtract(ghost.position());
        double distance = delta.length();
        if (distance < 1.0E-3D) {
            return;
        }

        double adjustedSpeed = speed * Mth.clamp(distance / 2.0D, 0.15D, 1.0D);
        Vec3 velocity = delta.scale(adjustedSpeed / distance);
        Vec3 currentVelocity = ghost.getDeltaMovement();

        ghost.setDeltaMovement(
                Mth.lerp(0.2D, currentVelocity.x, velocity.x),
                Mth.lerp(0.2D, currentVelocity.y, velocity.y),
                Mth.lerp(0.2D, currentVelocity.z, velocity.z)
        );
    }

}
