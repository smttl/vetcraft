package com.vetsim.vetcraft.entity.components;

import com.vetsim.vetcraft.entity.CattleEntity;
import com.vetsim.vetcraft.entity.ModEntities;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel; // ModEntities importuna dikkat et (init paketinde olmalı)
import com.vetsim.vetcraft.config.VetCraftConfig;

public class CattleReproduction {
    private final CattleEntity cow;

    private int breedingCooldown = 0;
    private int estrusCycleTimer = 0;
    private float progesteroneLevel = 1.0f;
    private String fatherBreed = "Bilinmiyor";
    // --- Phase 19: Genetik Verisi ---
    private String fatherQuality = "Commercial";
    private float fatherMilkPTA = 0.0f;
    private float fatherHealthPTA = 0.0f;
    // --------------------------------

    public CattleReproduction(CattleEntity cow) {
        this.cow = cow;
    }

    public void tick() {
        if (cow.level().isClientSide)
            return;

        // Sayaç her zaman düşmeli
        if (breedingCooldown > 0) {
            breedingCooldown--;
        }

        // --- CİNSİYET KONTROLÜ ---
        // Erkeklerin östrus döngüsü olmaz.
        if (cow.isMale()) {
            this.estrusCycleTimer = 0;
            this.progesteroneLevel = 0.5f; // Düşük tut
            return;
        }

        // DURUM 1: NORMAL DÖNGÜ (Gebe Değil, Bebek Değil, Lohusa Değil)
        if (!cow.isPregnant() && !cow.isBaby() && breedingCooldown <= 0) {
            estrusCycleTimer++;
            // DÜZELTME: Döngü süresini uzattık (2400 -> 3600).
            // Böylece "Kızgınlık" (Heat) süresi 1800'den 3600'e kadar sürecek (1.5 dakika).
            if (estrusCycleTimer >= VetCraftConfig.ESTRUS_CYCLE_LENGTH)
                estrusCycleTimer = 0;

            // Östrus (Kızgınlık) Dönemi: 1800 - 2400 arası (Yaklaşık 30 saniye)
            if (estrusCycleTimer > VetCraftConfig.ESTRUS_WINDOW_START
                    && estrusCycleTimer < VetCraftConfig.ESTRUS_WINDOW_END) {
                progesteroneLevel = Math.max(0.5f, progesteroneLevel - 0.01f);
            } else {
                // Diğer zamanlarda (Diöstrus/Metaöstrus) progesteron yükselir
                progesteroneLevel = Math.min(8.0f, progesteroneLevel + 0.005f); // FIX: 0.02'den 0.005'e düşürüldü (Çok
                                                                                // hızlı artıyordu)
            }
        }
        // DURUM 2: GEBE
        else if (cow.isPregnant()) {
            progesteroneLevel = 8.0f; // Progesteron yüksek

            // Doğum vakti geldi mi?
            if (breedingCooldown <= 0) {
                giveBirth();
            }
        }
        // DURUM 3: LOHUSA (Doğum Sonrası Dinlenme)
        else if (breedingCooldown > 0) {
            progesteroneLevel = 5.0f; // Güvenli bölge (Sakin)
        }
    }

    // Phase 19: Overloaded tryInseminate for Genetics
    public boolean tryInseminate(String fatherBreed, String quality, float milkPTA, float healthPTA) {
        // Cinsiyet Kontrolü
        if (cow.isMale())
            return false;

        // 1. HORMON KONTROLÜ
        // Kızgınlıkta değilse (Progesteron yüksekse) tutmaz
        if (progesteroneLevel > 2.5f || breedingCooldown > 0)
            return false;

        // 2. FİZYOLOJİK KONTROL (BCS - Vücut Kondisyonu)
        // Çok zayıf (2.5 altı) veya çok şişman (4.5 üstü) hayvanlar gebe kalamaz.
        float bcs = cow.getMetabolismSystem().getBcs();
        if (bcs < 2.5f || bcs > 4.5f)
            return false;

        // 3. STRES KONTROLÜ
        // Yüksek stres (%60 üstü) kortizol salgılar, döllenmeyi engeller.
        if (cow.getHealthSystem().getStressLevel() > 60)
            return false;

        cow.setPregnant(true);
        this.fatherBreed = fatherBreed;
        // Genetik veriyi kaydet
        this.fatherQuality = quality;
        this.fatherMilkPTA = milkPTA;
        this.fatherHealthPTA = healthPTA;

        this.breedingCooldown = CattleEntity.GESTATION_PERIOD; // Gebelik süresi başlat
        return true;
    }

