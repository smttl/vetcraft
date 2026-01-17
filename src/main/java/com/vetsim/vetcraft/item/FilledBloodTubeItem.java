package com.vetsim.vetcraft.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import com.vetsim.vetcraft.gui.BloodAnalysisMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class FilledBloodTubeItem extends Item {

    public FilledBloodTubeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.hasTag()) { // Sadece doluysa aç
                serverPlayer.openMenu(new SimpleMenuProvider(
                        (id, inv, p) -> new BloodAnalysisMenu(id, inv),
                        Component.literal("Laboratuvar Sonucu")), buffer -> {
                        });
            } else {
                player.sendSystemMessage(Component.literal("§cBu tüp boş veya hatalı!"));
            }
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
            TooltipFlag isAdvanced) {
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
                tooltipComponents.add(Component
                        .literal("§7Lökosit (WBC): " + color + String.format("%.1f", wbc) + " x10^3" + status));
            }

            // 3. pH (Asidoz Göstergesi)
            // Normal: 7.35 - 7.45
            if (tag.contains("VetSim_PH")) {
                float ph = tag.getFloat("VetSim_PH");
                String color = (ph < 7.30) ? "§c" : "§f"; // Asitse Kırmızı
                String status = (ph < 7.30) ? " (DÜŞÜK - Asidoz)" : "";
                tooltipComponents.add(Component.literal("§7Kan pH: " + color + String.format("%.2f", ph) + status));
            }

            // 4. TOKSİSİTE (Karaciğer)
            // Normal: 0 - 20
            if (tag.contains("VetSim_Toxicity")) {
                float toxicity = tag.getFloat("VetSim_Toxicity");
                String color = (toxicity > 50.0) ? "§c" : (toxicity > 20.0) ? "§e" : "§f";
                String status = (toxicity > 50.0) ? " (KRİTİK - Yetmezlik)" : (toxicity > 20.0) ? " (Yüksek)" : "";
                tooltipComponents
                        .add(Component.literal("§7Toksisite: " + color + String.format("%.1f", toxicity) + status));
            }

            // 5. KETONLAR (Ketozis)
            // Normal: 0 - 1.0 mmol/L
            if (tag.contains("VetSim_Ketones")) {
                float ketones = tag.getFloat("VetSim_Ketones");
                String color = (ketones > 3.0) ? "§c" : (ketones > 1.2) ? "§e" : "§f";
                String status = (ketones > 3.0) ? " (Klinik Ketozis)" : (ketones > 1.2) ? " (Subklinik)" : "";
                tooltipComponents.add(
                        Component.literal("§7Keton: " + color + String.format("%.2f", ketones) + " mmol/L" + status));
            }

            // İstersen RBC (Kansızlık) vb. de ekleyebilirsin buraya
        } else {
            tooltipComponents.add(Component.literal("§7Veri okunamadı."));
        }

        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }
}