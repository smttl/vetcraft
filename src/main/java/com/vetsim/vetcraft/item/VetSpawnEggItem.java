package com.vetsim.vetcraft.item;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.minecraft.world.flag.FeatureFlagSet;

import java.util.function.Supplier;

// NeoForge provides a DeferredSpawnEggItem that handles the supplier logic automatically.
// We will simply extend it or use it directly if available, but to be safe and explicit we create this wrapper.
// Checking NeoForge documentation (or standard pattern), DeferredSpawnEggItem is the standard solution.

public class VetSpawnEggItem extends DeferredSpawnEggItem {

    public VetSpawnEggItem(Supplier<? extends EntityType<? extends Mob>> type, int backgroundColor, int highlightColor,
            Item.Properties props) {
        super(type, backgroundColor, highlightColor, props);
    }
}
