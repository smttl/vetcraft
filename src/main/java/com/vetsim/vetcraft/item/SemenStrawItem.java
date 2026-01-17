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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
            TooltipFlag isAdvanced) {
        if (stack.hasTag()) {
            // 1. IRK BİLGİSİ
            if (stack.getTag().contains("VetSim_Breed")) {
                String breed = stack.getTag().getString("VetSim_Breed");
                // İlk harfi büyük yap
                breed = breed.substring(0, 1).toUpperCase() + breed.substring(1);
                tooltipComponents.add(Component.literal("§7Irk: §e" + breed));
            }

            // 2. GENETİK KALİTE
            if (stack.getTag().contains("VetSim_Quality")) {
                String quality = stack.getTag().getString("VetSim_Quality");
                String color = "§f";
                if (quality.equals("ELITE"))
                    color = "§d§l"; // Kalın Mor
                else if (quality.equals("SUPERIOR"))
                    color = "§b"; // Mavi
                else
                    color = "§7"; // Gri (Commercial)

                tooltipComponents.add(Component.literal("§7Kalite: " + color + quality));
            }

            tooltipComponents.add(Component.literal("§8----------------"));

            // 3. SÜT VERİM PUANI (PTA)
            if (stack.getTag().contains("VetSim_MilkPTA")) {
                float milkPTA = stack.getTag().getFloat("VetSim_MilkPTA");
                String sign = (milkPTA > 0) ? "+" : "";
                String color = (milkPTA > 0.5) ? "§a" : "§7";
                tooltipComponents
                        .add(Component.literal("§7Süt Potansiyeli: " + color + sign + String.format("%.2f", milkPTA)));
            }

            // 4. SAĞLIK PUANI (PTA)
            if (stack.getTag().contains("VetSim_HealthPTA")) {
                float healthPTA = stack.getTag().getFloat("VetSim_HealthPTA");
                String sign = (healthPTA > 0) ? "+" : "";
                String color = (healthPTA > 0.2) ? "§a" : "§7";
                tooltipComponents
                        .add(Component.literal("§7Sağlık Direnci: " + color + sign + String.format("%.2f", healthPTA)));
            }

        } else {
            tooltipComponents.add(Component.literal("§7Genetik: §cBilinmiyor"));
        }
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }
}