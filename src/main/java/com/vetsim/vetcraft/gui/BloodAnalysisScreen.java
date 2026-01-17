package com.vetsim.vetcraft.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vetsim.vetcraft.init.ModItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public class BloodAnalysisScreen extends AbstractContainerScreen<BloodAnalysisMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft",
            "textures/gui/container/creative_inventory/tab_items.png");

    public BloodAnalysisScreen(BloodAnalysisMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 195;
        this.imageHeight = 136;
        this.inventoryLabelY = 1000;
        this.titleLabelY = 1000;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        graphics.drawCenteredString(this.font, "§lVET-LAB SONUÇ RAPORU", x + (imageWidth / 2), y + 10, 0xFFFFFF);

        ItemStack stack = this.minecraft.player.getMainHandItem();
        if (stack.is(ModItems.FILLED_BLOOD_TUBE.get()) && stack.hasTag()) {
            CompoundTag tag = stack.getTag();

            // 1. KİMLİK
            graphics.drawString(this.font, "Küpe No: " + tag.getString("VetSim_EarTag"), x + 20, y + 30, 0x404040,
                    false);

            // 2. WBC
            float wbc = tag.getFloat("VetSim_WBC");
            String wbcColor = (wbc > 12.0) ? "§c" : (wbc < 4.0) ? "§e" : "§2";
            graphics.drawString(this.font, "WBC: " + wbcColor + String.format("%.1f", wbc), x + 20, y + 45, 0x000000,
                    false);

            // 3. pH
            float ph = tag.getFloat("VetSim_PH");
            String phColor = (ph < 7.30) ? "§c" : "§2";
            graphics.drawString(this.font, "Kan pH: " + phColor + String.format("%.2f", ph), x + 20, y + 60, 0x000000,
                    false);

            // 4. TOKSİSİTE
            float tox = tag.getFloat("VetSim_Toxicity");
            String toxColor = (tox > 20.0) ? "§c" : "§2";
            graphics.drawString(this.font, "Toksisite: " + toxColor + String.format("%.1f", tox), x + 100, y + 45,
                    0x000000, false);

            // 5. KETON
            float ket = tag.getFloat("VetSim_Ketones");
            String ketColor = (ket > 1.2) ? "§c" : "§2";
            graphics.drawString(this.font, "Keton: " + ketColor + String.format("%.2f", ket), x + 100, y + 60, 0x000000,
                    false);

            // SONUÇ YORUMU
            String result = "§2Sağlıklı Görünüyor";
            if (wbc > 12.0 || tox > 20.0 || ket > 1.2 || ph < 7.30) {
                result = "§cRisk Tespit Edildi!";
            }
            if (wbc > 20.0 || tox > 50.0) {
                result = "§4KRİTİK DURUM!";
            }
            graphics.drawCenteredString(this.font, result, x + (imageWidth / 2), y + 90, 0xFFFFFF);

        } else {
            graphics.drawCenteredString(this.font, "§cLütfen tüpü eline al!", x + (imageWidth / 2), y + 60, 0xFF0000);
        }
    }
}
