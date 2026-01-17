package com.vetsim.vetcraft.entity.ai;

import com.vetsim.vetcraft.entity.CattleEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class DrinkWaterGoal extends MoveToBlockGoal {
    private final CattleEntity cow;

    public DrinkWaterGoal(CattleEntity cow, double speedModifier) {
        super(cow, speedModifier, 16); // FIX: Menzil 8'den 16'ya çıkarıldı
        this.cow = cow;
    }

    @Override
    public boolean canUse() {
        // Susuz değilse (Hidrasyon > 80) arama yapma (Eşik yükseltildi)
        if (this.cow.getMetabolismSystem().getHydration() > 80.0f) {
            return false;
        }
        // Cooldown kontrolü (Sürekli su aramasın)
        if (this.nextStartTick > 0) {
            return false;
        }
        return super.canUse();
    }

    @Override
    public void tick() {
        super.tick();

        // Hedefe ulaştı mı?
        if (this.isReachedTarget()) {
            this.cow.getLookControl().setLookAt(this.blockPos.getX() + 0.5, this.blockPos.getY(),
                    this.blockPos.getZ() + 0.5);

            // İçme aksiyonu
            if (this.tryTicks % 20 == 0) { // Her saniye 1 yudum
                this.cow.playSound(SoundEvents.GENERIC_DRINK, 0.5f, 1.0f);
                this.cow.getMetabolismSystem().drink(10.0f); // FIX: 5'ten 10'a çıkarıldı (Daha hızlı içsin)

                // Animasyon için kafa eğme (opsiyonel, entity state ile yapılır genelde)
            }

            // Doydu mu?
            if (this.cow.getMetabolismSystem().getHydration() >= 100.0f) {
                // Bitir
                this.stop();
            }
        }
    }

    @Override
    protected boolean isValidTarget(LevelReader level, BlockPos pos) {
        if (!level.isEmptyBlock(pos.above())) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        // Su bloku veya Su Kazanı (Cauldron)
        return state.is(Blocks.WATER) || state.is(Blocks.WATER_CAULDRON);
    }

    @Override
    public double acceptedDistance() {
        return 2.5D; // FIX: Mesafe toleransı biraz artırıldı
    }
}
