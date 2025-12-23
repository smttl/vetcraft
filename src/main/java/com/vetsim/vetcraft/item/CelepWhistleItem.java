package com.vetsim.vetcraft.item;

import com.vetsim.vetcraft.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CelepWhistleItem extends Item {
    public CelepWhistleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            BlockPos pos = player.blockPosition().relative(player.getDirection(), 2); // Oyuncunun 2 blok önüne

            // Celep'i oluştur
            ModEntities.CELEP.get().spawn(serverLevel, pos, MobSpawnType.TRIGGERED);

            // Ses ve Mesaj
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GOAT_HORN_PLAY, SoundSource.PLAYERS, 1.0F, 1.0F);
            player.sendSystemMessage(Component.literal("§6[Celep] §fGeldim ağam, ne satacaksın?"));

            // Düdüğü tüketmek istiyorsan:
            // player.getItemInHand(hand).shrink(1);

            // Tüketmek istemiyorsan (Bekleme süresi koyalım):
            player.getCooldowns().addCooldown(this, 1200); // 60 saniye bekleme süresi
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}