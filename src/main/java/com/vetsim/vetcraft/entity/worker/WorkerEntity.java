package com.vetsim.vetcraft.entity.worker;

import com.vetsim.vetcraft.entity.CattleEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class WorkerEntity extends PathfinderMob {

    private int despawnTimer = 0;
    public static final int MAX_LIFE_TICKS = 24000; // 1 Gün

    public WorkerEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setCustomName(Component.literal("Afghan Worker"));
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    private java.util.UUID ownerUUID;

    public void setOwner(java.util.UUID uuid) {
        this.ownerUUID = uuid;
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("DespawnTimer", this.despawnTimer);
        if (this.ownerUUID != null) {
            tag.putUUID("Owner", this.ownerUUID);
        }
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("DespawnTimer")) {
            this.despawnTimer = tag.getInt("DespawnTimer");
        }
        if (tag.hasUUID("Owner")) {
            this.ownerUUID = tag.getUUID("Owner");
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MilkCowsGoal(this)); // Özel Goal
        this.goalSelector.addGoal(2, new CollectManureGoal(this)); // Gübre Toplama
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            despawnTimer++;
            if (despawnTimer >= MAX_LIFE_TICKS) {
                this.discard();
                // Opsiyonel: Duman efekti veya ses
                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF, this.getX(), this.getY(),
                            this.getZ(), 10, 0.5, 0.5, 0.5, 0);
                }
            }
        }
    }

    // --- ÖZEL GOAL: Gübre Toplama ---
    static class CollectManureGoal extends Goal {
        private final WorkerEntity worker;
        private net.minecraft.world.entity.item.ItemEntity targetManure;
        private int cooldown = 0;

        public CollectManureGoal(WorkerEntity worker) {
            this.worker = worker;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (cooldown > 0) {
                cooldown--;
                return false;
            }
            List<net.minecraft.world.entity.item.ItemEntity> items = worker.level().getEntitiesOfClass(
                    net.minecraft.world.entity.item.ItemEntity.class, worker.getBoundingBox().inflate(15.0));

            for (net.minecraft.world.entity.item.ItemEntity item : items) {
                if (item.getItem().is(com.vetsim.vetcraft.init.ModItems.MANURE.get())) {
                    // Sadece 1 dakikadan yeniyse veya her türlüsü mü? Her türlüsü.
                    this.targetManure = item;
                    return true;
                }
            }
            return false;
        }

        @Override
        public void start() {
            worker.getNavigation().moveTo(targetManure, 1.0);
        }

        @Override
        public void stop() {
            targetManure = null;
        }

        @Override
        public void tick() {
            if (targetManure == null || !targetManure.isAlive()) {
                stop();
                return;
            }

            worker.getLookControl().setLookAt(targetManure);

            if (worker.distanceToSqr(targetManure) < 2.5) {
                // Topla
                worker.swing(InteractionHand.MAIN_HAND);
                targetManure.discard(); // Yok et (Toplandı)

                if (worker.level() instanceof ServerLevel sl) {
                    sl.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, worker.getX(),
                            worker.getY() + 1.5, worker.getZ(), 3, 0.3, 0.3, 0.3, 0);
                    worker.playSound(SoundEvents.ITEM_PICKUP, 1.0f, 1.0f);

                    // Para Yatır
                    if (worker.ownerUUID != null) {
                        int price = 5; // Gübre Fiyatı
                        com.vetsim.vetcraft.money.BankData.deposit(sl, worker.ownerUUID, price);
                    }
                }

                cooldown = 40; // 2 saniye bekle
                stop();
            } else {
                worker.getNavigation().moveTo(targetManure, 1.0);
            }
        }
    }

    // --- ÖZEL GOAL: İnek Sağma ---
    static class MilkCowsGoal extends Goal {
        private final WorkerEntity worker;
        private CattleEntity targetCow;
        private int cooldown = 0;

        public MilkCowsGoal(WorkerEntity worker) {
            this.worker = worker;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (cooldown > 0) {
                cooldown--;
                return false;
            }
            // Etraftaki inekleri bul
            List<CattleEntity> cows = worker.level().getEntitiesOfClass(CattleEntity.class,
                    worker.getBoundingBox().inflate(15.0));
            for (CattleEntity cow : cows) {
                // Süt verebilecek durumda mı?
                if (!cow.isBaby() && !cow.isMale() && cow.getMetabolismSystem().isLactationActive()) {

                    // Sahiplik Kontrolü (Multiplayer Güvenliği)
                    if (cow.getOwnerUUID().isPresent() && worker.ownerUUID != null) {
                        if (!cow.getOwnerUUID().get().equals(worker.ownerUUID)) {
                            continue; // Bu inek başkasının, dokunma.
                        }
                    }

                    // Kalıntı var mı? (Kirli sütü sağmaz veya sağar atar mı? Bizim işçi temiz
                    // çalışsın, sağsın ama uyarsın)
                    // Şimdilik sadece yakınlaşsın.
                    this.targetCow = cow;
                    return true;
                }
            }
            return false;
        }

        @Override
        public void start() {
            worker.getNavigation().moveTo(targetCow, 1.0);
        }

        @Override
        public void stop() {
            targetCow = null;
        }

        @Override
        public void tick() {
            if (targetCow == null)
                return;

            worker.getLookControl().setLookAt(targetCow);

            if (worker.distanceToSqr(targetCow) < 4.0) {
                // Sağım yap
                worker.swing(InteractionHand.MAIN_HAND);

                // İneğe ses ver
                worker.playSound(SoundEvents.COW_MILK, 1.0f, 1.0f);

                // Kalıntı Kontrolü
                if (targetCow.getHealthSystem().isWithdrawalActive()) {
                    // İlaçlı süt satılamaz, imha edilir.
                    if (worker.level() instanceof ServerLevel sl) {
                        sl.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE, worker.getX(),
                                worker.getY() + 1,
                                worker.getZ(), 5, 0.2, 0.2, 0.2, 0);
                    }
                } else {
                    // Temiz Süt Satışı
                    if (worker.ownerUUID != null) {
                        if (worker.level() instanceof ServerLevel sl) {
                            int price = 50; // Süt Fiyatı
                            com.vetsim.vetcraft.money.BankData.deposit(sl, worker.ownerUUID, price);

                            // Kazanç Efekti
                            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, worker.getX(),
                                    worker.getY() + 1.5,
                                    worker.getZ(), 5, 0.5, 0.5, 0.5, 0);
                        }
                    } else {
                        // Sahibi yoksa yere at
                        worker.spawnAtLocation(new ItemStack(Items.MILK_BUCKET));
                    }
                }

                // Bekleme süresi koy (Bir ineği sürekli sağmasın)
                cooldown = 200; // 10 saniye bekle

                // İnekten uzaklaşması için rastgele bir yere gitmesi sağlanabilir ama AI
                // halleder.
                stop();
            } else {
                worker.getNavigation().moveTo(targetCow, 1.0);
            }
        }
    }
}