    // Eski metod uyumluluğu (Varsayılan genetik ile)
    public boolean tryInseminate(String fatherBreed, float placeholderQuality) {
        return tryInseminate(fatherBreed, "Commercial", 0.0f, 0.0f);
    }

    public void startPregnancy(String fatherBreed) {
        if (cow.isMale())
            return; // Güvenlik
        cow.setPregnant(true);
        this.fatherBreed = fatherBreed;
        this.breedingCooldown = CattleEntity.GESTATION_PERIOD;
    }

    @SuppressWarnings("null")
    private void giveBirth() {
        if (cow.level() instanceof ServerLevel serverLevel) {
            CattleEntity baby = ModEntities.CATTLE.get().create(serverLevel);
            if (baby != null) {
                String babyBreed = cow.getRandom().nextBoolean() ? cow.getBreed() : this.fatherBreed;
                baby.setBreed(babyBreed);

                // Phase 19: GENETİK MİRAS (Genetic Inheritance)
                // Anne Genetiği
                float momMilk = cow.getMetabolismSystem().getGeneticMilkModifier();
                float momHealth = cow.getMetabolismSystem().getGeneticHealthModifier();
                float momImmunity = cow.getMetabolismSystem().getImmunity(); // Anne bağışıklığı

                // Yavru Genetiği = (Anne + Baba) / 2 + Varyasyon
                // Varyasyon: -0.2 ile +0.2 arası rastgele sapma
                float variationMilk = (cow.getRandom().nextFloat() - 0.5f) * 0.4f;
                float variationHealth = (cow.getRandom().nextFloat() - 0.5f) * 0.2f;

                // Bağışıklık Varyasyonu (0.0 - 10.0 arası)
                float variationImmunity = (cow.getRandom().nextFloat() - 0.5f) * 20.0f;

                float babyMilk = (momMilk + this.fatherMilkPTA) / 2.0f + variationMilk;
                float babyHealth = (momHealth + this.fatherHealthPTA) / 2.0f + variationHealth;
                // Bağışıklık, annenin yarısı + rastgele faktör (Başlangıç seviyesi)
                float babyImmunity = (momImmunity * 0.5f) + variationImmunity;
                babyImmunity = Math.max(0, Math.min(100, babyImmunity));

                // Yavruya işle
                baby.getMetabolismSystem().setGeneticModifiers(babyMilk, babyHealth);
                baby.getMetabolismSystem().setImmunity(babyImmunity);

                // Bebek Küpesi
                String newTag = "TR" + (cow.getRandom().nextInt(900000) + 100000);
                baby.setEarTag(newTag);

                // 3.8.5 FIX: Yavru, annenin sahibini miras alır
                if (cow.getOwnerUUID().isPresent()) {
                    baby.setOwnerUUID(cow.getOwnerUUID().get());
                }

                baby.setWeight(30.0F);
                baby.setAge(-6000); // Bebek olarak doğar
                baby.moveTo(cow.getX(), cow.getY(), cow.getZ(), 0.0F, 0.0F);
                serverLevel.addFreshEntity(baby);

                // --- 5. AĞIZ SÜTÜ (COLOSTRUM) DÜŞÜR ---
                // Yeni doğan buzağı için hayat kurtarıcı
                cow.spawnAtLocation(com.vetsim.vetcraft.init.ModItems.COLOSTRUM_BUCKET.get());
                cow.sendSystemMessage(net.minecraft.network.chat.Component
                        .literal("§d💕 Doğum gerçekleşti! Yere Ağız Sütü (Colostrum) düştü."));

                // --- DOĞUM SONRASI AYARLAR ---
                cow.setPregnant(false);
                this.breedingCooldown = CattleEntity.POST_BIRTH_COOLDOWN; // Lohusalık süresi

                // Hormonları ve Döngüyü RESETLE (Doğurur doğurmaz kızgınlığa girmesin diye)
                this.progesteroneLevel = 5.0f; // Sakin moda al
                this.estrusCycleTimer = 0; // Döngüyü başa sar

                // --- DÜZELTME: LAKTASYONU BAŞLAT ---
                // Artık süt, reprodüksiyon sayacından bağımsız işleyecek!
                cow.getMetabolismSystem().startLactation();
                // -----------------------------------

                cow.setBirthCount(cow.getBirthCount() + 1);
                serverLevel.sendParticles(ParticleTypes.HEART, cow.getX(), cow.getY() + 1, cow.getZ(), 10, 0.5, 0.5,
                        0.5, 0.1);
            }
        }
    }

    // --- ÖNEMLİ METODLAR ---

    // 1. Kuru Dönem Kontrolü (Süt vermeyi kesmek için)
    public boolean isInDryPeriod() {
        if (!cow.isPregnant())
            return false;
        // Doğuma 600 tick (yaklaşık 30 saniye) kala sütü kes
        return this.breedingCooldown < 600;
    }

