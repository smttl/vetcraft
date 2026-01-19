# VetCraft 🐮🩺
**Minecraft için Gelişmiş Veterinerlik ve Hayvancılık Simülasyonu**

VetCraft, Minecraft'taki hayvancılık mekaniklerini tamamen değiştirerek **gerçekçi bir veteriner hekimlik ve çiftlik yönetimi** deneyimi sunar. Sadece inekleri besleyip çoğaltmak yerine; onların metabolizmasını, hormon döngülerini, genetik kalitesini ve hastalıklarını yönetmeniz gerekir.

---

## 🌟 Detaylı Sistem Özellikleri

### 1. 🧬 Fizyoloji ve Metabolizma
Her hayvanın yaşayan, dinamik bir metabolizması vardır.

*   **Rumen pH Dengesi:** 
    *   **Asidoz (pH < 5.8):** Çok fazla tahıl (Buğday, Mısır) ve az kaba yem verilirse oluşur. Süt yağı düşer, hayvan topallar. Tedavisi: Saman .
    *   **Alkaloz (pH > 7.5):** Çok fazla kaba yem veya protein kaynaklı. Tedavisi: Dengeli rasyon.
*   **Hidrasyon ve Su:** 
    *   Süt veren inekler günde ~100L su içer. Susuzluk süt verimini anında keser.
    *   **Hastalık Etkisi:** İshal (Scours) olan hayvanlar 2 kat hızlı susar.
*   **Gaz (Bloat/Timpani):** 
    *   Islak yonca veya taze ot (yonca) aşırı tüketilirse gaz birikir.
    *   Gaz %80'i geçerse hayvan şişer ve canı yanmaya başlar. Tedavi: Yağ Şişesi (Oil Bottle).
*   **Isı Stresi (Heat Stress):**
    *   **Irk Faktörü:** Angus/Holstein gibi soğuk iklim ırkları Çöl/Savana biyomlarında strese girer. Jersey ve Brahman sıcağa dayanıklıdır.
    *   Aşırı sıcakta süt verimi ve döl tutma oranı (Fertilite) düşer.

### 2. 🛡️ Bağışıklık ve Buzağı Bakımı
*   **Ağız Sütü (Colostrum):**
    *   Yeni doğan buzağıların bağışıklığı **0**'dır.
    *   Doğumdan sonraki ilk 24 saat içinde anneden sağılan **Ağız Sütü** `colostrum_bucket` içirilmezse bağışıklık gelişmez.
    *   Ağız sütü almayan buzağılar **Buzağı İshali (Calf Scours)** hastalığına yakalanır ve gelişimi durur.
*   **Genetik Direnç:**
    *   Annenin genetik bağışıklık mirası yavruya geçer.
    *   Direnci yüksek ırklar (Native) hastalıklara %50 daha az yakalanır.

### 3. 🦠 Hastalık ve Patoloji
Hastalıklar artık rastgele değil, sebebe dayalıdır.

*   **Bulaşma (Contagion):**
    *   **Vektörler:** Koyunlar **Mavi Dil**, Domuzlar **Şap** taşıyıcısı olabilir. Onları ineklerden uzak tutun!
    *   **Hız:** Şap (FMD) gibi hastalıklar çok hızlı (%80), Mantar gibi hastalıklar yavaş (%5) yayılır.
*   **Ölümcüllük:**
    *   Basit hastalıklar (Topallık, Mantar) hayvanı öldürmez (Canı 1 Kalpte kalır).
    *   Ciddi hastalıklar (Şap, Pnömoni, Şarbın) tedavi edilmezse **ÖLDÜRÜR**.
*   **Semptomlar:**
    *   Öksürük (Pnömoni), Topallama (Ayak Çürüğü), Şişme (Bloat), Düşük (Abort).

### 4. 💉 Veteriner Hekimlik ve Farmakoloji
Gerçek tedavi protokolleri uygulayın.

*   **Teşhis Araçları:**
    *   🩺 **Stetoskop:** Nabız (Taşikardi/Bradikardi) ve solunum.
    *   🌡️ **Termometre:** Ateş (Enfeksiyon belirtisi).
    *   🩸 **Kan Analizi:** Lökosit (WBC), Kan pH, Keton ve Karaciğer Enzimleri.
*   **İlaç Yan Etkileri:**
    *   **Toksisite:** Aşırı ilaç kullanımı karaciğeri yorar ve zehirlenmeye yol açar.
    *   **Kalıtım Süresi (Withdrawal):** Antibiyotik yapılan hayvanın sütü belirli bir süre (3-7 gün) **TÜKETİLEMEZ/SATILAMAZ**.

### 5. 🧬 Genetik ve Islah (Breeding)
Basit çiftleştirme yerine bilimsel ıslah yapın.

*   **Suni Tohumlama:**
    *   Boğalardan **Sperma Payeti (Straw)** alın veya "Genetik Market"ten sertifikalı (Elite, Superior) spermalar satın alın.
    *   **Östrus (Kızgınlık):** İnekler sadece 21 günde bir kızgınlığa gelir. Progesteron seviyesi düştüğünde tohumlama yapılmalıdır.
    *   **Hormon Yönetimi:** PGF2a ve GnRH ile kızgınlığı senkronize edebilirsiniz.
