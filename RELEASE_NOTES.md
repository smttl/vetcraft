# VetCraft 2.0.0 - "Fizyoloji Güncellemesi" 🐮🩺

**Yayın Tarihi:** 11 Ocak 2026
**Minecraft Sürümü:** 1.20.4 (NeoForge)
**Durum:** Kararlı Sürüm (Stable Release)

Bu büyük güncelleme ile VetCraft, basit bir mod olmaktan çıkıp derinlemesine bir veterinerlik simülasyonuna dönüşüyor. Sığırların sindiriminden genetiğine kadar her yönü gerçek biyolojik sistemleri simüle edecek şekilde yeniden yazıldı.

---

## 🌟 Öne Çıkan Özellikler

### 🧬 Gelişmiş Fizyoloji Çekirdeği
*   **Metabolizma Sistemi:** İnekler artık Rumen pH dengesini, Susuzluğu (Hidrasyon), Gaz sıkışmasını (Bloat) ve Vücut Kondisyonunu (BCS) yönetiyor.
*   **Isı Stresi (Heat Stress):**
    *   Çöl veya Savana gibi sıcak biyomlarda inekler ısı stresine girer ve verim kaybeder.
    *   **Irk Toleransı:** `Jersey` ve `Yerli (Native)` ırkları sıcağa dayanıklıyken, `Angus` ve `Holstein` sıcakta zorlanır.
*   **Sindirim:**
    *   Sürekli tahıl (buğday/mısır) vermek pH'ı düşürür (**Asidoz Riski**).
    *   Saman vermek pH'ı yükseltir ve dengeyi sağlar.
    *   Islak yonca veya taze ot, midede Köpüklü Gaz (**Bloat**) oluşumuna neden olur.

### 🦠 Dinamik Hastalık & Bağışıklık
*   **Patoloji:** Hastalıklar artık rastgele değil! Bulaşma oranları ve taşıyıcı vektörler (Koyun/Domuz) ile yayılır.
*   **Buzağı Bağışıklığı:**
    *   Yeni doğan buzağıların **Bağışıklığı 0**'dır.
    *   İlk 24 saat içinde mutlaka anneden sağılan **Ağız Sütü (Colostrum Bucket)** içirilmelidir. Aksi takdirde **Buzağı İshali (Scours)** gelişir.
*   **Ölümcüllük:** Şap (FMD), Şarbın gibi ağır hastalıklar tedavi edilmezse **ÖLDÜRÜR**. Hafif hastalıklar canı 1 kalpte bırakır ama verimi keser.

### 💉 Veteriner Hekimlik ve Farmakoloji
*   **Gerçek Tedaviler:**
    *   **Antibiyotikler:** Enfeksiyonu bitirir ama süte kalıntı bırakır.
    *   **Ağrı Kesiciler (Flunixin):** Ateşi düşürür ve ağrıyı keser.
    *   **Doğal Yöntemler:** Asidoz için Sirke, Gaz şişkinliği için Yağ Şişesi.
*   **İlaç Güvenliği:**
    *   **Toksisite:** Aşırı ilaç kullanımı karaciğeri zehirler.
    *   **Kalıntı Süresi (Withdrawal):** İlaçlanan ineğin sütü 3-7 gün boyunca **SATILAMAZ**.
*   **Teşhis:**
    *   **Kan Analizi:** Kan örneği alın -> Laboratuvarda inceleyin -> Lökosit (WBC), Keton ve pH değerlerini görün.

### 🧬 Genetik & Borsa
*   **Genetik Market:** **VetPhone** (Akıllı Telefon) kullanarak sertifikalı (Ticari, Süper, Elit) boğa spermaları satın alın.
*   **Şans Bazlı Verim (Probabilistic):**
    *   Süt verimi artık matematiksel bir şansa bağlıdır. (Örn: 1.6 Verim Puanı = 1 Kova Garanti + %60 İhtimalle 2. Kova).
*   **Kalıtım:** Doğan buzağılar anne ve babadan `MilkPTA` (Süt Genetiği) ve `HealthPTA` (Sağlık Genetiği) özelliklerini miras alır.

---

## 🛠️ Yeni Eşyalar
*   **Genel:**
    *   `Colostrum Bucket` (Ağız Sütü Kovası)
    *   `Calf Formula` (Buzağı Maması - Annesi ölenler için)
    *   `Cattle Spawn Egg` (Rastgele Irk Çağırma Yumurtası)
*   **Yeni Yemler:**
    *   `Pancar (Beetroot)`: Sulu kaba yem alternatifi.
    *   `Altın Havuç`: Yüksek enerjili konsantre yem.
    *   `Ekmek`: Enerji takviyesi.

---

## 🐛 Hata Düzeltmeleri & İyileştirmeler
*   **Düzeltme:** `hormone_pgf2a` iğnesinin çalışmama sorunu giderildi (JSON hatası).
*   **Düzeltme:** Sonsuz süt sağma hatası (Exploit) giderildi. Artık "Manuel Sağım" zorunlu.
*   **Düzeltme:** Östrus sayacının eksiye düşme hatası giderildi ("Metaestrus" evresi eklendi).
*   **Refactor:** Kod yapısı tamamen temizlendi (`vetsim` -> `vetcraft` uyumluluğu sağlandı).

---

## 📦 Kurulum
1.  Minecraft 1.20.4 için **NeoForge** yükleyin.
2.  `vetcraft-2.0.0.jar` dosyasını `mods` klasörüne atın.
3.  İyi oyunlar!
