package dev.xylonity.bonsai.ghosts.common.entity.boat;

import dev.xylonity.bonsai.ghosts.registry.GhostsEntities;
import dev.xylonity.bonsai.ghosts.registry.GhostsItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.level.Level;

public class HauntedChestBoat extends ChestBoat {

    public HauntedChestBoat(EntityType<? extends ChestBoat> entityType, Level level) {
        super(entityType, level, GhostsItems.HAUNTED_CHEST_BOAT);
    }

    public HauntedChestBoat(Level level, double x, double y, double z) {
        this(GhostsEntities.HAUNTED_CHEST_BOAT.get(), level);
        this.setPos(x, y, z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    public void setBoatVariant(HauntedBoat.Type variant) {
        ;;
    }

    public HauntedBoat.Type getBoatVariant() {
        return HauntedBoat.Type.HAUNTED;
    }


}
