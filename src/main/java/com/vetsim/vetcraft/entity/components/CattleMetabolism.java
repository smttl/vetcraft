package com.vetsim.vetcraft.entity.components;

import com.vetsim.vetcraft.entity.CattleEntity;
import com.vetsim.vetcraft.init.ModItems;
import com.vetsim.vetcraft.util.BreedData;
import com.vetsim.vetcraft.util.BreedManager;
import com.vetsim.vetcraft.util.DiseaseData;
import com.vetsim.vetcraft.util.DiseaseManager;
import com.vetsim.vetcraft.util.FeedData;
import com.vetsim.vetcraft.config.VetCraftConfig;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;

public class CattleMetabolism {
    private final CattleEntity cow;

    private float rumenPh = 6.8f;
    private float rumenFill = 50.0f;
    private float bodyConditionScore = 3.5f;
    private float hydration = 100.0f; // Su seviyesi (%100 dolu)
    private float methaneGas = 0.0f; // Gaz sıkışması (%0 risksiz)
    // --- YENİ: LAKTASYON (SÜT VERME) SÜRESİ ---
    // Gerçekte 305 gündür. Biz oyunda 24000 tick (1 oyun günü) diyelim test için.
    private int lactationPeriod = 0;
    private int unmilkedTicks = 0; // Ne kadar süredir sağılmadı?

    // --- GENETİK FARKLILIKLAR (PTA - Predicted Transmitting Ability) ---
    // Irkın temel özelliklerine eklenen bireysel bonuslar.
    private float geneticMilkModifier = 0.0f; // Örn: +0.5 litre
    private float geneticHealthModifier = 0.0f; // Örn: +0.1 direnç
    // ------------------------------------------

    private int manureTimer = 0;
    private int milkTimer = 0;

    // --- YENİ: BAĞIŞIKLIK SİSTEMİ (IMMUNITY) ---
    private float immunity = 50.0f; // 0-100 arası (Doğuştan gelir)
    private boolean colostrumReceived = false; // Ağız sütü içti mi?
    private int ageInTicks = 0; // Yaş takibi (saatlik)

    // --- ISI STRESİ (HEAT STRESS) ---
    private float bodyTemperature = 38.5f;

    public CattleMetabolism(CattleEntity cow) {
        this.cow = cow;
    }

