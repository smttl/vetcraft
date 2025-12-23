package com.vetsim.vetcraft.item;

import com.vetsim.vetcraft.entity.CattleEntity;
import com.vetsim.vetcraft.gui.PhoneMenu;
import com.vetsim.vetcraft.money.BankData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SmartPhoneItem extends Item {

    public SmartPhoneItem(Properties properties) {
        super(properties);
    }

    // 1. HAVAYA SAĞ TIK -> GUI AÇ
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            // Menüyü Aç
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new PhoneMenu(id, inv),
                    Component.literal("VetPhone Uygulaması")
            ), buffer -> {});
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    // 2. HAYVANA SAĞ TIK -> DİREKT SATIŞ (Bu özellik kalsın, çok pratik)
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!player.level().isClientSide && hand == InteractionHand.MAIN_HAND) {
            if (target instanceof CattleEntity cattle) {
                ServerLevel level = (ServerLevel) player.level();
                double weight = cattle.getWeight();
                double price = weight * 20.0;

                BankData.deposit(level, player, price);
                cattle.discard();

                level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
                player.sendSystemMessage(Component.literal("§a[VetPhone] §fSatış: " + (int)price + " TL"));
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}