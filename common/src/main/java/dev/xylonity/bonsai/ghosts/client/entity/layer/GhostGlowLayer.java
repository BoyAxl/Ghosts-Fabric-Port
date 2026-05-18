package dev.xylonity.bonsai.ghosts.client.entity.layer;

import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import dev.xylonity.bonsai.ghosts.Ghosts;
import dev.xylonity.bonsai.ghosts.common.entity.ghost.GhostEntity;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;

public class GhostGlowLayer extends AutoGlowingGeoLayer<GhostEntity, Void, EntityRenderState> {

    public GhostGlowLayer(GeoRenderer<GhostEntity, Void, EntityRenderState> renderer) {
        super(renderer);
    }

    @Override
    protected Identifier getTextureResource(EntityRenderState renderState) {
        return Ghosts.of("textures/entity/ghost_glowmask.png");
    }

}
