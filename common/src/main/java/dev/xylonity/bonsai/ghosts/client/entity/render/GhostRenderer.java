package dev.xylonity.bonsai.ghosts.client.entity.render;

import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.ItemArmorGeoLayer;
import com.geckolib.renderer.layer.builtin.ItemInHandGeoLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.xylonity.bonsai.ghosts.client.entity.layer.GhostGlowLayer;
import dev.xylonity.bonsai.ghosts.client.entity.model.GhostModel;
import dev.xylonity.bonsai.ghosts.client.entity.render.core.BaseGhostRenderer;
import dev.xylonity.bonsai.ghosts.common.entity.ghost.GhostEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.List;

public class GhostRenderer extends BaseGhostRenderer<GhostEntity> {

    private static final DataTicket<ItemStack> HEAD_ITEM = DataTickets.create("ghosts_head_item", ItemStack.class);
    private static final String MAIN_BONE = "main";
    private static final String BODY_BONE = "glow_1";
    private static final String EYES_BONE = "eyes";
    private static final String RED_MUSHROOM_BONE = "mushroom_red";
    private static final String BROWN_MUSHROOM_BONE = "mushroom_brown";
    private static final String ITEM_ANIMATION_BONE = "item_animation";
    private static final int FULL_ALPHA = 0xFF000000;
    private static final int OPAQUE_WHITE = 0xFFFFFFFF;

    public GhostRenderer(EntityRendererProvider.Context context) {
        super(context, new GhostModel());
        this.withRenderLayer(new GhostGlowLayer(this));
        this.withRenderLayer(new GhostHeadArmorLayer(this, context));
        this.withRenderLayer(new ItemInHandGeoLayer<>(context, this, "item", "item"));
    }

    @Override
    public void addRenderData(GhostEntity animatable, Void relatedObject, EntityRenderState renderState, float partialTick) {
        super.addRenderData(animatable, relatedObject, renderState, partialTick);
        renderState.addGeckolibData(HEAD_ITEM, animatable.getItemBySlot(EquipmentSlot.HEAD));
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<EntityRenderState> renderInfo, BoneSnapshots boneSnapshots) {
        super.adjustModelBonesForRender(renderInfo, boneSnapshots);
        ItemStack headItem = renderInfo.getOrDefaultGeckolibData(HEAD_ITEM, ItemStack.EMPTY);

        if (!GhostEntity.isRedHeadMushroom(headItem)) {
            boneSnapshots.ifPresent(RED_MUSHROOM_BONE, bone -> bone.skipRender(true));
        }

        if (!GhostEntity.isBrownHeadMushroom(headItem)) {
            boneSnapshots.ifPresent(BROWN_MUSHROOM_BONE, bone -> bone.skipRender(true));
        }
    }

