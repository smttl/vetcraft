package com.vetsim.vetcraft.entity;

import com.vetsim.vetcraft.money.BankData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class CelepEntity extends PathfinderMob {

    public CelepEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.5D); // Köylüden biraz hızlı
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide && hand == InteractionHand.MAIN_HAND) {
            ItemStack heldItem = player.getItemInHand(hand);
            ServerLevel serverLevel = (ServerLevel) this.level();

            // 1. SÜT ALIMI
            if (heldItem.is(Items.MILK_BUCKET)) {
                BankData.deposit(serverLevel, player, 150.0);
                player.setItemInHand(hand, new ItemStack(Items.BUCKET)); // Boş kova ver
                this.playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.0F);
                player.sendSystemMessage(Component.literal("§6[Celep] §fSütü aldım, 150 TL hesabına geçti."));
                return InteractionResult.SUCCESS;
            }

            // 2. ET ALIMI
            if (heldItem.is(Items.BEEF)) {
                BankData.deposit(serverLevel, player, 200.0);
                heldItem.shrink(1);
                this.playSound(SoundEvents.VILLAGER_TRADE, 1.0F, 1.0F);
                player.sendSystemMessage(Component.literal("§6[Celep] §fEti aldım, 200 TL hesabına geçti."));
                return InteractionResult.SUCCESS;
            }

            // 3. BOŞ EL (Konuşma)
            if (heldItem.isEmpty()) {
                player.sendSystemMessage(Component.literal("§6[Celep] §fSelam ağam! Süt veya hayvanın varsa alırım."));
                player.sendSystemMessage(Component.literal("§7(Süt Kovası: 150 TL | Çiğ Et: 200 TL)"));
                this.playSound(SoundEvents.VILLAGER_AMBIENT, 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }
}