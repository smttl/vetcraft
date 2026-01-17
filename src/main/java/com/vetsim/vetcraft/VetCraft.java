package com.vetsim.vetcraft;

import com.mojang.logging.LogUtils;
import com.vetsim.vetcraft.entity.ModEntities;
import com.vetsim.vetcraft.gui.PhoneScreen;
import com.vetsim.vetcraft.init.ModBlocks;
import com.vetsim.vetcraft.init.ModCreativeModeTabs;
import com.vetsim.vetcraft.init.ModItems;
import com.vetsim.vetcraft.init.ModMenuTypes;
import com.vetsim.vetcraft.network.VetNetwork;
import com.vetsim.vetcraft.util.DiseaseManager;
import com.vetsim.vetcraft.util.MarketManager;
import net.minecraft.client.gui.screens.MenuScreens;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod(VetCraft.MOD_ID)
public class VetCraft {
    public static final String MOD_ID = "vetcraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    public VetCraft(IEventBus modEventBus) {
        // 1. Mod Eşyalarını, Bloklarını ve Varlıklarını Kaydet
        ModBlocks.register(modEventBus); // ÖNCE BLOKLAR (Items buna bağlı)
        ModEntities.register(modEventBus); // SONRA VARLIKLAR (Spawn Egg buna bağlı)
        ModItems.register(modEventBus); // EN SON EŞYALAR
        ModCreativeModeTabs.register(modEventBus);

        // 2. Setup (Kurulum) olaylarını dinle
        // Oyun açılırken çalışacak kodlar (Sunucu + İstemci Ortak)
        modEventBus.addListener(this::commonSetup);

        // Sadece İstemci (Client) tarafında çalışacak kodlar (Görüntü/Render işlemleri)
        modEventBus.addListener(this::clientSetup);

        ModMenuTypes.register(modEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Hastalık verilerini JSON'dan yükle
            MarketManager.loadMarkets();
            com.vetsim.vetcraft.util.FeedManager.loadFeeds();
            com.vetsim.vetcraft.util.DrugManager.loadDrugs();
            DiseaseManager.loadDiseases();
            com.vetsim.vetcraft.util.BreedManager.loadBreeds();
            LOGGER.info("VetCraft: Hastalık ve Irk veritabanı yüklendi.");
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        // Varlıkların Render (Görüntü) Ayarlarını Yap
        // ModEvents.registerRenderers içinde zaten kayıtlı.
        // Buradaki eski kodları siliyoruz:

        // GUI Ekranını kaydet (Menu ile Screen'i eşleştir)
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.PHONE_MENU.get(), PhoneScreen::new);
            MenuScreens.register(ModMenuTypes.BLOOD_ANALYSIS_MENU.get(),
                    com.vetsim.vetcraft.gui.BloodAnalysisScreen::new); // <-- YENİ // <-- BUNU EKLE
        });

    }
}