# 🛠️ Utility (Helper) Architecture

Bu klasör, oyunun "Veri Yöneticileri"ni içerir. JSON dosyalarını okuyan, hastalık bulaşma şansını hesaplayan ve teşhis koyan tüm yardımcı araçlar buradadır.

## 📄 Dosyalar

### **Manager Sınıfları (Veri Okuyucular)**
Bu sınıflar oyun açılırken `assets/vetcraft/...` klasöründeki JSON dosyalarını okur ve belleğe (RAM) alır.
*   **`BreedManager.java`:** İnek ırklarını (`cattle.json`) okur.
*   **`DiseaseManager.java`:** Hastalıkları (`diseases/cattle.json`) okur.
*   **`DrugManager.java`:** İlaçları (`drugs.json`) okur.
*   **`FeedManager.java`:** Yem özelliklerini (`feeds.json`) okur.
*   **`MarketManager.java`:** Piyasa fiyatlarını (`shop_*.json`) okur.

### **Data Sınıfları (Veri Taşıyıcılar)**
Bu sınıflar sadece veri tutan basit kutulardır (POJO).
*   **`BreedData`, `DiseaseData`, `DrugData`, `FeedData`**: JSON'dan okunan verilerin Java nesnesi hali.

### **`VetDiagnostics.java` (Başhekim)**
*   **Görevi:** Teşhis koyma ve bilgi verme.
*   **İşlevi:** `showVetInfo` metodu ile oyuncuya hayvanın sağlık durumu, ateşi, nabzı ve hastalıkları hakkında detaylı rapor (chat mesajı) verir. `CattleEntity` bu sınıfı çağırarak raporu ekrana basar.
