package com.vetsim.vetcraft.service;

import com.vetsim.vetcraft.money.BankData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class BankService {

    public static void checkBalance(ServerLevel level, ServerPlayer player) {
        double balance = BankData.getBalance(level, player);
        double loan = BankData.getLoan(level, player);
        long loanStartDate = BankData.getLoanDate(level, player);
        long currentTime = level.getGameTime();

        // SYNC TO CLIENT (For UI Display)
        syncBalance(level, player);

        // CHAT INFO (Keep for loan details, but balance is now in UI)
        if (loan > 0) {
            long passedTime = currentTime - loanStartDate;
            long remainingTime = 216000 - passedTime;
            int remainingDays = (int) (remainingTime / 24000);

            player.sendSystemMessage(Component.literal("§fGüncel Borç: §c-" + loan + " TL"));

            if (remainingTime > 0) {
                player.sendSystemMessage(
                        Component.literal("§7Son Ödeme: §e" + remainingDays + " Minecraft Günü kaldı."));
            } else {
                player.sendSystemMessage(Component.literal("§4⚠ ÖDEME SÜRESİ DOLDU! HESAP BLOKE."));
            }
        }
    }

    public static void syncBalance(ServerLevel level, ServerPlayer player) {
        double balance = BankData.getBalance(level, player);
        net.neoforged.neoforge.network.PacketDistributor.PLAYER.with(player)
                .send(new com.vetsim.vetcraft.network.BalanceSyncPacket(balance));
    }

    public static void takeLoan(ServerLevel level, ServerPlayer player, int amount) {
        double currentLoan = BankData.getLoan(level, player);
        double limit = 50000.0;

        if (currentLoan + amount <= limit) {
            BankData.addLoan(level, player, amount);
            syncBalance(level, player);
            player.sendSystemMessage(Component.literal("§a✔ Kredi Onaylandı: §f" + amount + " TL hesabınıza yattı."));
            level.playSound(null, player.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.PLAYERS, 1.0f, 1.0f);
        } else {
            player.sendSystemMessage(Component.literal("§c❌ Kredi Reddedildi! §7(Limit: 50.000 TL)"));
            level.playSound(null, player.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }

    public static void payLoan(ServerLevel level, ServerPlayer player, int amount) {
        if (BankData.payLoan(level, player, amount)) {
            player.sendSystemMessage(Component.literal("§a✔ Ödeme Alındı: §f" + amount + " TL borçtan düşüldü."));
            syncBalance(level, player);
            level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f,
                    1.0f);
        } else {
            player.sendSystemMessage(Component.literal("§c❌ Yetersiz Bakiye veya Borcunuz Yok."));
        }
    }

    public static void claimGrant(ServerLevel level, ServerPlayer player, String grantId, int reward) {
        if (!BankData.hasClaimedGrant(level, player, grantId)) {
            BankData.claimGrant(level, player, grantId);
            BankData.claimGrant(level, player, grantId);
            BankData.deposit(level, player, reward);
            syncBalance(level, player);

            player.sendSystemMessage(Component.literal("§6★ HİBE ONAYLANDI! ★"));
            player.sendSystemMessage(Component.literal("§fHesabınıza §e+" + reward + " TL §fdevlet desteği yattı."));

            level.playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS,
                    1.0f, 1.0f);
        } else {
            player.sendSystemMessage(Component.literal("§cBu hibeyi zaten aldınız."));
        }
    }

    public static boolean isAccountBlocked(ServerLevel level, ServerPlayer player) {
        double currentLoan = BankData.getLoan(level, player);
        if (currentLoan <= 0)
            return false;

        long loanStartDate = BankData.getLoanDate(level, player);
        long currentTime = level.getGameTime();
        // 9 GÜN KURALI (216.000 Tick)
        return (currentTime - loanStartDate) > 216000;
    }

    public static void sendBlockMessage(ServerPlayer player, ServerLevel level) {
        long loanStartDate = BankData.getLoanDate(level, player);
        long passedTime = level.getGameTime() - loanStartDate;
        int daysPassed = (int) (passedTime / 24000);

        player.sendSystemMessage(Component.literal("§c⛔ HESAP BLOKE EDİLDİ!"));
        player.sendSystemMessage(Component.literal("§7Kredi ödeme süresi (9 Gün) doldu."));
        player.sendSystemMessage(Component.literal("§7Geçen Süre: §c" + daysPassed + " Gün"));
        player.sendSystemMessage(Component.literal("§eHarcama yapmak için önce borcunuzu ödeyin."));
        player.level().playSound(null, player.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0f,
                1.0f);
    }
}
