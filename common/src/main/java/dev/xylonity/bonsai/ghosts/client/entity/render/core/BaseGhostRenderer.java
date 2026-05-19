package dev.xylonity.bonsai.ghosts.client.entity.render.core;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

public class BaseGhostRenderer<T extends LivingEntity & GeoEntity> extends GeoEntityRenderer<T, EntityRenderState> {

    protected BaseGhostRenderer(EntityRendererProvider.Context context, GeoModel<T> modelProvider) {
        super(context, modelProvider);
    }

    @Override
    public RenderType getRenderType(EntityRenderState renderState, Identifier texture) {
        return RenderTypes.entityTranslucent(texture);
    }

    @Override
    public int getRenderColor(T animatable, Void relatedObject, float partialTick) {
        int alpha = Math.round(0.20f * 255.0f);
        return (alpha << 24) | 0xFFFFFF;
    }

}
