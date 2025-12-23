package com.vetsim.vetcraft.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vetsim.vetcraft.network.PhonePacket;
import com.vetsim.vetcraft.util.MarketManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import static com.vetsim.vetcraft.money.BankData.claimGrant;
import static com.vetsim.vetcraft.money.BankData.payLoan;

public class PhoneScreen extends AbstractContainerScreen<PhoneMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/creative_inventory/tab_items.png");

    // Hangi sekmedeyiz? (0: Ziraat, 1: Hayvan, 2: Market, 3: Satış)
    private int currentTab = 0;

    public PhoneScreen(PhoneMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 195;
        this.imageHeight = 136;
        this.inventoryLabelY = 1000; // Yazıları gizle
        this.titleLabelY = 1000;
    }

    @Override
    protected void init() {
        super.init();
        rebuildMenu(); // Menüyü oluştur
    }

    // Her sekme değiştiğinde butonları silip yeniden koyar
    private void rebuildMenu() {
        this.clearWidgets(); // Öncekileri temizle
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // --- SOL TARAFTAKİ 4 ANA BUTON (NAVİGASYON) ---
        int btnW = 50;
        int btnH = 20;

        // 1. ZİRAAT
        addRenderableWidget(Button.builder(Component.literal("§cZiraat"), b -> changeTab(0))
                .bounds(x - 55, y + 10, btnW, btnH).build());

        // 2. HAYVAN
        addRenderableWidget(Button.builder(Component.literal("Hayvan"), b -> changeTab(1))
                .bounds(x - 55, y + 35, btnW, btnH).build());

        // 3. MARKET
        addRenderableWidget(Button.builder(Component.literal("Market"), b -> changeTab(2))
                .bounds(x - 55, y + 60, btnW, btnH).build());

        // 4. SATIŞ
        addRenderableWidget(Button.builder(Component.literal("Satış"), b -> changeTab(3))
                .bounds(x - 55, y + 85, btnW, btnH).build());


        // --- ORTA ALAN (SEKMEYE GÖRE DEĞİŞİR) ---

        if (currentTab == 0) { // ZİRAAT EKRANI (KREDİ VE HİBE)

            // --- ÜST KISIM: BİLGİ VE KREDİ ---
            // "Kredi Çek (10.000 TL)" Butonu
            addRenderableWidget(Button.builder(Component.literal("Kredi Çek (+10k)"), b -> takeLoan(10000))
                    .bounds(x + 10, y + 20, 80, 20)
                    .tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("Faizsiz 10.000 TL borç al.\nLimit: 50.000 TL")))
                    .build());

            // "Borç Öde (5.000 TL)" Butonu
            addRenderableWidget(Button.builder(Component.literal("Borç Öde (-5k)"), b -> payLoan(5000))
                    .bounds(x + 10, y + 45, 80, 20)
                    .build());

            // "Bakiye Sorgula" Butonu
            addRenderableWidget(Button.builder(Component.literal("Hesap Özeti"), b -> checkBalance())
                    .bounds(x + 10, y + 70, 80, 20).build());


            // --- SAĞ KISIM: HİBELER (GRANTS) ---

            // Hibe 1: Genç Çiftçi (Başlangıç Desteği)
            addRenderableWidget(Button.builder(Component.literal("Genç Çiftçi\nHibesi (5k)"), b -> claimGrant("genc_ciftci", 5000))
                    .bounds(x + 100, y + 20, 80, 30)
                    .tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("Yeni başlayanlar için devlet desteği.\nTek seferlik.")))
                    .build());

            // Hibe 2: Ekipman Desteği
            addRenderableWidget(Button.builder(Component.literal("Ekipman\nDesteği (2k)"), b -> claimGrant("ekipman", 2000))
                    .bounds(x + 100, y + 55, 80, 30)
                    .tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("Tıbbi malzeme alımı için destek.\nTek seferlik.")))
                    .build());
        }
        else if (currentTab == 1) { // HAYVAN PAZARI
            addRenderableWidget(Button.builder(Component.literal("Holstein (15k)"), b -> buyAnimal("Holstein", 15000))
                    .bounds(x + 10, y + 20, 80, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Angus (25k)"), b -> buyAnimal("Angus", 25000))
                    .bounds(x + 100, y + 20, 80, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Simmental (20k)"), b -> buyAnimal("Simmental", 20000))
                    .bounds(x + 10, y + 50, 80, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Jersey (18k)"), b -> buyAnimal("Jersey", 18000))
                    .bounds(x + 100, y + 50, 80, 20).build());
        }
        else if (currentTab == 2) { // MARKET (JSON'dan gelenler)
            int i = 0;
            for (MarketManager.MarketItem item : MarketManager.BUY_LIST) {
                if (i >= 6) break; // Ekrana sığacak kadar
                int col = i % 2; // 2 sütunlu
                int row = i / 2;
                addRenderableWidget(Button.builder(Component.literal(item.name + " (" + item.price + ")"), b -> buyItem(item.itemId, item.price))
                        .bounds(x + 10 + (col * 90), y + 20 + (row * 25), 85, 20).build());
                i++;
            }
        }
        else if (currentTab == 3) { // SATIŞ EKRANI
            addRenderableWidget(Button.builder(Component.literal("§2TÜMÜNÜ NAKİTE ÇEVİR"), b -> sellAll())
                    .bounds(x + 25, y + 40, 150, 40).build());
        }
    }

    private void changeTab(int tabId) {
        this.currentTab = tabId;
        rebuildMenu();
    }

    // --- İŞLEMLER ---
    private void checkBalance() {
        PacketDistributor.SERVER.noArg().send(new PhonePacket("CHECK_BALANCE", "", 0));
        this.onClose();
    }
    private void buyAnimal(String breed, int price) {
        PacketDistributor.SERVER.noArg().send(new PhonePacket("BUY_ANIMAL", breed, price));
    }
    private void buyItem(String itemId, int price) {
        PacketDistributor.SERVER.noArg().send(new PhonePacket("BUY_ITEM", itemId, price));
    }
    private void sellAll() {
        PacketDistributor.SERVER.noArg().send(new PhonePacket("SELL_ALL", "", 0));
        this.onClose();
    }

    // --- BU METOD EKSİKTİ, BU YÜZDEN HATA VERİYORDU ---
    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        // Arka planı çiz
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // --- BAŞLIKLAR ---
        String title = switch (currentTab) {
            case 0 -> "Ziraat Bankası Mobil";
            case 1 -> "Canlı Hayvan Borsası";
            case 2 -> "Veteriner Market";
            case 3 -> "Hızlı Satış Noktası";
            default -> "VetPhone";
        };
        graphics.drawCenteredString(this.font, "§l" + title, x + (imageWidth / 2), y + 6, 0x404040);

        // ZİRAAT LOGOSU VEYA YAZISI
        if (currentTab == 0) {
            graphics.drawCenteredString(this.font, "", x + (imageWidth / 2), y + 40, 0xFFFFFF);
            graphics.drawCenteredString(this.font, "", x + (imageWidth / 2), y + 65, 0xA0A0A0);
        }
        else if (currentTab == 3) {
            graphics.drawCenteredString(this.font, "Süt, Et ve Derileriniz", x + (imageWidth / 2), y + 90, 0x505050);
            graphics.drawCenteredString(this.font, "anında paraya çevrilir.", x + (imageWidth / 2), y + 100, 0x505050);
        }
    }

    // --- YENİ İŞLEMLER ---
    private void takeLoan(int amount) {
        PacketDistributor.SERVER.noArg().send(new PhonePacket("TAKE_LOAN", "", amount));
        this.onClose();
    }

    private void payLoan(int amount) {
        PacketDistributor.SERVER.noArg().send(new PhonePacket("PAY_LOAN", "", amount));
        this.onClose();
    }

    private void claimGrant(String id, int amount) {
        PacketDistributor.SERVER.noArg().send(new PhonePacket("CLAIM_GRANT", id, amount));
        this.onClose();
    }
}