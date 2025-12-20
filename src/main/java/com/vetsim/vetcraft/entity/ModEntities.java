package com.vetsim.vetcraft.entity;

import com.vetsim.vetcraft.VetCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, "vetsim");

    public static final DeferredHolder<EntityType<?>, EntityType<CattleEntity>> CATTLE =
            ENTITIES.register("cattle", () -> EntityType.Builder.of(CattleEntity::new, MobCategory.CREATURE)
                    .sized(1.4F, 1.4F) // Boyutlar
                    .build("cattle"));

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}