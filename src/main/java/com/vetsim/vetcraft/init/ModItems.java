package com.vetsim.vetcraft.init;

import com.vetsim.vetcraft.VetCraft;
import com.vetsim.vetcraft.entity.ModEntities;
import com.vetsim.vetcraft.item.*;

import com.vetsim.vetcraft.money.BankData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem; // <--- BU LAZIM (Blok Eşyası için)
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
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

        // YENİ EKLENEN YULAR (HALTER) - Düzeltilmiş Hali
        public static final DeferredHolder<Item, Item> HALTER = ITEMS.register("halter",
                        () -> new com.vetsim.vetcraft.item.HalterItem(new Item.Properties().stacksTo(1)));

        // tuccar ÇAĞIRMA YUMURTASI
        public static final DeferredHolder<Item, Item> CELEP_SPAWN_EGG = ITEMS.register("celep_spawn_egg",
                        () -> new VetSpawnEggItem(ModEntities.CELEP, 0x4a3b2b, 0xd4af37, new Item.Properties()));
        // Kahverengi ve Altın rengi yumurta

        // CATTLE (SIĞIR) ÇAĞIRMA YUMURTASI
        public static final DeferredHolder<Item, Item> CATTLE_SPAWN_EGG = ITEMS.register("cattle_spawn_egg",
                        () -> new VetSpawnEggItem(ModEntities.CATTLE, 0x443626, 0xA1A1A1, new Item.Properties()));
        // Siyah ve Beyaz (Holstein) rengi yumurta

        // --- LABORATUVAR (BU KISIM EKSİKTİ) ---
        public static final DeferredHolder<Item, Item> EMPTY_BLOOD_TUBE = ITEMS.register("empty_blood_tube",
                        () -> new Item(new Item.Properties().stacksTo(64)));

        public static final DeferredHolder<Item, Item> FILLED_BLOOD_TUBE = ITEMS.register("filled_blood_tube",
                        () -> new FilledBloodTubeItem(new Item.Properties().stacksTo(16)));
        // --- BLOK EŞYALARI (EKSİK OLAN KISIM BUYDU) ---
        // Yemlik bloğunu eline alabilmen için onun "Item" versiyonunu burada
        // tanımlıyoruz.
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

        public static final DeferredHolder<Item, Item> ALFALFA = ITEMS.register("alfalfa",
                        () -> new Item(new Item.Properties().stacksTo(64)));

        // --- İLAÇLAR (DETAYLI TIP) ---

        // 1. Genel Antibiyotik
        public static final DeferredHolder<Item, Item> ANTIBIOTICS = ITEMS.register("antibiotics",
                        () -> new MedicineItem(new Item.Properties().stacksTo(16),
                                        "Genel bakteriyel enfeksiyonlar için."));

        // 2. Penisilin
        public static final DeferredHolder<Item, Item> PENICILLIN = ITEMS.register("penicillin",
                        () -> new MedicineItem(new Item.Properties().stacksTo(16),
                                        "Gram-pozitif bakterilere karşı etkili enjeksiyon."));

        // 3. Flunixin
        public static final DeferredHolder<Item, Item> FLUNIXIN = ITEMS.register("flunixin",
                        () -> new MedicineItem(new Item.Properties().stacksTo(16),
                                        "Yüksek ateş ve ağrı durumlarında kullanılır."));

        // 4. Şap Aşısı (FMD Vaccine) - EKSİKTİ EKLENDİ
        public static final DeferredHolder<Item, Item> FMD_VACCINE = ITEMS.register("fmd_vaccine",
                        () -> new MedicineItem(new Item.Properties().stacksTo(16),
                                        "Şap hastalığına (FMD) karşı koruyucu aşı."));

        public static final DeferredHolder<Item, Item> MULTIVITAMIN = ITEMS.register("multivitamin",
                        () -> new MedicineItem(new Item.Properties().stacksTo(64),
                                        "Bağışıklık sistemini güçlendirir."));

        // 5. Sirke (Alkaloz Tedavisi)
        public static final DeferredHolder<Item, Item> VINEGAR = ITEMS.register("vinegar",
                        () -> new MedicineItem(new Item.Properties().stacksTo(16),
                                        "Rumen pH'ını düşürür (Alkaloz tedavisi)."));

        // 6. Yağ Şişesi (Gaz/Bloat Tedavisi)
        public static final DeferredHolder<Item, Item> OIL_BOTTLE = ITEMS.register("oil_bottle",
                        () -> new MedicineItem(new Item.Properties().stacksTo(16),
                                        "Gaz sıkışmasını giderir (Bloat tedavisi)."));

        // 2. BANKA KARTI (Bakiye Sorgulama)
        public static final DeferredHolder<Item, Item> DEBIT_CARD = ITEMS.register("debit_card",
                        () -> new Item(new Item.Properties().stacksTo(1)) {
                                @Override
                                public InteractionResultHolder<ItemStack> use(Level level, Player player,
                                                InteractionHand hand) {
                                        if (!level.isClientSide) {
                                                double balance = BankData.getBalance((ServerLevel) level, player);
                                                player.sendSystemMessage(Component.literal(
                                                                "§6[ 💳 BANKA ] §fBakiyeniz: §e" + balance + " TL"));
                                        }
                                        return super.use(level, player, hand);
                                }
                        });

        // tuccar ÇAĞIRMA DÜDÜĞÜ
        public static final DeferredHolder<Item, Item> CELEP_WHISTLE = ITEMS.register("celep_whistle",
                        () -> new CelepWhistleItem(new Item.Properties().stacksTo(1)));

        // YENİ AKILLI TELEFON
        public static final DeferredHolder<Item, Item> SMART_PHONE = ITEMS.register("smart_phone",
                        () -> new SmartPhoneItem(new Item.Properties().stacksTo(1)));

        // --- 4. HORMONLAR (Functional Drugs) ---
        // PGF2a (Prostaglandin) - Düşük İğnesi / Kızgınlık İğnesi
        public static final DeferredHolder<Item, Item> HORMONE_PGF2A = ITEMS.register("hormone_pgf2a",
                        () -> new Item(new Item.Properties()));

        // GnRH - Yumurtlama İğnesi (İleride kullanılabilir)
        public static final DeferredHolder<Item, Item> HORMONE_GNRH = ITEMS.register("hormone_gnrh",
                        () -> new Item(new Item.Properties()));

        // Oxytocin - Süt İndirme İğnesi
        public static final DeferredHolder<Item, Item> HORMONE_OXYTOCIN = ITEMS.register("hormone_oxytocin",
                        () -> new Item(new Item.Properties()));

        // --- İLAVE İLAÇLAR ---
        public static final DeferredHolder<Item, Item> DEXTROSE_SERUM = ITEMS.register("dextrose_serum",
                        () -> new MedicineItem(new Item.Properties().stacksTo(16),
                                        "Ketozis tedavisi için enerji kaynağı."));

        // --- DİĞER BLOK EŞYALARI ---
        public static final DeferredHolder<Item, Item> SALT_LICK = ITEMS.register("salt_lick",
                        () -> new BlockItem(ModBlocks.SALT_LICK.get(), new Item.Properties()));

        // --- 5. YENİ DOĞUM ÜRÜNLERİ ---
        // Ağız Sütü (Bağışıklık)
        public static final DeferredHolder<Item, Item> COLOSTRUM_BUCKET = ITEMS.register("colostrum_bucket",
                        () -> new Item(new Item.Properties().stacksTo(1)));

        // Buzağı Maması (Yedek)
        public static final DeferredHolder<Item, Item> CALF_FORMULA = ITEMS.register("calf_formula",
                        () -> new Item(new Item.Properties().stacksTo(16)));

        // -------------------------------------------------------------------------

        public static void register(IEventBus eventBus) {
                ITEMS.register(eventBus);
        }
}