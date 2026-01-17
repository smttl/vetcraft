# 🩺 Cattle Disease Configuration Guide (`cattle.json`)

Bu dosya, oyundaki tüm büyükbaş hastalıklarının kurallarını belirleyen yapılandırma dosyasıdır. Aşağıdaki değişkenleri değiştirerek oyunun zorluk seviyesini ve hastalık mekaniklerini özelleştirebilirsiniz.

## 📝 Değişkenler ve Anlamları

### **Temel Kimlik**
*   **`id`**: Oyunun arka planda tanıdığı kod isim (Örn: `pneumonia`). **Asla Türkçe karakter veya boşluk içermemelidir.**
*   **`displayName`**: Oyuncunun oyun içinde gördüğü hastalık ismidir (Örn: "Pnömoni (Zatürre)").

### **Tetikleyiciler (Hastalık Nasıl Başlar?)**
*   **`triggerHungerBelow`**: (`0` - `100`) Hayvanın tokluk oranı (Rumen Doluluğu) bu sayının altına düşerse hastalık riski başlar.
    *   *Örnek:* `40` yazılırsa, hayvan %40 açlığın altına indiğinde risk başlar.
*   **`triggerChance`**: (`0.0` - `1.0`) Tetiklenme şartı sağlandığında hastalığın bulaşma ihtimalidir.
    *   *Örnek:* `0.15` = %15 ihtimal.
*   **`randomOccurrenceChance`**: Hiçbir sebep yokken durduk yere (havadan/sudam) bulaşma ihtimalidir.
    *   *Örnek:* `0.002` = Binde 2 şans.
*   **`contagious`**: (`true`/`false`) `true` ise hasta hayvan yanındaki diğer hayvanlara hastalığı bulaştırır.
*   **`vectors`**: Hastalığı taşıyan diğer canlılar listesidir.
    *   `entity`: Taşıyıcı hayvan ID'si (Örn: `minecraft:sheep`).
    *   `chance`: Bulaştırma ihtimali.
    *   `radius`: Kaç blok yakına gelirse bulaşır.
*   **`risky_items`**: Yendiğinde bu hastalığı tetikleyen "yasaklı" yiyecekler listesidir.

### **Etkiler (Hastalık Ne Yapar?)**
*   **`weightLossPerTick`**: Hayvanın her saniye kaybettiği kilo miktarı.
*   **`damagePerTick`**: Hayvanın her saniye aldığı fiziksel hasar (Can barı düşer).
*   **`abortChance`**: (`0.0` - `1.0`) Eğer hayvan gebeyse, yavruyu düşürme (düşük yapma) ihtimali.
*   **`stopMilk`**: (`true`/`false`) `true` ise, hastalık süresince **süt üretimi tamamen durur**.
*   **`slowness`**: (`true`/`false`) `true` ise, hayvan halsizleşir ve yavaş yürür.
*   **`visualEffect`**: Görsel belirti efekti.
    *   `SNEEZE`: Hapşırma ve burun akıntısı parçacıkları.
    *   `SMOKE`: Gaz veya duman parçacıkları.
    *   `NONE`: Efekt yok.

### **Tedavi (Nasıl Geçer?)**
*   **`cureType`**: Tedavi yönteminin türü.
    *   `ITEM`: İlaç veya aşı kullanarak (Sağ tık ile).
    *   `FEED`: Doğru yemi yedirerek (Örn: Asidoz için Saman).
*   **`cureTarget`**: İyileşmek için sırasıyla verilmesi gereken eşya ID'leri listesi.
    *   *Örnek:* `["vetcraft:penicillin", "vetcraft:fmd_vaccine"]` -> Önce penisilin, sonra aşı yapılmalı.

### **Teşhis (Doktor Ne Görür?)**
`symptoms` bloğu, oyuncu veteriner aletlerini kullandığında sohbette (chat) yazılacak metinlerdir:
*   **`anamnesis`**: Clipboard (Not defteri) ile bakınca görünen hasta hikayesi.
*   **`visual`**: Çıplak gözle (Shift+Sağ Tık veya Gözlem) bakınca görünenler.
*   **`stethoscope`**: Stetoskop ile dinleyince duyulan sesler (Örn: "Ral", "Hırıltı").
*   **`temperature`**: Derece ile ölçüldüğünde çıkan vücut ısısı (Normal inek 38.5°C'dir).
