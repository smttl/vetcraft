package com.vetsim.vetcraft.event;

import com.vetsim.vetcraft.VetCraft;
import com.vetsim.vetcraft.entity.CattleEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

@Mod.EventBusSubscriber(modid = VetCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof CattleEntity cow) {
            // Eğer hayvanda ilaç kalıntısı varsa:
            if (cow.getHealthSystem().isWithdrawalActive()) {
                for (ItemEntity drop : event.getDrops()) {
                    if (drop.getItem().is(Items.BEEF) || drop.getItem().is(Items.COOKED_BEEF)) {
                        // Eti işaretle
                        drop.getItem().setHoverName(Component.literal("§cİlaçlı Et (Tüketilemez)"));
                    }
                }
            }
        }
    }
}
