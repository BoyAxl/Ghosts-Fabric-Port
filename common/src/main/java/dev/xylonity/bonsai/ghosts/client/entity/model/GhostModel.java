package dev.xylonity.bonsai.ghosts.client.entity.model;

import dev.xylonity.bonsai.ghosts.Ghosts;
import dev.xylonity.bonsai.ghosts.common.entity.ghost.GhostEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class GhostModel extends GeoModel<GhostEntity> {

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Ghosts.of("entity/ghost");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Ghosts.of("textures/entity/ghost.png");
    }

    @Override
    public Identifier getAnimationResource(GhostEntity animatable) {
        return Ghosts.of("entity/ghost");
    }

}
