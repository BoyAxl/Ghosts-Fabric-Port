package dev.xylonity.bonsai.ghosts.common.entity.boat;

import dev.xylonity.bonsai.ghosts.registry.GhostsBlocks;
import dev.xylonity.bonsai.ghosts.registry.GhostsEntities;
import dev.xylonity.bonsai.ghosts.registry.GhostsItems;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.function.IntFunction;

public class HauntedBoat extends Boat {

    public HauntedBoat(EntityType<? extends Boat> entityType, Level level) {
        super(entityType, level, GhostsItems.HAUNTED_BOAT);
    }

    public HauntedBoat(Level level, double x, double y, double z) {
        this(GhostsEntities.HAUNTED_BOAT.get(), level);
        this.setPos(x, y, z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    public void setBoatVariant(Type variant) {
        ;;
    }

    public Type getBoatVariant() {
        return Type.HAUNTED;
    }

    public enum Type implements StringRepresentable {
        HAUNTED(GhostsBlocks.HAUNTED_PLANKS.get(), "haunted");

        private final String name;
        private final Block planks;
        public static final StringRepresentable.EnumCodec<Type> CODEC = StringRepresentable.fromEnum(Type::values);
        private static final IntFunction<Type> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);

        Type(Block planks, String name) {
            this.name = name;
            this.planks = planks;
        }

        public String getSerializedName() {
            return this.name;
        }

        public String getName() {
            return this.name;
        }

        public Block getPlanks() {
            return this.planks;
        }

        public String toString() {
            return this.name;
        }

        public static Type byId(int id) {
            return BY_ID.apply(id);
        }

        public static Type byName(String name) {
            return CODEC.byName(name, HAUNTED);
        }

    }

}
