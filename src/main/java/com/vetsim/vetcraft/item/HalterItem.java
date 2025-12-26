package com.vetsim.vetcraft.item;

import com.vetsim.vetcraft.entity.CattleEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class HalterItem extends Item {

    public HalterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        // Sadece bizim ineklerimizde çalışsın
        if (target instanceof CattleEntity cattle) {

            // Eğer zaten bağlıysa çözer (Vanilla Lead mantığı)
            if (cattle.isLeashed()) {
                cattle.dropLeash(true, true); // Kayışı düşür
                player.sendSystemMessage(Component.literal("§eYular çözüldü."));
                return InteractionResult.SUCCESS;
            }
            // Bağlı değilse bağlar
            else {
                cattle.setLeashedTo(player, true); // Oyuncuya bağla
                cattle.playSound(SoundEvents.LEASH_KNOT_PLACE, 1.0F, 1.0F);
                player.sendSystemMessage(Component.literal("§aYular takıldı. Gel buraya!"));

                // Yuları tüketmeyelim (Reusable/Çok kullanımlık olsun istiyorsan burayı silme)
                // stack.shrink(1); // Eğer tek kullanımlık olacaksa bunu aç.

                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}