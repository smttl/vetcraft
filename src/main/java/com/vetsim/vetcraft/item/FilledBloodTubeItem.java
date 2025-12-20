
package com.vetsim.vetcraft.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FilledBloodTubeItem extends Item {

    public FilledBloodTubeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();

            // 1. KİMLİK BİLGİSİ
            if (tag.contains("VetSim_EarTag")) {
                String earTag = tag.getString("VetSim_EarTag");
                tooltipComponents.add(Component.literal("§b🆔 Küpe No: §f" + earTag));
            }

            tooltipComponents.add(Component.literal("§7--------------------"));
            tooltipComponents.add(Component.literal("§6🔬 HEMOGRAM SONUCU:"));

            // 2. WBC (Lökosit - Enfeksiyon Göstergesi)
            // Normal: 4.0 - 12.0
            if (tag.contains("VetSim_WBC")) {
                float wbc = tag.getFloat("VetSim_WBC");
                String color = (wbc > 12.0) ? "§c" : (wbc < 4.0) ? "§e" : "§f"; // Yüksekse Kırmızı, Düşükse Sarı
                String status = (wbc > 12.0) ? " (YÜKSEK - Enfeksiyon)" : "";
                tooltipComponents.add(Component.literal("§7Lökosit (WBC): " + color + String.format("%.1f", wbc) + " x10^3" + status));
            }

            // 3. pH (Asidoz Göstergesi)
            // Normal: 7.35 - 7.45
            if (tag.contains("VetSim_PH")) {
                float ph = tag.getFloat("VetSim_PH");
                String color = (ph < 7.30) ? "§c" : "§f"; // Asitse Kırmızı
                String status = (ph < 7.30) ? " (DÜŞÜK - Asidoz)" : "";
                tooltipComponents.add(Component.literal("§7Kan pH: " + color + String.format("%.2f", ph) + status));
            }

            // İstersen RBC (Kansızlık) vb. de ekleyebilirsin buraya
        } else {
            tooltipComponents.add(Component.literal("§7Veri okunamadı."));
        }

        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }
}