*   **PTA Değerleri (Predicted Transmitting Ability):**
    *   **MilkPTA:** Süt verim genetiği (+500kg).
    *   **HealthPTA:** Hastalık direnç genetiği (+1.2).
    *   Yavrular bu genleri anne ve babadan alır. Hedefiniz: Yüksek süt, yüksek sağlık!

### 6. 🌾 Yem ve Rasyon
*   **Kaba Yemler (Lifli):** Saman, Yonca, Kuru Ot. (Mideyi çalıştırır, pH yükseltir).
*   **Kesif Yemler (Enerji):** Buğday, Mısır, Arpa, Ekmek. (Hızlı kilo aldırır, Asidoz riski vardır).
*   **Sulu Yemler:** Pancar (Beetroot), Silaj. (Su ihtiyacını azaltır).
*   **Özel Yemler:**
    *   **Altın Havuç:** Yüksek enerjili "Power Feed". Zayıf hayvanları toparlar.
    *   **Buzağı Maması:** Annesi ölen yavrular için.

---

## 🛠️ Eşyalar ve Kullanımı

| Eşya | Görevi |
| :--- | :--- |
| **Boş Kan Tüpü** | Sağ tıklayarak kan örneği alır. |
| **Dolu Kan Tüpü** | Sağ tıklayarak detaylı **Laboratuvar Raporunu** açar. |
| **Stetoskop** | Hayvanın sağlık durumunu özetler. |
| **PGF2a İğnesi** | Kızgınlığı tetikler veya düşüğe (abort) sebep olur. |
| **Ağız Sütü** | Yeni doğan buzağıya İLK GÜN içirilmelidir. |
| **VetPhone** | Market, Banka ve Genetik Borsası. |
| **Gübre (Manure)** | İneklerden düşer, satılabilir veya tarlada kullanılır. |

---

## 🔧 Teknik Bilgi
*   **Mod Yükleyici:** NeoForge
*   **Minecraft Sürümü:** 1.20.4
*   **Geliştirici:** smtl

---

## 📂 Proje Dokümantasyonu (Project Documentation)

Projenin farklı modülleri hakkında detaylı bilgi için aşağıdaki dokümanları inceleyebilirsiniz:

### 📚 Oyun Sistemi Rehberleri (Game Mechanics)
*   [🌾 Beslenme ve Sindirim Sistemi](docs/Besleme_Sistemi.md)
*   [💰 Ekonomi ve İşletme Sistemi](docs/Ekonomi_ve_İşletme_Sistemi.md)
*   [🧪 Genetik ve Islah Sistemi](docs/Genetik_ve_Islah_Sistemi.md)
*   [🦠 Hastalık ve Metabolizma Sistemi](docs/Hastalık_ve_Metabolizma_Sistemi.md)
*   [🧬 Reprodüktif Sistem](docs/Reprodüktif_Sistem.md)

### ⚙️ Konfigürasyon ve Varlıklar (Assets & Config)
*   [🐄 Irklar (Breeds)](src/main/resources/assets/vetcraft/breeds/README.md) - Irk özellikleri, genetik ve varyasyonlar.
*   [🦠 Hastalıklar (Diseases)](src/main/resources/assets/vetcraft/diseases/README.md) - Hastalık tanımları, belirtiler ve tedavi.
*   [🏥 İlaçlar (Medicines)](src/main/resources/assets/vetcraft/medicines/README.md) - İlaç etkileri, yan etkiler ve kullanım.
*   [🌾 Yemler (Feeds)](src/main/resources/assets/vetcraft/data/README.md) - Yem değerleri ve besleme.
*   [🎨 Dokular (Textures)](src/main/resources/assets/vetcraft/textures/README.md) - Görsel kaynaklar ve modelleme.

### 💻 Kaynak Kod Modülleri (Source Code)
*   [🧱 Bloklar (Block)](src/main/java/com/vetsim/vetcraft/block/README.md)
*   [⚙️ Ayarlar (Config)](src/main/java/com/vetsim/vetcraft/config/README.md)
*   [animals Varlıklar (Entity)](src/main/java/com/vetsim/vetcraft/entity/README.md) - Yapay zeka, genetik ve fizyoloji.
*   [🔔 Olaylar (Event)](src/main/java/com/vetsim/vetcraft/event/README.md)
*   [🖥️ Arayüz (GUI)](src/main/java/com/vetsim/vetcraft/gui/README.md)
*   [🚀 Başlatma (Init)](src/main/java/com/vetsim/vetcraft/init/README.md)
*   [🧪 Eşyalar (Item)](src/main/java/com/vetsim/vetcraft/item/README.md)
*   [📡 Ağ (Network)](src/main/java/com/vetsim/vetcraft/network/README.md) - Paketler ve Client-Server senkronizasyonu.
*   [🎨 Render (Renderer)](src/main/java/com/vetsim/vetcraft/renderer/README.md)
*   [💼 Servisler (Service)](src/main/java/com/vetsim/vetcraft/service/README.md) - Market, Banka ve İş mantığı.
*   [🛠️ Araçlar (Util)](src/main/java/com/vetsim/vetcraft/util/README.md)

