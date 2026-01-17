# 🌾 Feed Configuration Guide (`feeds.json`)

Bu dosya, hayvanlara verilen yemlerin besin değerlerini belirler. Rumen sağlığı ve verim artışı için yemlerin dengeli olması gerekir.

## 📝 Değişkenler ve Anlamları

### **Yem Kimliği**
*   **`id`**: Yemin oyun içindeki eşya ID'si (Örn: `minecraft:wheat`).
*   **`name`**: Gösterilecek isim (Örn: "Buğday").

### **Besin Değerleri**
*   **`nutrition`**: (`0` - `100`) Tokluk değeri.
    *   Bu yemden bir adet yiyince Rumen (Mide) ne kadar dolar?
    *   Örn: `50` = Yarı yarıya doyurur.
*   **`is_dry`**: (`true`/`false`) Yemin "Kaba Yem" (Kuru) olup olmadığı.
    *   `true` (Saman/Yonca): **pH Yükseltir.** Mide asidini dengeler, geviş getirmeyi sağlar.
    *   `false` (Buğday/Mısır/Havuç): **pH Düşürür.** Enerji verir ama çok verilirse **Asidoz** yapar.
*   **`protein`**: (`0.0` - `1.0`) Yemin protein oranı.
    *   Protein, hayvanın **BCS (Kondisyon)** kazanmasını ve büyümesini sağlar.
    *   `0.08` (%8): Düşük protein (Saman). Sadece karın doyurur.
    *   `0.18` (%18): Yüksek protein (Yonca). Hızlı geliştirir.
    *   `0.20` (%20): Çok yüksek (Enerji Yemi/Altın Havuç).

### **Besleme Taktikleri**
*   **Asidoz Tedavisi:** `is_dry: true` olan yemler (Saman) verilmelidir.
*   **Hızlı Besi:** `protein` değeri yüksek yemler verilmelidir.
*   **Denge:** İdeal rasyon, hem kuru hem yaş yemlerin karışımıyla sağlanır. Sadece tek tip besleme sorun yaratır.
