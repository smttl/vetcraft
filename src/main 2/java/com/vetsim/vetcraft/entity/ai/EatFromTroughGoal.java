
package com.vetsim.vetcraft.entity.ai;

import com.vetsim.vetcraft.block.FeedTroughBlock;
import com.vetsim.vetcraft.entity.CattleEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public class EatFromTroughGoal extends MoveToBlockGoal {
    private final CattleEntity cow;

    public EatFromTroughGoal(CattleEntity cow, double speed) {
        super(cow, speed, 16); // 16 blok yarıçapında arar
        this.cow = cow;
    }

    // Hedef ne zaman çalışsın?
    @Override
    public boolean canUse() {
        // Sığır açsa (<%80) ve bekleme süresi yoksa çalışır
        return this.cow.getHunger() < 80 && super.canUse();
    }

    // Bu blok aradığımız blok mu?
    @Override
    protected boolean isValidTarget(LevelReader level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        // Blok bizim Yemlik Bloğu mu VE içi boş değil mi (Level > 0)?
        return state.getBlock() instanceof FeedTroughBlock && state.getValue(FeedTroughBlock.LEVEL) > 0;
    }

    // Bloğa ulaşınca ne yapsın?
    @Override
    public void tick() {
        super.tick();

        // Bloğun yanına geldiyse
        if (this.isReachedTarget()) {
            // Yemlikteki yemi azalt
            BlockPos pos = this.blockPos;
            BlockState state = this.cow.level().getBlockState(pos);

            if (state.getBlock() instanceof FeedTroughBlock) {
                int currentLevel = state.getValue(FeedTroughBlock.LEVEL);

                if (currentLevel > 0) {
                    // Yemlik seviyesini 1 düşür
                    this.cow.level().setBlock(pos, state.setValue(FeedTroughBlock.LEVEL, currentLevel - 1), 3);

                    // Sığırı doyur (+30 Tokluk)
                    this.cow.setHunger(this.cow.getHunger() + 30);

                    // Ses ve Efekt
                    this.cow.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
                    this.cow.level().broadcastEntityEvent(this.cow, (byte) 18); // Kalp efekti

                    // Hedefi tamamla, dursun
                    this.stop();
                }
            }
        }
    }
}