package com.vetsim.vetcraft.entity.components;

import com.vetsim.vetcraft.entity.CattleEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import com.vetsim.vetcraft.entity.ModEntities; // ModEntities importuna dikkat et (init paketinde olmalı)

public class CattleReproduction {
    private final CattleEntity cow;

    private int breedingCooldown = 0;
    private int estrusCycleTimer = 0;
    private float progesteroneLevel = 1.0f;
    private String fatherBreed = "Bilinmiyor";

    public CattleReproduction(CattleEntity cow) {
        this.cow = cow;
    }

    public void tick() {
        if (cow.level().isClientSide) return;

        // Sayaç her zaman düşmeli
        if (breedingCooldown > 0) {
            breedingCooldown--;
        }

        // DURUM 1: NORMAL DÖNGÜ (Gebe Değil, Bebek Değil, Lohusa Değil)
        if (!cow.isPregnant() && !cow.isBaby() && breedingCooldown <= 0) {
            estrusCycleTimer++;
            if (estrusCycleTimer >= 12000) estrusCycleTimer = 0;

            // Östrus (Kızgınlık) Dönemi: Döngünün sonları
            if (estrusCycleTimer > 9600) {
                progesteroneLevel = Math.max(0.5f, progesteroneLevel - 0.05f);
            } else {
                progesteroneLevel = Math.min(8.0f, progesteroneLevel + 0.1f);
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

    public boolean tryInseminate(String fatherBreed, float quality) {
        // Kızgınlıkta değilse (Progesteron yüksekse) tutmaz
        if (progesteroneLevel > 2.5f) return false;

        cow.setPregnant(true);
        this.fatherBreed = fatherBreed;
        this.breedingCooldown = CattleEntity.GESTATION_PERIOD; // Gebelik süresi başlat
        return true;
    }

    public void startPregnancy(String fatherBreed) {
        cow.setPregnant(true);
        this.fatherBreed = fatherBreed;
        this.breedingCooldown = CattleEntity.GESTATION_PERIOD;
    }

    private void giveBirth() {
        if (cow.level() instanceof ServerLevel serverLevel) {
            CattleEntity baby = ModEntities.CATTLE.get().create(serverLevel);
            if (baby != null) {
                String babyBreed = cow.getRandom().nextBoolean() ? cow.getBreed() : this.fatherBreed;
                baby.setBreed(babyBreed);

                // Bebek Küpesi
                String newTag = "TR" + (cow.getRandom().nextInt(900000) + 100000);
                baby.setEarTag(newTag);

                baby.setWeight(30.0F);
                baby.setAge(-24000); // Bebek olarak doğar
                baby.moveTo(cow.getX(), cow.getY(), cow.getZ(), 0.0F, 0.0F);
                serverLevel.addFreshEntity(baby);

                // --- DOĞUM SONRASI AYARLAR ---
                cow.setPregnant(false);
                this.breedingCooldown = CattleEntity.POST_BIRTH_COOLDOWN; // Lohusalık süresi

                // Hormonları ve Döngüyü RESETLE (Doğurur doğurmaz kızgınlığa girmesin diye)
                this.progesteroneLevel = 5.0f; // Sakin moda al
                this.estrusCycleTimer = 0;     // Döngüyü başa sar

                // --- DÜZELTME: LAKTASYONU BAŞLAT ---
                // Artık süt, reprodüksiyon sayacından bağımsız işleyecek!
                cow.getMetabolismSystem().startLactation();
                // -----------------------------------

                cow.setBirthCount(cow.getBirthCount() + 1);
                serverLevel.sendParticles(ParticleTypes.HEART, cow.getX(), cow.getY()+1, cow.getZ(), 10, 0.5, 0.5, 0.5, 0.1);
            }
        }
    }

    // --- ÖNEMLİ METODLAR ---

    // 1. Kuru Dönem Kontrolü (Süt vermeyi kesmek için)
    public boolean isInDryPeriod() {
        if (!cow.isPregnant()) return false;
        // Doğuma 600 tick (yaklaşık 30 saniye) kala sütü kes
        return this.breedingCooldown < 600;
    }

    // 2. Düşük Yaptırma (Abort)
    public void forceAbortion() {
        if (cow.isPregnant()) {
            cow.setPregnant(false);
            cow.startPregnancy("Melez"); // Genetiği sıfırla (teknik reset)
            cow.setPregnant(false); // Tekrar false yap

            this.progesteroneLevel = 0.5f;
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
            this.estrusCycleTimer = 10000; // Döngüyü sona sar (Kızgınlığa getir)
            this.progesteroneLevel = 1.0f; // Hormonu düşür
            this.breedingCooldown = 0;     // Lohusalığı iptal et

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
    }

    public void load(CompoundTag tag) {
        if(tag.contains("Repro_Cooldown")) breedingCooldown = tag.getInt("Repro_Cooldown");
        if(tag.contains("Repro_EstrusTimer")) estrusCycleTimer = tag.getInt("Repro_EstrusTimer");
        if(tag.contains("Repro_Progesterone")) progesteroneLevel = tag.getFloat("Repro_Progesterone");
        if(tag.contains("Repro_Father")) fatherBreed = tag.getString("Repro_Father");
    }

    public float getProgesterone() { return progesteroneLevel; }
    public int getBreedingCooldown() { return breedingCooldown; }
    public void setBreedingCooldown(int cd) { this.breedingCooldown = cd; }
}