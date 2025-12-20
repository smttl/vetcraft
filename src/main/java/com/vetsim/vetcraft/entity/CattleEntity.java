package com.vetsim.vetcraft.entity;

import com.vetsim.vetcraft.entity.ai.EatFromTroughGoal;
import com.vetsim.vetcraft.entity.ai.NaturalBreedingGoal;
import com.vetsim.vetcraft.entity.ModEntities;
import com.vetsim.vetcraft.init.ModItems;
import com.vetsim.vetcraft.util.DiseaseData;
import com.vetsim.vetcraft.util.DiseaseManager;
import com.vetsim.vetcraft.util.FeedData;
import com.vetsim.vetcraft.util.FeedManager;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class CattleEntity extends Animal {

    // --- SENKRONİZE VERİLER ---
    private static final EntityDataAccessor<String> EAR_TAG = SynchedEntityData.defineId(CattleEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> BREED = SynchedEntityData.defineId(CattleEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> IS_MALE = SynchedEntityData.defineId(CattleEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> WEIGHT = SynchedEntityData.defineId(CattleEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> AGE_DAYS = SynchedEntityData.defineId(CattleEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_PREGNANT = SynchedEntityData.defineId(CattleEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> BIRTH_COUNT = SynchedEntityData.defineId(CattleEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HUNGER = SynchedEntityData.defineId(CattleEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DISEASE = SynchedEntityData.defineId(CattleEntity.class, EntityDataSerializers.STRING);

    // --- SABİTLER VE SAYAÇLAR ---
    private int pregnancyTimer = 0;
    public static final int GESTATION_PERIOD = 216000;
    public static final int POST_BIRTH_COOLDOWN = 216000;
    public static final int BABY_GROWTH_DAYS = 7;
    private int breedingCooldown = 0;
    private String fatherBreed = "Melez";

    // Gübre Sayacı ve Metabolizma
    private int metabolismTimer = 0;
    private int manureTimer = 0;

    private static final String[] BREEDS = {"Holstein", "Angus", "Simmental", "Jersey", "Melez"};

    public CattleEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        if (!level.isClientSide()) {
            String randomBreed = BREEDS[this.random.nextInt(BREEDS.length)];
            this.setBreed(randomBreed);
            this.setMale(this.random.nextBoolean());
            float randomWeight = 30.0F + this.random.nextFloat() * 15.0F;
            this.setWeight(randomWeight);
            this.setAgeDays(0);
            this.setBirthCount(0);
            this.setHunger(50);
            this.setDisease("NONE");
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    // --- OYUN DÖNGÜSÜ (TICK) ---
    @Override
    public void aiStep() {
        super.aiStep();

        // 1. Görsel Efektler (Client)
        if (this.level().isClientSide() && !this.getDisease().equals("NONE")) {
            DiseaseData data = DiseaseManager.getDiseaseById(this.getDisease());
            if (data != null && data.visualEffect != null) {
                if (data.visualEffect.equals("SNEEZE") && this.random.nextInt(40) == 0) {
                    double d0 = this.getX() - (double)this.getBbWidth() * Math.sin(this.yBodyRot * ((float)Math.PI / 180F));
                    double d1 = this.getY() + (double)this.getEyeHeight() - 0.5D;
                    double d2 = this.getZ() + (double)this.getBbWidth() * Math.cos(this.yBodyRot * ((float)Math.PI / 180F));
                    this.level().addParticle(ParticleTypes.SNEEZE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                } else if (data.visualEffect.equals("SMOKE") && this.random.nextInt(40) == 0) {
                    this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.5D, this.getZ(), 0.0D, 0.05D, 0.0D);
                }
            }
        }

        // 2. Metabolizma ve Hastalık İlerlemesi (Server)
        if (!this.level().isClientSide()) {

            // --- GÜBRE SİSTEMİ (Her 12000 tick / Yarım Gün) ---
            if (!this.isBaby()) {
                this.manureTimer++;
                if (this.manureTimer >= 12000) {
                    this.manureTimer = 0;
                    this.spawnAtLocation(ModItems.MANURE.get());
                    this.playSound(SoundEvents.CHICKEN_EGG, 1.0F, 0.5F);
                }
            }

            // Büyüme
            if (this.getAgeDays() < BABY_GROWTH_DAYS) {
                if (!this.isBaby()) this.setAge(-24000);
            } else if (this.isBaby()) {
                this.setAge(0);
            }

            // Günlük Sayaçlar
            if (this.tickCount % 24000 == 0) {
                this.setAgeDays(this.getAgeDays() + 1);
                if (!this.isMale() && this.breedingCooldown > 0 && this.getHunger() > 30) {
                    this.spawnAtLocation(Items.MILK_BUCKET);
                }
            }

            // Hastalık Hızı
            if (!this.getDisease().equals("NONE")) this.setSpeed(0.1F);
            else this.setSpeed(0.2F);

            // Dakikalık Metabolizma Döngüsü (1200 tick = 1 dakika)
            this.metabolismTimer++;
            if (this.metabolismTimer >= 1200) {
                this.metabolismTimer = 0;

                // A) Açlık Yönetimi
                int currentHunger = this.getHunger();
                if (currentHunger > 0) this.setHunger(currentHunger - 2);

                // B) Kilo Yönetimi
                float currentWeight = this.getWeight();
                if (currentHunger >= 80) {
                    float gain = this.isBaby() ? 0.8F : 0.4F;
                    if (currentWeight < 900.0F) this.setWeight(currentWeight + gain);
                } else if (currentHunger <= 20) {
                    float loss = 0.5F;
                    if (currentWeight > 30.0F) this.setWeight(currentWeight - loss);
                }

                // C) HASTALIK ETKİLERİ
                String currentDisease = this.getDisease();
                if (!currentDisease.equals("NONE")) {
                    DiseaseData data = DiseaseManager.getDiseaseById(currentDisease);
                    if (data != null) {
                        // 1. Can Yakma
                        if (data.damagePerTick > 0) {
                            this.hurt(this.damageSources().starve(), (float)data.damagePerTick);
                        }
                        // 2. Kilo Kaybı
                        if (this.getWeight() > 30.0F) {
                            this.setWeight(this.getWeight() - (float)data.weightLossPerTick);
                        }
                        // 3. ABORT Riski
                        if (this.isPregnant() && data.abortChance > 0) {
                            if (this.random.nextDouble() < data.abortChance) {
                                triggerAbortion();
                            }
                        }
                    }
                } else {
                    // Hasta değilse yeni hastalık kapma kontrolü (Rutin)
                    DiseaseData newDisease = DiseaseManager.checkForDisease(this.getHunger());
                    if (newDisease != null) this.setDisease(newDisease.id);
                }
            }

            // Gebelik Sayacı
            if (this.isPregnant()) {
                this.pregnancyTimer--;
                if (this.pregnancyTimer <= 0) this.giveBirth();
            }
            if (this.breedingCooldown > 0) this.breedingCooldown--;
        }
    }

    // --- ETKİLEŞİM YÖNETİCİSİ ---
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand == InteractionHand.OFF_HAND) return InteractionResult.PASS;

        ItemStack itemstack = player.getItemInHand(hand);

        if (!this.level().isClientSide()) {

            // --- 1. SPERMA ALMA (BOĞADAN) ---
            if (itemstack.is(ModItems.EMPTY_STRAW.get())) {
                if (this.isMale() && !this.isBaby()) {
                    itemstack.shrink(1);

                    ItemStack filledStraw = new ItemStack(ModItems.FILLED_STRAW.get());
                    CompoundTag tag = filledStraw.getOrCreateTag();
                    tag.putString("VetSim_Breed", this.getBreed());
                    filledStraw.setTag(tag);

                    if (!player.getInventory().add(filledStraw)) {
                        player.drop(filledStraw, false);
                    }

                    this.playSound(SoundEvents.COW_AMBIENT, 1.0F, 1.0F);
                    player.sendSystemMessage(Component.literal("§aSperma alındı. Irk: " + this.getBreed()));
                    return InteractionResult.SUCCESS;
                } else {
                    player.sendSystemMessage(Component.literal("§cSadece yetişkin boğalardan sperma alabilirsiniz!"));
                    return InteractionResult.CONSUME;
                }
            }

            // --- 2. SUNİ TOHUMLAMA (İNEĞE) ---
            if (itemstack.is(ModItems.FILLED_STRAW.get())) {
                if (!this.isMale() && !this.isBaby()) {
                    if (!this.isPregnant()) {
                        String fatherBreed = "Melez";
                        if (itemstack.hasTag() && itemstack.getTag().contains("VetSim_Breed")) {
                            fatherBreed = itemstack.getTag().getString("VetSim_Breed");
                        }

                        // %75 Şansla Tutma
                        if (this.random.nextFloat() < 0.75F) {
                            this.startPregnancy(fatherBreed);
                            this.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                            ((ServerLevel)this.level()).sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 1.0, this.getZ(), 5, 0.5, 0.5, 0.5, 0.0);
                            player.sendSystemMessage(Component.literal("§d ♥ Suni tohumlama BAŞARILI! Gebelik başladı."));
                        } else {
                            this.playSound(SoundEvents.COW_HURT, 1.0F, 1.0F);
                            player.sendSystemMessage(Component.literal("§c x Tohumlama başarısız oldu. Tekrar deneyin."));
                        }

                        itemstack.shrink(1);
                        return InteractionResult.SUCCESS;
                    } else {
                        player.sendSystemMessage(Component.literal("§cBu hayvan zaten gebe!"));
                        return InteractionResult.PASS;
                    }
                } else {
                    player.sendSystemMessage(Component.literal("§cSadece dişi inekleri tohumlayabilirsiniz."));
                    return InteractionResult.PASS;
                }
            }

            // --- 3. LAB: KAN ALMA ---
            if (itemstack.is(ModItems.EMPTY_BLOOD_TUBE.get())) {
                this.playSound(SoundEvents.BOTTLE_FILL, 1.0F, 1.0F);
                ItemStack bloodSample = new ItemStack(ModItems.FILLED_BLOOD_TUBE.get());
                CompoundTag tag = bloodSample.getOrCreateTag();

                tag.putString("VetSim_EarTag", this.getEarTag());
                float wbc = 5.0F + this.random.nextFloat() * 5.0F;
                float ph = 7.35F + this.random.nextFloat() * 0.10F;

                String currentDisease = this.getDisease();
                if (currentDisease.equals("pneumonia")) {
                    wbc = 18.0F + this.random.nextFloat() * 12.0F;
                } else if (currentDisease.equals("acidosis")) {
                    ph = 6.90F + this.random.nextFloat() * 0.30F;
                }

                tag.putFloat("VetSim_WBC", wbc);
                tag.putFloat("VetSim_PH", ph);
                bloodSample.setTag(tag);

                if (!player.getAbilities().instabuild) itemstack.shrink(1);
                if (!player.getInventory().add(bloodSample)) player.drop(bloodSample, false);

                player.sendSystemMessage(Component.literal("§cKan örneği alındı. Etiket: " + this.getEarTag()));
                return InteractionResult.SUCCESS;
            }

            // --- 4. VETERİNER ALETLERİ ---
            if (itemstack.is(ModItems.VET_CLIPBOARD.get())) { printAnamnesis(player); return InteractionResult.SUCCESS; }
            if (itemstack.is(ModItems.STETHOSCOPE.get())) { printStethoscope(player); return InteractionResult.SUCCESS; }
            if (itemstack.is(ModItems.THERMOMETER.get())) { printTemperature(player); return InteractionResult.SUCCESS; }
            if (itemstack.is(ModItems.ANTIBIOTICS.get())) { tryCure(player, itemstack, "ITEM"); return InteractionResult.SUCCESS; }
            if (itemstack.is(ModItems.PENICILLIN.get())) { tryCure(player, itemstack, "ITEM"); return InteractionResult.SUCCESS; }
            if (itemstack.is(ModItems.FLUNIXIN.get())) {
                player.sendSystemMessage(Component.literal("§eAğrı kesici uygulandı. Hayvan rahatladı."));
                if (!player.getAbilities().instabuild) itemstack.shrink(1);
                return InteractionResult.SUCCESS;
            }

            // --- 5. YEMLEME (JSON SİSTEMİ) ---
            FeedData feedData = FeedManager.getFeedData(itemstack);
            if (feedData != null) {
                this.feed(itemstack, feedData);
                return InteractionResult.SUCCESS;
            }

            // --- 6. BOŞ EL (BİLGİ) ---
            if (itemstack.isEmpty()) {
                if (player.isCrouching()) printVisualInspection(player);
                else showVetInfo(player);
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }

    // --- YARDIMCI METODLAR ---

    // Düşük Yapma Tetikleyicisi
    private void triggerAbortion() {
        if (!this.level().isClientSide) {
            this.entityData.set(IS_PREGNANT, false);
            this.pregnancyTimer = 0;
            this.fatherBreed = "Melez";
            this.breedingCooldown = 48000;

            this.playSound(SoundEvents.COW_HURT, 1.0F, 0.5F);
            this.level().broadcastEntityEvent(this, (byte) 61);

            if (this.level() instanceof ServerLevel serverLevel) {
                for (Player player : serverLevel.players()) {
                    if (player.distanceToSqr(this) < 256) {
                        player.sendSystemMessage(Component.literal("§c⚠ DİKKAT: " + this.getEarTag() + " küpeli inek DÜŞÜK YAPTI!"));
                    }
                }
            }
        }
    }

    private void printAnamnesis(Player player) {
        player.sendSystemMessage(Component.literal("§6--- [ 📔 GÜNLÜK KAYITLARI ] ---"));
        if (this.getDisease().equals("NONE")) {
            player.sendSystemMessage(Component.literal("§a ✓ Rutin kontroller normal."));
        } else {
            DiseaseData data = DiseaseManager.getDiseaseById(this.getDisease());
            if (data != null && data.symptoms != null)
                player.sendSystemMessage(Component.literal("§e ⚠ NOT: §f" + data.symptoms.anamnesis));
        }
    }

    private void printStethoscope(Player player) {
        this.playSound(SoundEvents.PLAYER_BREATH, 1.0F, 1.0F);
        if (this.getDisease().equals("NONE")) {
            player.sendSystemMessage(Component.literal("§aKalp/Ciğer: §fNormal ritim (60-80 bpm)."));
        } else {
            DiseaseData data = DiseaseManager.getDiseaseById(this.getDisease());
            if (data != null && data.symptoms != null)
                player.sendSystemMessage(Component.literal("§cBulgular: §f" + data.symptoms.stethoscope));
        }
    }

    private void printTemperature(Player player) {
        this.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5F, 2.0F);
        double temp = 38.5;
        if (!this.getDisease().equals("NONE")) {
            DiseaseData data = DiseaseManager.getDiseaseById(this.getDisease());
            if (data != null && data.symptoms != null) temp = data.symptoms.temperature;
        }
        String color = (temp > 39.5) ? "§c" : "§a";
        player.sendSystemMessage(Component.literal("§6[ 🌡️ ] Vücut Isısı: " + color + temp + " °C"));
    }

    private void printVisualInspection(Player player) {
        if (!this.getDisease().equals("NONE")) {
            DiseaseData data = DiseaseManager.getDiseaseById(this.getDisease());
            if (data != null && data.symptoms != null)
                player.sendSystemMessage(Component.literal("§cGözlem: §f" + data.symptoms.visual));
        } else {
            player.sendSystemMessage(Component.literal("§aGözlem: §fDuruşu canlı, tüyleri parlak."));
        }
    }

    private void tryCure(Player player, ItemStack itemstack, String type) {
        if (!this.getDisease().equals("NONE")) {
            DiseaseData data = DiseaseManager.getDiseaseById(this.getDisease());
            if (data != null && type.equals(data.cureType)) {
                String itemId = BuiltInRegistries.ITEM.getKey(itemstack.getItem()).toString();
                if (data.cureTarget.equals(itemId) || (data.cureTarget.equals("vetsim:antibiotics") && itemstack.is(ModItems.ANTIBIOTICS.get()))) {
                    this.setDisease("NONE");
                    this.playSound(SoundEvents.GENERIC_DRINK, 1.0F, 1.0F);
                    player.sendSystemMessage(Component.literal("§a ✓ Tedavi Başarılı!"));
                    if (!player.getAbilities().instabuild) itemstack.shrink(1);
                }
            }
        }
    }

    public void feed(ItemStack stack, FeedData foodData) {
        int currentHunger = this.getHunger();
        if (currentHunger < 100) {
            this.setHunger(Math.min(currentHunger + foodData.nutrition, 100));
            this.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
            this.level().broadcastEntityEvent(this, (byte) 18);

            // HASTALIK RİSKİ KONTROLÜ (JSON SİSTEMİ)
            if (!this.level().isClientSide) {
                // DiseaseManager otomatik olarak itemin riskini hesaplar
                String riskResult = DiseaseManager.calculateRisk(stack, this.random);

                if (riskResult != null) {
                    this.setDisease(riskResult);
                    this.playSound(SoundEvents.ZOMBIE_INFECT, 1.0F, 1.0F);
                    // System.out.println("Zehirlenme Tetiklendi: " + riskResult);
                }
            }

            stack.shrink(1);
        }

        // Beslenme ile tedavi
        if (!this.getDisease().equals("NONE")) {
            DiseaseData data = DiseaseManager.getDiseaseById(this.getDisease());
            if (data != null && "FEED".equals(data.cureType)) {
                if (foodData.itemId.equals(data.cureTarget)) {
                    this.setDisease("NONE");
                    this.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.0F);
                }
            }
        }
    }

    private void showVetInfo(Player player) {
        player.sendSystemMessage(Component.literal("§6=============================="));
        player.sendSystemMessage(Component.literal("§6     [ 📋 VET SİSTEMİ KAYDI ]"));
        player.sendSystemMessage(Component.literal("§6=============================="));
        player.sendSystemMessage(Component.literal("§b 🆔 Küpe No : §f" + this.getEarTag()));

        String breedColor = "§f";
        if (this.getBreed().equals("Angus")) breedColor = "§8";
        if (this.getBreed().equals("Jersey")) breedColor = "§6";
        if (this.getBreed().equals("Simmental")) breedColor = "§c";
        player.sendSystemMessage(Component.literal("§e 🧬 Irk      : " + breedColor + this.getBreed()));

        String ageStatus = this.isBaby() ? "§b(BUZAĞI)" : "§a(YETİŞKİN)";
        String genderStr = this.isMale() ? "§bErkek (Boğa) " + ageStatus : "§dDişi (İnek) " + ageStatus;
        player.sendSystemMessage(Component.literal("§e ⚤ Cinsiyet : " + genderStr));

        String weightStr = String.format("%.1f", this.getWeight());
        player.sendSystemMessage(Component.literal("§e ⚖️ Ağırlık  : §f" + weightStr + " kg"));
        player.sendSystemMessage(Component.literal("§e 🎂 Yaş      : §f" + this.getAgeDays() + " gün"));

        int hunger = this.getHunger();
        String barColor = (hunger > 70) ? "§a" : (hunger > 30) ? "§e" : "§c";
        String hungerStatus = (hunger > 80) ? "(Beside)" : (hunger < 20) ? "(Zayıflıyor!)" : "(Stabil)";
        player.sendSystemMessage(Component.literal("§e 🍽️ Tokluk   : " + barColor + createProgressBar(hunger) + " §f%" + hunger + " " + hungerStatus));

        String currentDisease = this.getDisease();
        if (!currentDisease.equals("NONE")) {
            player.sendSystemMessage(Component.literal("§c ☣ SAĞLIK UYARISI: Hayvan Hasta!"));
            player.sendSystemMessage(Component.literal("§c ⚠ Tanı için Stetoskop ve Kan Tahlili yapın."));
        } else {
            player.sendSystemMessage(Component.literal("§a ♥ Sağlık    : Stabil"));
        }

        if (!this.isMale() && !this.isBaby()) {
            player.sendSystemMessage(Component.literal("§7------------------------------"));
            player.sendSystemMessage(Component.literal("§a 🍼 Toplam Doğum : §f" + this.getBirthCount()));

            if (this.isPregnant()) {
                int daysLeft = this.pregnancyTimer / 24000;
                player.sendSystemMessage(Component.literal("§d ♥ GEBE - Doğuma " + daysLeft + " gün kaldı."));
            } else {
                if (this.breedingCooldown > 0) {
                    int cooldownDays = this.breedingCooldown / 24000;
                    player.sendSystemMessage(Component.literal("§b 🥛 Süt Verimi: AKTİF (" + cooldownDays + " gün kaldı)"));
                } else {
                    player.sendSystemMessage(Component.literal("§a ✓ Çiftleşmeye Hazır (Kızgınlıkta)"));
                }
            }
        }
        player.sendSystemMessage(Component.literal("§6=============================="));
    }

    private String createProgressBar(int value) {
        int bars = value / 10;
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 10; i++) {
            if (i < bars) sb.append("|"); else sb.append(".");
        }
        sb.append("]");
        return sb.toString();
    }

    // --- VERİ SENKRONİZASYONU (SAYACLAR DAHİL) ---
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(EAR_TAG, "TR000000");
        this.entityData.define(BREED, "Holstein");
        this.entityData.define(IS_MALE, false);
        this.entityData.define(WEIGHT, 40.0F);
        this.entityData.define(AGE_DAYS, 0);
        this.entityData.define(IS_PREGNANT, false);
        this.entityData.define(BIRTH_COUNT, 0);
        this.entityData.define(HUNGER, 50);
        this.entityData.define(DISEASE, "NONE");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("VetSim_EarTag", this.getEarTag());
        compound.putString("VetSim_Breed", this.getBreed());
        compound.putBoolean("VetSim_IsMale", this.isMale());
        compound.putFloat("VetSim_Weight", this.getWeight());
        compound.putInt("VetSim_AgeDays", this.getAgeDays());
        compound.putBoolean("VetSim_IsPregnant", this.isPregnant());
        compound.putInt("VetSim_PregnancyTimer", this.pregnancyTimer);
        compound.putString("VetSim_FatherBreed", this.fatherBreed);
        compound.putInt("VetSim_BreedingCooldown", this.breedingCooldown);
        compound.putInt("VetSim_BirthCount", this.getBirthCount());
        compound.putInt("VetSim_Hunger", this.getHunger());
        compound.putString("VetSim_Disease", this.getDisease());

        // Gübre Sayacı Kaydı
        compound.putInt("VetSim_ManureTimer", this.manureTimer);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("VetSim_EarTag")) this.setEarTag(compound.getString("VetSim_EarTag"));
        if (compound.contains("VetSim_Breed")) this.setBreed(compound.getString("VetSim_Breed"));
        if (compound.contains("VetSim_IsMale")) this.setMale(compound.getBoolean("VetSim_IsMale"));
        if (compound.contains("VetSim_Weight")) this.setWeight(compound.getFloat("VetSim_Weight"));
        if (compound.contains("VetSim_AgeDays")) this.setAgeDays(compound.getInt("VetSim_AgeDays"));
        if (compound.contains("VetSim_IsPregnant")) this.entityData.set(IS_PREGNANT, compound.getBoolean("VetSim_IsPregnant"));
        if (compound.contains("VetSim_PregnancyTimer")) this.pregnancyTimer = compound.getInt("VetSim_PregnancyTimer");
        if (compound.contains("VetSim_FatherBreed")) this.fatherBreed = compound.getString("VetSim_FatherBreed");
        if (compound.contains("VetSim_BreedingCooldown")) this.breedingCooldown = compound.getInt("VetSim_BreedingCooldown");
        if (compound.contains("VetSim_BirthCount")) this.setBirthCount(compound.getInt("VetSim_BirthCount"));
        if (compound.contains("VetSim_Hunger")) this.setHunger(compound.getInt("VetSim_Hunger"));
        if (compound.contains("VetSim_Disease")) this.setDisease(compound.getString("VetSim_Disease"));

        // Gübre Sayacı Yükleme
        if (compound.contains("VetSim_ManureTimer")) this.manureTimer = compound.getInt("VetSim_ManureTimer");
    }

    // --- GETTER & SETTER ---
    public String getDisease() { return this.entityData.get(DISEASE); }
    public void setDisease(String disease) { this.entityData.set(DISEASE, disease); }
    public int getHunger() { return this.entityData.get(HUNGER); }
    public void setHunger(int h) { this.entityData.set(HUNGER, Math.max(0, Math.min(100, h))); }
    public int getBirthCount() { return this.entityData.get(BIRTH_COUNT); }
    public void setBirthCount(int count) { this.entityData.set(BIRTH_COUNT, count); }
    public boolean isPregnant() { return this.entityData.get(IS_PREGNANT); }
    public int getBreedingCooldown() { return breedingCooldown; }
    public void setBreedingCooldown(int cd) { this.breedingCooldown = cd; }
    public String getEarTag() { return this.entityData.get(EAR_TAG); }
    public void setEarTag(String tag) {
        this.entityData.set(EAR_TAG, tag);
        this.setCustomName(Component.literal("Küpe: " + tag));
        this.setCustomNameVisible(true);
    }
    public String getBreed() { return this.entityData.get(BREED); }
    public void setBreed(String breed) { this.entityData.set(BREED, breed); }
    public boolean isMale() { return this.entityData.get(IS_MALE); }
    public void setMale(boolean isMale) { this.entityData.set(IS_MALE, isMale); }
    public float getWeight() { return this.entityData.get(WEIGHT); }
    public void setWeight(float weight) { this.entityData.set(WEIGHT, weight); }
    public int getAgeDays() { return this.entityData.get(AGE_DAYS); }
    public void setAgeDays(int days) { this.entityData.set(AGE_DAYS, days); }

    // --- BAŞLANGIÇ ---
    @Override
    public SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty, net.minecraft.world.entity.MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        if (this.getEarTag().equals("TR000000")) this.setEarTag(generateEarTag());
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    public void startPregnancy(String fatherBreedInput) {
        this.entityData.set(IS_PREGNANT, true);
        this.pregnancyTimer = GESTATION_PERIOD;
        this.fatherBreed = fatherBreedInput;
    }

    private void giveBirth() {
        if (this.level() instanceof ServerLevel serverLevel) {
            CattleEntity baby = ModEntities.CATTLE.get().create(serverLevel);
            if (baby != null) {
                String inheritedBreed = this.random.nextBoolean() ? this.getBreed() : this.fatherBreed;
                baby.setBreed(inheritedBreed);
                baby.setWeight(25.0F);
                baby.setAgeDays(0);
                baby.setAge(-24000);
                baby.moveTo(this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F);
                serverLevel.addFreshEntity(baby);
                this.entityData.set(IS_PREGNANT, false);
                this.pregnancyTimer = 0;
                this.breedingCooldown = POST_BIRTH_COOLDOWN;
                this.setBirthCount(this.getBirthCount() + 1);
            }
        }
    }

    private String generateEarTag() { return "TR" + (this.random.nextInt(900000) + 100000); }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 2.0D));
        this.goalSelector.addGoal(2, new EatFromTroughGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new NaturalBreedingGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide()) {
            int meatAmount = (int) (this.getWeight() / 100.0F);
            this.spawnAtLocation(Items.BEEF, Math.max(1, meatAmount));
            this.spawnAtLocation(Items.LEATHER, this.random.nextInt(3) + 1);
        }
        super.die(damageSource);
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (!this.level().isClientSide() && this.getEarTag().equals("TR000000")) this.setEarTag(generateEarTag());
    }

    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) { return null; }
    @Override public boolean isFood(ItemStack stack) { return false; }
}