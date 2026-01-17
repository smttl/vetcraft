# 🧬 Cattle Breeds Configuration Guide (`cattle.json`)

Bu dosya, oyundaki sığır ırklarının (Holstein, Jersey, Angus vb.) genetik özelliklerini tanımlar. Bu değerleri değiştirerek ırkların verimlerini ve dayanıklılıklarını ayarlayabilirsiniz.

## 📝 Değişkenler ve Anlamları

### **Temel Bilgiler**
*   **`id`**: Irkın kod ismi (Örn: `holstein`). **Türkçe karakter ve boşluk içermemelidir.**
*   **`displayName`**: Oyuncunun gördüğü ırk ismi (Örn: "Holstein").
*   **`texturePath`**: Hayvanın dış görünüşünü belirleyen doku dosyasının yolu.

### **Genetik Özellikler**
*   **`milkMultiplier`**: Süt verim çarpanı.
    *   `1.0` = Standart (1 Kova).
    *   `2.0` = Holstein (Çift verim).
    *   `0.5` = Angus (Çok az süt).
*   **`diseaseResistance`**: (`0.0` - `1.0`) Hastalıklara karşı doğal direnç oranı.
    *   `1.0` = Tam koruma (Asla hasta olmaz).
    *   `0.4` = Düşük direnç (Çabuk hasta olur).
*   **`heatTolerance`**: Sıcaklık toleransı türü.
    *   `HEAT`: Sıcağı sever (Çöl/Savana için uygun). Örn: Jersey.
    *   `COLD`: Soğuğu sever (Karlı biyomlar için uygun). Örn: Angus.
    *   `NEUTRAL`: Ilıman iklim sever. Örn: Holstein.
    *   `HEAT` olan bir ırk karda üşür, `COLD` olan çölde ısı stresi yaşar.
*   **`minBcs` / `maxBcs`**: İdeal Vücut Kondisyon Skoru (BCS) aralığı.
    *   Bu aralığın dışına çıkan hayvanların üreme performansı düşer.
    *   Örn: `minBcs: 2.5`, `maxBcs: 4.0`.

## ⚠️ Önemli Not
Eğer yeni bir ırk eklerseniz, oyunun bu ırkı tanıması için doku dosyasının (texture) belirtilen yolda (`textures/entity/cattle/`) mevcut olduğundan emin olun.
