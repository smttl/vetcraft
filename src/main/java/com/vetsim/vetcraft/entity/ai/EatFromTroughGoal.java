package com.vetsim.vetcraft.entity.ai;

import com.vetsim.vetcraft.block.FeedTroughBlock;
import com.vetsim.vetcraft.entity.CattleEntity;
import com.vetsim.vetcraft.util.FeedData;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public class EatFromTroughGoal extends MoveToBlockGoal {
    private final CattleEntity cow;
    private int eatTimer; // Yeme süresi sayacı

    public EatFromTroughGoal(CattleEntity cow, double speed) {
        super(cow, speed, 16); // 16 blok yarıçapında arar
        this.cow = cow;
    }

    @Override
    public boolean canUse() {
        // Metabolizma sisteminden tokluk kontrolü yapalım (Daha hassas)
        // Eğer çok aç değilse gitmesin (Gereksiz hareket önleme)
        if (this.cow.getHunger() > 90) return false;

        return super.canUse();
    }

    @Override
    protected boolean isValidTarget(LevelReader level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        // Blok Yemlik mi VE dolu mu?
        return state.getBlock() instanceof FeedTroughBlock && state.getValue(FeedTroughBlock.LEVEL) > 0;
    }

    @Override
    public void start() {
        super.start();
        this.eatTimer = 0; // Hedef başlayınca sayacı sıfırla
    }

    @Override
    public void tick() {
        super.tick();

        // Bloğun yanına geldiyse ve bakıyorsa
        if (this.isReachedTarget()) {
            this.cow.getLookControl().setLookAt(this.blockPos.getX() + 0.5D, this.blockPos.getY() + 1.0D, this.blockPos.getZ() + 0.5D);
            this.eatTimer++;

            // Her 20 tickte bir (1 saniye) çiğneme sesi
            if (this.eatTimer % 20 == 0 && this.eatTimer < 60) {
                this.cow.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
            }

            // 60 tick (3 saniye) boyunca yedi, şimdi etkiyi uygula
            if (this.eatTimer >= 60) {
                BlockPos pos = this.blockPos;
                BlockState state = this.cow.level().getBlockState(pos);

                if (state.getBlock() instanceof FeedTroughBlock) {
                    int currentLevel = state.getValue(FeedTroughBlock.LEVEL);

                    if (currentLevel > 0) {
                        // 1. Yemlik seviyesini düşür
                        this.cow.level().setBlock(pos, state.setValue(FeedTroughBlock.LEVEL, currentLevel - 1), 3);

                        // 2. METABOLİZMA SİSTEMİNİ KULLAN
                        // Yemlikteki yemi temsil eden bir "FeedData" oluşturuyoruz.
                        // Yemlik yemi genelde "TMR" (Total Mixed Ration) olur; dengelidir.

                        FeedData troughFeed = new FeedData(
                                "trough_mix",    // ID
                                "Karışık Yem",   // İsim
                                40,              // Besin (Nutrition) - İyi doyurur
                                true,            // Kuru Madde (pH'ı hafif yükseltir/dengeler)
                                0.0f             // Protein (Şimdilik pasif)
                        );

                        this.cow.getMetabolismSystem().feed(troughFeed);

                        // 3. Efektler
                        this.cow.playSound(SoundEvents.PLAYER_BURP, 0.5F, 1.0F); // Doyma sesi
                        this.cow.level().broadcastEntityEvent(this.cow, (byte) 18); // Kalp efekti

                        // İşlem bitti
                        this.stop();
                    }
                }
                // Blok değişmişse veya boşalmışsa dur
                else {
                    this.stop();
                }
            }
        }
    }

    // Blok uzaklık ayarı (Bloğun içine girmesin, dibinde dursun)
    @Override
    public double acceptedDistance() {
        return 2.5D;
    }
}