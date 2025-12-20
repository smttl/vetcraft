package com.vetsim.vetcraft;

import com.mojang.logging.LogUtils;
import com.vetsim.vetcraft.init.ModBlocks;
import com.vetsim.vetcraft.init.ModCreativeModeTabs;
import com.vetsim.vetcraft.entity.ModEntities;
import com.vetsim.vetcraft.init.ModItems;
import com.vetsim.vetcraft.util.DiseaseManager;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod(VetCraft.MOD_ID)
public class VetCraft {
    public static final String MOD_ID = "vetsim";
    public static final Logger LOGGER = LogUtils.getLogger();

    public VetCraft(IEventBus modEventBus) {
        // 1. Mod Eşyalarını ve Bloklarını Kaydet
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModEntities.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);

        // 2. Setup (Kurulum) olayını dinle
        modEventBus.addListener(this::commonSetup);

        // 3. Genel Forge olaylarını kaydet
        // DÜZELTME: Bu satırı sildik veya yoruma aldık çünkü henüz @SubscribeEvent metodumuz yok.
        // NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Oyun açılırken hastalıkları JSON'dan yükle
            DiseaseManager.loadDiseases();
        });
    }
}