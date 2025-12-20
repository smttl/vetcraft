package com.vetsim.vetcraft.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SemenStrawItem extends Item {
    public SemenStrawItem(Properties properties) {
        super(properties);
    }

    // Mouse ile üzerine gelince bilgi gösterme
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        if (stack.hasTag() && stack.getTag().contains("VetSim_Breed")) {
            String breed = stack.getTag().getString("VetSim_Breed");
            tooltipComponents.add(Component.literal("§7Genetik: §e" + breed));
        } else {
            tooltipComponents.add(Component.literal("§7Genetik: §cBilinmiyor"));
        }
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }
}