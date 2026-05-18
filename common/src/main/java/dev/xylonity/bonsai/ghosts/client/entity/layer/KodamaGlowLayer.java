package dev.xylonity.bonsai.ghosts.client.entity.layer;

import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import dev.xylonity.bonsai.ghosts.Ghosts;
import dev.xylonity.bonsai.ghosts.common.entity.kodama.KodamaEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class KodamaGlowLayer extends GeoRenderLayer<KodamaEntity, Void, EntityRenderState> {

    private static final int FULL_BRIGHT = 15728880;
    private static final DataTicket<Float> GLOW_ALPHA = DataTickets.create("ghosts_kodama_glow_alpha", Float.class);
    private static final DataTicket<Identifier> GLOW_TEXTURE = DataTickets.create("ghosts_kodama_glow_texture", Identifier.class);

    public KodamaGlowLayer(GeoRenderer<KodamaEntity, Void, EntityRenderState> renderer) {
        super(renderer);
    }

    @Override
    public void addRenderData(KodamaEntity animatable, Void relatedObject, EntityRenderState renderState, float partialTick) {
        float pulse = 0.525f + 0.125f * (float) Math.sin(((animatable.tickCount + partialTick) / 80.0f) * (Math.PI * 2.0D));

        if (animatable.getRattlingTicks() == 14) {
            animatable.setFlashAlpha(1.0f);
        }

        if (animatable.getFlashAlpha() > 0.0f) {
            animatable.setFlashAlpha(animatable.getFlashAlpha() - 0.00125f);
        }

        renderState.addGeckolibData(GLOW_ALPHA, Math.max(pulse, animatable.getFlashAlpha()));
        renderState.addGeckolibData(GLOW_TEXTURE, Ghosts.of("textures/entity/kodama_" + animatable.getVariant() + ".png"));
    }

    @Override
    public void submitRenderTask(RenderPassInfo<EntityRenderState> renderInfo, SubmitNodeCollector submitNodeCollector) {
        if (!renderInfo.willRender()) {
            return;
        }

        Identifier texture = renderInfo.getGeckolibData(GLOW_TEXTURE);
        if (texture == null) {
            return;
        }

        int previousColor = renderInfo.renderColor();
        int previousLight = renderInfo.packedLight();
        float alpha = renderInfo.getOrDefaultGeckolibData(GLOW_ALPHA, 0.0f);
        int glowColor = ((Math.round(alpha * 255.0f) & 255) << 24) | 0xFFFFFF;

        renderInfo.renderState().addGeckolibData(DataTickets.RENDER_COLOR, glowColor);
        renderInfo.renderState().addGeckolibData(DataTickets.PACKED_LIGHT, FULL_BRIGHT);

        getRenderer().submitRenderTasks(renderInfo, submitNodeCollector.order(1), RenderTypes.entityTranslucentEmissive(texture));

        renderInfo.renderState().addGeckolibData(DataTickets.RENDER_COLOR, previousColor);
        renderInfo.renderState().addGeckolibData(DataTickets.PACKED_LIGHT, previousLight);
    }

}
