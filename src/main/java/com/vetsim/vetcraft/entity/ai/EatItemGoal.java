package com.vetsim.vetcraft.entity.ai;

import com.vetsim.vetcraft.entity.CattleEntity;
import com.vetsim.vetcraft.util.FeedData;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.sounds.SoundEvents;

public class EatItemGoal extends Goal {
    private final CattleEntity cow;
    private ItemEntity targetItem;
    private final double speedModifier;

    public EatItemGoal(CattleEntity cow, double speedModifier) {
        this.cow = cow;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // Tok ise yemesin (Yemlikten yeme mantığıyla benzer)
        if (this.cow.getMetabolismSystem().getRumenFill() >= 80.0f) {
            return false;
        }

        // Yerdeki itemları tara (10 blok)
        List<ItemEntity> items = this.cow.level().getEntitiesOfClass(ItemEntity.class,
                this.cow.getBoundingBox().inflate(10.0D, 3.0D, 10.0D));

        for (ItemEntity item : items) {
            if (isValidFood(item)) {
                this.targetItem = item;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetItem != null && this.targetItem.isAlive() && !this.cow.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.cow.getNavigation().moveTo(this.targetItem, this.speedModifier);
    }

    @Override
    public void stop() {
        this.targetItem = null;
        this.cow.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.targetItem == null || !this.targetItem.isAlive()) {
            this.stop();
            return;
        }

        this.cow.getLookControl().setLookAt(this.targetItem, 30.0F, 30.0F);

        // Yaklaştıysa ye (2 blok mesafe)
        if (this.cow.distanceToSqr(this.targetItem) < 4.0D) {
            eatItem();
        }
    }

    private void eatItem() {
        // FeedManager'dan veriyi al (Data-Driven)
        FeedData feedData = com.vetsim.vetcraft.util.FeedManager.getFeedData(this.targetItem.getItem());

        if (feedData != null) {
            // Metabolizmaya işle
            this.cow.getMetabolismSystem().feed(feedData);

            // Görsel / İşitsel Efekt
            this.cow.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);

            // İtemi yok et (1 tane azalt)
            if (this.targetItem.getItem().getCount() > 1) {
                this.targetItem.getItem().shrink(1);
            } else {
                this.targetItem.discard();
            }
        }

        // Hedefi sıfırla
        this.targetItem = null;
    }

    private boolean isValidFood(ItemEntity item) {
        // Sadece FeedManager'da tanımlı olanları ye!
        return com.vetsim.vetcraft.util.FeedManager.getFeedData(item.getItem()) != null;
    }
}
