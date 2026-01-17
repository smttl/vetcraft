# 🎨 Texture Assets Guide

Bu klasör, modun tüm görsellerini (PNG dosyaları) içerir. Oyunda eşyaların ve hayvanların görünmesi için aşağıdaki dosyaları **mutlaka** eklemelisiniz.

## 📁 `item/` Klasörü (Eşya İkonları)
Eşyaların envanterde ve elde nasıl görüneceğini belirler.
*   **Boyut:** 16x16 piksel (Pixel Art).
*   **Format:** `.png` (Arka planı şeffaf).

### **Gerekli Dosyalar:**
Aşağıdaki isimlerle PNG dosyaları oluşturup `item` klasörüne atın:

*   `antibiotics.png` (Antibiyotik Şişesi)
*   `penicillin.png` (Penisilin)
*   `fmd_vaccine.png` (Şap Aşısı)
*   `flunixin.png` (Ağrı Kesici)
*   `multivitamin.png` (Vitamin)
*   `hormone_pgf2a.png` (Kızgınlık İğnesi)
*   `hormone_gnrh.png` (Yumurtlama İğnesi)
*   `hormone_oxytocin.png` (Süt İğnesi)
*   `empty_straw.png` (Boş Payet)
*   `filled_straw.png` (Dolu Payet)
*   `empty_blood_tube.png` (Boş Kan Tüpü)
*   `filled_blood_tube.png` (Dolu Kan Tüpü)
*   `thermometer.png` (Derece)
*   `stethoscope.png` (Stetoskop)
*   `vet_clipboard.png` (Not Defteri)
*   `celep_whistle.png` (Düdük)
*   `halter.png` (Yular)
*   `phone.png` (VetPhone)
*   `celep_spawn_egg.png` (Celep Çağırma Yumurtası)
*   `cattle_spawn_egg.png` (Sığır Çağırma Yumurtası)
*   `vinegar.png` (Sirke)
*   `oil_bottle.png` (Yağ Şişesi)
*   `debit_card.png` (Banka Kartı)
*   `manure.png` (Gübre)
*   `alfalfa.png` (Yonca)
*   `calf_formula.png` (Buzağı Maması)
*   `colostrum_bucket.png` (Ağız Sütü Kovası)
*   `dextrose_serum.png` (Dekstroz Serumu)
*   `salt_lick.png` (Tuz Taşı)
*   `feed_trough.png` (Yemlik - Envanter İkonu)

### **Blok Kaplamaları (`textures/block/` içine):**
Eğer bloklarınız (Tuz taşı, Yemlik vb.) oyun içinde "pembe-siyah" görünüyorsa, `src/main/resources/assets/vetcraft/textures/block/` klasörüne şu dosyaları eklemelisiniz:

*   `salt_lick.png`
*   `feed_trough.png`

---

## 📁 `entity/cattle/` Klasörü (İnek Derileri)
İneklerin 3D model üzerindeki kaplamasıdır.
*   **Boyut:** 64x32 veya 64x64 piksel (Minecraft İnek Şablonu).
*   **Format:** `.png`.

### **Gerekli Dosyalar (Irklar İçin):**
`breeds/cattle.json` dosyasındaki `texturePath` yollarına uygun olmalıdır:

*   `holstein.png` (Siyah-Beyaz alacalı)
*   `angus.png` (Simsiyah veya Kızıl)
*   `jersey.png` (Açık kahve, geyik gibi)
*   `simmental.png` (Krem-Kızıl alacalı)
*   `default.png` (Varsayılan/Yerli ırk dokusu)

### **Önemli Not:**
Eğer bir dosya eksik olursa, oyun o eşyayı veya hayvanı **"Pembe-Siyah Kareler" (Missing Texture)** olarak gösterir.
