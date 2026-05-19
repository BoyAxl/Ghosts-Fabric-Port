package dev.xylonity.bonsai.ghosts.client.entity.layer;

import com.geckolib.constant.DataTickets;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import dev.xylonity.bonsai.ghosts.Ghosts;
import dev.xylonity.bonsai.ghosts.common.entity.ghost.SmallGhostEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class SmallGhostGlowLayer extends GeoRenderLayer<SmallGhostEntity, Void, EntityRenderState> {

    private static final int FULL_BRIGHT = 15728880;
    private static final int GLOW_COLOR = 0xFFFFFFFF;
    private static final Identifier GLOW_TEXTURE = Ghosts.of("textures/entity/small_ghost_glowmask.png");

    public SmallGhostGlowLayer(GeoRenderer<SmallGhostEntity, Void, EntityRenderState> renderer) {
        super(renderer);
    }

    @Override
    public void submitRenderTask(RenderPassInfo<EntityRenderState> renderInfo, SubmitNodeCollector submitNodeCollector) {
        if (!renderInfo.willRender()) {
            return;
        }

        int previousColor = renderInfo.renderColor();
        int previousLight = renderInfo.packedLight();
        int previousOverlay = renderInfo.packedOverlay();

        renderInfo.renderState().addGeckolibData(DataTickets.RENDER_COLOR, GLOW_COLOR);
        renderInfo.renderState().addGeckolibData(DataTickets.PACKED_LIGHT, FULL_BRIGHT);
        renderInfo.renderState().addGeckolibData(DataTickets.PACKED_OVERLAY, OverlayTexture.NO_OVERLAY);

        getRenderer().submitRenderTasks(renderInfo, submitNodeCollector.order(1), RenderTypes.entityTranslucentEmissive(GLOW_TEXTURE));

        renderInfo.renderState().addGeckolibData(DataTickets.RENDER_COLOR, previousColor);
        renderInfo.renderState().addGeckolibData(DataTickets.PACKED_LIGHT, previousLight);
        renderInfo.renderState().addGeckolibData(DataTickets.PACKED_OVERLAY, previousOverlay);
    }

}
