package dev.xylonity.bonsai.ghosts.client.entity.render;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.renderer.layer.builtin.BlockAndItemGeoLayer;
import com.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.xylonity.bonsai.ghosts.client.entity.layer.KodamaGlowLayer;
import dev.xylonity.bonsai.ghosts.client.entity.model.KodamaModel;
import dev.xylonity.bonsai.ghosts.common.entity.kodama.KodamaEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class KodamaRenderer extends GeoEntityRenderer<KodamaEntity, EntityRenderState> {

    public KodamaRenderer(EntityRendererProvider.Context context) {
        super(context, new KodamaModel());
        this.withRenderLayer(new KodamaGlowLayer(this));
        this.withRenderLayer(new KodamaHeldItemLayer(context, this));
    }

    private static class KodamaHeldItemLayer extends BlockAndItemGeoLayer<KodamaEntity, Void, EntityRenderState> {

        public KodamaHeldItemLayer(EntityRendererProvider.Context context, KodamaRenderer renderer) {
            super(context, renderer);
        }

        @Override
        protected List<RenderData> getRelevantBones(KodamaEntity animatable, Void relatedObject, EntityRenderState renderState, float partialTick) {
            ItemStack heldStack = animatable.getItemBySlot(EquipmentSlot.MAINHAND);
            if (heldStack.isEmpty()) {
                return List.of();
            }

            ItemDisplayContext displayContext = ItemDisplayContext.GROUND;
            ItemStackRenderState itemRenderState = RenderUtil.createRenderStateForItem(heldStack, this.itemModelResolver, displayContext, animatable);

            return List.of(RenderData.item("item", displayContext, itemRenderState));
        }

        @Override
        public void addRenderData(KodamaEntity animatable, Void relatedObject, EntityRenderState renderState, float partialTick) {
            List<RenderData> relevantBones = getRelevantBones(animatable, relatedObject, renderState, partialTick);
            if (!relevantBones.isEmpty()) {
                renderState.addGeckolibData(CONTENTS, relevantBones);
            }
        }

        @Override
        protected void submitItemStackRender(PoseStack poseStack, GeoBone bone, ItemStackRenderState itemRenderState, ItemDisplayContext displayContext, EntityRenderState renderState, SubmitNodeCollector submitNodeCollector, int packedLight) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.mulPose(Axis.ZN.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.XN.rotationDegrees(180.0F));
            poseStack.translate(0.3D, 0.0D, -0.13D);
            poseStack.scale(0.92F, 0.92F, 0.92F);

            super.submitItemStackRender(poseStack, bone, itemRenderState, displayContext, renderState, submitNodeCollector, packedLight);
            poseStack.popPose();
        }

    }

}
