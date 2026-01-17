package com.vetsim.vetcraft.service;

import com.vetsim.vetcraft.entity.CattleEntity;
import com.vetsim.vetcraft.entity.ModEntities;
import com.vetsim.vetcraft.VetCraft;
import com.vetsim.vetcraft.money.BankData;
import com.vetsim.vetcraft.util.MarketManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class MarketService {

    public static void buyAnimal(ServerLevel level, ServerPlayer player, String breed, int cost) {
        // Blok Kontrolü
        if (BankService.isAccountBlocked(level, player)) {
            BankService.sendBlockMessage(player, level);
            return;
        }

        if (BankData.withdraw(level, player, cost)) {
            // Safe Position Calculation
            double spawnX = player.getX() + (player.getLookAngle().x * 2);
            double spawnY = player.getY();
            double spawnZ = player.getZ() + (player.getLookAngle().z * 2);

            CattleEntity cow = ModEntities.CATTLE.get().create(level);
            if (cow != null) {
                cow.moveTo(spawnX, spawnY, spawnZ, player.getYRot(), 0.0F);
                cow.setBreed(breed);
                cow.generateNewEarTag(); // Fix: Generate Ear Tag
                cow.setAge(0);
                cow.setWeight(breed.equals("Angus") ? 550 : 450);
                cow.setOwnerUUID(player.getUUID()); // Sahiplik Ata

                // Fix: Satın alınan hayvan tok gelsin (Scours/Hastalık tetiklenmemesi için)
                cow.getMetabolismSystem().setRumenFill(100.0f);

                // DISEASE ORIGIN - MARKET RISK (Phase 15, User Request #1)
                if (level.random.nextFloat() < 0.05f) { // %5 Şans
                    String randomDisease = level.random.nextBoolean() ? "fmd" : "pneumonia";
                    cow.setDisease(randomDisease);
                    VetCraft.LOGGER.info("Marketten hasta hayvan alındı! Hastalık: " + randomDisease + ", Oyuncu: "
                            + player.getName().getString());
                }

                if (level.addFreshEntity(cow)) {
                    BankService.syncBalance(level, player);
                    player.sendSystemMessage(Component.literal("§aSatın alındı: " + breed));
                    level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_CHIME.value(),
                            SoundSource.PLAYERS,
                            1.0f, 1.0f);
                    VetCraft.LOGGER
                            .info("Market Spawn Success: " + breed + " at " + spawnX + "," + spawnY + "," + spawnZ);
                } else {
                    VetCraft.LOGGER.error("Market Spawn FAILED for " + breed);
                    player.sendSystemMessage(Component.literal("§cHata: Hayvan oluşturulamadı (Alan müsait mi?)"));
                    // Refund
                    BankData.deposit(level, player, cost);
                    BankService.syncBalance(level, player);
                }
            }
        } else {
            player.sendSystemMessage(Component.literal("§cYetersiz Bakiye! Gereken: " + cost + " TL"));
        }
    }

    public static void buyItem(ServerLevel level, ServerPlayer player, String itemId, int cost) {
        // Blok Kontrolü
        if (BankService.isAccountBlocked(level, player)) {
            BankService.sendBlockMessage(player, level);
            return;
        }

        if (BankData.withdraw(level, player, cost)) {
            ResourceLocation itemLoc = new ResourceLocation(itemId);
            Item item = BuiltInRegistries.ITEM.get(itemLoc);
            ItemStack stack = new ItemStack(item, 1);

            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }

            player.sendSystemMessage(Component.literal("§aSatın alındı: " + item.getDescription().getString()));
            BankService.syncBalance(level, player);
            level.playSound(null, player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5f, 1.0f);
        } else {
            player.sendSystemMessage(Component.literal("§cYetersiz Bakiye!"));
        }
    }

    public static void sellAll(ServerLevel level, ServerPlayer player) {
        double total = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty())
                continue;

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
            BankService.syncBalance(level, player);
            player.sendSystemMessage(Component.literal("§aSatış Başarılı: §6+" + total + " TL"));
            level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f,
                    1.0f);
        } else {
            player.sendSystemMessage(Component.literal("§7Satılacak ürün (Süt, Et, Deri vb.) bulunamadı."));
        }
    }

    public static void buyStraw(ServerLevel level, ServerPlayer player, String breed, String quality, int cost) {
        if (BankService.isAccountBlocked(level, player)) {
            BankService.sendBlockMessage(player, level);
            return;
        }

        if (BankData.withdraw(level, player, cost)) {
            ItemStack straw = new ItemStack(com.vetsim.vetcraft.init.ModItems.FILLED_STRAW.get());
            net.minecraft.nbt.CompoundTag tag = straw.getOrCreateTag();

            tag.putString("VetSim_Breed", breed);
            tag.putString("VetSim_Quality", quality);

            // Genetik İstatistikleri Üret
            float milkPTA = 0.0f;
            float healthPTA = 0.0f;

            if (quality.equals("Commercial")) {
                milkPTA = (level.random.nextFloat() - 0.5f) * 0.5f; // -0.25 ile +0.25
                healthPTA = (level.random.nextFloat() - 0.5f) * 0.2f;
            } else if (quality.equals("Superior")) {
                milkPTA = 0.5f + (level.random.nextFloat() * 0.5f); // +0.5 ile +1.0
                healthPTA = 0.1f + (level.random.nextFloat() * 0.2f);
            } else if (quality.equals("Elite")) {
                milkPTA = 1.0f + (level.random.nextFloat() * 1.0f); // +1.0 ile +2.0
                healthPTA = 0.3f + (level.random.nextFloat() * 0.3f);
            }

            tag.putFloat("VetSim_MilkPTA", milkPTA);
            tag.putFloat("VetSim_HealthPTA", healthPTA);
            straw.setTag(tag);

            if (!player.getInventory().add(straw)) {
                player.drop(straw, false);
            }

            player.sendSystemMessage(Component.literal("§aGenetik Satın Alındı: " + breed + " (" + quality + ")"));
            BankService.syncBalance(level, player);
            level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8f,
                    1.0f);
        } else {
            player.sendSystemMessage(Component.literal("§cYetersiz Bakiye! Gereken: " + cost + " TL"));
        }
    }
}
