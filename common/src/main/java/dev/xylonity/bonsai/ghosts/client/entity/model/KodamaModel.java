package dev.xylonity.bonsai.ghosts.client.entity.model;

import dev.xylonity.bonsai.ghosts.Ghosts;
import dev.xylonity.bonsai.ghosts.common.entity.kodama.KodamaEntity;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class KodamaModel extends GeoModel<KodamaEntity> {

    private static final DataTicket<Integer> VARIANT = DataTickets.create("ghosts_kodama_variant", Integer.class);

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Ghosts.of("entity/kodama_" + variant(renderState));
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Ghosts.of("textures/entity/kodama_" + variant(renderState) + ".png");
    }

    @Override
    public Identifier getAnimationResource(KodamaEntity animatable) {
        return Ghosts.of("entity/kodama");
    }

    @Override
    public void addAdditionalStateData(KodamaEntity animatable, Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        renderState.addGeckolibData(VARIANT, animatable.getVariant());
    }

    private static int variant(GeoRenderState renderState) {
        return Mth.clamp(renderState.getOrDefaultGeckolibData(VARIANT, 0), 0, 4);
    }

}
