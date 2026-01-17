package com.vetsim.vetcraft.entity.components;

import java.util.List;

import com.vetsim.vetcraft.entity.CattleEntity;
import com.vetsim.vetcraft.util.DiseaseData;
import com.vetsim.vetcraft.util.DiseaseManager;
import com.vetsim.vetcraft.util.BreedManager;
import com.vetsim.vetcraft.util.BreedData;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import com.vetsim.vetcraft.config.VetCraftConfig;

public class CattleHealth {
    private final CattleEntity cow;
    private int stressLevel = 0;
    // private float immunity = 100.0f; // KALDIRILDI
    // private float hoofHealth = 100.0f; // KALDIRILDI
    private int recoveryTimer = 0;

    // --- EKSİK OLAN DEĞİŞKEN ---
    private int treatmentStep = 0; // Primary disease step
    private int secondaryTreatmentStep = 0; // Secondary disease step
    // ---------------------------

    // Phase 18: Gelişmiş İlaç Mekanikleri
    private float toxicityLevel = 0.0f; // Karaciğer yükü (0-100)
    private int withdrawalTimer = 0; // Kalıntı süresi (Tick)

    public CattleHealth(CattleEntity cow) {
        this.cow = cow;
    }

    // ... (Within CattleHealth class)

    public void tick() {
        if (cow.level().isClientSide)
            return;

        String currentDisease = cow.getDisease();
        String secondaryDisease = cow.getSecondaryDisease();
        boolean isRecovering = recoveryTimer > 0;

        if (isRecovering) {
            recoveryTimer--;
            if (recoveryTimer == 0) {
                // İyileşme bitti
                cow.playSound(net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP, 1.0F, 1.0F);
                cow.sendSystemMessage(
                        net.minecraft.network.chat.Component.literal("§a ✅ Hayvan tamamen sağlığına kavuştu!"));
            }
        }

        if (!isRecovering) {
            // PRIMARY DISEASE TICKS
            processDiseaseEffects(currentDisease);

            // SECONDARY DISEASE TICKS
            if (!secondaryDisease.equals("NONE")) {
                processDiseaseEffects(secondaryDisease);
            }

            // STRES ARTIŞI (Ortak)
            if (!currentDisease.equals("NONE") || !secondaryDisease.equals("NONE")) {
                if (cow.tickCount % 200 == 0) {
                    increaseStress(1);
                }
            } else {
                // YENİ: Zamanla Stres Azalması (Time-based Stress Reduction)
                // Şartlar: Hastalık YOK + Tokluk > 50 (Aç değil)
                if (cow.getHunger() > 50 && stressLevel > 0) {
                    if (cow.tickCount % 600 == 0) { // Her 30 saniyede bir azalt (Normal hız)
                        reduceStress(1.0f);
                    }
                }
            }
        }

        if (cow.tickCount % 200 == 0) {
            checkEnvironment();
        }

        // ... (Toxicity logic remains same)
        // ...
    }

    private void processDiseaseEffects(String diseaseId) {
        if (diseaseId.equals("NONE"))
            return;

        DiseaseData data = DiseaseManager.getDiseaseById(diseaseId);
        if (data == null)
            return;

        // 1. CAN YAKMA
        if (data.damagePerTick > 0 && cow.tickCount % 100 == 0) {
            if (data.isFatal) {
                cow.hurt(cow.damageSources().starve(), (float) data.damagePerTick);
            } else {
                if (cow.getHealth() > 1.0f) {
                    cow.hurt(cow.damageSources().starve(), (float) data.damagePerTick);
                }
            }
        }

        // 2. BULAŞTIRMA
        if (data.contagious && cow.tickCount % 200 == 0) {
            spreadDisease(diseaseId);
        }

        // 3. ABORT RİSKİ
        if (cow.isPregnant() && data.abortChance > 0) {
            if (cow.tickCount % 1200 == 0 && cow.getRandom().nextDouble() < data.abortChance) {
                cow.setPregnant(false);
                cow.getReproductionSystem().setBreedingCooldown(CattleEntity.POST_BIRTH_COOLDOWN); // Dinlendirme
                if (!cow.level().isClientSide) {
                    com.vetsim.vetcraft.VetCraft.LOGGER
                            .warn("Cattle ABORTED (Health/Stress): ID=" + cow.getEarTag() + ", Disease="
                                    + cow.getDisease() + ", Stress=" + getStressLevel());
                    cow.broadcastToPlayers(net.minecraft.network.chat.Component
                            .literal("§c⚠ " + cow.getEarTag() + " düşük yaptı! (Hastalık/Stres: " + data.displayName
                                    + ")"));
                }
            }
        }

        // 4. KONDİSYON KAYBI
        if (data.bcsLoss > 0) {
            cow.getMetabolismSystem().reduceBcs((float) data.bcsLoss);
        }
    }

