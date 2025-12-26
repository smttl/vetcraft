package com.vetsim.vetcraft.entity.components;

import com.vetsim.vetcraft.entity.CattleEntity;
import com.vetsim.vetcraft.init.ModItems;
import com.vetsim.vetcraft.util.DiseaseData;
import com.vetsim.vetcraft.util.DiseaseManager;
import com.vetsim.vetcraft.util.FeedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Items;

public class CattleMetabolism {
    private final CattleEntity cow;

    private float rumenPh = 6.8f;
    private float rumenFill = 50.0f;
    private float bodyConditionScore = 3.5f;
    private float hydration = 100.0f; // Su seviyesi (%100 dolu)
    private float methaneGas = 0.0f;  // Gaz sıkışması (%0 risksiz)
    // --- YENİ: LAKTASYON (SÜT VERME) SÜRESİ ---
    // Gerçekte 305 gündür. Biz oyunda 24000 tick (1 oyun günü) diyelim test için.
    private int lactationPeriod = 0;
    // ------------------------------------------

    private int manureTimer = 0;
    private int milkTimer = 0;

    public CattleMetabolism(CattleEntity cow) {
        this.cow = cow;
    }

    public void tick() {
        if (cow.level().isClientSide) return;

        // Sindirim
        if (cow.tickCount % 100 == 0) digest();

        // Laktasyon Süresi Geri Sayımı
        if (lactationPeriod > 0) {
            lactationPeriod--;
        }

        // Gübre
        manureTimer++;
        if (manureTimer >= 1200) {
            manureTimer = 0;
            if (!cow.isBaby()) {
                cow.spawnAtLocation(ModItems.MANURE.get());
                cow.playSound(SoundEvents.CHICKEN_EGG, 1.0F, 0.5F);
            }
        }

        // --- SÜT SİSTEMİ (GÜNCELLENDİ) ---
        // Artık breedingCooldown'a değil, lactationPeriod'a bakıyoruz!
        if (!cow.isMale() && !cow.isBaby() && lactationPeriod > 0) {
            milkTimer++;
            if (milkTimer >= 2400) {
                milkTimer = 0;
                produceMilk();
            }
        }

        // --- 1. SU TÜKETİMİ (HİDRASYON) ---
        // Süt veren inekler ÇOK DAHA HIZLI susar.
        float thirstRate = 0.02f;
        if (isLactating()) thirstRate = 0.05f; // Laktasyondaysa 2.5 kat hızlı susar

        hydration -= thirstRate;
        if (hydration < 0) hydration = 0;

        // Susuzluk Etkileri
        if (hydration < 30.0f) {
            // Dehidrasyon stresi artırır
            if (cow.tickCount % 200 == 0) cow.getHealthSystem().increaseStress(5);
        }

        // --- 2. GAZ (METAN) DÖNGÜSÜ ---
        // İnekler sürekli gaz üretir ama sağlıklıysa bunu geğirerek atarlar.
        if (methaneGas > 0) {
            // Normalde gaz azalır (Geğirme)
            methaneGas -= 0.1f;
        }

        // TİMPANİ (BLOAT) KRİZİ
        // Eğer gaz %80'i geçerse hayvan şişer ve canı yanmaya başlar.
        if (methaneGas > 80.0f) {
            if (cow.tickCount % 100 == 0) {
                cow.hurt(cow.damageSources().starve(), 2.0f); // Canı yanar
                cow.playSound(net.minecraft.sounds.SoundEvents.COW_HURT, 1.0f, 0.5f);

                // Görsel olarak şişme efekti (Partikül)
                if (cow.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                    sl.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE,
                            cow.getX(), cow.getY()+1, cow.getZ(), 5, 0.3, 0.3, 0.3, 0);
                }
            }
        }
    }

    private void produceMilk() {
        // 1. KESİN ENGELLEYİCİLER (Kırmızı Işık)
        if (hydration < 40.0f) {
            // Su %40'ın altındaysa süt vermez!
            return;
        }
        // A) Kuru Dönem (Doğuma Yakın)
        if (cow.getReproductionSystem().isInDryPeriod()) return;

        // B) Ciddi Hastalıklar (Mastitis vb.)
        String currentDisease = cow.getDisease();
        if (!currentDisease.equals("NONE")) {
            DiseaseData data = DiseaseManager.getDiseaseById(currentDisease);
            // Eğer JSON dosyasında "stop_milk": true ise süt tamamen kesilir.
            if (data != null && data.stopMilk) return;
        }

        // C) Fizyolojik Krizler
        boolean isHungry = rumenFill < 30.0f;
        boolean isStressed = cow.getHealthSystem().getStressLevel() > 60; // Stres çok yüksekse süt iner mi? Zor.
        boolean hasAcidosis = rumenPh < 5.8f; // Asidoz süt yağını düşürür ve verimi keser

        if (isHungry || isStressed) return;

        // Asidoz varsa %50 ihtimalle süt vermez (Düzensiz verim)
        if (hasAcidosis && cow.getRandom().nextBoolean()) return;

        // --- 2. VERİM HESAPLAMA (Yeşil Işık) ---
        // Buraya kadar geldiyse süt verecek. Ama kaç tane?

        int milkAmount = 1; // Taban verim (Herkes en az 1 verir)

        // A) Genetik Faktör (Irk)
        // Holstein ve Jersey sütçü ırklardır.
        if (cow.getBreed().equals("Holstein") || cow.getBreed().equals("Jersey")) {
            milkAmount += 1;
        }

        // B) Beslenme Faktörü (BCS & Protein)
        // Eğer hayvanın kondisyonu çok iyiyse (Yüksek proteinli beslenmişse)
        if (bodyConditionScore > 4.0f) {
            milkAmount += 1;
        }

        // C) Hastalık Cezası (Hafif Hastalıklar)
        // Hastalık sütü tamamen kesmese bile (stopMilk=false), verimi 1 düşürür.
        if (!currentDisease.equals("NONE")) {
            milkAmount = Math.max(1, milkAmount - 1);
        }

        // --- 3. ÜRETİM ---
        // Hesaplanan miktar kadar süt düşür
        // Döngü kuruyoruz çünkü spawnAtLocation tek seferde 1 item atar.
        for (int i = 0; i < milkAmount; i++) {
            cow.spawnAtLocation(Items.MILK_BUCKET);
        }

        // Ses ve efekt (Miktar çoksa ses daha tok çıksın veya partikül olsun)
        cow.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F + (milkAmount * 0.1f));

        // Eğer verim yüksekse oyuncuya görsel bir ipucu ver (Mutlu villager partikülü)
        if (milkAmount >= 3 && cow.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                    cow.getX(), cow.getY() + 0.5, cow.getZ(), 3, 0.5, 0.5, 0.5, 0);
        }
    }

    // --- YENİ METOD: Laktasyonu Başlat (Doğumda Çağrılacak) ---
    public void startLactation() {
        // 305 Günlük süt verimi (Test için 10 oyun günü = 240000 tick yapabilirsin)
        // Şimdilik test kolay olsun diye 3 gün (72000 tick) veriyorum.
        this.lactationPeriod = 72000;
    }

    // Laktasyonda mı? (Entity tarafından görülmesi için)
    public boolean isLactating() {
        return lactationPeriod > 0;
    }
    // ----------------------------------------------------------

    public void feed(FeedData food) {
        // 1. pH DENGESİ (Mide Asiditesi)
        // Kuru yemler (Saman, Yonca) tükürük salgısını artırır, pH yükselir (İyi).
        // Islak/Nişastalı yemler (Buğday, Mısır) pH düşürür (Kötü/Asidoz riski).
        if (food.isDry) { // Saman, Yonca, Ot
            // EĞER ASİDOZ VARSA (pH < 6.2): Saman hayat kurtarır, pH'ı hızlı yükselt
            if (rumenPh < 6.2f) {
                rumenPh += 0.30f;
            }
            // EĞER NORMAL SEVİYEDEYSE (6.2 - 7.0): Sadece dengede tut, çok az artır
            else if (rumenPh < 7.0f) {
                rumenPh += 0.10f;
            }
            // EĞER ZATEN YÜKSEKSE (> 7.0): Artık artırma (Alkaloz riskini önle)
            else {
                rumenPh += 0.02f; // Çok minimal artış
            }
        }
        else { // Buğday, Arpa, Mısır (Nişasta)
            // Nişasta her zaman pH düşürür (Asidoz riski yaratır)
            rumenPh -= 0.25f;
        }

        // pH sınırları (5.0 ile 8.0 arası)
        rumenPh = Math.max(5.0f, Math.min(8.0f, rumenPh));

        // 2. TOKLUK (Mide Doluluğu)
        rumenFill = Math.min(100, rumenFill + food.nutrition);
        cow.setHunger((int)rumenFill);

        // 3. PROTEİN ETKİSİ (Kondisyon / Kilo Alımı)
        // Yüksek proteinli yemler (Yonca > %16, Buğday > %12) hayvanı geliştirir.
        // Saman (%8) sadece karnını doyurur.
        if (food.protein > 0.15f) {
            // Yüksek Protein (Süper Gelişim)
            bodyConditionScore += 0.02f;
        } else if (food.protein > 0.10f) {
            // Orta Protein (Normal Gelişim)
            bodyConditionScore += 0.01f;
        } else {
            // Düşük Protein (Sadece koruma)
            // BCS artmaz.
        }
        if (!food.isDry) {
            // Islak yemler gaz üretir
            methaneGas += 15.0f;

            // Eğer hayvan zaten Asidozdaysa gazı atamaz, birikir!
            if (rumenPh < 6.0f) {
                methaneGas += 10.0f; // Ekstra gaz
            }
        } else {
            // Kuru yem (Saman) geviş getirmeyi tetikler, gazı düşürür
            methaneGas -= 5.0f;
        }

        methaneGas = Math.max(0, Math.min(100, methaneGas));

        // BCS Sınırı (1.0 - 5.0)
        bodyConditionScore = Math.max(1.0f, Math.min(5.0f, bodyConditionScore));

        // Kilosunu güncelle
        float weight = 30.0f + (bodyConditionScore * 100.0f);
        cow.setWeight(weight);

        // Hastalık kontrolü yap
        checkDigestiveIssues();
    }
    // Su içme metodu
    public void drink(float amount) {
        this.hydration = Math.min(100.0f, this.hydration + amount);

        // Su içmek gazı biraz rahatlatır
        this.methaneGas = Math.max(0, this.methaneGas - 5.0f);
    }
    private void digest() {
        rumenFill -= 1.5f;
        if (rumenPh < 6.8f) rumenPh += 0.05f;
        if (rumenPh > 7.0f) rumenPh -= 0.05f;

        if (rumenFill > 50 && rumenPh > 6.0f) bodyConditionScore = Math.min(5.0f, bodyConditionScore + 0.01f);
        else if (rumenFill < 20) bodyConditionScore = Math.max(1.0f, bodyConditionScore - 0.02f);

        float weight = 30.0f + (bodyConditionScore * 100.0f);
        cow.setWeight(weight);
        cow.setHunger((int)Math.max(0, rumenFill));
        checkDigestiveIssues();
    }

    private void checkDigestiveIssues() {
        String currentDisease = cow.getDisease();
        if (rumenPh < 5.8f) {
            if (!currentDisease.equals("acidosis")) cow.getHealthSystem().setDisease("acidosis");
        } else if (rumenPh > 6.0f) {
            if (currentDisease.equals("acidosis")) cow.getHealthSystem().setDisease("NONE");
        }
    }

    public void reduceBcs(float amount) {
        this.bodyConditionScore = Math.max(1.0f, this.bodyConditionScore - amount);
        float weight = 30.0f + (bodyConditionScore * 100.0f);
        cow.setWeight(weight);
    }

    public void save(CompoundTag tag) {
        tag.putFloat("Meta_RumenPH", rumenPh);
        tag.putFloat("Meta_BCS", bodyConditionScore);
        tag.putFloat("Meta_Fill", rumenFill);
        tag.putInt("Meta_MilkTimer", milkTimer);
        // Yeni veri
        tag.putInt("Meta_Lactation", lactationPeriod);
    }

    public void load(CompoundTag tag) {
        if(tag.contains("Meta_RumenPH")) rumenPh = tag.getFloat("Meta_RumenPH");
        if(tag.contains("Meta_BCS")) bodyConditionScore = tag.getFloat("Meta_BCS");
        if(tag.contains("Meta_Fill")) rumenFill = tag.getFloat("Meta_Fill");
        if(tag.contains("Meta_MilkTimer")) milkTimer = tag.getInt("Meta_MilkTimer");
        // Yeni veri
        if(tag.contains("Meta_Lactation")) lactationPeriod = tag.getInt("Meta_Lactation");
    }

    public float getRumenPh() { return rumenPh; }
    public float getBcs() { return bodyConditionScore; }
    public float getHydration() { return hydration; }  // <-- EKSİKTİ
    public float getGasLevel() { return methaneGas; }  // <-- EKSİKTİ
}