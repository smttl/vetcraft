package com.vetsim.vetcraft.gui;

import com.vetsim.vetcraft.network.VetDiagnosisPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class VetDiagnosisScreen extends Screen {
    private final VetDiagnosisPacket data;
    private final int imageWidth = 256;
    private final int imageHeight = 200; // Adjusted height

    public VetDiagnosisScreen(VetDiagnosisPacket data) {
        super(Component.literal("Vet Diagnosis"));
        this.data = data;
    }

    @Override
    protected void init() {
        super.init();
        // Add buttons here if needed (e.g., Close button)
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        int centerX = (this.width - this.imageWidth) / 2;
        int centerY = (this.height - this.imageHeight) / 2;

        // Draw background (Paper look)
        pGuiGraphics.fill(centerX, centerY, centerX + imageWidth, centerY + imageHeight, 0xFFF5E6CC); // Paper color
        pGuiGraphics.renderOutline(centerX, centerY, imageWidth, imageHeight, 0xFF5A3E2B); // Brown border

        // --- HEADER ---
        pGuiGraphics.drawCenteredString(this.font, "§6§l⭐ VETERİNER KAYIT FORMU ⭐", this.width / 2, centerY + 10,
                0xFFFFFF);
        pGuiGraphics.drawString(this.font, "Küpe No: " + data.earTag(), centerX + 10, centerY + 25, 0x000000, false);
        pGuiGraphics.drawString(this.font, "Sahip: " + data.owner(), centerX + 120, centerY + 25, 0x555555, false);

        int y = centerY + 45;
        int spacing = 10;

        // --- LEFT COLUMN (General & Metabolism) ---
        pGuiGraphics.drawString(this.font, "§nGENEL BİLGİLER", centerX + 10, y, 0x333333, false);
        y += spacing + 2;
        pGuiGraphics.drawString(this.font, "Irk: " + data.breed(), centerX + 10, y, 0x000000, false);
        y += spacing;
        pGuiGraphics.drawString(this.font, "Cinsiyet: " + data.gender(), centerX + 10, y, 0x000000, false);
        y += spacing;
        pGuiGraphics.drawString(this.font, "Yaş: " + data.age(), centerX + 10, y, 0x000000, false);
        y += spacing + 5;

        pGuiGraphics.drawString(this.font, "§nMETABOLİZMA", centerX + 10, y, 0x333333, false);
        y += spacing + 2;
        pGuiGraphics.drawString(this.font, "Kondisyon (BCS): " + String.format("%.1f", data.bcs()), centerX + 10, y,
                0x000000, false);
        y += spacing;
        pGuiGraphics.drawString(this.font, "Ağırlık: " + (int) data.weight() + " kg", centerX + 10, y, 0x000000, false);
        y += spacing;
        pGuiGraphics.drawString(this.font, "Rumen Doluluk: %" + (int) data.rumenFill(), centerX + 10, y,
                getFillColor(data.rumenFill()), false);
        y += spacing;
        pGuiGraphics.drawString(this.font, "Rumen pH: " + String.format("%.1f", data.rumenPh()), centerX + 10, y,
                getPhColor(data.rumenPh()), false);
        y += spacing;
        pGuiGraphics.drawString(this.font, "Hidrasyon: %" + (int) data.hydration(), centerX + 10, y,
                (data.hydration() < 30 ? 0xFF0000 : 0x000000), false);

        // --- RIGHT COLUMN (Health & Repro) ---
        y = centerY + 45;
        int x2 = centerX + 130;

        pGuiGraphics.drawString(this.font, "§nSAĞLIK DURUMU", x2, y, 0x333333, false);
        y += spacing + 2;

        String diseaseText = data.disease().equals("NONE") ? "§aTEMİZ" : "§c" + data.disease().toUpperCase();
        pGuiGraphics.drawString(this.font, "Hastalık: " + diseaseText, x2, y, 0x000000, false);
        y += spacing;

        if (!data.secondaryDisease().equals("NONE")) {
            pGuiGraphics.drawString(this.font, "Ek Teşhis: §4" + data.secondaryDisease().toUpperCase(), x2, y, 0x000000,
                    false);
            y += spacing;
        }

        String stressColor = data.stress() > 50 ? "§c" : "§a";
        pGuiGraphics.drawString(this.font, "Stres: " + stressColor + "%" + data.stress(), x2, y, 0x000000, false);
        y += spacing + 5;

        if (data.gender().equals("Erkek")) {
            pGuiGraphics.drawString(this.font, "§nANDROLOJİ", x2, y, 0x333333, false);
            y += spacing + 2;

            if (data.age().equals("Yavru")) {
                pGuiGraphics.drawString(this.font, "§7Ergenlik Öncesi", x2, y, 0x000000, false);
            } else {
                pGuiGraphics.drawString(this.font, "Sperma Kalitesi: Uygun", x2, y, 0x000000, false);
                y += spacing;

                // Cooldown Display
                if (data.breedingCooldown() > 0) {
                    int minutes = data.breedingCooldown() / 1200; // 1200 ticks = 1 min
                    pGuiGraphics.drawString(this.font, "Sperma Toplama:", x2, y, 0x000000, false);
                    y += spacing;
                    pGuiGraphics.drawString(this.font, "§c⏳ Bekleniyor (" + (minutes + 1) + " dk)", x2, y, 0x000000,
                            false);
                } else {
                    pGuiGraphics.drawString(this.font, "Sperma Toplama:", x2, y, 0x000000, false);
                    y += spacing;
                    pGuiGraphics.drawString(this.font, "§a✅ HAZIR", x2, y, 0x000000, false);
                }
            }
            y += spacing;

        } else {
            // FEMALE LOGIC
            pGuiGraphics.drawString(this.font, "§nREPRODÜKSİYON", x2, y, 0x333333, false);
            y += spacing + 2;

            if (data.isPregnant()) {
                pGuiGraphics.drawString(this.font, "§d♥ GEBE", x2, y, 0x000000, false);
                y += spacing;
                if (data.isDryPeriod()) {
                    pGuiGraphics.drawString(this.font, "§e(Kuruda - Doğum Yakın)", x2, y, 0x000000, false);
                    y += spacing;
                }
            } else if (data.breedingCooldown() > 0 && !data.isLactating()) {
                pGuiGraphics.drawString(this.font, "§7Lohusa / Dinlenmede", x2, y, 0x000000, false);
                y += spacing;
            } else {
                // Cycle info
                pGuiGraphics.drawString(this.font, "Prog: " + String.format("%.2f", data.progesterone()) + " ng/mL", x2,
                        y,
                        0x000000, false);
                y += spacing;

                String cycleStatus = "Sakin";
                if (data.progesterone() < 2.5f)
                    cycleStatus = "§a§lKIZGINLIK (ÖSTRUS)";
                else if (data.progesterone() > 4.0f)
                    cycleStatus = "Sakin (Diöstrus)";

                pGuiGraphics.drawString(this.font, cycleStatus, x2, y, 0x000000, false);
                y += spacing;
            }
        }

        if (data.isLactating()) {
            pGuiGraphics.drawString(this.font, "§bSüt Veriyor (Laktasyon)", x2, y, 0x000000, false);
        } else {
            pGuiGraphics.drawString(this.font, "§7Süt Yok (Kuru)", x2, y, 0x000000, false);
        }

        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    private int getPhColor(float ph) {
        if (ph < 5.8 || ph > 7.2)
            return 0xFF0000; // Red
        return 0x00AA00; // Green
    }

    private int getFillColor(float fill) {
        if (fill < 30)
            return 0xFF0000;
        if (fill > 80)
            return 0x00AA00;
        return 0xAAAA00; // Yellow-ish
    }
}
