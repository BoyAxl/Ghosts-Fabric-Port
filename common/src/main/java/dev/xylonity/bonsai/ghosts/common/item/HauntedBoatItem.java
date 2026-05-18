package dev.xylonity.bonsai.ghosts.common.item;

import dev.xylonity.bonsai.ghosts.common.entity.boat.HauntedBoat;
import dev.xylonity.bonsai.ghosts.common.entity.boat.HauntedChestBoat;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Predicate;

public class HauntedBoatItem extends Item {

    private static final Predicate<Entity> ENTITY_PREDICATE;
    private final HauntedBoat.Type type;
    private final boolean hasChest;

    public HauntedBoatItem(Properties properties, boolean hasChest, HauntedBoat.Type type) {
        super(properties);
        this.hasChest = hasChest;
        this.type = type;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        HitResult hitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (hitresult.getType() == HitResult.Type.MISS) {
            return InteractionResult.PASS;
        }
        else {
            Vec3 vec3 = player.getViewVector(1.0F);
            List<Entity> list = level.getEntities(player, player.getBoundingBox().expandTowards(vec3.scale((double)5.0F)).inflate((double)1.0F), ENTITY_PREDICATE);
            if (!list.isEmpty()) {
                Vec3 vec31 = player.getEyePosition();

                for (Entity entity : list) {
                    AABB aabb = entity.getBoundingBox().inflate((double)entity.getPickRadius());
                    if (aabb.contains(vec31)) {
                        return InteractionResult.PASS;
                    }

                }

            }

            if (hitresult.getType() == HitResult.Type.BLOCK) {
                AbstractBoat boat = this.getBoat(level, hitresult);
                if (boat instanceof HauntedBoat) {
                    ((HauntedBoat) boat).setBoatVariant(this.type);
                }
                else if (boat instanceof HauntedChestBoat) {
                    ((HauntedChestBoat) boat).setBoatVariant(this.type);
                }

                boat.setYRot(player.getYRot());
                if (!level.noCollision(boat, boat.getBoundingBox())) {
                    return InteractionResult.FAIL;
                }
                else {
                    if (!level.isClientSide()) {
                        level.addFreshEntity(boat);
                        level.gameEvent(player, GameEvent.ENTITY_PLACE, hitresult.getLocation());
                        itemstack.consume(1, player);

                    }

                    player.awardStat(Stats.ITEM_USED.get(this));

                    return InteractionResult.SUCCESS;
                }
            }
            else {
                return InteractionResult.PASS;
            }

        }

    }

    private AbstractBoat getBoat(Level level, HitResult hitResult) {
        return (this.hasChest ? new HauntedChestBoat(level, hitResult.getLocation().x, hitResult.getLocation().y, hitResult.getLocation().z) : new HauntedBoat(level, hitResult.getLocation().x, hitResult.getLocation().y, hitResult.getLocation().z));
    }

    static {
        ENTITY_PREDICATE = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);
    }

}
