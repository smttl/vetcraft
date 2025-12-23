package com.vetsim.vetcraft.entity;

import com.vetsim.vetcraft.VetCraft;
import net.minecraft.core.registries.BuiltInRegistries; // BU EKSİKTİ
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    // 1. KAYITÇIYI TANIMLA (BU SATIR EKSİKTİ)
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, VetCraft.MOD_ID);

    // 2. SIĞIR (CATTLE) KAYDI
    public static final DeferredHolder<EntityType<?>, EntityType<CattleEntity>> CATTLE = ENTITY_TYPES.register("cattle",
            () -> EntityType.Builder.of(CattleEntity::new, MobCategory.CREATURE)
                    .sized(1.4F, 1.4F)
                    .build("cattle"));

    // 3. CELEP (TÜCCAR) KAYDI
    public static final DeferredHolder<EntityType<?>, EntityType<CelepEntity>> CELEP = ENTITY_TYPES.register("celep",
            () -> EntityType.Builder.of(CelepEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F) // İnsan boyutu
                    .build("celep"));

    // 4. REGISTER METODU
    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}