    @Override
    public void submitRenderTasks(RenderPassInfo<EntityRenderState> renderInfo, OrderedSubmitNodeCollector submitNodeCollector, RenderType renderType) {
        ItemStack headItem = renderInfo.getOrDefaultGeckolibData(HEAD_ITEM, ItemStack.EMPTY);
        boolean renderRedMushroom = GhostEntity.isRedHeadMushroom(headItem);
        boolean renderBrownMushroom = GhostEntity.isBrownHeadMushroom(headItem);

        if (renderType == null || (!renderRedMushroom && !renderBrownMushroom)) {
            super.submitRenderTasks(renderInfo, submitNodeCollector, renderType);
            return;
        }

        BakedGeoModel model = renderInfo.model();
        GeoBone mainBone = model.getBone(MAIN_BONE).orElse(null);
        GeoBone bodyBone = model.getBone(BODY_BONE).orElse(null);
        GeoBone eyesBone = model.getBone(EYES_BONE).orElse(null);
        GeoBone itemAnimationBone = model.getBone(ITEM_ANIMATION_BONE).orElse(null);
        GeoBone selectedMushroomBone = model.getBone(renderRedMushroom ? RED_MUSHROOM_BONE : BROWN_MUSHROOM_BONE).orElse(null);
        GeoBone hiddenMushroomBone = model.getBone(renderRedMushroom ? BROWN_MUSHROOM_BONE : RED_MUSHROOM_BONE).orElse(null);

        if (model.isMissingno() || mainBone == null || bodyBone == null || eyesBone == null || itemAnimationBone == null || selectedMushroomBone == null || hiddenMushroomBone == null) {
            super.submitRenderTasks(renderInfo, submitNodeCollector, renderType);
            return;
        }

        int packedLight = renderInfo.packedLight();
        int packedOverlay = renderInfo.packedOverlay();
        int renderColor = renderInfo.renderColor();
        int accessoryColor = FULL_ALPHA | (renderColor & 0xFFFFFF);

        submitNodeCollector.submitCustomGeometry(renderInfo.poseStack(), renderType, (pose, vertexConsumer) -> {
            PoseStack poseStack = renderInfo.poseStack();
            poseStack.pushPose();
            poseStack.last().set(pose);

            renderInfo.renderPosed(() -> {
                BoneRenderVisibility mushroomVisibility = BoneRenderVisibility.setBranchHidden(selectedMushroomBone, true);

                try {
                    model.render(renderInfo, vertexConsumer, packedLight, packedOverlay, renderColor);
                } finally {
                    mushroomVisibility.restore();
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
                BoneRenderVisibility eyesVisibility = BoneRenderVisibility.setBranchHidden(eyesBone, true);
                BoneRenderVisibility itemVisibility = BoneRenderVisibility.setBranchHidden(itemAnimationBone, true);
                BoneRenderVisibility hiddenMushroomVisibility = BoneRenderVisibility.setBranchHidden(hiddenMushroomBone, true);
                BoneRenderVisibility selectedMushroomVisibility = BoneRenderVisibility.setBranchHidden(selectedMushroomBone, false);

                try {
                    mainBone.positionAndRender(renderInfo, vertexConsumer, packedLight, packedOverlay, accessoryColor);
                } finally {
                    selectedMushroomVisibility.restore();
                    hiddenMushroomVisibility.restore();
                    itemVisibility.restore();
                    eyesVisibility.restore();
                    bodyVisibility.restore();
                }
            });

            poseStack.popPose();
        });
    }

    private static class GhostHeadArmorLayer extends ItemArmorGeoLayer<GhostEntity, Void, EntityRenderState> {

        private GhostHeadArmorLayer(GhostRenderer renderer, EntityRendererProvider.Context context) {
            super(renderer, context);
        }

        @Override
        protected List<RenderData> getRelevantBones(RenderPassInfo<EntityRenderState> renderInfo) {
            EnumMap<EquipmentSlot, ItemStack> equipment = renderInfo.getOrDefaultGeckolibData(DataTickets.EQUIPMENT_BY_SLOT, new EnumMap<>(EquipmentSlot.class));
            ItemStack headItem = equipment.getOrDefault(EquipmentSlot.HEAD, ItemStack.EMPTY);

            if (headItem.isEmpty() || GhostEntity.isHeadMushroomItem(headItem)) {
                return List.of();
            }

            return List.of(RenderData.head("glow_1"));
        }

        @Override
        public void submitRenderTask(RenderPassInfo<EntityRenderState> renderInfo, SubmitNodeCollector submitNodeCollector) {
            int previousColor = renderInfo.renderColor();

            renderInfo.renderState().addGeckolibData(DataTickets.RENDER_COLOR, OPAQUE_WHITE);
            super.submitRenderTask(renderInfo, submitNodeCollector);
            renderInfo.renderState().addGeckolibData(DataTickets.RENDER_COLOR, previousColor);
        }

        @Override
        protected <S extends HumanoidRenderState & GeoRenderState, A extends HumanoidModel<S>> void collectArmorData(EntityRenderState renderState, GhostEntity animatable, float partialTick, EnumMap<EquipmentSlot, ItemStack> equipmentBySlot) {
            super.collectArmorData(renderState, animatable, partialTick, equipmentBySlot);

            EnumMap<EquipmentSlot, ? extends HumanoidRenderState> perSlotRenderData = renderState.getOrDefaultGeckolibData(DataTickets.PER_SLOT_RENDER_DATA, new EnumMap<>(EquipmentSlot.class));
            HumanoidRenderState headRenderState = perSlotRenderData.get(EquipmentSlot.HEAD);
            if (headRenderState == null) {
                return;
            }

            clearVanillaHeadMotion(headRenderState);
        }

        @Override
        protected <S extends HumanoidRenderState & GeoRenderState> S getOrCreateHumanoidRenderState(EntityRenderState renderState, boolean forceNew) {
            S state = super.getOrCreateHumanoidRenderState(renderState, forceNew);
            clearVanillaHeadMotion(state);

            return state;
        }

        @Override
        protected void renderVanillaEquippable(RenderPassInfo<EntityRenderState> renderInfo, SubmitNodeCollector submitNodeCollector, RenderData renderData, GeoBone bone, ItemStack stack, PoseStack poseStack, Model<?> model, ModelPart modelPart, ResourceKey<EquipmentAsset> assetId) {
            if ("glow_1".equals(bone.name())) {
                poseStack.translate(0.0D, -0.1D, 0.0D);
            }

            super.renderVanillaEquippable(renderInfo, submitNodeCollector, renderData, bone, stack, poseStack, model, modelPart, assetId);
        }

        private static void clearVanillaHeadMotion(HumanoidRenderState state) {
            state.xRot = 0.0F;
            state.yRot = 0.0F;
            state.bodyRot = 0.0F;
            state.walkAnimationPos = 0.0F;
            state.walkAnimationSpeed = 0.0F;
            state.attackTime = 0.0F;
            state.speedValue = 0.0F;
            state.isPassenger = false;
            state.isCrouching = false;
            state.isFallFlying = false;
            state.isVisuallySwimming = false;
        }

    }

}