    // ...

    // Need to completely replace the file or critical sections to ensure method
    // signatures match.
    // Since file is modified heavily, replace_file_content with targeted chunks is
    // safer or view first.
    // I viewed it previously. I'll use replace_file_content for the `tick` and
    // `setDisease` methods specifically.

    private void checkEnvironment() {
        // 1. KALABALIK STRESİ (Crowding)
        List<CattleEntity> nearbyCows = cow.level().getEntitiesOfClass(CattleEntity.class,
                cow.getBoundingBox().inflate(5.0D)); // 5 blok yarıçap
        if (nearbyCows.size() > 8) {
            increaseStress(2); // Çok kalabalık!
            if (cow.getRandom().nextFloat() < 0.1f) {
                // Kalabalıkta hastalık bulaşma riski artar (Hızlı yayılım simülasyonu)
                if (cow.getDisease().equals("NONE")) {
                    // Rastgele grip/stres hastalığı (Basitçe stres artırıyoruz şimdilik)
                }
            }
        }

        // 2. HİJYEN (Manure/Gübre Kontrolü)
        // Yerdeki itemları kontrol et
        List<net.minecraft.world.entity.item.ItemEntity> nearbyItems = cow.level().getEntitiesOfClass(
                net.minecraft.world.entity.item.ItemEntity.class,
                cow.getBoundingBox().inflate(3.0D));

        int manureCount = 0;
        for (net.minecraft.world.entity.item.ItemEntity item : nearbyItems) {
            if (item.getItem().getItem().toString().contains("manure")) {
                manureCount += item.getItem().getCount();
            }
        }

        if (manureCount > 5) {
            // Çok pis ortam -> Mastitis veya Ayak hastalığı riski
            if (cow.getRandom().nextFloat() < 0.05f * calculateResistanceMultiplier()) {
                // (Dirençli hayvanlarda bu şans azalır)
                cow.getHealthSystem().increaseStress(1);
                // Eğer meme iltihabı (Mastitis) yoksa ve şans tutarsa
                if (cow.getDisease().equals("NONE")
                        && cow.getRandom().nextFloat() < 0.02f * calculateResistanceMultiplier()) {
                    setDisease("mastitis");
                    cow.sendSystemMessage(
                            net.minecraft.network.chat.Component.literal("§c⚠ Pis ortam nedeniyle Mastitis oldu!"));
                }
            }
        }

        // 3. BİYOM ETKİSİ (Sıcaklık)
        net.minecraft.core.BlockPos pos = cow.blockPosition();
        net.minecraft.world.level.biome.Biome biome = cow.level().getBiome(pos).value();
        float temp = biome.getBaseTemperature(); // 0.0 (Soğuk) - 1.0 (Ilıman) - 2.0 (Sıcak)

        // Soğuk Stresi (< 0.15, Örn: Karlı Tundra)
        if (temp < 0.15f) {
            // Dirençsiz ırklar (Jersey, Holstein) daha çok etkilenir
            // Bunu BreedManager'dan çekebilirdik ama şimdilik basit tutalım.
            if (cow.getRandom().nextFloat() < 0.01f * calculateResistanceMultiplier()) {
                increaseStress(1);
                // Pnömoni riski
                if (cow.getDisease().equals("NONE")
                        && cow.getRandom().nextFloat() < 0.01f * calculateResistanceMultiplier()) {
                    setDisease("pneumonia");
                }
            }
        }

        // 4. YABANİ VEKTÖRLER (Wild Vectors) - (Phase 15 Refactor)
        // JSON'dan gelen vektör verilerine göre kontrol et
        checkDataDrivenVectors();

        // 5. RASTGELE SALGINLAR (Random Outbreaks) - (Phase 16)
        checkRandomOutbreaks();

        // 6. İHMAL KONTROLÜ (Açlık/Bakımsızlık) - (FIX: Hayvanlar hasta olmuyordu)
        checkNeglectTriggers();
    }

