package com.vetsim.vetcraft.renderer;

import com.vetsim.vetcraft.VetCraft;
import com.vetsim.vetcraft.entity.CelepEntity;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class CelepRenderer extends MobRenderer<CelepEntity, VillagerModel<CelepEntity>> {

    // Köylü modeli kullanıyoruz ama dokusu farklı olacak
    private static final ResourceLocation TEXTURE = new ResourceLocation(VetCraft.MOD_ID, "textures/entity/celep.png");

    public CelepRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(CelepEntity entity) {
        return TEXTURE;
    }
}