package com.vetsim.vetcraft.util;

import com.vetsim.vetcraft.entity.CattleEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class VetDiagnostics {

    // Metodu static yaptık, böylece "new VetDiagnostics()" demeye gerek kalmadan çağırabiliriz.
    // Parametre olarak "hangi inek" (cow) ve "hangi oyuncu" (player) olduğunu alıyoruz.
    public static void showVetInfo(CattleEntity cow, Player player) {

        // 1. Ekranı Temizle
        for (int i = 0; i < 20; i++) player.sendSystemMessage(Component.literal(""));

        // 2. Başlık
        player.sendSystemMessage(Component.literal("§6§l⭐ VET KAYDI: §b§l" + cow.getEarTag() + " §8§l=================="));

        // 3. Temel Bilgiler (this. yerine cow. kullanıyoruz)
        String genderIcon = cow.isMale() ? "§b♂" : "§d♀";
        player.sendSystemMessage(Component.literal("§e🧬 " + cow.getBreed() + " " + genderIcon + " §f| §e🎂 " + cow.getAgeDays() + " Gün"));

        // 4. Metabolizma Verileri
        // cow.getMetabolismSystem() getter metodunu kullandığından emin ol
        float bcs = cow.getMetabolismSystem().getBcs();
        float rumenPh = cow.getMetabolismSystem().getRumenPh();

        String phStatus = (rumenPh < 5.8) ? " §c(⚠ ASİDOZ)" : (rumenPh > 7.2 ? " §c(⚠ ALKALOZ)" : " §a(✔ Dengeli)");

        player.sendSystemMessage(Component.literal("§e⚖ Kondisyon (BCS): §f" + String.format("%.1f", bcs) + " / 5.0"));
        player.sendSystemMessage(Component.literal("§e🧪 Rumen pH: §f" + String.format("%.1f", rumenPh) + phStatus));

        // 5. SÜT DURUMU
        if (!cow.isMale() && !cow.isBaby()) {
            if (cow.getMetabolismSystem().isLactating()) {
                if (cow.getReproductionSystem().isInDryPeriod()) {
                    player.sendSystemMessage(Component.literal("§8🚫 SÜT: KURU DÖNEM (Doğuma Hazırlık - Kesildi)"));
                } else {
                    player.sendSystemMessage(Component.literal("§b💧 SÜT: LAKTASYONDA (Verim Aktif)"));
                }
            } else {
                player.sendSystemMessage(Component.literal("§7⚪ SÜT: YOK (Kuruda/Verimsiz)"));
            }
        }

        // 6. REPRODÜKSİYON
        if (!cow.isMale() && !cow.isBaby()) {
            float prog = cow.getReproductionSystem().getProgesterone();
            player.sendSystemMessage(Component.literal("§8§m--------------------------------------------------"));

            if (cow.isPregnant()) {
                player.sendSystemMessage(Component.literal("§d♥ DURUM: GEBE §7(Prog: " + String.format("%.1f", prog) + ")"));
                if (cow.getReproductionSystem().isInDryPeriod()) {
                    player.sendSystemMessage(Component.literal("   §e➥ Doğuma az kaldı!"));
                }
            }
            else if (cow.getBreedingCooldown() > 0) {
                int seconds = cow.getBreedingCooldown() / 20;
                player.sendSystemMessage(Component.literal("§7💤 DURUM: DİNLENMEDE (Lohusa - " + seconds + "sn)"));
                player.sendSystemMessage(Component.literal("   §8(Uterus toparlanıyor, tohumlama yapılamaz)"));
            }
            else {
                if (prog < 2.0f) {
                    player.sendSystemMessage(Component.literal("§6🔥 DURUM: KIZGINLIK (ÖSTRUS) §a(✔ Tohumlanabilir!)"));
                } else {
                    player.sendSystemMessage(Component.literal("§7❄ DURUM: SAKİN (DİÖSTRUS) §8(❌ Tohumlama tutmaz)"));
                }
            }
        }

        // 7. SAĞLIK VE STRES
        if (!cow.getDisease().equals("NONE")) {
            player.sendSystemMessage(Component.literal("§c☣ TEŞHİS: " + cow.getDisease().toUpperCase()));
        }

        if (cow.getHealthSystem().getStressLevel() > 50) {
            player.sendSystemMessage(Component.literal("§e⚠ UYARI: Yüksek Stres! (" + cow.getHealthSystem().getStressLevel() + "%)"));
        } else if (cow.getHealthSystem().isRecovering()) {
            player.sendSystemMessage(Component.literal("§a✚ İYİLEŞİYOR (Nekahet Dönemi)"));
        }

        player.sendSystemMessage(Component.literal("§8§l=================================================="));


        // Su Durumu

    }
}
