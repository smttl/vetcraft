package com.vetsim.vetcraft.renderer;

import com.vetsim.vetcraft.entity.CattleEntity;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class CattleRenderer extends MobRenderer<CattleEntity, CowModel<CattleEntity>> {

    // YENİ YOLLAR: Artık "textures/entity/cattle/" klasörüne bakıyorlar.
    private static final ResourceLocation HOLSTEIN = new ResourceLocation("vetsim", "textures/entity/cattle/holstein.png");
    private static final ResourceLocation ANGUS = new ResourceLocation("vetsim", "textures/entity/cattle/angus.png");
    private static final ResourceLocation SIMMENTAL = new ResourceLocation("vetsim", "textures/entity/cattle/simmental.png");
    private static final ResourceLocation JERSEY = new ResourceLocation("vetsim", "textures/entity/cattle/jersey.png");

    // Bilinmeyen veya Melez ırklar için yedek resim
    private static final ResourceLocation DEFAULT = new ResourceLocation("vetsim", "textures/entity/cattle/default.png");

    public CattleRenderer(EntityRendererProvider.Context context) {
        super(context, new CowModel<>(context.bakeLayer(ModelLayers.COW)), 0.7F);
    }

    @Override
    public ResourceLocation getTextureLocation(CattleEntity entity) {
        String breed = entity.getBreed();

        // Switch-Case yapısı daha temiz ve hızlıdır
        switch (breed) {
            case "Holstein":
                return HOLSTEIN;
            case "Angus":
                return ANGUS;
            case "Simmental":
                return SIMMENTAL;
            case "Jersey":
                return JERSEY;
            default:
                return DEFAULT; // Melez veya tanımlanmamış ırklar
        }
    }
}