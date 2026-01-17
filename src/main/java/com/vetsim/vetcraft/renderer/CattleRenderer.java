package com.vetsim.vetcraft.renderer;

import com.vetsim.vetcraft.entity.CattleEntity;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class CattleRenderer extends MobRenderer<CattleEntity, CowModel<CattleEntity>> {

    // YENİ YOLLAR: Artık "textures/entity/cattle/" klasörüne bakıyorlar.
    private static final ResourceLocation HOLSTEIN = new ResourceLocation("vetcraft",
            "textures/entity/cattle/holstein.png");
    private static final ResourceLocation ANGUS = new ResourceLocation("vetcraft", "textures/entity/cattle/angus.png");
    private static final ResourceLocation SIMMENTAL = new ResourceLocation("vetcraft",
            "textures/entity/cattle/simmental.png");
    private static final ResourceLocation JERSEY = new ResourceLocation("vetcraft",
            "textures/entity/cattle/jersey.png");

    // Bilinmeyen veya Melez ırklar için yedek resim
    private static final ResourceLocation DEFAULT = new ResourceLocation("vetcraft",
            "textures/entity/cattle/default.png");

    public CattleRenderer(EntityRendererProvider.Context context) {
        super(context, new CowModel<>(context.bakeLayer(ModelLayers.COW)), 0.7F);
    }

    @Override
    public ResourceLocation getTextureLocation(CattleEntity entity) {
        String breedName = entity.getBreed();
        com.vetsim.vetcraft.util.BreedData data = com.vetsim.vetcraft.util.BreedManager.getBreed(breedName);

        if (data != null && data.texturePath != null && !data.texturePath.isEmpty()) {
            try {
                // Eğer tam path verildiyse (örn: "minecraft:textures/...") onu ayrıştır
                if (data.texturePath.contains(":")) {
                    return new ResourceLocation(data.texturePath);
                }
                // Yoksa varsayılan mod ID (vetcraft) ile birleştir
                return new ResourceLocation("vetcraft", data.texturePath);
            } catch (Exception e) {
                // Hatalı path durumunda default dön
                return DEFAULT;
            }
        }

        return DEFAULT;
    }

    @Override
    protected void scale(CattleEntity entity, com.mojang.blaze3d.vertex.PoseStack poseStack, float partialTickTime) {
        float scale = 0.6f; // Default (Bilinmeyen ırklar için ufak)

        String breed = entity.getBreed();
        if ("Holstein".equalsIgnoreCase(breed)) {
            scale = 1.0f;
        } else if ("Jersey".equalsIgnoreCase(breed)) {
            scale = 0.8f;
        } else if ("Angus".equalsIgnoreCase(breed) || "Simmental".equalsIgnoreCase(breed)) {
            scale = 1.2f;
        }

        // Apply scale
        poseStack.scale(scale, scale, scale);

        super.scale(entity, poseStack, partialTickTime);
    }
}