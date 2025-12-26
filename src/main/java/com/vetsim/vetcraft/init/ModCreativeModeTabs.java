package com.vetsim.vetcraft.init;

import com.vetsim.vetcraft.VetCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, VetCraft.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> VET_TAB = CREATIVE_MODE_TABS.register("vet_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.vetsim_tab")) // Dil dosyasındaki isim
                    .icon(() -> new ItemStack(ModItems.STETHOSCOPE.get())) // Sekme ikonu (Stetoskop)
                    .displayItems((pParameters, pOutput) -> {

                        // --- 1. EKİPMANLAR ---
                        pOutput.accept(ModItems.STETHOSCOPE.get());
                        pOutput.accept(ModItems.VET_CLIPBOARD.get());
                        pOutput.accept(ModItems.THERMOMETER.get());
                        pOutput.accept(ModItems.HALTER.get()); // Yular

                        // --- 2. LABORATUVAR & TOHUMLAMA ---
                        pOutput.accept(ModItems.EMPTY_BLOOD_TUBE.get());
                        pOutput.accept(ModItems.FILLED_BLOOD_TUBE.get());
                        pOutput.accept(ModItems.EMPTY_STRAW.get());
                        pOutput.accept(ModItems.FILLED_STRAW.get());

                        // --- 3. İLAÇLAR ---
                        pOutput.accept(ModItems.ANTIBIOTICS.get());
                        pOutput.accept(ModItems.PENICILLIN.get());
                        pOutput.accept(ModItems.FLUNIXIN.get());
                        pOutput.accept(ModItems.MULTIVITAMIN.get());

                        // --- 3. HORMONLAR ---
                        pOutput.accept(ModItems.HORMONE_PGF2A.get());
                        pOutput.accept(ModItems.HORMONE_GNRH.get());
                        pOutput.accept(ModItems.HORMONE_OXYTOCIN.get());

                        // --- 4. EKONOMİ VE TİCARET (YENİ) ---
                        pOutput.accept(ModItems.DEBIT_CARD.get());     // Banka Kartı
                        pOutput.accept(ModItems.CELEP_WHISTLE.get());  // Celep Düdüğü
                        pOutput.accept(ModItems.CELEP_SPAWN_EGG.get());// Celep Yumurtası
                        //pOutput.accept(ModItems.MARKET_BLOCK_ITEM.get()); // Pazar Bloğu (Varsa)
                        pOutput.accept(ModItems.SMART_PHONE.get());

                        // --- 5. BLOKLAR VE YEMLER ---
                        pOutput.accept(ModItems.FEED_TROUGH_ITEM.get());
                        pOutput.accept(ModItems.MANURE.get());

                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}