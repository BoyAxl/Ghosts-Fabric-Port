package dev.xylonity.bonsai.ghosts.common.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.instance.InstancedAnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;

public class PassiveEntity extends Animal implements GeoEntity {

    private final AnimatableInstanceCache cache = new InstancedAnimatableInstanceCache(this);

    protected PassiveEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    protected ItemEntity spawnAtLocation(ItemStack stack) {
        return level() instanceof ServerLevel serverLevel ? super.spawnAtLocation(serverLevel, stack) : null;
    }

    protected ItemEntity spawnAtLocation(ItemStack stack, float yOffset) {
        return level() instanceof ServerLevel serverLevel ? super.spawnAtLocation(serverLevel, stack, yOffset) : null;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) { }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

}
