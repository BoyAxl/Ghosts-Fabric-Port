package dev.xylonity.bonsai.ghosts.client.entity.render;

import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.BlockAndItemGeoLayer;
import com.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.xylonity.bonsai.ghosts.client.entity.layer.SmallGhostGlowLayer;
import dev.xylonity.bonsai.ghosts.client.entity.model.SmallGhostModel;
import dev.xylonity.bonsai.ghosts.client.entity.render.core.BaseGhostRenderer;
import dev.xylonity.bonsai.ghosts.common.entity.ghost.SmallGhostEntity;
import dev.xylonity.bonsai.ghosts.common.entity.variant.SmallGhostVariant;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SmallGhostRenderer extends BaseGhostRenderer<SmallGhostEntity> {

    private static final DataTicket<SmallGhostVariant> VARIANT = DataTickets.create("ghosts_small_ghost_variant", SmallGhostVariant.class);
    private static final String MAIN_BONE = "main";
    private static final String BODY_BONE = "body";
    private static final String PLANT_BONE = "plant";
    private static final int FULL_ALPHA = 0xFF000000;

    public SmallGhostRenderer(EntityRendererProvider.Context context) {
        super(context, new SmallGhostModel());
        this.withRenderLayer(new SmallGhostGlowLayer(this));
        this.withRenderLayer(new SmallGhostHeldItemLayer(context, this));
    }

    @Override
    public void addRenderData(SmallGhostEntity animatable, Void relatedObject, EntityRenderState renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(VARIANT, animatable.getVariant());
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<EntityRenderState> renderInfo, BoneSnapshots boneSnapshots) {
        super.adjustModelBonesForRender(renderInfo, boneSnapshots);

        if (renderInfo.getOrDefaultGeckolibData(VARIANT, SmallGhostVariant.NORMAL) != SmallGhostVariant.PLANT) {
            boneSnapshots.ifPresent("plant", bone -> bone.skipRender(true));
            boneSnapshots.ifPresent("plant_2", bone -> bone.skipRender(true));
            boneSnapshots.ifPresent("left_leaf", bone -> bone.skipRender(true));
            boneSnapshots.ifPresent("right_leaf", bone -> bone.skipRender(true));
        }
    }

    @Override
    public void submitRenderTasks(RenderPassInfo<EntityRenderState> renderInfo, OrderedSubmitNodeCollector submitNodeCollector, RenderType renderType) {
        if (renderType == null || renderInfo.getOrDefaultGeckolibData(VARIANT, SmallGhostVariant.NORMAL) != SmallGhostVariant.PLANT) {
            super.submitRenderTasks(renderInfo, submitNodeCollector, renderType);
            return;
        }

        BakedGeoModel model = renderInfo.model();
        GeoBone mainBone = model.getBone(MAIN_BONE).orElse(null);
        GeoBone bodyBone = model.getBone(BODY_BONE).orElse(null);
        GeoBone plantBone = model.getBone(PLANT_BONE).orElse(null);

        if (model.isMissingno() || mainBone == null || bodyBone == null || plantBone == null) {
            super.submitRenderTasks(renderInfo, submitNodeCollector, renderType);
            return;
        }

        int packedLight = renderInfo.packedLight();
        int packedOverlay = renderInfo.packedOverlay();
        int renderColor = renderInfo.renderColor();
        int plantColor = FULL_ALPHA | (renderColor & 0xFFFFFF);

        submitNodeCollector.submitCustomGeometry(renderInfo.poseStack(), renderType, (pose, vertexConsumer) -> {
            PoseStack poseStack = renderInfo.poseStack();
            poseStack.pushPose();
            poseStack.last().set(pose);

            renderInfo.renderPosed(() -> {
                BoneRenderVisibility plantVisibility = BoneRenderVisibility.setBranchHidden(plantBone, true);

                try {
                    model.render(renderInfo, vertexConsumer, packedLight, packedOverlay, renderColor);
                } finally {
                    plantVisibility.restore();
                }
            });

            poseStack.popPose();
        });

        submitNodeCollector.submitCustomGeometry(renderInfo.poseStack(), renderType, (pose, vertexConsumer) -> {
            PoseStack poseStack = renderInfo.poseStack();
            poseStack.pushPose();
            poseStack.last().set(pose);

            renderInfo.renderPosed(() -> {
                BoneRenderVisibility bodyVisibility = BoneRenderVisibility.setBranchHidden(bodyBone, true);
                BoneRenderVisibility plantVisibility = BoneRenderVisibility.setBranchHidden(plantBone, false);

                try {
                    mainBone.positionAndRender(renderInfo, vertexConsumer, packedLight, packedOverlay, plantColor);
                } finally {
                    plantVisibility.restore();
                    bodyVisibility.restore();
                }
            });

            poseStack.popPose();
        });
    }

    private static class SmallGhostHeldItemLayer extends BlockAndItemGeoLayer<SmallGhostEntity, Void, EntityRenderState> {

        public SmallGhostHeldItemLayer(EntityRendererProvider.Context context, SmallGhostRenderer renderer) {
            super(context, renderer);
        }

        @Override
        protected List<RenderData> getRelevantBones(SmallGhostEntity animatable, Void relatedObject, EntityRenderState renderState, float partialTick) {
            ItemStack heldStack = animatable.getItemBySlot(EquipmentSlot.MAINHAND);
            if (heldStack.isEmpty()) {
                return List.of();
            }

            ItemDisplayContext displayContext = ItemDisplayContext.GROUND;
            ItemStackRenderState itemRenderState = RenderUtil.createRenderStateForItem(heldStack, this.itemModelResolver, displayContext, animatable);

            return List.of(RenderData.item("item", displayContext, itemRenderState));
        }

        @Override
        public void addRenderData(SmallGhostEntity animatable, Void relatedObject, EntityRenderState renderState, float partialTick) {
            List<RenderData> relevantBones = getRelevantBones(animatable, relatedObject, renderState, partialTick);
            if (!relevantBones.isEmpty()) {
                renderState.addGeckolibData(CONTENTS, relevantBones);
            }
        }

        @Override
        protected void submitItemStackRender(PoseStack poseStack, GeoBone bone, ItemStackRenderState itemRenderState, ItemDisplayContext displayContext, EntityRenderState renderState, SubmitNodeCollector submitNodeCollector, int packedLight) {
            poseStack.pushPose();
            poseStack.scale(0.6F, 0.6F, 0.6F);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

            super.submitItemStackRender(poseStack, bone, itemRenderState, displayContext, renderState, submitNodeCollector, packedLight);
            poseStack.popPose();
        }

    }

}
