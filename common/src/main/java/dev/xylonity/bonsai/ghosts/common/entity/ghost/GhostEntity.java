package dev.xylonity.bonsai.ghosts.common.entity.ghost;

import dev.xylonity.bonsai.ghosts.common.entity.AbstractGhostEntity;
import dev.xylonity.bonsai.ghosts.common.entity.ai.control.GhostMoveControl;
import dev.xylonity.bonsai.ghosts.common.entity.ai.generic.*;
import dev.xylonity.bonsai.ghosts.registry.GhostsSounds;
import dev.xylonity.bonsai.ghosts.tag.GhostsTags;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.AirRandomPos;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;

import org.jetbrains.annotations.Nullable;

public class GhostEntity extends AbstractGhostEntity {

    private static final EntityDataAccessor<Boolean> SHOULD_RESET_CD = SynchedEntityData.defineId(GhostEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> BLINK_CD = SynchedEntityData.defineId(GhostEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BLINK_ANIM_CD = SynchedEntityData.defineId(GhostEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SHOULD_UNENCHANT = SynchedEntityData.defineId(GhostEntity.class, EntityDataSerializers.BOOLEAN);

    private int cdUnenchant = 0;

    public GhostEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);

        this.setPathfindingMalus(PathType.POWDER_SNOW, -1.0F);
        this.setPathfindingMalus(PathType.LAVA, -1.0F);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.BLOCKED, -1.0F);
        this.setPathfindingMalus(PathType.LEAVES, -1.0F);

        this.moveControl = new GhostMoveControl(this);
    }

    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigator = new FlyingPathNavigation(this, level);

        navigator.setCanOpenDoors(false);
        navigator.setCanFloat(true);

