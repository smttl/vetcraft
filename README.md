# 🩺 VetCraft - Advanced Veterinary Simulation Mod

**VetCraft**, Minecraft dünyasına gerçekçi veterinerlik, genetik ve hayvancılık mekanikleri ekleyen kapsamlı bir moddur. Standart Minecraft hayvancılığının ötesine geçerek; hastalık teşhisi, laboratuvar testleri, ilaç tedavileri ve detaylı genetik takibi sunar.

---

## 🌟 Temel Özellikler

### 1. Gelişmiş Sığır Genetiği (Cattle Entity)
Oyundaki standart ineklerin yerini alan, tamamen özelleştirilmiş **VetSim Cattle** varlığı:
- **Kimlik Sistemi:** Her hayvanın kendine özel, görsel olarak tepesinde ve veritabanında görünen bir Küpe Numarası (Ear Tag) vardır (Örn: `TR45129`).
- **Irk ve Cinsiyet:** Holstein, Simmental, Angus, Jersey gibi gerçek ırklar. Boğa (Erkek) ve İnek (Dişi) ayrımı mevcuttur.
- **Fiziksel Özellikler:** Dinamik ağırlık sistemi (kg), yaş takibi (gün bazlı büyüme) ve açlık/metabolizma döngüsü.

### 2. Gerçekçi Sağlık ve Hastalık Sistemi
Hayvanlar sadece can barından ibaret değildir. Çevresel faktörlere ve beslenmeye göre hastalanabilirler.

**Hastalıklar:**
- **🦠 Pnömoni (Zatürre):** Bulaşıcıdır. Hapşırma efekti ve hırıltılı solunum yapar. Lökosit (WBC) değerlerini fırlatır.
- **🧪 Rumen Asidozu:** Yanlış beslenme sonucu oluşur. Hayvanın kan pH değeri düşer, hareketleri yavaşlar.

**Belirtiler:** Kilo kaybı, hareket yavaşlaması, parçacık efektleri (duman, hapşırık) ve sesli tepkiler.

### 3. Tanı ve Laboratuvar Sistemi 🔬
Hastalıkları teşhis etmek için gerçekçi veteriner aletleri:
- **🩺 Stetoskop:** Kalp ve akciğer seslerini dinler (Örn: "Akciğerlerde yaş hırıltı/Ral").
- **🌡️ Termometre:** Vücut ısısını ölçer (Yüksek ateş enfeksiyon belirtisidir).
- **📋 Vet Kayıt Defteri (Anamnez):** Hayvanın genel durumunu ve geçmişini raporlar.
- **🩸 Laboratuvar (Kan Tahlili):**
    - Hayvandan **Boş Tüp** ile kan alınır.
    - **Kan Numunesi** envanterde incelendiğinde WBC (Lökosit) ve pH değerlerini gösterir.
    - Oyuncu bu değerlere bakarak (Yüksek WBC = Enfeksiyon vb.) teşhis koymalıdır.

### 4. Farmakoloji ve Tedavi 💊
Her hastalığın spesifik bir ilacı veya tedavi yöntemi vardır:
- **Penisilin & Antibiyotikler:** Bakteriyel enfeksiyonlar (Pnömoni) için.
- **Flunixin:** Ağrı kesici ve ateş düşürücü.
- **Multivitamin:** Bağışıklık desteği.
- **Diyet Tedavisi:** Asidoz geçiren hayvanlar için Kuru Ot (Saman) diyeti.

---

## 🎮 Oynanış ve Kontroller

Mod, sağ tıklama etkileşimleri üzerine kuruludur. Çift el karışıklığını önlemek için özel bir etkileşim sistemi kodlanmıştır.

| Eşya / Durum | Eylem | Sonuç |
| :--- | :--- | :--- |
| **Boş El** | İneğe Sağ Tık | **Detaylı Bilgi Ekranı** (Irk, yaş, kilo, gebelik durumu). |
| **Boş El + Shift** | Eğilerek Sağ Tık | **Gözle Muayene** (Tüylerin durumu, duruş bozukluğu). |
| **Stetoskop** | İneğe Sağ Tık | Kalp ritmi ve akciğer seslerini sohbete yazar. |
| **Boş Kan Tüpü** | İneğe Sağ Tık | Kan alır ve envantere etiketli bir numune verir. |
| **İlaçlar** | İneğe Sağ Tık | İlacı uygular (Doğru ilaçsa iyileştirir). |

### 🩸 Laboratuvar Sonuçlarını Okuma
Envanterinizdeki kanlı tüpün üzerine geldiğinizde:
- **WBC (Lökosit):** `> 12.0` ise **Kırmızı** yanar (Enfeksiyon/Pnömoni Var).
- **pH:** `< 7.30` ise **Kırmızı** yanar (Asidoz Var).
- **Normal:** Değerler beyaz renktedir.

---

## 🛠️ Teknik Detaylar
Minecraft Sürümü: 1.20.4 (NeoForge)

Dil Desteği: Türkçe (tr_tr), İngilizce (en_us)

Veri Kaydı: Tüm veriler (Hastalık, Genetik, Küpe No) NBT tagları ile sunucu tarafında saklanır ve dünya kapatılıp açılsa bile korunur.

## 🧬 JSON Tabanlı Hastalık Sistemi (Data-Driven)
Mod, hard-coded yerine Veri Odaklı bir yapı kullanır. Yeni hastalıklar kod yazmadan, sadece JSON düzenleyerek eklenebilir.

Dosya Yolu: assets/vetsim/diseases/cattle.json

## 🛠️ Etkileşim Mantığı (Interaction Logic)
Minecraft'ın "Çift El" (Main Hand / Off Hand) sorununu çözmek için mobInteract metodunda katı bir hiyerarşi uygulanmıştır:

OFF_HAND (Sol el) etkileşimleri iptal edilir (InteractionResult.PASS).

Sunucu tarafında (!level.isClientSide) işlem yapılır.

Öncelik sırası: Aletler > İlaçlar > Yemler > Boş El.

## 🧪 Genetik Algoritması
Buzağı doğduğunda (giveBirth metodu):

Annenin ırkını ve babanın ırkını (VetSim_FatherBreed) alır.

%50 ihtimalle anneden, %50 ihtimalle babadan ırk özelliğini miras alır.

Rastgele bir küpe numarası (TR + 6 hane) atanır.

## 💻 Komutlar (Geliştirici/Test)

Test ortamında hızlıca senaryo oluşturmak için özel NBT verileriyle çağırma komutları:

**1. Hasta İnek (Pnömoni - Enfeksiyonlu):**
```mcfunction
/summon vetsim:cattle ~ ~ ~ {VetSim_Disease:"pneumonia", VetSim_AgeDays:20}

2. Hasta İnek (Asidoz - Mide Rahatsızlığı):

Kod snippet'i

/summon vetsim:cattle ~ ~ ~ {VetSim_Disease:"acidosis", VetSim_AgeDays:20}
3. Damızlık Boğa (Simmental, 900kg):

Kod snippet'i

/summon vetsim:cattle ~ ~ ~ {VetSim_IsMale:1b, VetSim_Weight:900.0f, VetSim_Breed:"Simmental", VetSim_AgeDays:20}
4. Gebe İnek (Doğuma Yakın):

Kod snippet'i

/summon vetsim:cattle ~ ~ ~ {VetSim_IsPregnant:1b, VetSim_PregnancyTimer:200, VetSim_AgeDays:20}


