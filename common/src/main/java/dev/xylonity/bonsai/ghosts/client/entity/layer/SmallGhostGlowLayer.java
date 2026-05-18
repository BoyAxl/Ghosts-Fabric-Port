package dev.xylonity.bonsai.ghosts.client.entity.layer;

import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import dev.xylonity.bonsai.ghosts.Ghosts;
import dev.xylonity.bonsai.ghosts.common.entity.ghost.SmallGhostEntity;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;

public class SmallGhostGlowLayer extends AutoGlowingGeoLayer<SmallGhostEntity, Void, EntityRenderState> {

    public SmallGhostGlowLayer(GeoRenderer<SmallGhostEntity, Void, EntityRenderState> renderer) {
        super(renderer);
    }

    @Override
    protected Identifier getTextureResource(EntityRenderState renderState) {
        return Ghosts.of("textures/entity/small_ghost_glowmask.png");
    }

}