    public void tick() {
        if (cow.level().isClientSide)
            return;

        ageInTicks++;

        // Sindirim
        if (cow.tickCount % 100 == 0)
            digest();

        // --- ISI STRESİ KONTROLÜ (HEAT STRESS) ---
        if (cow.tickCount % 200 == 0) {
            checkHeatStress();
        }

        // --- BUZAĞI BAĞIŞIKLIK KONTROLÜ ---
        if (cow.isBaby()) {
            if (cow.tickCount % 1200 == 0) { // Her dakika kontrol
                // İlk 24 saat (24000 tick) kritik
                if (ageInTicks < 24000 && !colostrumReceived) {
                    // Ağız sütü almadıysa bağışıklık düşer
                    immunity -= 0.5f;
                }

                // 24 saati geçti ve hala almadıysa -> ÇÖKÜŞ
                if (ageInTicks > 24000 && !colostrumReceived) {
                    if (immunity > 20.0f)
                        immunity = 20.0f; // Maksimum %20'ye düşer

                    // Hastalık Riski: Scours
                    if (cow.getRandom().nextFloat() < 0.1f && cow.getDisease().equals("NONE")) {
                        cow.getHealthSystem().setDisease("calf_scours"); // Buzağı İshali
                        cow.sendSystemMessage(net.minecraft.network.chat.Component
                                .literal("§c⚠ Buzağı ağız sütü almadığı için hastalandı!"));
                    }
                }
            }
        }

        // Laktasyon Süresi Geri Sayımı
        if (lactationPeriod > 0) {
            lactationPeriod--;
            unmilkedTicks++; // Sağılmadığı her an artar

            // 1. UYARI: MÖLEME (Yarım gün sağılmadıysa)
            if (unmilkedTicks > VetCraftConfig.MILKING_WARNING_TICKS) {
                // Sık sık möle (Her 20 saniyede bir)
                if (cow.tickCount % 400 == 0) {
                    cow.playSound(SoundEvents.COW_AMBIENT, 1.5F, 0.8F); // Biraz daha kalın ve acılı ses
                    if (cow.getRandom().nextFloat() < 0.3f) {
                        cow.sendSystemMessage(net.minecraft.network.chat.Component.literal("§e🐄 (Beni sağ!)"));
                    }
                }
            }

            // 2. CEZA: MASTİTİS (1 gün sağılmadıysa)
            if (unmilkedTicks > VetCraftConfig.MASTITIS_RISK_TICKS) {
                // Her saat başı (1000 tick) %20 şansla mastit olur
                if (cow.tickCount % 1000 == 0 && cow.getRandom().nextFloat() < 0.20f) {
                    if (cow.getDisease().equals("NONE")) {
                        cow.getHealthSystem().setDisease("mastitis");
                        cow.sendSystemMessage(net.minecraft.network.chat.Component
                                .literal("§c⚠ İnek uzun süre sağılmadığı için Mastitis (Meme İltihabı) oldu!"));
                    }
                }
            }
        } else {
            // Laktasyon bittiyse veya yoksa sayaç işlemez
            unmilkedTicks = 0;
        }

        // Gübre
        manureTimer++;
        if (manureTimer >= VetCraftConfig.MANURE_FREQUENCY) {
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
            if (milkTimer >= VetCraftConfig.MILK_PRODUCTION_FREQUENCY) {
                milkTimer = 0;
                produceMilk();
            }
        }

        // --- 1. SU TÜKETİMİ (HİDRASYON) ---
        // Süt veren inekler ÇOK DAHA HIZLI susar.
        float thirstRate = 0.002f;
        if (isLactating())
            thirstRate = 0.005f; // Laktasyondaysa 2.5 kat hızlı susar

        // Phase 23: Hastalık Su Kaybı (Water Loss)
        String currentDisease = cow.getDisease();
        if (!currentDisease.equals("NONE")) {
            DiseaseData dData = DiseaseManager.getDiseaseById(currentDisease);
            if (dData != null && dData.waterLoss > 0) {
                thirstRate += dData.waterLoss; // Ekstra kayıp ekle
            }
        }

        hydration -= thirstRate;
        if (hydration < 0)
            hydration = 0;

        // Susuzluk Etkileri
        if (hydration < 10.0f) {
            // Dehidrasyon stresi artırır
            if (cow.tickCount % 200 == 0) {
                cow.getHealthSystem().increaseStress(5);
                // FIX: Susuzluk hastalığına dönüşsün
                if (cow.getDisease().equals("NONE")) {
                    cow.getHealthSystem().setDisease("dehydration");
                    cow.sendSystemMessage(net.minecraft.network.chat.Component
                            .literal("§c⚠ Aşırı susuzluk nedeniyle Dehidrasyon başladı!"));
                }
            }
        } else if (hydration < 30.0f) {
            if (cow.tickCount % 200 == 0)
                cow.getHealthSystem().increaseStress(2);
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
                            cow.getX(), cow.getY() + 1, cow.getZ(), 5, 0.3, 0.3, 0.3, 0);
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
        if (cow.getReproductionSystem().isInDryPeriod())
            return;

        // B) Ciddi Hastalıklar (Mastitis vb.)
        String currentDisease = cow.getDisease();
        if (!currentDisease.equals("NONE")) {
            DiseaseData data = DiseaseManager.getDiseaseById(currentDisease);
            // Eğer JSON dosyasında "stop_milk": true ise süt tamamen kesilir.
            if (data != null && data.stopMilk)
                return;
        }

        // C) Fizyolojik Krizler
        boolean isHungry = rumenFill < 30.0f;
        boolean isStressed = cow.getHealthSystem().getStressLevel() > 60; // Stres çok yüksekse süt iner mi? Zor.
        boolean hasAcidosis = rumenPh < 5.8f; // Asidoz süt yağını düşürür ve verimi keser

        if (isHungry || isStressed)
            return;

        // Asidoz varsa %50 ihtimalle süt vermez (Düzensiz verim)
        if (hasAcidosis && cow.getRandom().nextBoolean())
            return;

        // --- 2. VERİM HESAPLAMA (Şans Bazlı) ---
        // Kullanıcı İsteği: 1.5 kova demek, %50 ihtimalle 1, %50 ihtimalle 2 kova
        // demektir.

        float potentialYield = 0.0f; // Taban sıfırla başlıyoruz, çarpanı ekleyeceğiz

        // A) Genetik Faktör (Irk)
        BreedData breedData = BreedManager.getBreed(cow.getBreed());
        if (breedData != null) {
            potentialYield += breedData.milkMultiplier;
            // Örn: Holstein (2.0) -> +2.0
            // Örn: Jersey (1.6) -> +1.6
            // Örn: Angus (0.5) -> +0.5
        } else {
            potentialYield += 1.0f; // Bilinmeyen ırk
        }

        // B) Beslenme Faktörü (BCS & Protein)
        // Eğer hayvanın kondisyonu çok iyiyse (Yüksek proteinli beslenmişse)
        if (bodyConditionScore > 4.0f) {
            potentialYield += 1.0f; // +1 Tam Kova Bonusu
        }

        // C) Hastalık Cezası (Hafif Hastalıklar)
        // Hastalık sütü tamamen kesmese bile verimi düşürür.
        if (!currentDisease.equals("NONE")) {
            potentialYield -= 1.0f;
        }

        // --- 3. ŞANS HESAPLAMASI ---
        potentialYield = Math.max(0.0f, potentialYield); // Negatif olamaz

        int baseAmount = (int) potentialYield; // Tam kısmı (Örn: 1.6 -> 1)
        float fractionalPart = potentialYield - baseAmount; // Küsürat (0.6)

        int milkAmount = baseAmount;

        // Küsürat kadar şansla +1 ekle
        if (cow.getRandom().nextFloat() < fractionalPart) {
            milkAmount += 1;
        }

        // Hiç süt vermeme durumu (Angus 0.5 -> %50 ihtimalle 0)
        // Eğer 0 çıkarsa ve oyuncu sağmaya çalıştıysa, en azından 1 versin mi?
        // Hayır, gerçekçilik için vermesin. (Oyuncu "boş" sesini duyar)
        // ANCAK: Kodun devamında "milkAmount" kadar kova verecek. 0 ise vermez.

        // --- 3. ÜRETİM (OTOMATİK YOK) ---
        // Otomatik kova düşürme iptal edildi (Sonsuz demir hilesini önlemek için).
        // Artık oyuncu kova ile sağ tıkladığında bu kontroller yapılacak.

        // Ses ve efekt (Sadece görsel/işitsel ipucu olarak kalsın)
        // cow.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);

        // Eğer verim yüksekse oyuncuya görsel bir ipucu ver (Mutlu villager partikülü)
        if (milkAmount >= 3 && cow.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                    cow.getX(), cow.getY() + 0.5, cow.getZ(), 3, 0.5, 0.5, 0.5, 0);
        }
    }

    // --- YENİ METOD: Laktasyonu Başlat (Doğumda Çağrılacak) ---
    public void startLactation() {
        // Minecraft'ta 1 gün = 24000 tick
        // Buradaki sayıyı değiştirerek kaç oyun günü süt vereceğini ayarlayabilirsiniz.
        int lactationDays = 10; // Örnek: 10 oyun günü boyunca süt verir
        this.lactationPeriod = lactationDays * 24000;
    }

    // Laktasyonda mı? (Entity tarafından görülmesi için)
    public boolean isLactating() {
        return lactationPeriod > 0;
    }

    public boolean isLactationActive() {
        return isLactating();
    } // Alias for compatibility with CattleEntity interaction
      // ----------------------------------------------------------

    public void feed(FeedData food) {
        // --- AŞIRI YEMLEME KONTROLÜ (OVERFEEDING) ---
        boolean isFull = rumenFill >= 90.0f;

        if (isFull) {
            // Tok olduğu halde yiyor -> CEZA
            // 1. Gaz çok hızlı artar
            methaneGas += 20.0f;

            // 2. pH dengesi bozulur (Mide ekşimesi)
            rumenPh -= 0.15f;

            // 3. Çok doluyken yemek can yakabilir
            if (rumenFill >= 100.0f) {
                cow.hurt(cow.damageSources().starve(), 1.0f);
                // Uyarı mesajını Entity tarafında verebiliriz veya burada görsel efekt
                if (cow.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                    sl.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE,
                            cow.getX(), cow.getY() + 1, cow.getZ(), 5, 0.2, 0.2, 0.2, 0);
                }
            }
        }

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
        } else { // Buğday, Arpa, Mısır (Nişasta)
                 // Nişasta her zaman pH düşürür (Asidoz riski yaratır)
            rumenPh -= 0.25f;
        }

        // pH sınırları (5.0 ile 8.0 arası)
        rumenPh = Math.max(5.0f, Math.min(8.0f, rumenPh));

        // 2. TOKLUK (Mide Doluluğu)
        rumenFill = Math.min(100, rumenFill + food.nutrition);
        cow.setHunger((int) rumenFill);

        // 3. PROTEİN ETKİSİ (Kondisyon / Kilo Alımı)
        // Yüksek proteinli yemler (Yonca > %16, Buğday > %12) hayvanı geliştirir.
        // Saman (%8) sadece karnını doyurur.

        // Sadece midesi aşırı dolu değilse protein işe yarar.
        // Çok doluyken sindirim durur.
        if (!isFull) {
            if (food.protein > 0.15f) {
                // Yüksek Protein (Süper Gelişim)
                bodyConditionScore += 0.02f;
            } else if (food.protein > 0.10f) {
                // Orta Protein (Normal Gelişim)
                bodyConditionScore += 0.01f;
            }
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
        rumenFill -= 0.15f;
        if (rumenPh < 6.8f)
            rumenPh += 0.05f;
        if (rumenPh > 7.0f)
            rumenPh -= 0.05f;

        if (rumenFill > 50 && rumenPh > 6.0f)
            bodyConditionScore = Math.min(5.0f, bodyConditionScore + 0.01f);
        else if (rumenFill < 20)
            bodyConditionScore = Math.max(1.0f, bodyConditionScore - 0.02f);

        float weight = 30.0f + (bodyConditionScore * 100.0f);
        cow.setWeight(weight);
        cow.setHunger((int) Math.max(0, rumenFill));
        checkDigestiveIssues();
    }

    private void checkDigestiveIssues() {
        String currentDisease = cow.getDisease();

        // 1. ASİDOZ (Düşük pH)
        if (rumenPh < 5.8f) {
            if (!currentDisease.equals("acidosis"))
                cow.getHealthSystem().setDisease("acidosis");
        }
        // 2. ALKALOZ (Yüksek pH)
        else if (rumenPh > 7.5f) {
            if (!currentDisease.equals("alkalosis"))
                cow.getHealthSystem().setDisease("alkalosis");
        }
        // İyileşme Kontrolü (pH normale döndü mü?)
        else if (rumenPh >= 6.0f && rumenPh <= 7.2f) {
            if (currentDisease.equals("acidosis") || currentDisease.equals("alkalosis")) {
                cow.getHealthSystem().setDisease("NONE");
            }
        }

        // 3. BLOAT (Gaz)
        if (methaneGas > 80.0f) {
            if (!currentDisease.equals("bloat"))
                cow.getHealthSystem().setDisease("bloat");
        } else if (methaneGas < 50.0f) {
            if (currentDisease.equals("bloat")) {
                cow.getHealthSystem().setDisease("NONE");
            }
        }
    }

    public void reduceBcs(float amount) {
        this.bodyConditionScore = Math.max(1.0f, this.bodyConditionScore - amount);
        float weight = 30.0f + (bodyConditionScore * 100.0f);
        cow.setWeight(weight);
    }

    @SuppressWarnings("null")
    private void checkHeatStress() {
        if (cow.level().isClientSide)
            return;

        // Biyom ve Ortam Sıcaklığı
        // Minecraft'ta çöl/savana sıcaklığı 2.0, ovalar 0.8, kar 0.0
        float biomeTemp = cow.level().getBiome(cow.blockPosition()).value().getBaseTemperature();
        boolean isDay = cow.level().isDay();
        boolean isSunny = cow.level().isThundering() == false && cow.level().isRaining() == false;

        // Eğer sıcak biyomdaysa (1.0 üzeri) ve güneş varsa
        if (biomeTemp > 0.9f && isDay && isSunny) {
            // Gökyüzünü görüyor mu? (Gölgede değilse)
            if (cow.level().canSeeSky(cow.blockPosition())) {
                // Phase 23: Irk Toleransı (Heat Tolerance)
                BreedData breed = BreedManager.getBreed(cow.getBreed());
                int stressAmount = 2; // Baz stres

                if (breed != null) {
                    if ("HEAT".equalsIgnoreCase(breed.heatTolerance)) {
                        stressAmount = 1; // Sıcağa dayanıklı (Jersey, Brahman) -> Az Stres
                    } else if ("COLD".equalsIgnoreCase(breed.heatTolerance)) {
                        stressAmount = 4; // Soğuğu seven (Angus, Highlander) -> Çok Stres
                    }
                }

                // ISI STRESİ BAŞLAR
                cow.getHealthSystem().increaseStress(stressAmount); // Stres artar
                hydration -= 0.5f; // Çok hızlı su kaybeder

                // Süt verimi düşer (Metot içinde kontrol ediliyor zaten)

                if (cow.tickCount % 1200 == 0) { // Dakikada bir uyar
                    cow.sendSystemMessage(net.minecraft.network.chat.Component
                            .literal("§6☀ İnek güneş altında bunalıyor! (Isı Stresi)"));
                }
            }
        }
    }

    @SuppressWarnings("null")
    public void feedColostrum() {
        if (!colostrumReceived) {
            this.colostrumReceived = true;
            this.immunity = 100.0f; // Tam bağışıklık
            cow.playSound(SoundEvents.GENERIC_DRINK, 1.0f, 1.0f);
            cow.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("§a✔ Buzağı ağız sütünü içti. (Bağışıklık: %100)"));
        }
    }

    @SuppressWarnings("null")
    public void feedCalfFormula() {
        if (!colostrumReceived) {
            this.colostrumReceived = true; // Artık çöküşe gitmez
            this.immunity = 70.0f; // Mama sadece %70 korur
            cow.playSound(SoundEvents.GENERIC_DRINK, 1.0f, 1.0f);
            cow.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("§e✔ Buzağı mama ile beslendi. (Bağışıklık: %70)"));
        }
    }

    public float getImmunity() {
        return immunity;
    }

    public void setImmunity(float immunity) {
        this.immunity = immunity;
    }

    public void save(CompoundTag tag) {
        tag.putFloat("Meta_RumenPH", rumenPh);
        tag.putFloat("Meta_BCS", bodyConditionScore);
        tag.putFloat("Meta_Fill", rumenFill);
        tag.putInt("Meta_MilkTimer", milkTimer);
        tag.putInt("Meta_Lactation", lactationPeriod);
        tag.putFloat("Meta_Hydration", hydration);
        tag.putFloat("Meta_Gas", methaneGas);
        tag.putFloat("Meta_GenMilk", geneticMilkModifier);
        tag.putFloat("Meta_GenHealth", geneticHealthModifier);
        tag.putInt("Meta_Unmilked", unmilkedTicks);

        // YENİ
        tag.putFloat("Meta_Immunity", immunity);
        tag.putBoolean("Meta_Colostrum", colostrumReceived);
        tag.putInt("Meta_AgeTicks", ageInTicks);
    }

    public void load(CompoundTag tag) {
        if (tag.contains("Meta_RumenPH"))
            rumenPh = tag.getFloat("Meta_RumenPH");
        if (tag.contains("Meta_BCS"))
            bodyConditionScore = tag.getFloat("Meta_BCS");
        if (tag.contains("Meta_Fill"))
            rumenFill = tag.getFloat("Meta_Fill");
        if (tag.contains("Meta_MilkTimer"))
            milkTimer = tag.getInt("Meta_MilkTimer");
        if (tag.contains("Meta_Lactation"))
            lactationPeriod = tag.getInt("Meta_Lactation");
        if (tag.contains("Meta_Hydration"))
            hydration = tag.getFloat("Meta_Hydration");
        if (tag.contains("Meta_Gas"))
            methaneGas = tag.getFloat("Meta_Gas");
        if (tag.contains("Meta_GenMilk"))
            geneticMilkModifier = tag.getFloat("Meta_GenMilk");
        if (tag.contains("Meta_GenHealth"))
            geneticHealthModifier = tag.getFloat("Meta_GenHealth");
        if (tag.contains("Meta_Unmilked"))
            unmilkedTicks = tag.getInt("Meta_Unmilked");

        // YENİ
        if (tag.contains("Meta_Immunity"))
            immunity = tag.getFloat("Meta_Immunity");
        if (tag.contains("Meta_Colostrum"))
            colostrumReceived = tag.getBoolean("Meta_Colostrum");
        if (tag.contains("Meta_AgeTicks"))
            ageInTicks = tag.getInt("Meta_AgeTicks");
    }

    public float getRumenPh() {
        return rumenPh;
    }

    public float getBcs() {
        return bodyConditionScore;
    }

    public float getHydration() {
        return hydration;
    }

    public float getGasLevel() {
        return methaneGas;
    }

    public float getRumenFill() {
        return rumenFill;
    }

    public void setGeneticModifiers(float milk, float health) {
        this.geneticMilkModifier = milk;
        this.geneticHealthModifier = health;
    }

    public float getGeneticMilkModifier() {
        return geneticMilkModifier;
    }

    public float getGeneticHealthModifier() {
        return geneticHealthModifier;
    }

    public void resetMilking() {
        this.unmilkedTicks = 0;
    }

    public void setRumenFill(float value) {
        this.rumenFill = Math.max(0.0f, Math.min(100.0f, value));
        cow.setHunger((int) this.rumenFill);
    }
}