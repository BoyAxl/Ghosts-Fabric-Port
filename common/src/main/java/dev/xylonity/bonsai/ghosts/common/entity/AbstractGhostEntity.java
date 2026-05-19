package dev.xylonity.bonsai.ghosts.common.entity;

import dev.xylonity.bonsai.ghosts.util.GhostOwnerTracker;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public abstract class AbstractGhostEntity extends TamableAnimal implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 0 sit, 1 follow, 2 idle
    private static final EntityDataAccessor<Integer> MAIN_INTERACTION = SynchedEntityData.defineId(AbstractGhostEntity.class, EntityDataSerializers.INT);

    private boolean chunkForced = false;
    private long forcedChunk = Long.MIN_VALUE;

    private boolean isTracked;

    public AbstractGhostEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.isTracked = false;
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide()) {
            if (getOwnerUUID() != null) {
                if (!isTracked) {
                    GhostOwnerTracker.getInstance().addGhost(this);
                    isTracked = true;
                }

                ensureChunkForced();
            }

        }

    }

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide() && isTracked) {
            GhostOwnerTracker.getInstance().removeGhost(this);
            isTracked = false;
        }

        super.remove(reason);
    }

    private void ensureChunkForced() {
        if (level() instanceof ServerLevel serverLevel) {
            ChunkPos chunkPosition = ChunkPos.containing(blockPosition());
            long now = chunkPosition.pack();

            if (chunkForced && forcedChunk != now) {
                ChunkPos old = ChunkPos.unpack(forcedChunk);
                serverLevel.setChunkForced(old.x(), old.z(), false);
                chunkForced = false;
            }

            if (!chunkForced) {
                serverLevel.setChunkForced(chunkPosition.x(), chunkPosition.z(), true);
                forcedChunk = now;
                chunkForced = true;
            }

        }

    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(MAIN_INTERACTION, 0);
    }

    public void cycleMainInteraction(Player player) {
        int interaction = (getMainInteraction() + 1) % 3;

        setOrderedToSit(interaction == 0);

        if (player != null) {
            switch (interaction) {
                case 0 -> player.sendOverlayMessage(Component.translatable("entity.ghosts.client_message.interaction_0", this.getName()));
                case 1 -> player.sendOverlayMessage(Component.translatable("entity.ghosts.client_message.interaction_1", this.getName()));
                case 2 -> player.sendOverlayMessage(Component.translatable("entity.ghosts.client_message.interaction_2", this.getName()));
            }

        }

        this.entityData.set(MAIN_INTERACTION, interaction);
    }

    /**
     * Handles internal ghost body rotation to match its movement direction
     */
    protected void rotateBody() {
        Vec3 vel = this.getDeltaMovement();
        if (vel.lengthSqr() < 1.0E-4) return;

        float yaw = (float) (Mth.atan2(vel.z, vel.x) * (180f / Math.PI)) - 90F;
        float pitch = (float) (-(Mth.atan2(vel.y, Math.sqrt(vel.x * vel.x + vel.z * vel.z)) * (180F / Math.PI)));

        this.setYRot(yaw);
        this.setYHeadRot(yaw);
        this.yBodyRot = yaw;
        this.yRotO = yaw;
        this.yBodyRotO = yaw;

        this.setXRot(pitch);
        this.xRotO = pitch;
    }

    protected void moveToPos(Vec3 target, double speed, float lerp) {
        Vec3 delta = target.subtract(this.position());
        if (delta.length() < 1.0E-3) return;

        Vec3 velocity = delta.scale(1.0 / delta.length()).scale(speed);
        Vec3 currentVelocity = this.getDeltaMovement();
        this.setDeltaMovement(Mth.lerp(lerp, currentVelocity.x, velocity.x), Mth.lerp(lerp, currentVelocity.y, velocity.y), Mth.lerp(lerp, currentVelocity.z, velocity.z));

        this.getLookControl().setLookAt(target.x, target.y, target.z);
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        if (source.is(DamageTypes.IN_WALL)) return false;
        return super.hurtServer(serverLevel, source, amount);
    }

    public void setMainInteraction(int interaction) {
        this.entityData.set(MAIN_INTERACTION, interaction);
    }

    public int getMainInteraction() {
        return this.entityData.get(MAIN_INTERACTION);
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setMainInteraction(input.getIntOr("MainInteraction", 0));
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("MainInteraction", getMainInteraction());
    }

    public ItemStack getHoldItem() {
        return this.getItemBySlot(EquipmentSlot.MAINHAND);
    }

    public UUID getOwnerUUID() {
        var owner = this.getOwnerReference();
        return owner == null ? null : owner.getUUID();
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.GLOW_BERRIES);
    }

    protected ItemEntity spawnAtLocation(ItemStack stack, float yOffset) {
        return level() instanceof ServerLevel serverLevel ? super.spawnAtLocation(serverLevel, stack, yOffset) : null;
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

}
