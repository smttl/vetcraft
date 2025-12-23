package com.vetsim.vetcraft.network;

import com.vetsim.vetcraft.VetCraft;
import com.vetsim.vetcraft.entity.CattleEntity;
import com.vetsim.vetcraft.entity.ModEntities;
import com.vetsim.vetcraft.money.BankData;
import com.vetsim.vetcraft.util.MarketManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record PhonePacket(String mode, String data, int amount) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(VetCraft.MOD_ID, "phone_packet");

    public PhonePacket(FriendlyByteBuf buf) {
        this(buf.readUtf(), buf.readUtf(), buf.readInt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(mode);
        buf.writeUtf(data);
        buf.writeInt(amount);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PhonePacket msg, PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            ctx.player().ifPresent(p -> {
                if (p instanceof ServerPlayer player) {
                    handleTransaction(msg, player);
                }
            });
        });
    }

    private static void handleTransaction(PhonePacket msg, ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();

        // --- BORÇ VE SÜRE KONTROLÜ ---
        double currentLoan = BankData.getLoan(level, player);
        long loanStartDate = BankData.getLoanDate(level, player);
        long currentTime = level.getGameTime();

        // 9 GÜN KURALI (216.000 Tick)
        boolean isTimeExpired = currentLoan > 0 && (currentTime - loanStartDate) > 216000;

        // --- 1. HAYVAN ALIMI ---
        if (msg.mode.equals("BUY_ANIMAL")) {

            if (isTimeExpired) {
                sendTimeBlockMessage(player, currentLoan, (currentTime - loanStartDate));
                return;
            }

            int cost = msg.amount;
            if (BankData.withdraw(level, player, cost)) {
                BlockPos pos = player.blockPosition().relative(player.getDirection(), 2);
                CattleEntity cow = ModEntities.CATTLE.get().create(level);
                if (cow != null) {
                    cow.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player.getYRot(), 0.0F);
                    cow.setBreed(msg.data);
                    cow.setAge(0);
                    cow.setWeight(msg.data.equals("Angus") ? 550 : 450);
                    level.addFreshEntity(cow);

                    player.sendSystemMessage(Component.literal("§aSatın alındı: " + msg.data));
                    // Note Block sesleri genelde Holder olduğu için .value() kalmalı
                    level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
                }
            } else {
                player.sendSystemMessage(Component.literal("§cYetersiz Bakiye! Gereken: " + cost + " TL"));
            }
        }

        // --- 2. EŞYA ALIMI (MARKET) ---
        else if (msg.mode.equals("BUY_ITEM")) {

            if (isTimeExpired) {
                sendTimeBlockMessage(player, currentLoan, (currentTime - loanStartDate));
                return;
            }

            int cost = msg.amount;
            if (BankData.withdraw(level, player, cost)) {
                ResourceLocation itemLoc = new ResourceLocation(msg.data);
                Item item = BuiltInRegistries.ITEM.get(itemLoc);
                ItemStack stack = new ItemStack(item, 1);

                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }

                player.sendSystemMessage(Component.literal("§aSatın alındı: " + item.getDescription().getString()));

                // DÜZELTME: .value() kaldırıldı
                level.playSound(null, player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5f, 1.0f);
            } else {
                player.sendSystemMessage(Component.literal("§cYetersiz Bakiye!"));
            }
        }

        // --- 3. SATIŞ (TÜMÜNÜ SAT) ---
        else if (msg.mode.equals("SELL_ALL")) {
            double total = 0;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.isEmpty()) continue;

                int pricePerItem = MarketManager.getSellPrice(stack);

                if (pricePerItem > 0) {
                    total += (pricePerItem * stack.getCount());

                    if (stack.is(Items.MILK_BUCKET)) {
                        player.getInventory().setItem(i, new ItemStack(Items.BUCKET, stack.getCount()));
                    } else {
                        player.getInventory().removeItem(stack);
                    }
                }
            }

            if (total > 0) {
                BankData.deposit(level, player, total);
                player.sendSystemMessage(Component.literal("§aSatış Başarılı: §6+" + total + " TL"));
                // Burada da hata verirse .value() eklenebilir veya çıkarılabilir, şimdilik böyle
                level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);
            } else {
                player.sendSystemMessage(Component.literal("§7Satılacak ürün (Süt, Et, Deri vb.) bulunamadı."));
            }
        }

        // --- 4. ZİRAAT: BAKİYE SORGULA ---
        else if (msg.mode.equals("CHECK_BALANCE")) {
            double balance = BankData.getBalance(level, player);
            double loan = BankData.getLoan(level, player);

            player.sendSystemMessage(Component.literal("§c=== ZİRAAT BANKASI ==="));
            player.sendSystemMessage(Component.literal("§fVadesiz Hesap: §e" + balance + " TL"));

            if (loan > 0) {
                long passedTime = currentTime - loanStartDate;
                long remainingTime = 216000 - passedTime;
                int remainingDays = (int) (remainingTime / 24000);

                player.sendSystemMessage(Component.literal("§fGüncel Borç: §c-" + loan + " TL"));

                if (remainingTime > 0) {
                    player.sendSystemMessage(Component.literal("§7Son Ödeme: §e" + remainingDays + " Minecraft Günü kaldı."));
                } else {
                    player.sendSystemMessage(Component.literal("§4⚠ ÖDEME SÜRESİ DOLDU! HESAP BLOKE."));
                    player.sendSystemMessage(Component.literal("§7(Alışveriş yapamazsınız, sadece satış ve ödeme.)"));
                }
            } else {
                player.sendSystemMessage(Component.literal("§aHiç borcunuz yok."));
            }

            level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS, 1.0f, 2.0f);
        }

        // --- 5. ZİRAAT: KREDİ ÇEK ---
        else if (msg.mode.equals("TAKE_LOAN")) {
            double currentLoanCheck = BankData.getLoan(level, player);
            double limit = 50000.0;

            if (currentLoanCheck + msg.amount <= limit) {
                BankData.addLoan(level, player, msg.amount);
                player.sendSystemMessage(Component.literal("§a✔ Kredi Onaylandı: §f" + msg.amount + " TL hesabınıza yattı."));
                level.playSound(null, player.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.PLAYERS, 1.0f, 1.0f);
            } else {
                player.sendSystemMessage(Component.literal("§c❌ Kredi Reddedildi! §7(Limit: 50.000 TL)"));
                level.playSound(null, player.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
        }

        // --- 6. ZİRAAT: BORÇ ÖDE ---
        else if (msg.mode.equals("PAY_LOAN")) {
            if (BankData.payLoan(level, player, msg.amount)) {
                player.sendSystemMessage(Component.literal("§a✔ Ödeme Alındı: §f" + msg.amount + " TL borçtan düşüldü."));
                level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);
            } else {
                player.sendSystemMessage(Component.literal("§c❌ Yetersiz Bakiye veya Borcunuz Yok."));
            }
        }

        // --- 7. ZİRAAT: HİBE AL (GRANT) ---
        else if (msg.mode.equals("CLAIM_GRANT")) {
            String grantId = msg.data;
            int reward = msg.amount;

            if (!BankData.hasClaimedGrant(level, player, grantId)) {
                BankData.claimGrant(level, player, grantId);
                BankData.deposit(level, player, reward);

                player.sendSystemMessage(Component.literal("§6★ HİBE ONAYLANDI! ★"));
                player.sendSystemMessage(Component.literal("§fHesabınıza §e+" + reward + " TL §fdevlet desteği yattı."));

                // DÜZELTME: .value() kaldırıldı
                level.playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 1.0f);
            } else {
                player.sendSystemMessage(Component.literal("§cBu hibeyi zaten aldınız."));
            }
        }
    }

    private static void sendTimeBlockMessage(ServerPlayer player, double loan, long passedTicks) {
        int daysPassed = (int) (passedTicks / 24000);
        player.sendSystemMessage(Component.literal("§c⛔ HESAP BLOKE EDİLDİ!"));
        player.sendSystemMessage(Component.literal("§7Kredi ödeme süresi (9 Gün) doldu."));
        player.sendSystemMessage(Component.literal("§7Geçen Süre: §c" + daysPassed + " Gün"));
        player.sendSystemMessage(Component.literal("§eHarcama yapmak için önce borcunuzu ödeyin."));
        player.level().playSound(null, player.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}