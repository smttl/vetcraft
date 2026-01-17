# 🏦 Service Architecture

Bu klasör, oyunun "Arka Plan İş Mantığı"nı (Business Logic) yöneten servisleri içerir. Bu kodlar genellikle entity'lerden bağımsız, global sistemlerdir.

## 📄 Dosyalar

### **`BankService.java` (Merkez Bankası)**
*   **Görevi:** Oyuncunun parasını yönetir.
*   **İşlevleri:**
    *   Bakiye sorgulama/ekleme/çıkarma.
    *   Kredi çekme ve faiz işletme.
    *   Verileri `Level` (Dünya) dosyasına kaydeder, böylece oyun kapanınca para silinmez.

### **`MarketService.java` (Ticaret Bakanlığı)**
*   **Görevi:** Alım-satım işlemlerini yönetir.
*   **İşlevleri:**
    *   Hayvan satın alma (Parayı düş, hayvanı spawn et).
    *   Hayvan satma (Hayvanı sil, parayı ekle).
    *   eşya ve Sperma ticareti.
    *   `shop_buy.json` ve `shop_sell.json` dosyalarındaki fiyatları kullanır.
