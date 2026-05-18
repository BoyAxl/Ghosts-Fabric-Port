package dev.xylonity.bonsai.ghosts.platform;

import com.mojang.serialization.MapCodec;
import dev.xylonity.bonsai.ghosts.Ghosts;
import dev.xylonity.bonsai.ghosts.registry.GhostsBlockEntities;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.type.WoodTypeBuilder;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

import org.jetbrains.annotations.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class GhostsPlatformFabric implements GhostsPlatform {

    @Override
    public <X extends Item> Supplier<X> registerItem(String id, Supplier<X> item) {
        return registerSupplier(BuiltInRegistries.ITEM, id, item);
    }

    @Override
    public <X extends Block> Supplier<X> registerBlock(String id, Supplier<X> block, boolean registerItem) {
        Supplier<X> blockSupplier = registerSupplier(BuiltInRegistries.BLOCK, id, block);

        if (registerItem) {
            registerItem(id, () -> new BlockItem(blockSupplier.get(), itemProperties(id).useBlockDescriptionPrefix()));
        }

        return blockSupplier;
    }

    @Override
    public <X extends BlockEntity> Supplier<BlockEntityType<X>> registerBlockEntity(String id, GhostsBlockEntities.BlockEntityFactory<X> supplier, Supplier<Block>... blocks) {
        return registerSupplier(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, () -> {
            Block[] blockArray = Arrays.stream(blocks)
                    .map(Supplier::get)
                    .toArray(Block[]::new);

            return FabricBlockEntityTypeBuilder.create(supplier::create, blockArray).build();
        });

    }

    @Override
    public <X extends CreativeModeTab> Supplier<X> registerCreativeTab(String id, Supplier<X> creativeModeTab) {
        return registerSupplier(BuiltInRegistries.CREATIVE_MODE_TAB, id, creativeModeTab);
    }

    @Override
    public <T extends ParticleType<?>> Supplier<T> registerParticle(String id, boolean overrideLimiter) {
        return registerSupplier(BuiltInRegistries.PARTICLE_TYPE, id, () -> (T) FabricParticleTypes.simple());
    }

    @Override
    public <T extends Item, X extends LivingEntity> Supplier<T> registerSpawnEgg(String id, Supplier<? extends EntityType<? extends Mob>> entity, int color1, int color2, Item.Properties properties) {
        return (Supplier<T>) registerItem(id, () -> new SpawnEggItem(properties.spawnEgg(entity.get())));
    }

    @Override
    public <X extends Entity> Supplier<EntityType<X>> registerEntity(String name, EntityType.EntityFactory<X> entity, MobCategory category, float width, float height, @Nullable List<Consumer<EntityType.Builder<X>>> properties) {
        return registerSupplier(BuiltInRegistries.ENTITY_TYPE, name, () -> {
            EntityType.Builder<X> builder = EntityType.Builder.of(entity, category).sized(width, height);

            if (properties != null) {
                for (Consumer<EntityType.Builder<X>> property : properties) {
                    property.accept(builder);
                }
            }

            return builder.build(ResourceKey.create(Registries.ENTITY_TYPE, Ghosts.of(name)));
        });
    }

    @Override
    public <X extends SoundEvent> Supplier<X> registerSound(String id, Supplier<X> sound) {
        return registerSupplier(BuiltInRegistries.SOUND_EVENT, id, sound);
    }

    @Override
    public <U extends TrunkPlacer> Supplier<TrunkPlacerType<U>> registerTrunkPlacer(String id, MapCodec<U> codec) {
        return registerSupplier(BuiltInRegistries.TRUNK_PLACER_TYPE, id, () -> new TrunkPlacerType<>(codec));
    }

    @Override
    public <U extends FoliagePlacer> Supplier<FoliagePlacerType<U>> registerFoliagePlacer(String id, MapCodec<U> codec) {
        return registerSupplier(BuiltInRegistries.FOLIAGE_PLACER_TYPE, id, () -> new FoliagePlacerType<>(codec));
    }

    @Override
    public WoodType registerWoodType(Identifier id, BlockSetType setType) {
        return WoodTypeBuilder.copyOf(WoodType.ACACIA).register(id, setType);
    }

    @Override
    public CreativeModeTab.Builder creativeTabBuilder() {
        return FabricCreativeModeTab.builder();
    }

    private static <T, R extends Registry<? super T>> Supplier<T> registerSupplier(R registry, String id, Supplier<T> factory) {
        T value = factory.get();
        Registry.register((Registry<T>) registry, Ghosts.of(id), value);
        return () -> value;
    }

    private static Item.Properties itemProperties(String id) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Ghosts.of(id)));
    }

}
