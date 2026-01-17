package com.vetsim.vetcraft.renderer;

import com.vetsim.vetcraft.entity.worker.WorkerEntity;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class WorkerRenderer extends MobRenderer<WorkerEntity, VillagerModel<WorkerEntity>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft",
            "textures/entity/villager/type/plains.png");

    public WorkerRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(WorkerEntity entity) {
        return TEXTURE;
    }
}
