package com.vetsim.vetcraft.entity.components;

import com.vetsim.vetcraft.entity.CattleEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import com.vetsim.vetcraft.entity.ModEntities;

public class CattleReproduction {
    private final CattleEntity cow;

    private int recoveryTimer = 0;   // Doğum sonrası veya düşük sonrası iyileşme süresi
    private int gestationTimer = 0;  // Gebelik süresi sayacı
    private int estrusCycleTimer = 0;
    private float progesteroneLevel = 1.0f;
    private String fatherBreed = "Bilinmiyor";

    public CattleReproduction(CattleEntity cow) {
        this.cow = cow;
    }

    public void tick() {
        if (cow.level().isClientSide) return;

        // 1. İyileşme (Recovery) Sayacı
        if (recoveryTimer > 0) {
            recoveryTimer--;
        }

        // 2. GEBELİK DURUMU
        if (cow.isPregnant()) {
            progesteroneLevel = 8.0f; // Gebelikte progesteron yüksek kalır

            if (gestationTimer > 0) {
                gestationTimer--;
            } else {
                giveBirth();
            }
        }
        // 3. NORMAL DÖNGÜ (Gebe Değil, Bebek Değil, İyileşme Sürecinde Değil)
        else if (!cow.isBaby() && recoveryTimer <= 0) {
            estrusCycleTimer++;
            if (estrusCycleTimer >= 12000) estrusCycleTimer = 0;

            // Östrus (Kızgınlık) Dönemi: Döngünün sonları (9600 - 12000 arası)
            if (estrusCycleTimer > 9600) {
                // Kızgınlıkta progesteron düşer (Östradiol artar)
                progesteroneLevel = Math.max(0.5f, progesteroneLevel - 0.05f);
            } else {
                // Diöstrus (Sarı Cisim) evresi: Progesteron yüksek
                progesteroneLevel = Math.min(8.0f, progesteroneLevel + 0.1f);
            }
        }
        // 4. LOHUSA / İYİLEŞME
        else if (recoveryTimer > 0) {
            progesteroneLevel = 5.0f; // Güvenli, sakin seviye
        }
    }

    public boolean tryInseminate(String fatherBreed, float quality) {
        // Kızgınlıkta değilse (Progesteron yüksekse) tutmaz
        if (progesteroneLevel > 2.5f) return false;

        startPregnancy(fatherBreed);
        return true;
    }

    public void startPregnancy(String fatherBreed) {
        cow.setPregnant(true);
        this.fatherBreed = fatherBreed;
        this.gestationTimer = CattleEntity.GESTATION_PERIOD; // Gebelik süresini başlat
        this.recoveryTimer = 0; // İyileşme süresini sıfırla
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
                this.gestationTimer = 0;
                this.recoveryTimer = CattleEntity.POST_BIRTH_COOLDOWN; // Lohusalık süresi başlar

                // Hormonları ve Döngüyü RESETLE
                this.progesteroneLevel = 5.0f; // Sakin mod
                this.estrusCycleTimer = 0;     // Döngü başa döner

                // Laktasyonu Başlat
                cow.getMetabolismSystem().startLactation();

                cow.setBirthCount(cow.getBirthCount() + 1);
                serverLevel.sendParticles(ParticleTypes.HEART, cow.getX(), cow.getY()+1, cow.getZ(), 10, 0.5, 0.5, 0.5, 0.1);
            }
        }
    }

    // --- ÖNEMLİ METODLAR ---

    public boolean isInDryPeriod() {
        if (!cow.isPregnant()) return false;
        // Doğuma 600 tick kala sütü kes (gestationTimer kontrolü)
        return this.gestationTimer < 600;
    }

    public void forceAbortion() {
        if (cow.isPregnant()) {
            cow.setPregnant(false);
            this.gestationTimer = 0;
            this.recoveryTimer = 24000; // 1 gün iyileşme/ceza
            this.estrusCycleTimer = 0;
            this.progesteroneLevel = 0.5f;

            cow.getHealthSystem().increaseStress(40);
            cow.getMetabolismSystem().reduceBcs(0.2f);

            cow.playSound(net.minecraft.sounds.SoundEvents.COW_HURT, 1.0F, 0.5F);
            if (cow.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                        cow.getX(), cow.getY() + 1.0, cow.getZ(), 10, 0.5, 0.5, 0.5, 0.0);
            }
        }
    }

    public void induceEstrus() {
        if (!cow.isPregnant() && !cow.isBaby()) {
            this.estrusCycleTimer = 10000; // Döngüyü sona sar (Kızgınlığa getir)
            this.progesteroneLevel = 1.0f; // Hormonu düşür
            this.recoveryTimer = 0;        // Lohusalığı iptal et

            cow.playSound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
            if (cow.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HEART,
                        cow.getX(), cow.getY() + 1.0, cow.getZ(), 5, 0.5, 0.5, 0.5, 0.0);
            }
        }
    }

    public void save(CompoundTag tag) {
        tag.putInt("Repro_RecoveryTimer", recoveryTimer);
        tag.putInt("Repro_GestationTimer", gestationTimer);
        tag.putInt("Repro_EstrusTimer", estrusCycleTimer);
        tag.putFloat("Repro_Progesterone", progesteroneLevel);
        tag.putString("Repro_Father", fatherBreed);
    }

    public void load(CompoundTag tag) {
        // Eski sürüm uyumluluğu (Migration)
        int legacyCooldown = 0;
        if(tag.contains("Repro_Cooldown")) legacyCooldown = tag.getInt("Repro_Cooldown");

        if(tag.contains("Repro_RecoveryTimer")) {
            recoveryTimer = tag.getInt("Repro_RecoveryTimer");
        } else {
            recoveryTimer = legacyCooldown;
        }

        if(tag.contains("Repro_GestationTimer")) {
            gestationTimer = tag.getInt("Repro_GestationTimer");
        } else {
            // Eğer yeni veri yoksa ama hayvan hamileyse, eski cooldown aslında gestationTimer'dır.
            if (cow.isPregnant()) {
                gestationTimer = legacyCooldown;
                recoveryTimer = 0;
            }
        }

        if(tag.contains("Repro_EstrusTimer")) estrusCycleTimer = tag.getInt("Repro_EstrusTimer");
        if(tag.contains("Repro_Progesterone")) progesteroneLevel = tag.getFloat("Repro_Progesterone");
        if(tag.contains("Repro_Father")) fatherBreed = tag.getString("Repro_Father");
    }

    public float getProgesterone() { return progesteroneLevel; }

    // API Uyumluluğu için (CattleEntity ve NaturalBreedingGoal kullanıyor)
    public int getBreedingCooldown() { return recoveryTimer; }
    public void setBreedingCooldown(int cd) { this.recoveryTimer = cd; }
}