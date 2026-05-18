package dev.xylonity.bonsai.ghosts.registry;

import dev.xylonity.bonsai.ghosts.Ghosts;
import dev.xylonity.bonsai.ghosts.common.entity.boat.HauntedBoat;
import dev.xylonity.bonsai.ghosts.common.item.HauntedBoatItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.*;

import java.util.function.Supplier;

public class GhostsItems {

    public static void init() { ;; }

    public static final Supplier<SpawnEggItem> GHOST_SPAWN_EGG = registerSpawnEgg("ghost_spawn_egg", GhostsEntities.GHOST, 0xA5FFFF, 0x783D7C, properties("ghost_spawn_egg"));
    public static final Supplier<SpawnEggItem> SMALL_GHOST_SPAWN_EGG = registerSpawnEgg("small_ghost_spawn_egg", GhostsEntities.SMALL_GHOST, 0xA5FFFF, 0x00FF00, properties("small_ghost_spawn_egg"));
    public static final Supplier<SpawnEggItem> KODAMA_SPAWN_EGG = registerSpawnEgg("kodama_spawn_egg", GhostsEntities.KODAMA, 0xe8ead7, 0x92a8a5, properties("kodama_spawn_egg"));

    public static final Supplier<Item> HAUNTED_BOAT = registerItem("haunted_boat", () -> new HauntedBoatItem(properties("haunted_boat"), false, HauntedBoat.Type.HAUNTED));
    public static final Supplier<Item> HAUNTED_CHEST_BOAT = registerItem("haunted_chest_boat", () -> new HauntedBoatItem(properties("haunted_chest_boat"), true, HauntedBoat.Type.HAUNTED));

    public static final Supplier<Item> HAUNTED_SIGN = registerItem("haunted_sign", () -> new SignItem(GhostsBlocks.HAUNTED_SIGN.get(), GhostsBlocks.HAUNTED_WALL_SIGN.get(), properties("haunted_sign").stacksTo(16).useBlockDescriptionPrefix()));
    public static final Supplier<Item> HAUNTED_HANGING_SIGN = registerItem("haunted_hanging_sign", () -> new HangingSignItem(GhostsBlocks.HAUNTED_HANGING_SIGN.get(), GhostsBlocks.HAUNTED_WALL_HANGING_SIGN.get(), properties("haunted_hanging_sign").stacksTo(16).useBlockDescriptionPrefix()));

    private static <T extends Item, X extends LivingEntity> Supplier<T> registerSpawnEgg(String id, Supplier<? extends EntityType<? extends Mob>> entity, int color1, int color2, Item.Properties properties) {
        return Ghosts.PLATFORM.registerSpawnEgg(id, entity, color1, color2, properties);
    }

    private static <T extends Item> Supplier<T> registerItem(String id, Supplier<T> item) {
        return Ghosts.PLATFORM.registerItem(id, item);
    }

    private static Item.Properties properties(String id) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Ghosts.of(id)));
    }

}
