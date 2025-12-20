package com.vetsim.vetcraft.init;

import com.vetsim.vetcraft.VetCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, VetCraft.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> VET_TAB = CREATIVE_MODE_TABS.register("vet_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.STETHOSCOPE.get())) // İkon artık Stetoskop olsun
                    .title(Component.translatable("creativetab.vet_tab"))
                    .displayItems((pParameters, pOutput) -> {

                        // 1. BLOKLAR
                        pOutput.accept(ModItems.FEED_TROUGH_ITEM.get());

                        // 2. TANI ALETLERİ (Buraya ekliyoruz)
                        pOutput.accept(ModItems.VET_CLIPBOARD.get());
                        pOutput.accept(ModItems.STETHOSCOPE.get());
                        pOutput.accept(ModItems.THERMOMETER.get());

                        // 3. İLAÇLAR

                        pOutput.accept(ModItems.ANTIBIOTICS.get());
                        pOutput.accept(ModItems.PENICILLIN.get());
                        pOutput.accept(ModItems.FLUNIXIN.get());
                        pOutput.accept(ModItems.MULTIVITAMIN.get());

                        // --- 3. LABORATUVAR (YENİ EKLENDİ) 🩸 ---
                        pOutput.accept(ModItems.EMPTY_BLOOD_TUBE.get());  // Boş Tüp
                        pOutput.accept(ModItems.FILLED_BLOOD_TUBE.get()); // Dolu Tüp

                        // 4. ENTITY YUMURTALARI (Spawn Eggs)
                        // Eğer spawn egg yaptıysan buraya eklersin, şimdilik komutla çağırıyoruz.
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}