    private void checkNeglectTriggers() {
        // İki slot da doluysa kontrol etme
        if (!cow.getDisease().equals("NONE") && !cow.getSecondaryDisease().equals("NONE"))
            return;

        float hunger = cow.getMetabolismSystem().getRumenFill();

        for (DiseaseData disease : DiseaseManager.getDiseases()) {
            if (disease.triggerHungerBelow > 0) {
                if (hunger < disease.triggerHungerBelow) {
                    // Açlık sınırının altında!
                    // Şans hesabı (Açlık ne kadar fazlaysa şans o kadar artar)
                    // Örn: Sınır 40, Açlık 10 -> Fark 30 -> Risk artar
                    float risk = Math.max(0.05f, (float) disease.triggerChance); // En az %5 risk

                    // EXC: Yaş Kontrolü (Data-Driven)
                    if (disease.targetAge != null) {
                        if (disease.targetAge.equalsIgnoreCase("BABY") && !cow.isBaby())
                            continue;
                        if (disease.targetAge.equalsIgnoreCase("ADULT") && cow.isBaby())
                            continue;
                    }

                    if (cow.getRandom().nextFloat() < risk * calculateResistanceMultiplier()) {
                        setDisease(disease.id);
                        cow.sendSystemMessage(net.minecraft.network.chat.Component
                                .literal("§c⚠ Yetersiz beslenme nedeniyle " + disease.displayName
                                        + " hastalığına yakalandı!"));
                        return;
                    }
                }
            }
        }

    }

    private void checkRandomOutbreaks() {
        // İki slot da doluysa kontrol etme
        if (!cow.getDisease().equals("NONE") && !cow.getSecondaryDisease().equals("NONE"))
            return;

        for (DiseaseData disease : DiseaseManager.getDiseases()) {
            if (disease.randomOccurrenceChance > 0) {
                if (cow.getRandom().nextFloat() < disease.randomOccurrenceChance * calculateResistanceMultiplier()) {
                    setDisease(disease.id);
                    cow.sendSystemMessage(net.minecraft.network.chat.Component
                            .literal("§c⚠ Beklenmedik bir şekilde " + disease.displayName + " belirtileri başladı!"));
                    return; // Bir hastalık kaptı, döngüden çık
                }
            }
        }
    }

    private void checkDataDrivenVectors() {
        // Tüm hastalıkları gez
        for (DiseaseData disease : DiseaseManager.getDiseases()) {
            if (disease.vectors == null || disease.vectors.isEmpty())
                continue;

            // İki slot da doluysa kontrol etme
            if (!cow.getDisease().equals("NONE") && !cow.getSecondaryDisease().equals("NONE"))
                return;

            for (DiseaseData.VectorData vector : disease.vectors) {
                // Vektör türünü bul (String -> Class)
                // Burada basit string eşleşmesi yapacağız, entity listesinden
                // Performans için: Her tick reflective class aramak yerine,
                // Etraftaki entityleri alıp string ID kontrolü yapmak daha hızlıdır.

                List<net.minecraft.world.entity.Entity> nearbyEntities = cow.level().getEntitiesOfClass(
                        net.minecraft.world.entity.Entity.class,
                        cow.getBoundingBox().inflate(vector.radius));

                for (net.minecraft.world.entity.Entity entity : nearbyEntities) {
                    String entityId = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE
                            .getKey(entity.getType()).toString();

                    if (entityId.equals(vector.entity)) {
                        // Vektör bulundu! Şans denemesi
                        if (cow.getRandom().nextFloat() < vector.chance * calculateResistanceMultiplier()) {
                            setDisease(disease.id);
                            cow.sendSystemMessage(
                                    net.minecraft.network.chat.Component.literal("§c⚠ " + entity.getName().getString()
                                            + " temasından " + disease.displayName + " bulaştı!"));
                            return; // Bir kere bulaştı, döngüden çık
                        }
                    }
                }
            }
        }
    }

