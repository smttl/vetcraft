package com.vetsim.vetcraft.network;

import com.vetsim.vetcraft.VetCraft;
import com.vetsim.vetcraft.entity.CattleEntity;
import com.vetsim.vetcraft.entity.ModEntities;
import com.vetsim.vetcraft.money.BankData;
import com.vetsim.vetcraft.service.BankService;
import com.vetsim.vetcraft.service.MarketService;
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

        switch (msg.mode) {
            case "BUY_ANIMAL" -> MarketService.buyAnimal(level, player, msg.data, msg.amount);
            case "BUY_ITEM" -> MarketService.buyItem(level, player, msg.data, msg.amount);
            case "SELL_ALL" -> MarketService.sellAll(level, player);
            case "CHECK_BALANCE" -> BankService.checkBalance(level, player);
            case "TAKE_LOAN" -> BankService.takeLoan(level, player, msg.amount);
            case "PAY_LOAN" -> BankService.payLoan(level, player, msg.amount);
            case "CLAIM_GRANT" -> BankService.claimGrant(level, player, msg.data, msg.amount);
            case "BUY_STRAW" -> {
                String[] parts = msg.data.split(":");
                if (parts.length == 2) {
                    MarketService.buyStraw(level, player, parts[0], parts[1], msg.amount);
                }
            }
            case "HIRE_WORKER" -> {
                if (BankData.withdraw(level, player, msg.amount)) {
                    com.vetsim.vetcraft.entity.worker.WorkerEntity worker = new com.vetsim.vetcraft.entity.worker.WorkerEntity(
                            ModEntities.WORKER.get(), level);
                    worker.setPos(player.getX(), player.getY(), player.getZ());
                    worker.setOwner(player.getUUID());
                    level.addFreshEntity(worker);
                    player.sendSystemMessage(Component.literal("§aAfghan Worker kiralandı! (24 Saatlik)"));
                    BankService.syncBalance(level, player);
                    level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_PLING.value(),
                            SoundSource.PLAYERS, 1.0f, 1.0f);
                } else {
                    player.sendSystemMessage(Component.literal("§cYetersiz bakiye!"));
                }
            }
        }
    }
}