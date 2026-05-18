package dev.xylonity.bonsai.ghosts.client.entity.render;

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
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.List;

public class GhostRenderer extends BaseGhostRenderer<GhostEntity> {

    private static final DataTicket<ItemStack> HEAD_ITEM = DataTickets.create("ghosts_head_item", ItemStack.class);

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
            boneSnapshots.ifPresent("mushroom_red", bone -> bone.skipRender(true));
        }

        if (!GhostEntity.isBrownHeadMushroom(headItem)) {
            boneSnapshots.ifPresent("mushroom_brown", bone -> bone.skipRender(true));
        }
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