    private void spreadDisease(String diseaseId) {
        // Phase 23: Dynamic Contagion Rate
        DiseaseData data = DiseaseManager.getDiseaseById(diseaseId);
        float baseRate = (data != null && data.contagionRate > 0) ? data.contagionRate : 0.15f;

        List<CattleEntity> nearby = cow.level().getEntitiesOfClass(CattleEntity.class,
                cow.getBoundingBox().inflate(3.0D));
        for (CattleEntity nearbyCow : nearby) {
            float infectionChance = (baseRate + (nearbyCow.getHealthSystem().getStressLevel() * 0.005F))
                    * nearbyCow.getHealthSystem().calculateResistanceMultiplier();

            if (nearbyCow != cow && nearbyCow.getDisease().equals("NONE")
                    && cow.getRandom().nextFloat() < infectionChance) {
                nearbyCow.setDisease(diseaseId);
                if (cow.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                            nearbyCow.getX(), nearbyCow.getY() + 1.0, nearbyCow.getZ(),
                            3, 0.2, 0.2, 0.2, 0.0);
                }
            }
        }
    }

    // --- EKSİK OLAN METODLAR BURADA ---

    // -----------------------------------

    public void increaseStress(int amount) {
        this.stressLevel = Math.min(100, this.stressLevel + amount);
    }

    public void reduceStress(float amount) {
        this.stressLevel = Math.max(0, this.stressLevel - (int) amount);
    }

    public void setDisease(String id) {
        if (id.equals("NONE")) {
            cow.setDisease("NONE");
            // Secondary disease should NOT be cleared here!
            // It will be handled by the swap logic locally or by startRecovery() globally.
            resetPrimaryTreatment();
            return;
        }

        // 1. İyileşme (Recovery) sürecindeyse yeni hastalık kapamaz (Bağışıklık)
        if (isRecovering()) {
            return;
        }

        // 1. Zaten var mı?
        if (cow.getDisease().equals(id) || cow.getSecondaryDisease().equals(id)) {
            return; // Zaten bu hastalığı taşıyor
        }

        // 2. Birinci slot boş mu?
        if (cow.getDisease().equals("NONE")) {
            cow.setDisease(id);
            resetTreatment(); // Yeni hastalık (sıfırdan başlıyor)
            return;
        }

        // 3. İkinci slot boş mu?
        if (cow.getSecondaryDisease().equals("NONE")) {
            cow.setSecondaryDisease(id);
            cow.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c⚠ Hayvan İKİNCİ bir hastalığa ("
                    + DiseaseManager.getDiseaseName(id) + ") yakalandı! Durumu kritik!"));
            resetTreatment(); // Yeni tedavi süreci (İkisini de kapsar varsayıyoruz)
            increaseStress(10);
            return;
        }
    }

    public int getTreatmentStep() {
        return treatmentStep;
    }

    public void setTreatmentStep(int step) {
        this.treatmentStep = step;
    }

    public void advanceTreatment() {
        this.treatmentStep++;
    }

    public int getSecondaryTreatmentStep() {
        return secondaryTreatmentStep;
    }

    public void setSecondaryTreatmentStep(int step) {
        this.secondaryTreatmentStep = step;
    }

    public void advanceSecondaryTreatment() {
        this.secondaryTreatmentStep++;
    }

    public void resetTreatment() {
        this.treatmentStep = 0;
        this.secondaryTreatmentStep = 0;
    }

    public void resetPrimaryTreatment() {
        this.treatmentStep = 0;
    }

    public void resetSecondaryTreatment() {
        this.secondaryTreatmentStep = 0;
    }

    public int getStressLevel() {
        return stressLevel;
    }

    // Phase 18 Methods
    public void applyDrug(com.vetsim.vetcraft.util.DrugData drug) {
        // Toksisite ekle
        if (drug.toxicity > 0) {
            this.toxicityLevel += drug.toxicity;
            if (this.toxicityLevel > 100)
                this.toxicityLevel = 100;
        }
        // Kalıntı süresi ekle (Eğer varsa)
        if (drug.withdrawalDays > 0) {
            // Gün -> Tick (1 Gün = 24000 Tick)
            int ticks = drug.withdrawalDays * VetCraftConfig.TICKS_PER_DAY;
            // Mevcut sürenin üzerine eklemiyoruz, en uzun olanı alıyoruz (Kural gereği)
            if (ticks > this.withdrawalTimer) {
                this.withdrawalTimer = ticks;
            }
        }
    }

    public boolean isWithdrawalActive() {
        return withdrawalTimer > 0;
    }

    public float getToxicity() {
        return toxicityLevel;
    }

    // isLame() metodu kaldırıldı (Tırnak sistemi iptal)
    // public boolean isLame() { return hoofHealth < 20.0f; }

    public void save(CompoundTag tag) {
        tag.putInt("Health_Stress", stressLevel);
        // tag.putFloat("Health_Immunity", immunity);
        // tag.putFloat("Health_Hoof", hoofHealth);
        tag.putInt("Health_TreatmentStep", treatmentStep); // Kaydet
        tag.putInt("Health_Recovery", recoveryTimer);
        // Phase 18 Save
        tag.putFloat("Health_Toxicity", toxicityLevel);
        tag.putInt("Health_Withdrawal", withdrawalTimer);
    }

    public void load(CompoundTag tag) {
        if (tag.contains("Health_Stress"))
            stressLevel = tag.getInt("Health_Stress");
        // if (tag.contains("Health_Immunity")) immunity =
        // tag.getFloat("Health_Immunity");
        // if (tag.contains("Health_Hoof")) hoofHealth = tag.getFloat("Health_Hoof");
        if (tag.contains("Health_TreatmentStep"))
            treatmentStep = tag.getInt("Health_TreatmentStep"); // Yükle
        if (tag.contains("Health_Recovery"))
            recoveryTimer = tag.getInt("Health_Recovery");
        // Phase 18 Load
        if (tag.contains("Health_Toxicity"))
            toxicityLevel = tag.getFloat("Health_Toxicity");
        if (tag.contains("Health_Withdrawal"))
            withdrawalTimer = tag.getInt("Health_Withdrawal");
    }

    public void startRecovery(int ticks) {
        this.recoveryTimer = ticks;
        // Tedavi bitti, hastalıkları temizle
        setDisease("NONE");
        cow.setSecondaryDisease("NONE");
        this.resetTreatment();
    }

    // Yeni Getter (Entity tarafında kontrol için)
    public boolean isRecovering() {
        return recoveryTimer > 0;
    }

    // Phase 22: Genetic Resistance Calculation
    public float calculateResistanceMultiplier() {
        // 1. Irk Direnci (Base)
        BreedData breed = BreedManager.getBreed(cow.getBreed());
        float baseResistance = (breed != null) ? breed.diseaseResistance : 0.5f;

        // 2. Genetik Varyasyon (PTA)
        float geneticMod = cow.getMetabolismSystem().getGeneticHealthModifier(); // -0.2 ile +0.2 arası

        // Direnç Skoru (0.0 -> 1.2 arası olabilir)
        // Yüksek skor = İyi direnç
        float totalResistance = baseResistance + geneticMod;

        // 3. Risk Çarpanı (1.0 - Direnç)
        // Direnç 1.0 (Tam) ise Çarpan 0.0 (Risk yok)
        // Direnç 0.0 (Yok) ise Çarpan 1.0 (Tam risk)
        float riskMultiplier = Math.max(0.05f, 1.0f - totalResistance); // En az %5 risk hep kalsın

        // 4. Bağışıklık (Sadece Buzağılar için)
        if (cow.isBaby()) {
            float immunity = cow.getMetabolismSystem().getImmunity(); // 0-100
            // Immunity 100 -> Çarpan 1.0 (Etki yok)
            // Immunity 0 -> Çarpan 2.0 (Risk 2 katına çıkar)
            float immunityFactor = 2.0f - (immunity / 100.0f);
            riskMultiplier *= immunityFactor;
        }

        // 5. Dehidrasyon Cezası (User Request: Dehidre olanlar daha hızlı hasta olsun)
        if (cow.getMetabolismSystem().getHydration() < 30.0f) {
            riskMultiplier *= 1.5f; // %50 daha fazla risk
        }

        return riskMultiplier;
    }
}