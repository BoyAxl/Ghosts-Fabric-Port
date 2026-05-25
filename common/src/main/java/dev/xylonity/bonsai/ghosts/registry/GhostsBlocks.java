package dev.xylonity.bonsai.ghosts.registry;

import dev.xylonity.bonsai.ghosts.Ghosts;
import dev.xylonity.bonsai.ghosts.common.block.*;
import dev.xylonity.bonsai.ghosts.configurations.tree.HauntedTreeGrower;
import dev.xylonity.bonsai.ghosts.tag.GhostsWoodTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.Supplier;

public class GhostsBlocks {

    public static void init() { }

    public static final Supplier<Block> HAUNTED_PLANKS = register("haunted_planks", () -> new Block(properties("haunted_planks", BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_PLANKS))));
    public static final Supplier<Block> HAUNTED_STAIRS = register("haunted_stairs", () -> new HauntedStair(Blocks.ACACIA_STAIRS.defaultBlockState(), properties("haunted_stairs", BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_STAIRS))));
    public static final Supplier<Block> HAUNTED_SLAB = register("haunted_slab", () -> new SlabBlock(properties("haunted_slab", BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_SLAB))));
    public static final Supplier<Block> HAUNTED_LOG = register("haunted_log", () -> new RotatedPillarBlock(properties("haunted_log", BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_LOG))));
    public static final Supplier<Block> STRIPPED_HAUNTED_LOG = register("stripped_haunted_log", () -> new RotatedPillarBlock(properties("stripped_haunted_log", BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_ACACIA_LOG))));
    public static final Supplier<Block> HAUNTED_TRAPDOOR = register("haunted_trapdoor", () -> new HauntedTrapdoor(properties("haunted_trapdoor", BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_TRAPDOOR)), BlockSetType.ACACIA));
    public static final Supplier<Block> HAUNTED_LEAVES = register("haunted_leaves", () -> new HauntedLeaves(properties("haunted_leaves", BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_LEAVES).lightLevel(value -> 7))));
    public static final Supplier<Block> HAUNTED_BUTTON = register("haunted_button", () -> new HauntedButton(properties("haunted_button", BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_BUTTON)), BlockSetType.ACACIA, 20, false));
    public static final Supplier<Block> HAUNTED_PRESSURE_PLATE = register("haunted_pressure_plate", () -> new HauntedPressurePlateBlock(properties("haunted_pressure_plate", BlockBehaviour.Properties.of().mapColor(Blocks.ACACIA_PLANKS.defaultMapColor()).forceSolidOn().instrument(NoteBlockInstrument.BASS).noCollision().strength(0.5F).ignitedByLava().pushReaction(PushReaction.DESTROY)), BlockSetType.ACACIA));
    public static final Supplier<Block> HAUNTED_EYE_LOG = register("haunted_eye_log", () -> new HauntedEyeBlock(properties("haunted_eye_log", BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_PLANKS).lightLevel(value -> 6))));
    public static final Supplier<Block> CALIBRATED_HAUNTED_EYE = register("calibrated_haunted_eye", () -> new CalibratedHauntedEyeBlock(properties("calibrated_haunted_eye", BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_PLANKS))));
    public static final Supplier<Block> HAUNTED_DOOR = register("haunted_door", () -> new HauntedDoor(properties("haunted_door", BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_DOOR)), BlockSetType.ACACIA));

    public static final Supplier<Block> HAUNTED_SIGN = register("haunted_sign", () -> new HauntedStandingSignBlock(properties("haunted_sign", BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_SIGN)), GhostsWoodTypes.HAUNTED), false);
    public static final Supplier<Block> HAUNTED_HANGING_SIGN = register("haunted_hanging_sign", () -> new HauntedHangingSignBlock(properties("haunted_hanging_sign", BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_HANGING_SIGN)), GhostsWoodTypes.HAUNTED), false);
    public static final Supplier<Block> HAUNTED_WALL_SIGN = register("haunted_wall_sign", () -> new HauntedWallSignBlock(properties("haunted_wall_sign", BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_WALL_SIGN)), GhostsWoodTypes.HAUNTED), false);
    public static final Supplier<Block> HAUNTED_WALL_HANGING_SIGN = register("haunted_wall_hanging_sign", () -> new HauntedWallHangingSignBlock(properties("haunted_wall_hanging_sign", BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_WALL_HANGING_SIGN)), GhostsWoodTypes.HAUNTED), false);

    public static final Supplier<Block> HAUNTED_FENCE = register("haunted_fence", () -> new FenceBlock(properties("haunted_fence", BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_PLANKS))));
    public static final Supplier<Block> HAUNTED_FENCE_GATE = register("haunted_fence_gate", () -> new FenceGateBlock(WoodType.ACACIA, properties("haunted_fence_gate", BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_PLANKS))));

    public static final Supplier<Block> HAUNTED_SAPLING = register("haunted_sapling", () -> new HauntedSapling(HauntedTreeGrower.HAUNTED, properties("haunted_sapling", BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_SAPLING))));

    private static <T extends Block> Supplier<T> register(String id, Supplier<T> block) {
        return Ghosts.PLATFORM.registerBlock(id, block, true);
    }

    private static <T extends Block> Supplier<T> register(String id, Supplier<T> block, boolean registerItem) {
        return Ghosts.PLATFORM.registerBlock(id, block, registerItem);
    }

    private static BlockBehaviour.Properties properties(String id, BlockBehaviour.Properties properties) {
        return properties.setId(ResourceKey.create(Registries.BLOCK, Ghosts.of(id)));
    }

}
