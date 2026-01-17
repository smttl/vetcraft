package com.vetsim.vetcraft.util;

import com.vetsim.vetcraft.entity.CattleEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class VetDiagnostics {

    // Metodu static yaptık, böylece "new VetDiagnostics()" demeye gerek kalmadan
    // çağırabiliriz.
    // Parametre olarak "hangi inek" (cow) ve "hangi oyuncu" (player) olduğunu
    // alıyoruz.
    public static void showVetInfo(CattleEntity cow, Player player) {
        if (cow.level().isClientSide)
            return;

        // Collect Data
        String ownerInfo = cow.getOwnerUUID().isPresent() ? cow.getOwnerUUID().get().toString().substring(0, 8) + "..."
                : "Yok";
        String genderIcon = cow.isMale() ? "Erkek" : "Dişi";

        String ageInfo;
        if (cow.isBaby()) {
            ageInfo = "Yavru";
        } else {
            int days = cow.getAgeDays();
            // 20+ gün "Yaşlı" olarak kabul edilebilir (Opsiyonel)
            ageInfo = "Yetişkin (" + days + " Gün)";
        }

        // Male Safety Checks
        boolean isMale = cow.isMale();
        boolean isPregnant = isMale ? false : cow.isPregnant();
        float progesterone = isMale ? 0.0f : cow.getReproductionSystem().getProgesterone();
        int estrusTimer = isMale ? 0 : cow.getReproductionSystem().getEstrusCycleTimer();
        boolean isLactating = isMale ? false : cow.getMetabolismSystem().isLactating();
        boolean isDry = isMale ? false : cow.getReproductionSystem().isInDryPeriod();

        com.vetsim.vetcraft.network.VetDiagnosisPacket packet = new com.vetsim.vetcraft.network.VetDiagnosisPacket(
                cow.getId(),
                cow.getEarTag(),
                ageInfo,
                cow.getBreed(),
                genderIcon,
                ownerInfo,
                cow.getMetabolismSystem().getBcs(),
                cow.getMetabolismSystem().getRumenFill(),
                cow.getMetabolismSystem().getRumenPh(),
                cow.getMetabolismSystem().getHydration(),
                cow.getHealthSystem().getStressLevel(),
                com.vetsim.vetcraft.util.DiseaseManager.getDiseaseName(cow.getDisease()),
                com.vetsim.vetcraft.util.DiseaseManager.getDiseaseName(cow.getSecondaryDisease()),
                isPregnant,
                progesterone,
                cow.getBreedingCooldown(),
                estrusTimer,
                isLactating,
                isDry,
                cow.getWeight());

        // Send Packet to Player
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            net.neoforged.neoforge.network.PacketDistributor.PLAYER.with(serverPlayer).send(packet);
        }
    }
}
