# 🔔 Event System Architecture

Bu klasör, Minecraft'ın olay (Event) sistemine kancalar (Hook) atar. Oyunda "bir şey olduğunda" (örneğin hayvan doğduğunda) devreye giren kodlardır.

## 📄 Dosyalar

### **`ModEvents.java` (Mod Bus Events)**
*   **Görevi:** Mod yüklenirken çalışan olaylar.
*   **İşlevi:** Entity'lerin özelliklerini (Can, Hız) kaydeder (`EntityAttributeCreationEvent`).

### **`ForgeEvents.java` (Game Bus Events)**
*   **Görevi:** Oyun çalışırken sürekli olan olaylar.
*   **İşlevi:** Komutları kaydeder (`RegisterCommandsEvent`) veya oyuncu oyuna girdiğinde yapılacakları belirler.
