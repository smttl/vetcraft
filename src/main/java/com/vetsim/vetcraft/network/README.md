# 📡 Network (Packet) Architecture

Bu klasör, İstemci (Client) ve Sunucu (Server) arasındaki iletişimi sağlar. Minecraft çok oyunculu bir oyun olduğu için, "Telefonda tuşa bastım" bilgisinin sunucuya gitmesi gerekir.

## 📄 Dosyalar

### **`VetNetwork.java`**
*   **Görevi:** Ağ kanalını (Channel) kurar ve paketleri kaydeder.

### **`PhonePacket.java`**
*   **Görevi:** Telefondan gelen emirleri taşır.
*   **İşlevi:** "İnek Satın Al" butonuna basıldığında bu paket sunucuya gider, parayı düşer ve ineği spawn eder. `MarketService` ile konuşur.

### **`BalanceSyncPacket.java`**
*   **Görevi:** Banka bakiyesini senkronize eder.
*   **İşlevi:** Sunucudaki para miktarı değiştiğinde (satış/alış), oyuncunun ekranındaki sayıyı güncellemek için Client'a veri yollar.
