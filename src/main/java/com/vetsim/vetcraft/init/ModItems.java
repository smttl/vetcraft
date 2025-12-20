package com.vetsim.vetcraft.init;

import com.vetsim.vetcraft.VetCraft;
import com.vetsim.vetcraft.item.MedicineItem; // İlaç sınıfımız
import com.vetsim.vetcraft.item.FilledBloodTubeItem;

import com.vetsim.vetcraft.item.SemenStrawItem;
import net.minecraft.world.item.BlockItem; // <--- BU LAZIM (Blok Eşyası için)
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(VetCraft.MOD_ID);

    // --- ALETLER ---
    public static final DeferredHolder<Item, Item> STETHOSCOPE = ITEMS.register("stethoscope",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> VET_CLIPBOARD = ITEMS.register("vet_clipboard",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> THERMOMETER = ITEMS.register("thermometer",
            () -> new Item(new Item.Properties().stacksTo(1)));
    // --- LABORATUVAR (BU KISIM EKSİKTİ) ---
    public static final DeferredHolder<Item, Item> EMPTY_BLOOD_TUBE = ITEMS.register("empty_blood_tube",
            () -> new Item(new Item.Properties().stacksTo(64)));

    public static final DeferredHolder<Item, Item> FILLED_BLOOD_TUBE = ITEMS.register("filled_blood_tube",
            () -> new FilledBloodTubeItem(new Item.Properties().stacksTo(16)));
    // --- BLOK EŞYALARI (EKSİK OLAN KISIM BUYDU) ---
    // Yemlik bloğunu eline alabilmen için onun "Item" versiyonunu burada tanımlıyoruz.
    public static final DeferredHolder<Item, Item> FEED_TROUGH_ITEM = ITEMS.register("feed_trough",
            () -> new BlockItem(ModBlocks.FEED_TROUGH.get(), new Item.Properties()));


    // --- SUNİ TOHUMLAMA ---

    // 1. Boş Payet (Boğadan almak için)
    public static final DeferredHolder<Item, Item> EMPTY_STRAW = ITEMS.register("empty_straw",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 2. Dolu Payet (İneğe uygulamak için) -> Özel sınıf yazacağız (Tooltip için)
    public static final DeferredHolder<Item, Item> FILLED_STRAW = ITEMS.register("filled_straw",
            () -> new SemenStrawItem(new Item.Properties().stacksTo(16)));


    public static final DeferredHolder<Item, Item> MANURE = ITEMS.register("manure",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // --- İLAÇLAR (DETAYLI TIP) ---

    // 1. Genel Antibiyotik
    public static final DeferredHolder<Item, Item> ANTIBIOTICS = ITEMS.register("antibiotics",
            () -> new MedicineItem(new Item.Properties().stacksTo(16), "Genel bakteriyel enfeksiyonlar için."));

    // 2. Penisilin
    public static final DeferredHolder<Item, Item> PENICILLIN = ITEMS.register("penicillin",
            () -> new MedicineItem(new Item.Properties().stacksTo(16), "Gram-pozitif bakterilere karşı etkili enjeksiyon."));

    // 3. Flunixin
    public static final DeferredHolder<Item, Item> FLUNIXIN = ITEMS.register("flunixin",
            () -> new MedicineItem(new Item.Properties().stacksTo(16), "Yüksek ateş ve ağrı durumlarında kullanılır."));

    // 4. Multivitamin
    public static final DeferredHolder<Item, Item> MULTIVITAMIN = ITEMS.register("multivitamin",
            () -> new MedicineItem(new Item.Properties().stacksTo(64), "Bağışıklık sistemini güçlendirir."));

    // -------------------------------------------------------------------------

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}