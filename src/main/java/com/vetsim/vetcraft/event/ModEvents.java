package com.vetsim.vetcraft.event;

import com.vetsim.vetcraft.VetCraft;
import com.vetsim.vetcraft.entity.CattleEntity;
import com.vetsim.vetcraft.entity.CelepEntity;
import com.vetsim.vetcraft.entity.ModEntities;
import com.vetsim.vetcraft.renderer.CattleRenderer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

// BU SATIR ÇOK ÖNEMLİ! (Bus.MOD dediğimiz için oyun açılırken çalışır)
@Mod.EventBusSubscriber(modid = VetCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        // Can, Hız gibi özellikleri kaydeder
        event.put(ModEntities.CATTLE.get(), CattleEntity.createAttributes().build());

        event.put(ModEntities.CELEP.get(), CelepEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Görünümü (Render) kaydeder
        event.registerEntityRenderer(ModEntities.CATTLE.get(), CattleRenderer::new);
    }
}