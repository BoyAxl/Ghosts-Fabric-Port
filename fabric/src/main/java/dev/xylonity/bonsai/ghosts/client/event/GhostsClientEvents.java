package dev.xylonity.bonsai.ghosts.client.event;

import dev.xylonity.bonsai.ghosts.Ghosts;
import dev.xylonity.bonsai.ghosts.client.entity.render.GhostRenderer;
import dev.xylonity.bonsai.ghosts.client.entity.render.KodamaRenderer;
import dev.xylonity.bonsai.ghosts.client.entity.render.SmallGhostRenderer;
import dev.xylonity.bonsai.ghosts.client.particle.FlyingGhostParticle;
import dev.xylonity.bonsai.ghosts.registry.GhostsBlockEntities;
import dev.xylonity.bonsai.ghosts.registry.GhostsEntities;
import dev.xylonity.bonsai.ghosts.registry.GhostsParticles;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.blockentity.StandingSignRenderer;
import net.minecraft.client.renderer.entity.BoatRenderer;

public class GhostsClientEvents {

    private static final ModelLayerLocation HAUNTED_BOAT_LAYER = new ModelLayerLocation(Ghosts.of("boat/haunted"), "main");
    private static final ModelLayerLocation HAUNTED_CHEST_BOAT_LAYER = new ModelLayerLocation(Ghosts.of("chest_boat/haunted"), "main");

    public static void init() {
        EntityRendererRegistry.register(GhostsEntities.GHOST.get(), GhostRenderer::new);
        EntityRendererRegistry.register(GhostsEntities.SMALL_GHOST.get(), SmallGhostRenderer::new);
        EntityRendererRegistry.register(GhostsEntities.KODAMA.get(), KodamaRenderer::new);
        EntityRendererRegistry.register(GhostsEntities.HAUNTED_BOAT.get(), context -> new BoatRenderer(context, HAUNTED_BOAT_LAYER));
        EntityRendererRegistry.register(GhostsEntities.HAUNTED_CHEST_BOAT.get(), context -> new BoatRenderer(context, HAUNTED_CHEST_BOAT_LAYER));

        BlockEntityRendererRegistry.register(
                GhostsBlockEntities.HAUNTED_SIGN.get(),
                context -> (BlockEntityRenderer) new StandingSignRenderer(context)
        );
        BlockEntityRendererRegistry.register(
                GhostsBlockEntities.HAUNTED_HANGING_SIGN.get(),
                context -> (BlockEntityRenderer) new HangingSignRenderer(context)
        );

        ModelLayerRegistry.registerModelLayer(HAUNTED_BOAT_LAYER, BoatModel::createBoatModel);
        ModelLayerRegistry.registerModelLayer(HAUNTED_CHEST_BOAT_LAYER, BoatModel::createChestBoatModel);

        ParticleProviderRegistry.getInstance().register(GhostsParticles.FLYING_GHOST.get(), FlyingGhostParticle.Provider::new);
    }

}
