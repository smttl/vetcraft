package com.vetsim.vetcraft.entity.components;

import com.vetsim.vetcraft.entity.CattleEntity;
import com.vetsim.vetcraft.util.DiseaseData;
import com.vetsim.vetcraft.util.DiseaseManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import java.util.List;

public class CattleHealth {
    private final CattleEntity cow;
    private int stressLevel = 0;
    private float immunity = 100.0f;
    private float hoofHealth = 100.0f;
    private int recoveryTimer = 0;

    // --- EKSİK OLAN DEĞİŞKEN ---
    private int treatmentStep = 0; // Kaçıncı ilaçta? (0: Hiçbiri)
    // ---------------------------

    public CattleHealth(CattleEntity cow) {
        this.cow = cow;
    }

    public void tick() {
        if (cow.level().isClientSide) return;

        String currentDisease = cow.getDisease();

        if (recoveryTimer > 0) {
            recoveryTimer--;

            // İyileşme süresince hastalık hasarı VERMEZ ama hastalık hala "görünür"
            if (recoveryTimer == 0) {
                // Süre bitti, artık tamamen temiz
                setDisease("NONE");
                cow.playSound(net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP, 1.0F, 1.0F);
                cow.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a ✅ Hayvan tamamen sağlığına kavuştu!"));
            }
            return; // Aşağıdaki hastalık hasar kodlarını çalıştırma
        }

        if (!currentDisease.equals("NONE")) {
            DiseaseData data = DiseaseManager.getDiseaseById(currentDisease);

            if (data != null) {
                // 1. CAN YAKMA
                if (data.damagePerTick > 0 && cow.tickCount % 100 == 0) {
                    cow.hurt(cow.damageSources().starve(), (float)data.damagePerTick);
                }

                // 2. BULAŞTIRMA
                if (data.contagious && cow.tickCount % 200 == 0) {
                    spreadDisease(currentDisease);
                }

                // 3. ABORT RİSKİ
                if (cow.isPregnant() && data.abortChance > 0) {
                    if (cow.tickCount % 1200 == 0 && cow.getRandom().nextDouble() < data.abortChance) {
                        cow.startPregnancy("Melez");
                        cow.setPregnant(false);
                        cow.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c⚠ Hastalık nedeniyle düşük yaptı!"));
                    }
                }

                // 4. KONDİSYON KAYBI
                if (data.bcsLoss > 0) {
                    cow.getMetabolismSystem().reduceBcs((float) data.bcsLoss);
                }
            }

            increaseStress(1);
        } else {
            if (stressLevel > 0 && cow.tickCount % 20 == 0) {
                stressLevel--;
            }
        }

        if (cow.getDeltaMovement().lengthSqr() > 0.001) {
            hoofHealth -= 0.005f;
        }

        stressLevel = Math.min(100, stressLevel);
    }

    private void spreadDisease(String diseaseId) {
        List<CattleEntity> nearby = cow.level().getEntitiesOfClass(CattleEntity.class, cow.getBoundingBox().inflate(3.0D));
        for (CattleEntity nearbyCow : nearby) {
            float infectionChance = 0.15F + (nearbyCow.getHealthSystem().getStressLevel() * 0.005F);

            if (nearbyCow != cow && nearbyCow.getDisease().equals("NONE") && cow.getRandom().nextFloat() < infectionChance) {
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

    public int getTreatmentStep() {
        return treatmentStep;
    }

    public void advanceTreatment() {
        this.treatmentStep++;
    }

    public void resetTreatment() {
        this.treatmentStep = 0;
    }

    // -----------------------------------

    public void increaseStress(int amount) {
        this.stressLevel = Math.min(100, this.stressLevel + amount);
    }

    public void setDisease(String id) {
        cow.setDisease(id);
        resetTreatment(); // Yeni hastalık gelince tedavi sıfırlanır
    }

    public int getStressLevel() { return stressLevel; }
    public boolean isLame() { return hoofHealth < 20.0f; }

    public void save(CompoundTag tag) {
        tag.putInt("Health_Stress", stressLevel);
        tag.putFloat("Health_Immunity", immunity);
        tag.putFloat("Health_Hoof", hoofHealth);
        tag.putInt("Health_TreatmentStep", treatmentStep); // Kaydet
        tag.putInt("Health_Recovery", recoveryTimer);
    }

    public void load(CompoundTag tag) {
        if(tag.contains("Health_Stress")) stressLevel = tag.getInt("Health_Stress");
        if(tag.contains("Health_Immunity")) immunity = tag.getFloat("Health_Immunity");
        if(tag.contains("Health_Hoof")) hoofHealth = tag.getFloat("Health_Hoof");
        if(tag.contains("Health_TreatmentStep")) treatmentStep = tag.getInt("Health_TreatmentStep"); // Yükle
        if(tag.contains("Health_Recovery")) recoveryTimer = tag.getInt("Health_Recovery");
    }
    public void startRecovery(int ticks) {
        this.recoveryTimer = ticks;
        // Tedavi sayacını sıfırla ki tekrar ilaç verilmesin
        this.resetTreatment();
    }

    // Yeni Getter (Entity tarafında kontrol için)
    public boolean isRecovering() {
        return recoveryTimer > 0;
    }
}