package com.vetsim.vetcraft.entity;

import com.vetsim.vetcraft.entity.ai.EatFromTroughGoal;
import com.vetsim.vetcraft.entity.ai.NaturalBreedingGoal;
import com.vetsim.vetcraft.entity.components.CattleHealth;
import com.vetsim.vetcraft.entity.components.CattleMetabolism;
import com.vetsim.vetcraft.entity.components.CattleReproduction;
import com.vetsim.vetcraft.init.ModItems;
import com.vetsim.vetcraft.util.*;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import com.vetsim.vetcraft.util.VetDiagnostics;

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

    // --- SİSTEM BİLEŞENLERİ ---
    private final CattleReproduction reproductionSystem;
    private final CattleMetabolism metabolismSystem;
    private final CattleHealth healthSystem;

    private EatBlockGoal eatBlockGoal;

    public static final int GESTATION_PERIOD = 2400; // doum
    public static final int POST_BIRTH_COOLDOWN = 1200; // diostrus
    public static final int BABY_GROWTH_DAYS = 1;

    public CattleEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);

        // Sistemleri başlat
        this.reproductionSystem = new CattleReproduction(this);
        this.metabolismSystem = new CattleMetabolism(this);
        this.healthSystem = new CattleHealth(this);

        if (!level.isClientSide()) {
            String[] BREEDS = {"Holstein", "Angus", "Simmental", "Jersey", "Melez"};
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

    // --- OYUN DÖNGÜSÜ ---
    @Override
    public void aiStep() {
        super.aiStep();

        // 1. Görsel Efektler (Client Side)
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

        // 2. Mantıksal İşlemler (Server Side)
        if (!this.level().isClientSide()) {
            this.reproductionSystem.tick();
            this.metabolismSystem.tick();
            this.healthSystem.tick();

            if (this.getAgeDays() < BABY_GROWTH_DAYS) {
                if (!this.isBaby()) this.setAge(-24000);
            } else if (this.isBaby()) {
                this.setAge(0);
            }

            if (this.tickCount % 24000 == 0) {
                this.setAgeDays(this.getAgeDays() + 1);
            }

            if (!this.getDisease().equals("NONE")) {
                DiseaseData data = DiseaseManager.getDiseaseById(this.getDisease());
                if (data != null && data.slowness) this.setSpeed(0.08F);
                else this.setSpeed(0.12F);
            } else {
                if (this.healthSystem.isLame()) {
                    this.setSpeed(0.10F);
                } else {
                    this.setSpeed(0.2F);
                }
            }
        }
    }

    // --- ETKİLEŞİM YÖNETİCİSİ ---
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand == InteractionHand.OFF_HAND) return InteractionResult.PASS;

        ItemStack itemstack = player.getItemInHand(hand);
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

            // KRİTİK NOKTA: 'SUCCESS' döndürerek "Yere su koyma" işlemini %100 iptal ediyoruz.
            return InteractionResult.SUCCESS;
        }
        if (!this.level().isClientSide()) {

            // 1. SPERMA ALMA
            if (itemstack.is(ModItems.EMPTY_STRAW.get())) {
                if (this.isMale() && !this.isBaby()) {
                    itemstack.shrink(1);
                    ItemStack filledStraw = new ItemStack(ModItems.FILLED_STRAW.get());
                    CompoundTag tag = filledStraw.getOrCreateTag();
                    tag.putString("VetSim_Breed", this.getBreed());
                    filledStraw.setTag(tag);
                    if (!player.getInventory().add(filledStraw)) player.drop(filledStraw, false);
                    this.playSound(SoundEvents.COW_AMBIENT, 1.0F, 1.0F);
                    player.sendSystemMessage(Component.literal("§aSperma alındı. Irk: " + this.getBreed()));
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
                    if (itemstack.hasTag() && itemstack.getTag().contains("VetSim_Breed")) {
                        fatherBreed = itemstack.getTag().getString("VetSim_Breed");
                    }

                    boolean success = this.reproductionSystem.tryInseminate(fatherBreed, 1.0f);

                    if (success) {
                        this.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                        ((ServerLevel)this.level()).sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 1.0, this.getZ(), 5, 0.5, 0.5, 0.5, 0.0);
                        player.sendSystemMessage(Component.literal("§d ♥ Suni tohumlama BAŞARILI! Gebelik başladı."));
                    } else {
                        float prog = this.reproductionSystem.getProgesterone();
                        if (prog > 2.5f) {
                            if (this.isPregnant()) player.sendSystemMessage(Component.literal("§cBu hayvan zaten gebe!"));
                            else player.sendSystemMessage(Component.literal("§cHayvan kızgınlıkta değil! (Progesteron Yüksek)"));
                        } else {
                            this.playSound(SoundEvents.COW_HURT, 1.0F, 1.0F);
                            player.sendSystemMessage(Component.literal("§c x Tohumlama tutmadı. Tekrar deneyin."));
                        }
                    }
                    itemstack.shrink(1);
                    return InteractionResult.SUCCESS;
                } else {
                    player.sendSystemMessage(Component.literal("§cSadece dişi inekleri tohumlayabilirsiniz."));
                    return InteractionResult.PASS;
                }
            }

            // 3. LAB: KAN ALMA
            if (itemstack.is(ModItems.EMPTY_BLOOD_TUBE.get())) {
                this.playSound(SoundEvents.BOTTLE_FILL, 1.0F, 1.0F);
                ItemStack bloodSample = new ItemStack(ModItems.FILLED_BLOOD_TUBE.get());
                CompoundTag tag = bloodSample.getOrCreateTag();
                tag.putString("VetSim_EarTag", this.getEarTag());
                float stress = this.healthSystem.getStressLevel();
                float wbc = 5.0F + (stress / 10.0F) + this.random.nextFloat() * 2.0F;
                float rumenPh = this.metabolismSystem.getRumenPh();
                float bloodPh = 7.35F + (rumenPh - 6.8F) * 0.1F;

                String currentDisease = this.getDisease();
                if (currentDisease.equals("pneumonia")) wbc += 15.0F;
                else if (currentDisease.equals("acidosis")) bloodPh -= 0.2F;

                tag.putFloat("VetSim_WBC", wbc);
                tag.putFloat("VetSim_PH", bloodPh);
                bloodSample.setTag(tag);
                if (!player.getAbilities().instabuild) itemstack.shrink(1);
                if (!player.getInventory().add(bloodSample)) player.drop(bloodSample, false);
                player.sendSystemMessage(Component.literal("§cKan örneği alındı. Etiket: " + this.getEarTag()));
                return InteractionResult.SUCCESS;
            }

            // 4. VETERİNER ALETLERİ
            if (itemstack.is(ModItems.VET_CLIPBOARD.get())) { printAnamnesis(player); return InteractionResult.SUCCESS; }
            if (itemstack.is(ModItems.STETHOSCOPE.get())) { printStethoscope(player); return InteractionResult.SUCCESS; }
            if (itemstack.is(ModItems.THERMOMETER.get())) { printTemperature(player); return InteractionResult.SUCCESS; }
            if (itemstack.getItem() instanceof net.minecraft.world.item.Item) {
                if (itemstack.is(ModItems.ANTIBIOTICS.get()) || itemstack.is(ModItems.PENICILLIN.get())) {
                    tryCure(player, itemstack, "ITEM"); return InteractionResult.SUCCESS;
                }
                if (itemstack.is(ModItems.FLUNIXIN.get())) {
                    player.sendSystemMessage(Component.literal("§eAğrı kesici uygulandı. Stres azaldı."));
                    if (!player.getAbilities().instabuild) itemstack.shrink(1);
                    return InteractionResult.SUCCESS;
                }
            }

            // 5. YEMLEME
            FeedData feedData = FeedManager.getFeedData(itemstack);
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
                if (player.isCrouching()) printVisualInspection(player);
                else showVetInfo(player);
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
                    player.sendSystemMessage(Component.literal("§c ⚠ Bu hormon sadece dişi hayvanlarda (İnek/Düve) etkilidir."));
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
            }
            else if (drug.actionTag.equals("STRESS_RELIEF")) { // Ağrı Kesici
                player.sendSystemMessage(Component.literal("§a Ağrı kesici uygulandı."));
                actionTaken = true;
            }

            // Hormon kullanıldıysa yan etkilerini uygula ve itemi sil
            if (actionTaken) {
                if (drug.stressImpact != 0) this.healthSystem.increaseStress(drug.stressImpact);
                if (drug.bcsImpact != 0) this.metabolismSystem.reduceBcs(drug.bcsImpact);
                if (!player.getAbilities().instabuild) itemstack.shrink(1);
                return InteractionResult.SUCCESS;
            }
        }

        // DURUM 2: BU BİR HASTALIK TEDAVİSİ Mİ?
        // İlaç DrugManager'da olsa da olmasa da (null olsa da) hastalık listesini kontrol et.
        // tryCure artık boolean döndürüyor!
        boolean cureResult = tryCure(player, itemstack, "ITEM");

        if (cureResult) {
            // Eğer hastalık tedavisinde başarılı bir adım atıldıysa...

            // VE bu ilaç DrugManager'da tanımlıysa (Yan etkileri varsa)
            if (drug != null) {
                // Yan etkileri uygula (Örn: Penisilin stresi artırır, Vitamin düşürür)
                if (drug.stressImpact != 0) this.healthSystem.increaseStress(drug.stressImpact);
                if (drug.bcsImpact != 0) this.metabolismSystem.reduceBcs(drug.bcsImpact);
            }

            // tryCure zaten itemi sildiği için burada shrink yapmıyoruz.
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    // --- YARDIMCI METODLAR ---
    private void printAnamnesis(Player player) {
        player.sendSystemMessage(Component.literal("§6--- [ 📔 GÜNLÜK KAYITLARI ] ---"));
        float ph = this.metabolismSystem.getRumenPh();
        String phStatus = (ph < 5.8) ? "§c(Asidoz Riski!)" : "§a(Normal)";
        player.sendSystemMessage(Component.literal("§7Beslenme: Rumen pH " + String.format("%.1f", ph) + " " + phStatus));
        if (this.getDisease().equals("NONE")) player.sendSystemMessage(Component.literal("§a ✓ Rutin kontroller normal."));
        else {
            DiseaseData data = DiseaseManager.getDiseaseById(this.getDisease());
            if (data != null && data.symptoms != null) player.sendSystemMessage(Component.literal("§e ⚠ NOT: §f" + data.symptoms.anamnesis));
        }
    }

    private void printStethoscope(Player player) {
        this.playSound(SoundEvents.PLAYER_BREATH, 1.0F, 1.0F);
        int stress = this.healthSystem.getStressLevel();
        int bpm = 60 + (stress / 2) + this.random.nextInt(10);
        player.sendSystemMessage(Component.literal("§b[ 🩺 ] Oskültasyon:"));
        player.sendSystemMessage(Component.literal("§7Nabız: " + bpm + " bpm " + (stress > 50 ? "§c(Taşikardi)" : "§a(Normal)")));
        if (!this.getDisease().equals("NONE")) {
            DiseaseData data = DiseaseManager.getDiseaseById(this.getDisease());
            if (data != null && data.symptoms != null) player.sendSystemMessage(Component.literal("§cCiğer Sesi: §f" + data.symptoms.stethoscope));
        } else {
            player.sendSystemMessage(Component.literal("§aCiğer Sesi: §fTemiz ve ritmik."));
        }
    }

    private void printTemperature(Player player) {
        this.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5F, 2.0F);
        double temp = 38.5;
        if (!this.getDisease().equals("NONE")) {
            DiseaseData data = DiseaseManager.getDiseaseById(this.getDisease());
            if (data != null && data.symptoms != null) temp = data.symptoms.temperature;
        }
        if (this.healthSystem.getStressLevel() > 70) temp += 0.5;
        String color = (temp > 39.5) ? "§c" : "§a";
        player.sendSystemMessage(Component.literal("§6[ 🌡️ ] Rektal Isı: " + color + String.format("%.1f", temp) + " °C"));
    }

    private void printVisualInspection(Player player) {
        float bcs = this.metabolismSystem.getBcs();
        String bcsDesc = (bcs < 2.5) ? "§c(Aşırı Zayıf)" : (bcs > 4.0) ? "§c(Obez)" : "§a(İdeal Kondisyon)";
        player.sendSystemMessage(Component.literal("§6[ 👁️ ] Gözlem:"));
        player.sendSystemMessage(Component.literal("§7BCS Skoru: §f" + String.format("%.1f", bcs) + " " + bcsDesc));
        if (!this.getDisease().equals("NONE")) {
            DiseaseData data = DiseaseManager.getDiseaseById(this.getDisease());
            if (data != null && data.symptoms != null) player.sendSystemMessage(Component.literal("§cBelirti: §f" + data.symptoms.visual));
        } else {
            player.sendSystemMessage(Component.literal("§aGenel: §fDuruşu canlı, tüyleri parlak."));
        }
    }

    // void yerine boolean yaptık
    private boolean tryCure(Player player, ItemStack itemstack, String type) {

        // 1. NEKAHET KONTROLÜ
        if (this.healthSystem.isRecovering()) {
            player.sendSystemMessage(Component.literal("§e ⏳ Hayvan şu an nekahet (iyileşme) döneminde."));
            return false; // İşlem yapılmadı
        }

        // 2. HASTALIK KONTROLÜ
        if (!this.getDisease().equals("NONE")) {
            DiseaseData data = DiseaseManager.getDiseaseById(this.getDisease());

            if (data != null && type.equals(data.cureType)) {

                String itemId = BuiltInRegistries.ITEM.getKey(itemstack.getItem()).toString();
                if (itemstack.getItem().toString().contains("antibiotics")) itemId = "vetsim:antibiotics";
                if (itemstack.getItem().toString().contains("penicillin")) itemId = "vetsim:penicillin";

                // 3. PROTOKOL KONTROLÜ
                int currentStep = this.healthSystem.getTreatmentStep();

                if (data.cureTarget != null && currentStep < data.cureTarget.size()) {
                    String requiredItem = data.cureTarget.get(currentStep);

                    if (itemId.equals(requiredItem)) {
                        // --- DOĞRU İLAÇ! ---
                        if (!player.getAbilities().instabuild) itemstack.shrink(1);
                        this.healthSystem.advanceTreatment();

                        // Protokol bitti mi?
                        if (this.healthSystem.getTreatmentStep() >= data.cureTarget.size()) {
                            // ŞANS FAKTÖRÜ
                            float successChance = 0.80f - (this.healthSystem.getStressLevel() * 0.003f);

                            if (this.random.nextFloat() < successChance) {
                                this.healthSystem.startRecovery(24000);
                                this.playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.0F);
                                player.sendSystemMessage(Component.literal("§a ✓ Tedavi Protokolü Tamamlandı. İyileşme başladı."));
                            } else {
                                this.healthSystem.resetTreatment();
                                this.playSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
                                player.sendSystemMessage(Component.literal("§c ❌ Tedavi başarısız! Yanıt vermedi."));
                            }
                        } else {
                            // Sıradaki
                            this.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                            int remaining = data.cureTarget.size() - this.healthSystem.getTreatmentStep();
                            player.sendSystemMessage(Component.literal("§e ✓ Doz doğru. Kalan: " + remaining));
                        }
                        return true; // BAŞARILI OLDU (Item kullanıldı)
                    }
                }
            }
        }
        return false; // HİÇBİR İŞLEM YAPILMADI (Yanlış ilaç veya hastalık yok)
    }

    private void showVetInfo(Player player) {
        // Artık işi "Taşeron" firmaya (VetDiagnostics) yaptırıyoruz :)
        VetDiagnostics.showVetInfo(this, player);
        // ... showVetInfo içinde pH göstergesinin altına ...

        float hyd = this.metabolismSystem.getHydration(); // Artık hata vermez
        float gas = this.metabolismSystem.getGasLevel();  // Artık hata vermez

        // SU GÖSTERGESİ
        String waterStatus = (hyd < 40) ? " §c(⚠ KRİTİK)" : " §a(✔)";
        player.sendSystemMessage(Component.literal("§b💧 Hidrasyon: % " + (int)hyd + waterStatus));

        // GAZ GÖSTERGESİ
        if (gas > 50) {
            player.sendSystemMessage(Component.literal("§4⚠ GAZ RİSKİ: % " + (int)gas + " (Timpani)"));
        } else {
            player.sendSystemMessage(Component.literal("§7💨 Gaz Seviyesi: % " + (int)gas));
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes().add(Attributes.MAX_HEALTH, 20.0D).add(Attributes.MOVEMENT_SPEED, 0.2D).add(Attributes.FOLLOW_RANGE, 16.0D);
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
    }

    // ... defineSynchedData metodunun altına ekle ...

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        // Eğer sunucudan veya kayıttan KÜPE NO verisi güncellendiyse:
        if (EAR_TAG.equals(key)) {
            String tag = this.getEarTag();
            // İsim etiketini anında güncelle
            if (!tag.equals("TR000000")) {
                this.setCustomName(Component.literal("Küpe: " + tag));
                this.setCustomNameVisible(true);
            }
        }
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
        compound.putInt("VetSim_BirthCount", this.getBirthCount());
        compound.putInt("VetSim_Hunger", this.getHunger());
        compound.putString("VetSim_Disease", this.getDisease());
        this.reproductionSystem.save(compound);
        this.metabolismSystem.save(compound);
        this.healthSystem.save(compound);
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
        if (compound.contains("VetSim_BirthCount")) this.setBirthCount(compound.getInt("VetSim_BirthCount"));
        if (compound.contains("VetSim_Hunger")) this.setHunger(compound.getInt("VetSim_Hunger"));
        if (compound.contains("VetSim_Disease")) this.setDisease(compound.getString("VetSim_Disease"));
        this.reproductionSystem.load(compound);
        this.metabolismSystem.load(compound);
        this.healthSystem.load(compound);
        if (compound.contains("VetSim_EarTag")) {
            this.setEarTag(compound.getString("VetSim_EarTag"));
        }

        // YÜKLEME SONRASI GÖRÜNÜRLÜĞÜ ZORLA
        if (!this.getEarTag().equals("TR000000")) {
            this.setCustomNameVisible(true);
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty, net.minecraft.world.entity.MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        if (this.getEarTag().equals("TR000000")) this.setEarTag("TR" + (this.random.nextInt(900000) + 100000));
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    // --- YAPAY ZEKA (AI) AYARLARI ---
    @Override
    protected void registerGoals() {
        this.eatBlockGoal = new EatBlockGoal(this); // Hedefi oluştur

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 2.0D));

        // ÖNCELİK 2: YEMLİK (Kaliteli Yem)
        // Eğer yemlik varsa önce ona gitmeye çalışır.
        this.goalSelector.addGoal(2, new EatFromTroughGoal(this, 1.0D));

        this.goalSelector.addGoal(3, new NaturalBreedingGoal(this, 1.0D));

        // ÖNCELİK 5: OTLATMA (Sıradan Çimen)
        // Yemlik bulamazsa veya işi yoksa ot yer.
        this.goalSelector.addGoal(5, this.eatBlockGoal);

        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
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
                    "grass",    // ID
                    "Çayır Otu", // İsim
                    15,         // Besin: Düşük (Sadece açlığı bastırır)
                    true,       // Kuru Madde: Evet (pH dengeler)
                    0.0f        // Protein: İhmal edilebilir
            );

            // Metabolizmaya işle
            this.metabolismSystem.feed(grassFeed);
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide()) {
            this.spawnAtLocation(new ItemStack(Items.BEEF, Math.max(1, (int)(this.getWeight()/100))));
            this.spawnAtLocation(new ItemStack(Items.LEATHER, this.random.nextInt(3)+1));
        }
        super.die(damageSource);
    }

    // --- GETTERS & SETTERS (STANDART) ---
    public String getDisease() { return this.entityData.get(DISEASE); }
    public void setDisease(String disease) { this.entityData.set(DISEASE, disease); }
    public int getHunger() { return this.entityData.get(HUNGER); }
    public void setHunger(int h) { this.entityData.set(HUNGER, Math.max(0, Math.min(100, h))); }
    public int getBirthCount() { return this.entityData.get(BIRTH_COUNT); }
    public void setBirthCount(int count) { this.entityData.set(BIRTH_COUNT, count); }
    public boolean isPregnant() { return this.entityData.get(IS_PREGNANT); }
    public void setPregnant(boolean isPregnant) { this.entityData.set(IS_PREGNANT, isPregnant); }
    public String getEarTag() { return this.entityData.get(EAR_TAG); }
    public void setEarTag(String tag) { this.entityData.set(EAR_TAG, tag); this.setCustomName(Component.literal("Küpe: " + tag)); this.setCustomNameVisible(true); }
    public String getBreed() { return this.entityData.get(BREED); }
    public void setBreed(String breed) { this.entityData.set(BREED, breed); }
    public boolean isMale() { return this.entityData.get(IS_MALE); }
    public void setMale(boolean isMale) { this.entityData.set(IS_MALE, isMale); }
    public float getWeight() { return this.entityData.get(WEIGHT); }
    public void setWeight(float weight) { this.entityData.set(WEIGHT, weight); }
    public int getAgeDays() { return this.entityData.get(AGE_DAYS); }
    public void setAgeDays(int days) { this.entityData.set(AGE_DAYS, days); }

    // --- KÖPRÜ METODLAR (AI HEDEFLERİ İÇİN) ---
    // NaturalBreedingGoal gibi eski sınıfların yeni sistemle konuşmasını sağlar
    public int getBreedingCooldown() { return this.reproductionSystem.getBreedingCooldown(); }
    public void setBreedingCooldown(int cd) { this.reproductionSystem.setBreedingCooldown(cd); }
    public void startPregnancy(String fatherBreed) { this.reproductionSystem.startPregnancy(fatherBreed); }

    // --- SİSTEMLERE ERİŞİM ---
    public CattleReproduction getReproductionSystem() { return reproductionSystem; }
    public CattleMetabolism getMetabolismSystem() { return metabolismSystem; }
    public CattleHealth getHealthSystem() { return healthSystem; }

    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) { return null; }
    @Override public boolean isFood(ItemStack stack) { return false; }
}