        return navigator;
    }

    public static AttributeSupplier.Builder setAttributes() {
        return AbstractGolem.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.FLYING_SPEED, 0.3F)
                .add(Attributes.MOVEMENT_SPEED, 0.3F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this) {
            @Override
            public boolean canUse() {
                if (GhostEntity.this.isTame() && GhostEntity.this.isInWater()) {
                    return false;
                }

                return super.canUse();
            }
        });
        this.goalSelector.addGoal(2, new StayWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new GhostApproachHeldGlowBerriesGoal(this, 0.6D, 1.6D, 6.0D, 0.1f, 12));
        this.goalSelector.addGoal(7, new GhostFollowOwnerGoal(this, 0.6D, 3.0F, 7.0F, 0.2f));
        this.goalSelector.addGoal(6, new GhostPlaceGoal(this, stack -> stack.is(GhostsTags.GHOST_PLACEABLE), state -> true, 6, 10, 0.75));
        this.goalSelector.addGoal(9, new GhostWanderGoal(this, 0.43f));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 6.0F));
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isAffectedByFluids() {
        return !isTame();
    }

    @Override
    public boolean isPushedByFluid() {
        return !isTame();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SHOULD_RESET_CD, false);
        builder.define(BLINK_CD, 0);
        builder.define(BLINK_ANIM_CD, 0);
        builder.define(SHOULD_UNENCHANT, false);
    }

    public void setHoldItem(ItemStack holdItem) {
        if (this.shouldUnechant()) {
            this.setShouldUnenchant(false);
            this.setCdUnenchant(0);
        }
        if (holdItem.isEnchanted()) {
            this.setShouldUnenchant(true);
            this.setCdUnenchant(82);
        }

        this.setItemSlotAndDropWhenKilled(EquipmentSlot.MAINHAND, holdItem);
    }

    public int getCdUnenchant() {
        return this.cdUnenchant;
    }

    public void setCdUnenchant(int cd) {
        this.cdUnenchant = cd;
    }

    public int getBlinkCd() {
        return this.entityData.get(BLINK_CD);
    }

    public void setBlinkCd(int cd) {
        this.entityData.set(BLINK_CD, cd);
    }

    public int getBlinkAnimCd() {
        return this.entityData.get(BLINK_ANIM_CD);
    }

    public void setBlinkAnimCd(int cd) {
        this.entityData.set(BLINK_ANIM_CD, cd);
    }

    public void setShouldResetCd(boolean shouldResetCd) {
        this.entityData.set(SHOULD_RESET_CD, shouldResetCd);
    }

    public boolean getShouldResetCd() {
        return this.entityData.get(SHOULD_RESET_CD);
    }

    private void setShouldUnenchant(boolean shouldUnenchant) {
        this.entityData.set(SHOULD_UNENCHANT, shouldUnenchant);
    }

    private boolean shouldUnechant() {
        return this.entityData.get(SHOULD_UNENCHANT);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("CdUnenchant", getCdUnenchant());
    }

    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Main tame handler
        if (!isTame() && stack.getItem() == Items.GLOW_BERRIES) {

            if (!player.getAbilities().instabuild) stack.shrink(1);

            if (this.random.nextInt(3) == 0) {
                this.tame(player);

                this.setPersistenceRequired();
                this.navigation.stop();
                this.setOrderedToSit(true);

                level().broadcastEntityEvent(this, (byte) 7);

                return InteractionResult.SUCCESS;
            }
            else {
                level().broadcastEntityEvent(this, (byte) 6);

                return InteractionResult.FAIL;
            }
        }

        if (level().isClientSide()) return InteractionResult.SUCCESS;

        // Heal or cycle owner interaction state (per priority order)
        if (isTame() && player == getOwner()) {
            if (player.isShiftKeyDown()) {
                // Healing
                if (stack.getItem() == Items.GLOW_BERRIES && getHealth() < getMaxHealth()) {
                    this.heal(4f);
                    return InteractionResult.SUCCESS;
                }

                // Head slot items use vanilla equipment sync, while mushroom items render on custom model bones.
                if (isHeadEquipment(stack) || isHeadMushroomItem(stack)) {
                    equipHeadItem(player, stack);

                    return InteractionResult.SUCCESS;
                }

                // Item retrieval
                if (!getHoldItem().isEmpty()) {
                    this.spawnAtLocation(this.getHoldItem(), 0.5F);
                    setHoldItem(ItemStack.EMPTY);

                    return InteractionResult.SUCCESS;
                }

                // Item equipped
                if (!stack.isEmpty()) {
                    this.setHoldItem(stack.copy());
                    if (!player.getAbilities().instabuild) stack.setCount(0);

                    return InteractionResult.SUCCESS;
                }

                // Head item unequipped
                ItemStack headItem = getItemBySlot(EquipmentSlot.HEAD);
                if (!headItem.isEmpty()) {
                    this.spawnAtLocation(headItem, 0.5F);
                    setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);

                    return InteractionResult.SUCCESS;
                }
            }
            else {
                cycleMainInteraction(player);
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    public static boolean isHeadMushroomItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(GhostsTags.GHOST_HEAD_MUSHROOMS);
    }

    public static boolean isBrownHeadMushroom(ItemStack stack) {
        return stack.is(Items.BROWN_MUSHROOM);
    }

    public static boolean isRedHeadMushroom(ItemStack stack) {
        return isHeadMushroomItem(stack) && !isBrownHeadMushroom(stack);
    }

    private boolean isHeadEquipment(ItemStack stack) {
        return !stack.isEmpty() && getEquipmentSlotForItem(stack) == EquipmentSlot.HEAD;
    }

    private void equipHeadItem(Player player, ItemStack stack) {
        ItemStack currentHead = getItemBySlot(EquipmentSlot.HEAD);
        if (!currentHead.isEmpty()) {
            this.spawnAtLocation(currentHead, 0.5F);
            setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        }

        ItemStack copy = stack.copy();
        copy.setCount(1);
        setItemSlotAndDropWhenKilled(EquipmentSlot.HEAD, copy);

        if (!player.getAbilities().instabuild) stack.shrink(1);
    }

    @Override
    protected int getBaseExperienceReward(ServerLevel serverLevel) {
        return 1 + serverLevel.getRandom().nextInt(2, 4);
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);

        if (level().isClientSide())
            return;

        rotateBody();

        if (getBlinkCd() > 0) {
            setBlinkCd(getBlinkCd() - 1);
        } else {
            if (getBlinkAnimCd() > 0)
                setBlinkAnimCd(getBlinkAnimCd() - 1);
            else {
                if (getShouldResetCd()) {
                    setShouldResetCd(false);

                    setBlinkCd(this.random.nextInt(80, 120));
                } else {
                    setShouldResetCd(true);

                    setBlinkAnimCd(6);
                }
            }
        }

        if (getCdUnenchant() > 0)
            setCdUnenchant(getCdUnenchant() - 1);

        ItemStack heldItemStack = getHoldItem();
        if (heldItemStack.isEnchanted()) {
            if (getCdUnenchant() == 10 && this.shouldUnechant()) {
                this.spawnAtLocation(removeEnchants(heldItemStack), 0.5F);
                setHoldItem(ItemStack.EMPTY);
                this.setShouldUnenchant(false);
            }
            else if (getCdUnenchant() == 0) {
                if (!this.shouldUnechant()) {
                    startUnenchantAnim();
                } else {
                    this.spawnAtLocation(removeEnchants(heldItemStack), 0.5F);
                    setHoldItem(ItemStack.EMPTY);
                    this.setShouldUnenchant(false);
                }
            }

        }

    }

    private void startUnenchantAnim() {
        this.setCdUnenchant(82);

        this.setShouldUnenchant(true);
    }

    private ItemStack removeEnchants(ItemStack item) {
        ItemStack itemstack = item.copy();

        if (level() instanceof ServerLevel level)
            ExperienceOrb.award(level, this.getPosition(0), getExperienceFromItem(itemstack));

        itemstack.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        itemstack.remove(DataComponents.STORED_ENCHANTMENTS);
        itemstack.remove(DataComponents.REPAIR_COST);

        return itemstack.copy();
    }

    private int getExperienceFromItem(ItemStack stack) {
        int l = 0;
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : stack.getEnchantments().entrySet()) {
            Holder<Enchantment> enchantment = entry.getKey();
            if (!enchantment.is(EnchantmentTags.CURSE)) {
                l += enchantment.value().getMinCost(entry.getIntValue());
            }
        }

        return l;
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        if (source.getEntity() != null) {
            Vec3 vec = AirRandomPos.getPosTowards(this, 32, 32, 32, new Vec3(32, 32, 32), 32);
            if (vec != null) {
                moveToPos(vec, 0.55D, 0.6f);
            }
        }

        return super.hurtServer(serverLevel, source, amount);
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.getInt("CdUnenchant").ifPresent(this::setCdUnenchant);
        input.read("HeadCosmetic", ItemStack.OPTIONAL_CODEC).ifPresent(stack -> {
            if (!stack.isEmpty() && getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
                setItemSlotAndDropWhenKilled(EquipmentSlot.HEAD, stack);
            }
        });
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return GhostsSounds.GHOST_AMBIENT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return GhostsSounds.GHOST_DEATH.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return GhostsSounds.GHOST_HURT.get();
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor levelAccessor, DifficultyInstance difficulty, EntitySpawnReason mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        return super.finalizeSpawn(levelAccessor, difficulty, mobSpawnType, spawnGroupData);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>("bodyController", 4, this::bodyAC));
        registrar.add(new AnimationController<>("armsController", 4, this::armsAC));
        registrar.add(new AnimationController<>("blinkController", 2, this::blinkAC));
        registrar.add(new AnimationController<GhostEntity>("torch_place_controller", 2, state -> PlayState.STOP).triggerableAnim("torch_place", RawAnimation.begin().thenPlay("torch_place")));
    }

    private PlayState bodyAC(AnimationTest<GhostEntity> event) {
        if (event.isMoving()) {
            event.setAnimation(RawAnimation.begin().thenPlay("ghost_move"));
        }
        else if (isInSittingPose()) {
            event.setAnimation(RawAnimation.begin().thenPlay("ghost_sitting"));
        }
        else {
            event.setAnimation(RawAnimation.begin().thenPlay("ghost_idle"));
        }

        return PlayState.CONTINUE;
    }

    private PlayState blinkAC(AnimationTest<GhostEntity> event) {
        if (getBlinkCd() == 0) {
            event.setAnimation(RawAnimation.begin().thenPlay("ghost_blink"));
        }

        return PlayState.CONTINUE;
    }

    private PlayState armsAC(AnimationTest<GhostEntity> event) {
        boolean hasItem = !getHoldItem().isEmpty();
        if (hasItem) {
            if (this.shouldUnechant()) {
                event.setAnimation(RawAnimation.begin().thenPlay("ghost_unenchant"));
            } else {
                event.setAnimation(RawAnimation.begin().thenLoop("ghost_arms_hold"));
            }

        } else {
            if (isInSittingPose()) {
                event.setAnimation(RawAnimation.begin().thenLoop("ghost_idle_arms"));
            } else {
                event.setAnimation(RawAnimation.begin().thenLoop(event.isMoving() ? "ghost_move_arms" : "ghost_idle_arms"));
            }

        }

        return PlayState.CONTINUE;
    }


}
