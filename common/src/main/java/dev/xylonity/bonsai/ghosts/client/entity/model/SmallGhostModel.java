package dev.xylonity.bonsai.ghosts.client.entity.model;

import dev.xylonity.bonsai.ghosts.Ghosts;
import dev.xylonity.bonsai.ghosts.common.entity.ghost.SmallGhostEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class SmallGhostModel extends GeoModel<SmallGhostEntity> {

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Ghosts.of("entity/small_ghost");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Ghosts.of("textures/entity/small_ghost.png");
    }

    @Override
    public Identifier getAnimationResource(SmallGhostEntity animatable) {
        return Ghosts.of("entity/small_ghost");
    }

}
