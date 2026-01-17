package com.vetsim.vetcraft.entity;

import com.vetsim.vetcraft.entity.ai.EatFromTroughGoal;
import com.vetsim.vetcraft.entity.ai.NaturalBreedingGoal;
import com.vetsim.vetcraft.entity.components.CattleHealth;
import com.vetsim.vetcraft.entity.components.CattleMetabolism;
import com.vetsim.vetcraft.entity.components.CattleReproduction;
import com.vetsim.vetcraft.init.ModItems;
import com.vetsim.vetcraft.util.*;
import com.vetsim.vetcraft.VetCraft;
import com.vetsim.vetcraft.config.VetCraftConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
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

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import com.vetsim.vetcraft.util.VetDiagnostics;

public class CattleEntity extends Animal {

    // --- SENKRONİZE VERİLER ---
    private static final EntityDataAccessor<String> EAR_TAG = SynchedEntityData.defineId(CattleEntity.class,
            EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> BREED = SynchedEntityData.defineId(CattleEntity.class,
            EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> IS_MALE = SynchedEntityData.defineId(CattleEntity.class,
            EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> WEIGHT = SynchedEntityData.defineId(CattleEntity.class,
            EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> AGE_DAYS = SynchedEntityData.defineId(CattleEntity.class,
            EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_PREGNANT = SynchedEntityData.defineId(CattleEntity.class,
            EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> BIRTH_COUNT = SynchedEntityData.defineId(CattleEntity.class,
            EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HUNGER = SynchedEntityData.defineId(CattleEntity.class,
            EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DISEASE = SynchedEntityData.defineId(CattleEntity.class,
            EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> SECONDARY_DISEASE = SynchedEntityData.defineId(CattleEntity.class,
            EntityDataSerializers.STRING);
    private static final EntityDataAccessor<java.util.Optional<java.util.UUID>> OWNER_UUID = SynchedEntityData
            .defineId(CattleEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    // --- SİSTEM BİLEŞENLERİ ---
    private final CattleReproduction reproductionSystem;
    private final CattleMetabolism metabolismSystem;
    private final CattleHealth healthSystem;

    private EatBlockGoal eatBlockGoal;

    public static final int GESTATION_PERIOD = VetCraftConfig.GESTATION_PERIOD; // doum
    public static final int POST_BIRTH_COOLDOWN = VetCraftConfig.POST_BIRTH_COOLDOWN; // diostrus
    public static final int BABY_GROWTH_DAYS = VetCraftConfig.BABY_GROWTH_DAYS;

    public CattleEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);

        // Sistemleri başlat
        this.reproductionSystem = new CattleReproduction(this);
        this.metabolismSystem = new CattleMetabolism(this);
        this.healthSystem = new CattleHealth(this);

        if (!level.isClientSide()) {
            try {
                // Refaktör: Irkları JSON'dan çek
                BreedData randomBreedData = BreedManager.getRandomBreed();
                String breedName = (randomBreedData != null) ? randomBreedData.displayName : "Holstein";

                this.setBreed(breedName);
                this.setMale(this.random.nextBoolean());
                float randomWeight = 30.0F + this.random.nextFloat() * 15.0F;
                this.setWeight(randomWeight);
                this.setAgeDays(0);
                this.setBirthCount(0);
                this.setHunger(50);
                this.setDisease("NONE");

                VetCraft.LOGGER.info("CattleEntity Spawned: " + breedName + ", Pos: " + this.blockPosition());

            } catch (Exception e) {
                VetCraft.LOGGER.error("Error initializing CattleEntity: " + e.getMessage());
                e.printStackTrace();
                // Fallback default
                this.setBreed("Holstein");
                this.setMale(false);
                this.setWeight(40.0F);
            }
        }
    }

    // --- OYUN DÖNGÜSÜ ---
    @Override
    public void aiStep() {
        super.aiStep();

        // 1. Görsel Efektler (Client Side)
        if (this.level().isClientSide() && !this.getDisease().equals("NONE")) {
            DiseaseData data = DiseaseManager.getDiseaseById(this.getDisease());
            if (data != null && data.visualEffect != null) {
                if (data.visualEffect.equals("SNEEZE") && this.random.nextInt(40) == 0) {
                    double d0 = this.getX()
                            - (double) this.getBbWidth() * Math.sin(this.yBodyRot * ((float) Math.PI / 180F));
                    double d1 = this.getY() + (double) this.getEyeHeight() - 0.5D;
                    double d2 = this.getZ()
                            + (double) this.getBbWidth() * Math.cos(this.yBodyRot * ((float) Math.PI / 180F));
                    this.level().addParticle(ParticleTypes.SNEEZE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                } else if (data.visualEffect.equals("SMOKE") && this.random.nextInt(40) == 0) {
                    this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.5D, this.getZ(), 0.0D,
                            0.05D, 0.0D);
                }
            }
        }

        // 2. Mantıksal İşlemler (Server Side)
        if (!this.level().isClientSide()) {
            this.reproductionSystem.tick();
            this.metabolismSystem.tick();
            this.healthSystem.tick();

            if (!this.level().isClientSide()) {
                // Vanilla yaşını kontrol et ve AGE_DAYS'i buna göre güncelle (Sadece bilgi
                // amaçlı)
                if (this.isBaby()) {
                    this.setAgeDays(0);
                } else {
                    // Erginse, kaç gündür ergin olduğunu takip edebilirsin
                    if (this.tickCount % 24000 == 0) {
                        this.setAgeDays(this.getAgeDays() + 1);
                    }
                }
            }

            if (!this.getDisease().equals("NONE")) {
                DiseaseData data = DiseaseManager.getDiseaseById(this.getDisease());
                if (data != null && data.slowness)
                    this.setSpeed(0.08F);
                else
                    this.setSpeed(0.12F);
            } else {
                // Sağlıklı hız (Tırnak sistemi kaldırıldı)
                this.setSpeed(0.2F);
            }
        }
    }

    // --- ETKİLEŞİM YÖNETİCİSİ ---
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand == InteractionHand.OFF_HAND)
            return InteractionResult.PASS;

        ItemStack itemstack = player.getItemInHand(hand);

        // --- 0. BUZAĞI BESLEME (COLOSTRUM/MAMA) ---
        if (this.isBaby()) {
            if (itemstack.is(ModItems.COLOSTRUM_BUCKET.get())) {
                this.metabolismSystem.feedColostrum();
                if (!player.getAbilities().instabuild) {
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }
                return InteractionResult.SUCCESS;
            }
            if (itemstack.is(ModItems.CALF_FORMULA.get())) {
                this.metabolismSystem.feedCalfFormula();
                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
        }

        // --- 1. SU KOVASI (EN ÜSTTE OLMALI!) ---
        // Bunu en tepeye koyuyoruz ki oyun başka bir şey yapmaya fırsat bulamasın.
        if (itemstack.is(Items.WATER_BUCKET)) {

            // Sesi hem Client hem Server duysun
            player.playSound(SoundEvents.GENERIC_DRINK, 1.0f, 1.0f);

            if (!this.level().isClientSide) {
                // Suyu içir
                this.metabolismSystem.drink(100.0f);
                player.sendSystemMessage(Component.literal("§b 💧 İnek su içti."));

                // Yaratıcı modda değilse kovayı boşalt
                if (!player.getAbilities().instabuild) {
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }
            }

            // KRİTİK NOKTA: 'SUCCESS' döndürerek "Yere su koyma" işlemini %100 iptal
            // ediyoruz.
            return InteractionResult.SUCCESS;
        }

        // --- 1.5 SÜT SAĞIM (MANUEL) ---
        if (itemstack.is(Items.BUCKET)) {
            if (!this.level().isClientSide && !this.isBaby() && !this.isMale()) {
                // Laktasyon kontrolü
                if (!this.metabolismSystem.isLactationActive()) {
                    player.sendSystemMessage(
                            Component.literal("§cBu inek şu an süt vermiyor (Kuru dönem veya laktasyon dışı)."));
                    return InteractionResult.FAIL;
                }

                ItemStack milkBucket = new ItemStack(Items.MILK_BUCKET);

                // Phase 18: Kalıntı Süresi Kontrolü
                if (this.healthSystem.isWithdrawalActive()) {
                    player.sendSystemMessage(Component.literal("§c⚠ DİKKAT: Sütte ilaç kalıntısı var! (Tüketilemez)"));
                    player.playSound(SoundEvents.COW_HURT, 1.0f, 1.0f);

                    // İsimlendir: İlaçlı Süt
                    milkBucket.setHoverName(Component.literal("§cİlaçlı Süt (Tüketilemez)"));
                }

                // Hastalık/Stres Kontrolleri (Metabolizma üzerinden yapılabilir veya burada
                // basitçe)
                // Basitlik için Metabolism'deki checks'leri public yapıp çağırabiliriz veya
                // mantığı buraya taşıyabiliriz.
                // Şimdilik Metabolism.tryMilk() gibi bir metod olmadığı için temel kontolleri
                // buraya ekliyorum.

                // İleride Metabolism.tryMilk(player) yapılabilir.

                player.playSound(SoundEvents.COW_MILK, 1.0f, 1.0f);
                itemstack.shrink(1);

                if (itemstack.isEmpty()) {
                    player.setItemInHand(hand, milkBucket);
                } else if (!player.getInventory().add(milkBucket)) {
                    player.drop(milkBucket, false);
                }

                this.metabolismSystem.resetMilking();
                player.sendSystemMessage(Component.literal("§aİnek sağıldı ve rahatladı."));

                return InteractionResult.SUCCESS;
            }
        }

        if (!this.level().isClientSide()) {

            // 1. SPERMA ALMA
            if (itemstack.is(ModItems.EMPTY_STRAW.get())) {
                if (this.isMale() && !this.isBaby()) {
                    // Check Cooldown
                    if (this.getBreedingCooldown() > 0) {
                        int secondsLeft = this.getBreedingCooldown() / 20;
                        player.sendSystemMessage(
                                Component.literal("§cBoğa yorgun. Dinlenmesi gerekiyor (" + secondsLeft + "s)."));
                        return InteractionResult.CONSUME; // Fail but consume interaction event (not item)
                    }

                    itemstack.shrink(1);
                    ItemStack filledStraw = new ItemStack(ModItems.FILLED_STRAW.get());
                    CompoundTag tag = filledStraw.getOrCreateTag();

                    // Irk Bilgisi
                    tag.putString("VetSim_Breed", this.getBreed());

                    // Phase 19: Doğal Boğa Genetiği Üret
                    float rnd = this.random.nextFloat();
                    String quality = "Commercial";

                    float milkPTA = (this.random.nextFloat() * 2.0f) - 0.5f;
                    float healthPTA = (this.random.nextFloat() * 0.8f) - 0.2f;

                    if (rnd < 0.1f) {
                        quality = "Elite";
                        milkPTA += 1.0f;
                        healthPTA += 0.3f;
                    } else if (rnd < 0.3f) {
                        quality = "Superior";
                        milkPTA += 0.5f;
                        healthPTA += 0.1f;
                    }

                    tag.putString("VetSim_Quality", quality);
                    tag.putFloat("VetSim_MilkPTA", milkPTA);
                    tag.putFloat("VetSim_HealthPTA", healthPTA);

                    filledStraw.setTag(tag);
                    if (!player.getInventory().add(filledStraw))
                        player.drop(filledStraw, false);

                    this.playSound(SoundEvents.COW_AMBIENT, 1.0F, 1.0F);
                    player.sendSystemMessage(
                            Component.literal("§aSperma alındı. Irk: " + this.getBreed() + " (" + quality + ")"));

                    // Set Cooldown (5 Minutes = 6000 Ticks)
                    this.reproductionSystem.setBreedingCooldown(6000);

                    return InteractionResult.SUCCESS;
                } else {
                    player.sendSystemMessage(Component.literal("§cSadece yetişkin boğalardan sperma alabilirsiniz!"));
                    return InteractionResult.CONSUME;
                }
            }

            // 2. SUNİ TOHUMLAMA
            if (itemstack.is(ModItems.FILLED_STRAW.get())) {
                if (!this.isMale() && !this.isBaby()) {
                    String fatherBreed = "Melez";
                    String fatherQuality = "Commercial";
                    float fatherMilk = 0.0f;
                    float fatherHealth = 0.0f;

                    if (itemstack.hasTag()) {
                        CompoundTag tag = itemstack.getTag();
                        if (tag.contains("VetSim_Breed"))
                            fatherBreed = tag.getString("VetSim_Breed");
                        if (tag.contains("VetSim_Quality"))
                            fatherQuality = tag.getString("VetSim_Quality");
                        if (tag.contains("VetSim_MilkPTA"))
                            fatherMilk = tag.getFloat("VetSim_MilkPTA");
                        if (tag.contains("VetSim_HealthPTA"))
                            fatherHealth = tag.getFloat("VetSim_HealthPTA");
                    }

                    // Phase 19: Genetik Veri ile Tohumlama
                    boolean success = this.reproductionSystem.tryInseminate(fatherBreed, fatherQuality, fatherMilk,
                            fatherHealth);

                    if (success) {
                        this.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                        player.sendSystemMessage(Component.literal("§d♥ İnek tohumlandı! (Baba: " + fatherBreed + ")"));
                        itemstack.shrink(1); // Payeti harca
                    } else {
                        // DETAYLI HATA MESAJLARI (User Improvement)
                        float prog = this.reproductionSystem.getProgesterone();
                        float stress = this.healthSystem.getStressLevel();
                        float bcs = this.metabolismSystem.getBcs();

                        if (prog > 2.5f) {
                            player.sendSystemMessage(
                                    Component.literal("§c❌ Başarısız: İnek kızgınlıkta (Östrus) değil."));
                            player.sendSystemMessage(Component.literal("§7(Progesteron Yüksek: " + prog + ")"));
                        } else if (stress > 60) {
                            player.sendSystemMessage(
                                    Component.literal("§c❌ Başarısız: Hayvan aşırı stresli! (Gebeliği reddediyor)"));
                            player.sendSystemMessage(Component.literal("§7(Stres: " + stress + "/100)"));
                        } else if (bcs < 2.5f) {
                            player.sendSystemMessage(Component.literal("§c❌ Başarısız: Hayvan çok zayıf."));
                        } else if (bcs > 4.5f) {
                            player.sendSystemMessage(Component.literal("§c❌ Başarısız: Hayvan çok yağlı."));
                        } else {
                            player.sendSystemMessage(Component.literal("§c❌ Başarısız: Fizyolojik sorun."));
                        }
                        // İpucu
                        player.sendSystemMessage(
                                Component.literal("§eİpucu: PGF2a veya GnRH kullanarak döngüyü düzenleyin."));
                    }
                    return InteractionResult.SUCCESS;
                }
            }

            // 3. HORMON TEDAVİLERİ

            // A) PGF2a (Kızgınlık İğnesi / Düşük)
            if (itemstack.is(ModItems.HORMONE_PGF2A.get())) {
                if (!this.isMale()) {
                    itemstack.shrink(1);
                    this.playSound(SoundEvents.TRIPWIRE_CLICK_OFF, 1.0f, 1.0f);

                    if (this.isPregnant()) {
                        // Düşük yaptır
                        this.reproductionSystem.forceAbortion();
                        player.sendSystemMessage(
                                Component.literal("§c💉 PGF2a uygulandı: Gebelik sonlandırıldı (Abort)."));
                    } else {
                        // Kızgınlığa sok
                        this.reproductionSystem.induceEstrus();
                        player.sendSystemMessage(
                                Component.literal("§e💉 PGF2a uygulandı: Luteoliz başladı (Kızgınlık tetiklendi)."));
                    }
                    return InteractionResult.SUCCESS;
                }
            }

            // B) GNRH (Yumurtlama / Kist Tedavisi)
            if (itemstack.is(ModItems.HORMONE_GNRH.get())) {
                if (!this.isMale() && !this.isPregnant()) {
                    itemstack.shrink(1);
                    // Döngüyü başa sar (veya östrusa yaklaştır)
                    // 1800-2400 arası östrus idi. 2200 yapalım (Pro-Estrus sonu).
                    this.reproductionSystem.setEstrusCycleTimer(2300);
                    this.playSound(SoundEvents.TRIPWIRE_CLICK_OFF, 1.0f, 1.0f);
                    player.sendSystemMessage(Component.literal("§e💉 GnRH uygulandı: Östrus (Kızgınlık) tetiklendi."));
                    return InteractionResult.SUCCESS;
                } else {
                    player.sendSystemMessage(Component.literal("§cBu hayvana GnRH uygulanamaz (Gebe veya Erkek)."));
                    return InteractionResult.CONSUME;
                }
            }

            // 4. OXYTOCIN (Süt İndirme / Doğum)
            if (itemstack.is(ModItems.HORMONE_OXYTOCIN.get())) {
                if (!this.isMale()) {
                    itemstack.shrink(1);
                    this.playSound(SoundEvents.BREWING_STAND_BREW, 1.0f, 1.0f);

                    // Etki 1: Stresi Sıfırla (Süt indirmeyi engelliyorsa)
                    this.healthSystem.reduceStress(100.0f);

                    // Etki 2: Eğer laktasyondaysa ve süt vermiyorsa (bazen bugda kalabilir veya çok
                    // inatçıdır)
                    // Zorla süt verdir (sadece efekt olarak şimdilik, sağım kova ile yapılır)
                    player.sendSystemMessage(
                            Component.literal("§b💉 Oksitosin uygulandı: Süt indirme sağlandı (Meme rahatladı)."));

                    // Etki 3: Doğuma yakınsa doğumu hızlandır (Bunu event'te işlemek lazım ama
                    // şimdilik mesaj verelim)
                    if (this.isPregnant() && this.reproductionSystem.isInDryPeriod()) {
                        player.sendSystemMessage(Component.literal("§d⚠ Doğum kasılmaları desteklendi."));
                    }
                    return InteractionResult.SUCCESS;
                }
            }

            // 3. LAB: KAN ALMA
            if (itemstack.is(ModItems.EMPTY_BLOOD_TUBE.get())) {
                this.playSound(SoundEvents.BOTTLE_FILL, 1.0F, 1.0F);
                ItemStack bloodSample = new ItemStack(ModItems.FILLED_BLOOD_TUBE.get());
                CompoundTag tag = bloodSample.getOrCreateTag();

                // 1. KİMLİK
                tag.putString("VetSim_EarTag", this.getEarTag());

                // 2. HEMOGRAM (WBC)
                float stress = this.healthSystem.getStressLevel();
                float wbc = 5.0F + (stress / 10.0F) + this.random.nextFloat() * 2.0F; // Baz: 5 + Stres etkisi

                // 3. pH (Asidoz/Alkaloz)
                float rumenPh = this.metabolismSystem.getRumenPh();
                float bloodPh = 7.35F + (rumenPh - 6.8F) * 0.1F; // Rumen pH kanı etkiler

                // Hastalık Etkileri
                String currentDisease = this
                        .getDisease();
                if (currentDisease.equals("pneumonia") || currentDisease.equals("mastitis"))
                    wbc += 12.0F; // Enfeksiyonda
                                  // WBC
                                  // uçar
                else if (currentDisease.equals("acidosis"))
                    bloodPh -= 0.15F;

                // 4. TOKSİSİTE (Karaciğer)
                float toxicity = this.healthSystem.getToxicity(); // 0-100 arası

                // 5. KETONLAR (Enerji Dengesi)
                // Düşük kondisyon (BCS) ve açlık (RumenFill) ketonları artırır
                float bcs = this.metabolismSystem.getBcs(); // 1-5
                float rumenFill = this.metabolismSystem.getRumenFill(); // 0-100

                float ketones = 0.5f; // Normal: 0.5 mmol/L
                if (bcs < 2.5f)
                    ketones += 1.0f; // Zayıfsa riskli
                if (rumenFill < 30.0f)
                    ketones += 1.5f; // Açsa çok riskli
                if (toxicity > 30.0f)
                    ketones += 0.5f; // Karaciğer yorgunsa artar

                // Varyasyon ekle
                ketones += (this.random.nextFloat() - 0.5f) * 0.4f;

                tag.putFloat("VetSim_WBC", wbc);
                tag.putFloat("VetSim_PH", bloodPh);
                tag.putFloat("VetSim_Toxicity", toxicity);
                tag.putFloat("VetSim_Ketones", Math.max(0.1f, ketones));

                bloodSample.setTag(tag);
                if (!player.getAbilities().instabuild)
                    itemstack.shrink(1);
                if (!player.getInventory().add(bloodSample))
                    player.drop(bloodSample, false);
                player.sendSystemMessage(Component.literal("§cKan örneği alındı. Etiket: " + this.getEarTag()));
                return InteractionResult.SUCCESS;
            }

            // 4. VETERİNER ALETLERİ
            if (itemstack.is(ModItems.VET_CLIPBOARD.get())) {
                showVetInfo(player);
                return InteractionResult.SUCCESS;
            }
            if (itemstack.is(ModItems.STETHOSCOPE.get())) {
                printStethoscope(player);
                return InteractionResult.SUCCESS;
            }
            if (itemstack.is(ModItems.THERMOMETER.get())) {
                printTemperature(player);
                return InteractionResult.SUCCESS;
            }
            if (itemstack.getItem() instanceof net.minecraft.world.item.Item) {
                if (itemstack.is(ModItems.ANTIBIOTICS.get()) || itemstack.is(ModItems.PENICILLIN.get())) {
                    tryCure(player, itemstack, "ITEM");
                    return InteractionResult.SUCCESS;
                }
                if (itemstack.is(ModItems.FLUNIXIN.get())) {
                    player.sendSystemMessage(Component.literal("§eAğrı kesici uygulandı. Stres azaldı."));
                    if (!player.getAbilities().instabuild)
                        itemstack.shrink(1);
                    return InteractionResult.SUCCESS;
                }
            }

            // 5. YEMLEME
            FeedData feedData = FeedManager.getFeedData(
                    itemstack);
            if (feedData != null) {
                this.metabolismSystem.feed(feedData);
                this.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
                this.level().broadcastEntityEvent(this, (byte) 18);
                if (this.getDisease().equals("NONE")) {
                    String riskResult = DiseaseManager.calculateRisk(itemstack, this.random);
                    if (riskResult != null) {
                        this.healthSystem.setDisease(riskResult);
                        this.playSound(SoundEvents.ZOMBIE_INFECT, 1.0F, 1.0F);
                    }
                }
                itemstack.shrink(1);
                return InteractionResult.SUCCESS;
            }

            // 6. BİLGİ
            if (itemstack.isEmpty()) {
                if (player.isCrouching())
                    printVisualInspection(player);
                else
                    showVetInfo(player);
                return InteractionResult.SUCCESS;
            }
        }
        // --- PROFESYONEL İLAÇ & HASTALIK ENTEGRASYONU ---
        // 4. İLAÇ & HORMON SİSTEMİ
        DrugData drug = DrugManager.getDrug(itemstack);
        boolean actionTaken = false;

        // Hormon Kontrolü
        if (drug != null && drug.category.equals("FUNCTIONAL")) {

            // A) REPRODÜKTİF HORMONLAR (PGF2a, GnRH vb.)
            if (drug.actionTag.equals("ABORT_OR_ESTRUS")) {

                // --- DÜZELTME: CİNSİYET KONTROLÜ ---
                if (this.isMale()) {
                    player.sendSystemMessage(
                            Component.literal("§c ⚠ Bu hormon sadece dişi hayvanlarda (İnek/Düve) etkilidir."));
                    player.sendSystemMessage(Component.literal("§7 (Erkeklerde östrus döngüsü bulunmaz)"));
                    return InteractionResult.FAIL; // İlacı harcama, işlemi iptal et
                }
                // -----------------------------------

                if (this.isPregnant()) {
                    this.reproductionSystem.forceAbortion();
                    player.sendSystemMessage(Component.literal("§c ⚠ Gebelik sonlandırıldı (Abort)."));
                    actionTaken = true;
                } else if (!this.isBaby()) {
                    this.reproductionSystem.induceEstrus();
                    player.sendSystemMessage(Component.literal("§d ♥ Kızgınlık (Östrus) tetiklendi."));
                    actionTaken = true;
                } else {
                    player.sendSystemMessage(Component.literal("§c Buzağılara hormon uygulanamaz."));
                    return InteractionResult.FAIL;
                }
            } else if (drug.actionTag.equals("STRESS_RELIEF")) { // Ağrı Kesici
                player.sendSystemMessage(Component.literal("§a Ağrı kesici uygulandı."));
                actionTaken = true;
            }

            // Hormon kullanıldıysa yan etkilerini uygula ve itemi sil
            if (actionTaken) {
                // Phase 18: İlaç Uygula
                this.healthSystem.applyDrug(drug);

                if (drug.stressImpact != 0)
                    this.healthSystem.increaseStress(drug.stressImpact);
                if (drug.bcsImpact != 0)
                    this.metabolismSystem.reduceBcs(drug.bcsImpact);
                if (!player.getAbilities().instabuild)
                    itemstack.shrink(1);
                return InteractionResult.SUCCESS;
            }
        }

        // DURUM 2: BU BİR HASTALIK TEDAVİSİ Mİ?
        // İlaç DrugManager'da olsa da olmasa da (null olsa da) hastalık listesini
        // kontrol et.
        // tryCure artık boolean döndürüyor!
        boolean cureResult = tryCure(player, itemstack, "ITEM");

        if (cureResult) {
            // Eğer hastalık tedavisinde başarılı bir adım atıldıysa...

            // VE bu ilaç DrugManager'da tanımlıysa (Yan etkileri varsa)
            if (drug != null) {
                // Phase 18: Merkezi İlaç Uygulaması (Toksisite ve Kalıntı dahil)
                this.healthSystem.applyDrug(drug);

                // Manuel stres/bcs işlemini kaldırdık, çünkü artık applyDrug içinde veya
                // healthSystem üzerinden yönetilebilir.
                // Ancak HealthSystem.applyDrug sadece toxicity/withdrawal yapıyor şu an.
                // Stres ve BCS'yi de oraya taşıyabilirdik ama şimdilik burada kalsın
                // UYARI: applyDrug metoduna stres ve bcs eklemedik, o yüzden burayı koruyoruz.

                if (drug.stressImpact != 0)
                    this.healthSystem.increaseStress(drug.stressImpact);
                if (drug.bcsImpact != 0)
                    this.metabolismSystem.reduceBcs(drug.bcsImpact);
            }

            // tryCure zaten itemi sildiği için burada shrink yapmıyoruz.
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    // --- YARDIMCI METODLAR ---

    private void printStethoscope(Player player) {
        this.playSound(SoundEvents.PLAYER_BREATH, 1.0F, 1.0F);
        int stress = this.healthSystem.getStressLevel();
        int bpm = 60 + (stress / 2) + this.random.nextInt(10);
        player.sendSystemMessage(Component.literal("§b[ 🩺 ] Oskültasyon:"));
        player.sendSystemMessage(
                Component.literal("§7Nabız: " + bpm + " bpm " + (stress > 50 ? "§c(Taşikardi)" : "§a(Normal)")));
        if (!this.getDisease().equals("NONE")) {
            DiseaseData data = DiseaseManager.getDiseaseById(this.getDisease());
            if (data != null && data.symptoms != null)
                player.sendSystemMessage(Component.literal("§cCiğer Sesi: §f" + data.symptoms.stethoscope));
        } else {
            player.sendSystemMessage(Component.literal("§aCiğer Sesi: §fTemiz ve ritmik."));
        }
    }

    private void printTemperature(Player player) {
        this.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5F, 2.0F);
        double temp = 38.5;
        if (!this.getDisease().equals("NONE")) {
            DiseaseData data = DiseaseManager.getDiseaseById(this.getDisease());
            if (data != null && data.symptoms != null)
                temp = data.symptoms.temperature;
        }
        if (this.healthSystem.getStressLevel() > 70)
            temp += 0.5;
        String color = (temp > 39.5) ? "§c" : "§a";
        player.sendSystemMessage(
                Component.literal("§6[ 🌡️ ] Rektal Isı: " + color + String.format("%.1f", temp) + " °C"));
    }

    private void printVisualInspection(Player player) {
        float bcs = this.metabolismSystem.getBcs();
        String bcsDesc = (bcs < 2.5) ? "§c(Aşırı Zayıf)" : (bcs > 4.0) ? "§c(Obez)" : "§a(İdeal Kondisyon)";
        player.sendSystemMessage(Component.literal("§6[ 👁️ ] Gözlem:"));
        player.sendSystemMessage(Component.literal("§7BCS Skoru: §f" + String.format("%.1f", bcs) + " " + bcsDesc));
        if (!this.getDisease().equals("NONE")) {
            DiseaseData data = DiseaseManager.getDiseaseById(this.getDisease());
            if (data != null && data.symptoms != null)
                player.sendSystemMessage(Component.literal("§cBelirti: §f" + data.symptoms.visual));
        } else {
            player.sendSystemMessage(Component.literal("§aGenel: §fDuruşu canlı, tüyleri parlak."));
        }
    }

    // void yerine boolean yaptık
    // void yerine boolean yaptık
    private boolean tryCure(Player player, ItemStack itemstack, String type) {

        // 1. NEKAHET KONTROLÜ
        if (this.healthSystem.isRecovering()) {
            player.sendSystemMessage(Component.literal("§e ⏳ Hayvan şu an nekahet (iyileşme) döneminde."));
            return false; // İşlem yapılmadı
        }

        // HASTALIK DÖNGÜSÜ (Primary ve Secondary)
        String[] diseases = { this.getDisease(), this.getSecondaryDisease() };
        boolean[] isSecondary = { false, true };

        for (int i = 0; i < 2; i++) {
            String diseaseId = diseases[i];

            if (diseaseId.equals("NONE"))
                continue;

            DiseaseData data = DiseaseManager.getDiseaseById(diseaseId);

            if (data != null && type.equals(data.cureType)) {

                String itemId = BuiltInRegistries.ITEM.getKey(itemstack.getItem()).toString();
                // ID eşleştirmeleri
                if (itemstack.getItem().toString().contains("antibiotics"))
                    itemId = "vetcraft:antibiotics";
                if (itemstack.getItem().toString().contains("penicillin"))
                    itemId = "vetcraft:penicillin";
                if (itemstack.getItem().toString().contains("oil_bottle"))
                    itemId = "vetcraft:oil_bottle";
                if (itemstack.getItem().toString().contains("vinegar"))
                    itemId = "vetcraft:vinegar";
                if (itemstack.getItem().toString().contains("fmd_vaccine"))
                    itemId = "vetcraft:fmd_vaccine";
                if (itemstack.getItem().toString().contains("dextrose_serum"))
                    itemId = "vetcraft:dextrose_serum";
                if (itemstack.getItem().toString().contains("salt_lick"))
                    itemId = "vetcraft:salt_lick";
                if (itemstack.getItem().toString().contains("calf_formula"))
                    itemId = "vetcraft:calf_formula";

                // 3. PROTOKOL KONTROLÜ
                int currentStep = (i == 0) ? this.healthSystem.getTreatmentStep()
                        : this.healthSystem.getSecondaryTreatmentStep();

                if (data.cureTarget != null) {
                    // CONFIG DEĞİŞİKLİĞİ KONTROLÜ
                    if (currentStep >= data.cureTarget.size()) {
                        if (i == 0)
                            this.healthSystem.setTreatmentStep(0);
                        else
                            this.healthSystem.setSecondaryTreatmentStep(0);

                        player.sendSystemMessage(Component.literal(
                                "§e⚠ Tedavi protokolü güncellendiği için süreç sıfırlandı. Lütfen ilacı tekrar uygulayın."));
                        return true;
                    }

                    String requiredItem = data.cureTarget.get(currentStep);

                    if (itemId.equals(requiredItem)) {
                        // --- DOĞRU İLAÇ! ---
                        if (!player.getAbilities().instabuild)
                            itemstack.shrink(1);

                        // İlgili tedaviyi ilerlet
                        if (i == 0)
                            this.healthSystem.advanceTreatment();
                        else
                            this.healthSystem.advanceSecondaryTreatment();

                        // Protokol bitti mi?
                        int newStep = (i == 0) ? this.healthSystem.getTreatmentStep()
                                : this.healthSystem.getSecondaryTreatmentStep();

                        if (newStep >= data.cureTarget.size()) {
                            // ŞANS FAKTÖRÜ
                            float successChance = 0.80f - (this.healthSystem.getStressLevel() * 0.003f);

                            if (this.random.nextFloat() < successChance) {
                                // BAŞARILI
                                this.playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.0F);
                                player.sendSystemMessage(
                                        Component.literal("§a ✓ " + data.displayName + " tedavisi tamamlandı."));

                                if (i == 0) {
                                    this.setDisease("NONE");
                                    this.healthSystem.resetPrimaryTreatment();
                                } else {
                                    this.setSecondaryDisease("NONE");
                                    this.healthSystem.resetSecondaryTreatment();
                                }

                                // HER İKİ HASTALIK DA BİTTİ Mİ?
                                if (this.getDisease().equals("NONE") && this.getSecondaryDisease().equals("NONE")) {
                                    this.healthSystem.startRecovery(24000);
                                } else if (this.getDisease().equals("NONE")
                                        && !this.getSecondaryDisease().equals("NONE")) {
                                    // Primary bitti ama Secondary var -> Secondary'i Primary'e taşı
                                    String sec = this.getSecondaryDisease();
                                    int secStep = this.healthSystem.getSecondaryTreatmentStep();

                                    this.setDisease(sec);
                                    this.healthSystem.setTreatmentStep(secStep);

                                    this.setSecondaryDisease("NONE");
                                    this.healthSystem.resetSecondaryTreatment();

                                    player.sendSystemMessage(Component.literal(
                                            "§e ℹ İkincil hastalık ana hastalığa dönüştü. Tedaviye devam edin."));
                                }

                            } else {
                                // BAŞARISIZ
                                if (i == 0)
                                    this.healthSystem.resetPrimaryTreatment();
                                else
                                    this.healthSystem.resetSecondaryTreatment();

                                this.playSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
                                player.sendSystemMessage(Component
                                        .literal("§c ❌ Tedavi başarısız! Yanıt vermedi (" + data.displayName + ")."));
                            }
                        } else {
                            // Sıradaki
                            this.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                            int remaining = data.cureTarget.size() - newStep;
                            player.sendSystemMessage(Component
                                    .literal("§e ✓ Doz doğru (" + data.displayName + "). Kalan: " + remaining));
                        }
                        return true; // BAŞARILI OLDU (Item kullanıldı)
                    }
                }
            }
        }

        return false; // HİÇBİR İŞLEM YAPILMADI
    }

    private void showVetInfo(Player player) {
        // Artık işi "Taşeron" firmaya (VetDiagnostics) yaptırıyoruz :)
        VetDiagnostics.showVetInfo(this, player);
        // ... showVetInfo içinde pH göstergesinin altına ...

        float hyd = this.metabolismSystem.getHydration(); // Artık hata vermez
        float gas = this.metabolismSystem.getGasLevel(); // Artık hata vermez

        // SU GÖSTERGESİ
        String waterStatus = (hyd < 40) ? " §c(⚠ KRİTİK)" : " §a(✔)";
        player.sendSystemMessage(Component.literal("§b💧 Hidrasyon: % " + (int) hyd + waterStatus));

        // GAZ GÖSTERGESİ
        if (gas > 50) {
            player.sendSystemMessage(Component.literal("§4⚠ GAZ RİSKİ: % " + (int) gas + " (Timpani)"));
        } else {
            player.sendSystemMessage(Component.literal("§7💨 Gaz Seviyesi: % " + (int) gas));
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes().add(Attributes.MAX_HEALTH, 20.0D).add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    // --- STRES MEKANİĞİ (Hasar alınca stres artar) ---
    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result && !this.level().isClientSide) {
            // Hasar alındığında Health sistemine bildir
            this.healthSystem.increaseStress(15); // Her vuruşta 15 stres ekle

            // Eğer oyuncu vurduysa ekstra kaçışma (PanicGoal zaten var)
        }
        return result;
    }

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
        this.entityData.define(SECONDARY_DISEASE, "NONE"); // YENİ
        this.entityData.define(OWNER_UUID, java.util.Optional.empty());
    }

    public void setOwnerUUID(@Nullable java.util.UUID uuid) {
        this.entityData.set(OWNER_UUID, java.util.Optional.ofNullable(uuid));
    }

    public java.util.Optional<java.util.UUID> getOwnerUUID() {
        return this.entityData.get(OWNER_UUID);
    }

    // ...

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("VetSim_EarTag", this.getEarTag());
        compound.putString("VetSim_Breed", this.getBreed());
        compound.putBoolean("VetSim_IsMale", this.isMale());
        compound.putFloat("VetSim_Weight", this.getWeight());
        compound.putInt("VetSim_AgeDays", this.getAgeDays());
        compound.putBoolean("VetSim_IsPregnant", this.isPregnant());
        compound.putInt("VetSim_BirthCount", this.getBirthCount());
        compound.putInt("VetSim_Hunger", this.getHunger());
        compound.putString("VetSim_Disease", this.getDisease());
        compound.putString("VetSim_SecondaryDisease", this.getSecondaryDisease()); // YENİ
        this.reproductionSystem.save(compound);
        this.metabolismSystem.save(compound);
        this.healthSystem.save(compound);
        if (this.getOwnerUUID().isPresent()) {
            compound.putUUID("Owner", this.getOwnerUUID().get());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("VetSim_EarTag"))
            this.setEarTag(compound.getString("VetSim_EarTag"));
        if (compound.contains("VetSim_Breed"))
            this.setBreed(compound.getString("VetSim_Breed"));
        if (compound.contains("VetSim_IsMale"))
            this.setMale(compound.getBoolean("VetSim_IsMale"));
        if (compound.contains("VetSim_Weight"))
            this.setWeight(compound.getFloat("VetSim_Weight"));
        if (compound.contains("VetSim_AgeDays"))
            this.setAgeDays(compound.getInt("VetSim_AgeDays"));
        if (compound.contains("VetSim_IsPregnant"))
            this.entityData.set(IS_PREGNANT, compound.getBoolean("VetSim_IsPregnant"));
        if (compound.contains("VetSim_BirthCount"))
            this.setBirthCount(compound.getInt("VetSim_BirthCount"));
        if (compound.contains("VetSim_Hunger"))
            this.setHunger(compound.getInt("VetSim_Hunger"));
        if (compound.contains("VetSim_Disease"))
            this.setDisease(compound.getString("VetSim_Disease"));
        if (compound.contains("VetSim_SecondaryDisease")) // YENİ
            this.setSecondaryDisease(compound.getString("VetSim_SecondaryDisease"));
        this.reproductionSystem.load(compound);
        this.metabolismSystem.load(compound);
        this.healthSystem.load(compound);
        if (compound.contains("VetSim_EarTag")) {
            this.setEarTag(compound.getString("VetSim_EarTag"));
        }
        if (compound.hasUUID("Owner")) {
            this.setOwnerUUID(compound.getUUID("Owner"));
        }

        // YÜKLEME SONRASI GÖRÜNÜRLÜĞÜ ZORLA
        // YÜKLEME SONRASI GÖRÜNÜRLÜĞÜ ZORLA
        if (!this.getEarTag().equals("TR000000")) {
            this.updateEarTagColor();
        }
    }

    public void generateNewEarTag() {
        this.setEarTag("TR" + (this.random.nextInt(900000) + 100000));
    }

    @Override
    public SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level,
            net.minecraft.world.DifficultyInstance difficulty, net.minecraft.world.entity.MobSpawnType reason,
            @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {

        if (this.getEarTag().equals("TR000000"))
            this.generateNewEarTag();

        // 3.8.5 FIX: Spawn Egg ile oluşursa en yakın oyuncuyu sahip yap (UUID Belirsiz
        // sorununu çözer)
        if (!this.getOwnerUUID().isPresent() && level.getLevel() != null) {
            Player nearestPlayer = level.getLevel().getNearestPlayer(this, 5.0D);
            if (nearestPlayer != null) {
                this.setOwnerUUID(nearestPlayer.getUUID());
                // YENİ: Sahip atandığına dair bilgi ver (User Request)
                if (!level.getLevel().isClientSide) {
                    nearestPlayer.sendSystemMessage(net.minecraft.network.chat.Component
                            .literal("§aBu hayvanın yeni sahibi sizsiniz: " + this.getEarTag()));
                }
            }
        }

        // YENİ: Yetişkin dişiler spawn olduğunda süt vermeye başlasın (Gameplay için)
        // İPTAL EDİLDİ: Kullanıcı isteği üzerine fizyolojik gerçeklik korundu.
        // Sadece doğum yapanlar süt verecek.

        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    // --- YAPAY ZEKA (AI) AYARLARI ---
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.25D)); // Panik her zaman öncelikli
        this.goalSelector.addGoal(2, new NaturalBreedingGoal(this, 1.0D)); // Çiftleşme
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.1D, Ingredient.of(Items.WHEAT), false));

        // YENİ: Su İçme Hedefi
        this.goalSelector.addGoal(4, new com.vetsim.vetcraft.entity.ai.DrinkWaterGoal(this, 1.0D));

        // YENİ: Yerdeki Yemi Yeme Hedefi (Phase 17)
        this.goalSelector.addGoal(5, new com.vetsim.vetcraft.entity.ai.EatItemGoal(this, 1.1D));

        this.goalSelector.addGoal(6, new FollowParentGoal(this, 1.1D));

        // YENİ: Yemlikten Yeme Hedefi
        this.eatBlockGoal = new EatBlockGoal(this); // Vanilla çim yeme
        this.goalSelector.addGoal(7, this.eatBlockGoal);
        this.goalSelector.addGoal(8, new EatFromTroughGoal(this, 1.0D)); // Trough yeme

        this.goalSelector.addGoal(9, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(11, new RandomLookAroundGoal(this));
    }

    // Otlatma Hedefinin güncellenmesi için gerekli
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
    }

    // --- YERDEN OT YEDİĞİNDE ÇALIŞIR ---
    @Override
    public void ate() {
        super.ate(); // Ses ve partikül çıkarır

        if (!this.level().isClientSide) {
            // Çimen için sanal bir yem oluşturuyoruz
            FeedData grassFeed = new FeedData(
                    "grass", // ID
                    "Çayır Otu", // İsim
                    15, // Besin: Düşük (Sadece açlığı bastırır)
                    true, // Kuru Madde: Evet (pH dengeler)
                    0.0f // Protein: İhmal edilebilir
            );

            // Metabolizmaya işle
            this.metabolismSystem.feed(grassFeed);
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide()) {
            VetCraft.LOGGER.info("Cattle DIED: ID=" + this.getEarTag() + ", Pos=" + this.blockPosition() + ", Cause="
                    + damageSource.getMsgId());
            this.broadcastToPlayers(net.minecraft.network.chat.Component
                    .literal("§c💀 " + this.getEarTag() + " öldü! (Sebep: "
                            + damageSource.getLocalizedDeathMessage(this).getString() + ")"));
            this.spawnAtLocation(new ItemStack(Items.BEEF, Math.max(1, (int) (this.getWeight() / 100))));
            this.spawnAtLocation(new ItemStack(Items.LEATHER, this.random.nextInt(3) + 1));
        }
        super.die(damageSource);
    }

    // --- GETTERS & SETTERS (STANDART) ---
    public String getDisease() {
        return this.entityData.get(DISEASE);
    }

    // --- EAR TAG COLOR UPDATE HELPER ---
    public void updateEarTagColor() {
        if (this.getEarTag().equals("TR000000"))
            return;

        String tag = this.getEarTag();
        if (this.getDisease().equals("NONE") && this.getSecondaryDisease().equals("NONE")) {
            this.setCustomName(Component.literal("Küpe: " + tag).withStyle(net.minecraft.ChatFormatting.WHITE));
        } else {
            this.setCustomName(Component.literal("Küpe: " + tag).withStyle(net.minecraft.ChatFormatting.RED));
        }
        this.setCustomNameVisible(true);
    }

    public void setDisease(String disease) {
        this.entityData.set(DISEASE, disease);
        this.updateEarTagColor();
    }

    public String getSecondaryDisease() {
        return this.entityData.get(SECONDARY_DISEASE);
    }

    public void setSecondaryDisease(String disease) {
        this.entityData.set(SECONDARY_DISEASE, disease);
        this.updateEarTagColor();
    }

    public int getHunger() {
        return this.entityData.get(HUNGER);
    }

    public void setHunger(int h) {
        this.entityData.set(HUNGER, Math.max(0, Math.min(100, h)));
    }

    public int getBirthCount() {
        return this.entityData.get(BIRTH_COUNT);
    }

    public void setBirthCount(int count) {
        this.entityData.set(BIRTH_COUNT, count);
    }

    public boolean isPregnant() {
        return this.entityData.get(IS_PREGNANT);
    }

    public void setPregnant(boolean isPregnant) {
        this.entityData.set(IS_PREGNANT, isPregnant);
    }

    public String getEarTag() {
        return this.entityData.get(EAR_TAG);
    }

    public void setEarTag(String tag) {
        this.entityData.set(EAR_TAG, tag);
        this.setCustomName(Component.literal("Küpe: " + tag));
        this.setCustomNameVisible(true);
    }

    public String getBreed() {
        return this.entityData.get(BREED);
    }

    public void setBreed(String breed) {
        this.entityData.set(BREED, breed);
    }

    public boolean isMale() {
        return this.entityData.get(IS_MALE);
    }

    public void setMale(boolean isMale) {
        this.entityData.set(IS_MALE, isMale);
    }

    public float getWeight() {
        return this.entityData.get(WEIGHT);
    }

    public void setWeight(float weight) {
        this.entityData.set(WEIGHT, weight);
    }

    public int getAgeDays() {
        return this.entityData.get(AGE_DAYS);
    }

    public void setAgeDays(int days) {
        this.entityData.set(AGE_DAYS, days);
    }

    // --- KÖPRÜ METODLAR (AI HEDEFLERİ İÇİN) ---
    // NaturalBreedingGoal gibi eski sınıfların yeni sistemle konuşmasını sağlar
    public int getBreedingCooldown() {
        return this.reproductionSystem.getBreedingCooldown();
    }

    public void setBreedingCooldown(int cd) {
        this.reproductionSystem.setBreedingCooldown(cd);
    }

    public void startPregnancy(String fatherBreed) {
        this.reproductionSystem.startPregnancy(fatherBreed);
    }

    // --- SİSTEMLERE ERİŞİM ---
    public CattleReproduction getReproductionSystem() {
        return reproductionSystem;
    }

    public CattleMetabolism getMetabolismSystem() {
        return metabolismSystem;
    }

    public CattleHealth getHealthSystem() {
        return healthSystem;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    // --- YARDIMCI METODLAR ---
    /**
     * Mesajı sahibine veya yakındaki oyunculara iletir.
     */
    public void broadcastToPlayers(net.minecraft.network.chat.Component message) {
        if (this.level().isClientSide)
            return;

        boolean ownerFound = false;

        // 1. Sahibini bulmaya çalış
        if (this.getOwnerUUID().isPresent()) {
            Player owner = this.level().getPlayerByUUID(this.getOwnerUUID().get());
            if (owner != null) {
                owner.sendSystemMessage(message);
                ownerFound = true;
            }
        }

        // 2. Sahip yoksa veya uzaktaysa/offline ise, yakındaki oyunculara göster
        if (!ownerFound) {
            for (Player player : this.level().players()) {
                if (player.distanceToSqr(this) < 400.0D) { // 20 blok yarıçap
                    player.sendSystemMessage(message);
                }
            }
        }
    }
}