package com.vetsim.vetcraft.entity.ai;

import com.vetsim.vetcraft.entity.CattleEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

import java.util.EnumSet;
import java.util.List;

public class NaturalBreedingGoal extends Goal {
    private final CattleEntity animal;
    private CattleEntity partner;
    private final double speedModifier;
    private int breedTime;

    public NaturalBreedingGoal(CattleEntity animal, double speed) {
        this.animal = animal;
        this.speedModifier = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    // --- HEDEF NE ZAMAN BAŞLAMALI? ---
    @Override
    public boolean canUse() {
        // 1. Hayvanın bekleme süresi (cooldown) varsa veya bebekse çalışma
        if (this.animal.getBreedingCooldown() > 0 || this.animal.isBaby()) {
            return false;
        }

        // 2. Eğer bu hayvan Dişiyse ve zaten hamileyse, eş aramasın
        if (!this.animal.isMale() && this.animal.isPregnant()) {
            return false;
        }

        // 3. Performans için rastgele çalıştır (%0.5 şans - her tick değil)
        if (this.animal.getRandom().nextInt(200) != 0) {
            return false;
        }

        // 4. Uygun bir partner ara
        this.partner = this.findPartner();
        return this.partner != null;
    }

    // --- HEDEF DEVAM ETSİN Mİ? ---
    @Override
    public boolean canContinueToUse() {
        return this.partner != null &&
                this.partner.isAlive() &&
                this.animal.distanceToSqr(this.partner) < 32 &&
                this.animal.getBreedingCooldown() == 0;
    }

    // --- BAŞLANGIÇ ---
    @Override
    public void start() {
        this.breedTime = 0;
        this.animal.getNavigation().moveTo(this.partner, this.speedModifier);
    }

    // --- BİTİŞ ---
    @Override
    public void stop() {
        this.partner = null;
        this.breedTime = 0;
    }

    // --- HER TICK (OYUN DÖNGÜSÜ) ---
    @Override
    public void tick() {
        this.animal.getLookControl().setLookAt(this.partner, 10.0F, (float)this.animal.getMaxHeadXRot());
        this.animal.getNavigation().moveTo(this.partner, this.speedModifier);

        // Yakınlaşma Kontrolü (2 blok mesafe)
        if (this.animal.distanceToSqr(this.partner) < 4.0D) {
            this.breedTime++;
            // 60 tick (3 saniye) boyunca yan yana dururlarsa işlem tamam
            if (this.breedTime >= 60) {
                this.breed();
            }
        }
    }

    // --- ÇİFTLEŞME MANTIĞI (GÜNCELLENDİ) ---
    private void breed() {
        // Sadece sunucuda işlem yap (Client tarafında yaparsan hayalet entityler oluşur)
        if (!(this.animal.level() instanceof ServerLevel)) return;

        CattleEntity male = null;
        CattleEntity female = null;

        // Rolleri belirle
        if (this.animal.isMale()) {
            male = this.animal;
            female = this.partner;
        } else {
            male = this.partner;
            female = this.animal;
        }

        // GÜVENLİK KONTROLÜ:
        // Eğer dişi hamileyse veya erkek yorgunsa işlemi iptal et.
        if (female.isPregnant() || male.getBreedingCooldown() > 0) {
            return;
        }

        // 1. GEBELİĞİ BAŞLAT
        // Dişiye babanın ırkını veriyoruz (Genetik Miras için)
        female.startPregnancy(male.getBreed());

        // 2. ERKEK İÇİN YORGUNLUK (COOLDOWN)
        // Erkek 1 gün (24000 tick) boyunca tekrar çiftleşemesin
        male.setBreedingCooldown(24000);

        // Not: Dişiye cooldown vermiyoruz çünkü "isPregnant" kontrolü onu zaten engelliyor.
        // Doğum yaptıktan sonra kendi kodunda cooldown alacak.

        // Opsiyonel: Kalp efektleri veya ses eklenebilir ama şu an sade kalsın.
    }

    // --- PARTNER BULMA ---
    private CattleEntity findPartner() {
        // 8 blok yarıçapındaki diğer sığırları listele
        List<CattleEntity> list = this.animal.level().getEntitiesOfClass(CattleEntity.class, this.animal.getBoundingBox().inflate(8.0D));

        for (CattleEntity potentialPartner : list) {
            // Kendisi olmasın
            if (this.animal == potentialPartner) continue;

            // Partner bebek olmamalı
            if (potentialPartner.isBaby()) continue;

            // Cinsiyetler ZIT olmalı (Biri Erkek, Biri Dişi)
            if (this.animal.isMale() == potentialPartner.isMale()) continue;

            // Partnerin de çiftleşmeye uygun olması lazım (Cooldown yok, Hamile değil)
            if (potentialPartner.getBreedingCooldown() > 0) continue;
            if (!potentialPartner.isMale() && potentialPartner.isPregnant()) continue; // Dişiyse ve hamileyse seçme

            // Tüm şartlar uydu, partner bulundu!
            return potentialPartner;
        }
        return null;
    }
}