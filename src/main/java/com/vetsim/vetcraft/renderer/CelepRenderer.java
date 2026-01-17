package com.vetsim.vetcraft.renderer;

import com.vetsim.vetcraft.VetCraft;
import com.vetsim.vetcraft.entity.CelepEntity;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class CelepRenderer extends MobRenderer<CelepEntity, net.minecraft.client.model.HumanoidModel<CelepEntity>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft",
            "textures/entity/player/wide/steve.png");

    public CelepRenderer(EntityRendererProvider.Context context) {
        super(context, new net.minecraft.client.model.HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(CelepEntity entity) {
        return TEXTURE;
    }
}