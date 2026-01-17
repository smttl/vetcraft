package com.vetsim.vetcraft.money;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class BankData extends SavedData {
    private final Map<UUID, Double> balances = new HashMap<>();
    private final Map<UUID, Double> loans = new HashMap<>();
    private final Map<UUID, List<String>> claimedGrants = new HashMap<>();

    // YENİ: Borcun alındığı zamanı tutan harita (Tick cinsinden)
    private final Map<UUID, Long> loanDates = new HashMap<>();

    // --- BAKİYE ---
    public static double getBalance(ServerLevel level, Player player) {
        return getServerData(level).balances.getOrDefault(player.getUUID(), 0.0);
    }

    public static void deposit(ServerLevel level, Player player, double amount) {
        deposit(level, player.getUUID(), amount);
    }

    public static void deposit(ServerLevel level, UUID uuid, double amount) {
        BankData data = getServerData(level);
        data.balances.put(uuid, data.balances.getOrDefault(uuid, 0.0) + amount);
        data.setDirty();
    }

    public static boolean withdraw(ServerLevel level, Player player, double amount) {
        BankData data = getServerData(level);
        double current = data.balances.getOrDefault(player.getUUID(), 0.0);
        if (current >= amount) {
            data.balances.put(player.getUUID(), current - amount);
            data.setDirty();
            return true;
        }
        return false;
    }

    // --- BORÇ VE ZAMANLAMA ---
    public static double getLoan(ServerLevel level, Player player) {
        return getServerData(level).loans.getOrDefault(player.getUUID(), 0.0);
    }

    // YENİ: Borç başlama tarihini getir
    public static long getLoanDate(ServerLevel level, Player player) {
        return getServerData(level).loanDates.getOrDefault(player.getUUID(), 0L);
    }

    public static void addLoan(ServerLevel level, Player player, double amount) {
        BankData data = getServerData(level);
        UUID id = player.getUUID();
        double currentLoan = data.loans.getOrDefault(id, 0.0);

        // Eğer oyuncunun hiç borcu yoksa, SÜREYİ BAŞLAT (Şu anki zamanı kaydet)
        if (currentLoan <= 0) {
            data.loanDates.put(id, level.getGameTime());
        }

        data.loans.put(id, currentLoan + amount);
        data.balances.put(id, data.balances.getOrDefault(id, 0.0) + amount);
        data.setDirty();
    }

    public static boolean payLoan(ServerLevel level, Player player, double amount) {
        BankData data = getServerData(level);
        UUID id = player.getUUID();
        double balance = data.balances.getOrDefault(id, 0.0);
        double loan = data.loans.getOrDefault(id, 0.0);

        if (balance >= amount && loan > 0) {
            double payAmount = Math.min(amount, loan);
            data.balances.put(id, balance - payAmount);
            double remainingLoan = loan - payAmount;
            data.loans.put(id, remainingLoan);

            // Eğer borç bittiyse, TARİHİ SİL (Temize çıktı)
            if (remainingLoan <= 0) {
                data.loanDates.remove(id);
            }

            data.setDirty();
            return true;
        }
        return false;
    }

    // --- HİBE ---
    public static boolean hasClaimedGrant(ServerLevel level, Player player, String grantId) {
        return getServerData(level).claimedGrants.getOrDefault(player.getUUID(), new ArrayList<>()).contains(grantId);
    }

    public static void claimGrant(ServerLevel level, Player player, String grantId) {
        BankData data = getServerData(level);
        List<String> list = data.claimedGrants.computeIfAbsent(player.getUUID(), k -> new ArrayList<>());
        if (!list.contains(grantId)) {
            list.add(grantId);
            data.setDirty();
        }
    }

    // --- SAVE & LOAD ---
    public static BankData getServerData(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(BankData::new, BankData::load, DataFixTypes.LEVEL), "vetsim_bank_data");
    }

    public static BankData load(CompoundTag tag) {
        BankData data = new BankData();
        // Balances
        ListTag balanceList = tag.getList("Balances", Tag.TAG_COMPOUND);
        for (int i = 0; i < balanceList.size(); i++)
            data.balances.put(balanceList.getCompound(i).getUUID("UUID"),
                    balanceList.getCompound(i).getDouble("Amount"));
        // Loans
        ListTag loanList = tag.getList("Loans", Tag.TAG_COMPOUND);
        for (int i = 0; i < loanList.size(); i++)
            data.loans.put(loanList.getCompound(i).getUUID("UUID"), loanList.getCompound(i).getDouble("Amount"));
        // Grants
        ListTag grantListTag = tag.getList("Grants", Tag.TAG_COMPOUND);
        for (int i = 0; i < grantListTag.size(); i++) {
            ListTag strList = grantListTag.getCompound(i).getList("List", Tag.TAG_STRING);
            List<String> g = new ArrayList<>();
            for (int j = 0; j < strList.size(); j++)
                g.add(strList.getString(j));
            data.claimedGrants.put(grantListTag.getCompound(i).getUUID("UUID"), g);
        }

        // YENİ: Loan Dates (Zamanları Yükle)
        ListTag dateList = tag.getList("LoanDates", Tag.TAG_COMPOUND);
        for (int i = 0; i < dateList.size(); i++) {
            data.loanDates.put(dateList.getCompound(i).getUUID("UUID"), dateList.getCompound(i).getLong("Time"));
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        // Balances
        ListTag balanceList = new ListTag();
        balances.forEach((k, v) -> {
            CompoundTag t = new CompoundTag();
            t.putUUID("UUID", k);
            t.putDouble("Amount", v);
            balanceList.add(t);
        });
        tag.put("Balances", balanceList);

        // Loans
        ListTag loanList = new ListTag();
        loans.forEach((k, v) -> {
            CompoundTag t = new CompoundTag();
            t.putUUID("UUID", k);
            t.putDouble("Amount", v);
            loanList.add(t);
        });
        tag.put("Loans", loanList);

        // Grants
        ListTag grantList = new ListTag();
        claimedGrants.forEach((k, v) -> {
            CompoundTag t = new CompoundTag();
            t.putUUID("UUID", k);
            ListTag sl = new ListTag();
            v.forEach(s -> sl.add(StringTag.valueOf(s)));
            t.put("List", sl);
            grantList.add(t);
        });
        tag.put("Grants", grantList);

        // YENİ: Loan Dates (Zamanları Kaydet)
        ListTag dateList = new ListTag();
        loanDates.forEach((k, v) -> {
            CompoundTag t = new CompoundTag();
            t.putUUID("UUID", k);
            t.putLong("Time", v);
            dateList.add(t);
        });
        tag.put("LoanDates", dateList);

        return tag;
    }
}