    // 2. Düşük Yaptırma (Abort)
    public void forceAbortion() {
        if (cow.isPregnant()) {
            cow.setPregnant(false);
            if (!cow.level().isClientSide) {
                com.vetsim.vetcraft.VetCraft.LOGGER.warn(
                        "Cattle ABORTED (Forced/Hormonal): ID=" + cow.getEarTag() + ", Pos=" + cow.blockPosition());
                cow.broadcastToPlayers(net.minecraft.network.chat.Component
                        .literal("§c⚠ " + cow.getEarTag() + " düşük yaptı! (Hormonal Sebepler)"));
            }
            this.fatherBreed = "Melez"; // DÜZELTME: Gereksiz metod çağrısı yerine doğrudan atama yapıldı.

            // Genetiği sıfırla
            this.fatherQuality = "Commercial";
            this.fatherMilkPTA = 0.0f;
            this.fatherHealthPTA = 0.0f;

            // this.progesteroneLevel = 0.5f; // GEREKSİZ: tick() metodunda breedingCooldown
            // > 0 olduğu için 5.0f'e (koruma modu) çekilecek.
            this.breedingCooldown = 24000; // 1 gün dinlenme
            this.estrusCycleTimer = 0;

            cow.getHealthSystem().increaseStress(40);
            cow.getMetabolismSystem().reduceBcs(0.2f);

            cow.playSound(net.minecraft.sounds.SoundEvents.COW_HURT, 1.0F, 0.5F);
            if (cow.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                        cow.getX(), cow.getY() + 1.0, cow.getZ(), 10, 0.5, 0.5, 0.5, 0.0);
            }
        }
    }

    // 3. Kızgınlık Başlatma (Induce Estrus)
    public void induceEstrus() {
        if (!cow.isPregnant() && !cow.isBaby()) {
            // GÜNCELLEME: Sayacı kızgınlığın BAŞINA (1850) alıyoruz ki oyuncunun tohumlamak
            // için süresi olsun.
            // Eskiden 2200 yapılıyordu, döngü hemen bitip hormonlar yükseliyordu (Sadece 10
            // saniye vardı).
            this.estrusCycleTimer = VetCraftConfig.HORMONE_INDUCED_ESTRUS_TIME;
            this.progesteroneLevel = 0.5f; // Hormonu düşür (Garanti olması için 0.5f)
            this.breedingCooldown = 0; // Lohusalığı iptal et

            cow.playSound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
            if (cow.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HEART,
                        cow.getX(), cow.getY() + 1.0, cow.getZ(), 5, 0.5, 0.5, 0.5, 0.0);
            }
        }
    }

    public void save(CompoundTag tag) {
        tag.putInt("Repro_Cooldown", breedingCooldown);
        tag.putInt("Repro_EstrusTimer", estrusCycleTimer);
        tag.putFloat("Repro_Progesterone", progesteroneLevel);
        tag.putString("Repro_Father", fatherBreed);
        // Phase 19 Save
        tag.putString("Repro_F_Quality", fatherQuality);
        tag.putFloat("Repro_F_Milk", fatherMilkPTA);
        tag.putFloat("Repro_F_Health", fatherHealthPTA);
    }

    public void load(CompoundTag tag) {
        if (tag.contains("Repro_Cooldown"))
            breedingCooldown = tag.getInt("Repro_Cooldown");
        if (tag.contains("Repro_EstrusTimer"))
            estrusCycleTimer = tag.getInt("Repro_EstrusTimer");
        if (tag.contains("Repro_Progesterone"))
            progesteroneLevel = tag.getFloat("Repro_Progesterone");
        if (tag.contains("Repro_Father"))
            fatherBreed = tag.getString("Repro_Father");
        // Phase 19 Load
        if (tag.contains("Repro_F_Quality"))
            fatherQuality = tag.getString("Repro_F_Quality");
        if (tag.contains("Repro_F_Milk"))
            fatherMilkPTA = tag.getFloat("Repro_F_Milk");
        if (tag.contains("Repro_F_Health"))
            fatherHealthPTA = tag.getFloat("Repro_F_Health");
    }

    public float getProgesterone() {
        return progesteroneLevel;
    }

    public int getBreedingCooldown() {
        return breedingCooldown;
    }

    public int getEstrusCycleTimer() {
        return estrusCycleTimer;
    }

    public void setBreedingCooldown(int cd) {
        this.breedingCooldown = cd;
    }

    public void setEstrusCycleTimer(int timer) {
        this.estrusCycleTimer = timer;
    }
}