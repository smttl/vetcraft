package com.vetsim.vetcraft.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MedicineItem extends Item {
    private final String description;

    public MedicineItem(Properties properties, String desc) {
        super(properties);
        this.description = desc;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        // İlacın altına gri renkte açıklama ekler
        tooltipComponents.add(Component.literal("§7" + this.description));
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }
}