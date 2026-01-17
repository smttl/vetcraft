# 🐄 Entity Architecture Documentation

Bu klasör, VetCraft modundaki canlı varlıkların (Entity) beyin ve vücut kodlarını içerir. Modun "nasıl düşündüğünü" anlamak için bu rehberi kullanabilirsiniz.

## 📂 Ana Sınıflar (Main Classes)

### **`CattleEntity.java` (Merkezi Beyin)**
*   **Görevi:** İneğin kendisidir. Minecraft'ın `Animal` sınıfından türetilmiştir.
*   **İşlevi:** Tüm alt sistemleri (Sağlık, Metabolizma, Üreme) birleştirir ve yönetir.
*   **Özellikleri:**
    *   Oyuncu etkileşimlerini (Sağ tık) yönetir (Süt sağma, Aşı yapma).
    *   Verileri kaydeder ve yükler (NBT Tags).
    *   Görsel efektleri (Hapşırma, Duman) oynatır.
    *   `tick()` döngüsü ile her saniye alt sistemleri günceller.

### **`CelepEntity.java` (Tüccar)**
*   **Görevi:** Hayvan alım-satımı yapan NPC (Köylü benzeri).
*   **İşlevi:** Oyunculara hayvan satar veya onlardan hayvan satın alır. Pazar mekaniğini yönetir.

### **`ModEntities.java` (Kayıt Defteri)**
*   **Görevi:** Tüm entity'lerin oyun motoruna (NeoForge) kaydedildiği yerdir.

---

## 🧩 Bileşenler (Components) - `components/`
İneğin karmaşık biyolojik sistemlerini tek bir dosyaya sıkıştırmak yerine parçalara ayırdık.

### **1. `CattleHealth.java` (Sağlık Bakanlığı)**
*   **Sorumluluğu:** Hastalıklar, Stres ve İyileşme.
*   **Neler Yapar?**
    *   Çevreyi tarar (Kirlilik, Kalabalık) ve hastalık bulaştırır.
    *   İlaç tedavilerini ve aşıları takip eder.
    *   Antibiyotik sonrası arınma süresini (Withdrawal) sayar.

### **2. `CattleMetabolism.java` (Enerji ve Beslenme)**
*   **Sorumluluğu:** Açlık, Susuzluk, Sindirim ve Süt Üretimi.
*   **Neler Yapar?**
    *   **Rumen pH:** Yediği yeme göre mide asidini (Asidoz/Alkaloz) hesaplar.
    *   **BCS (Kondisyon):** Hayvanın zayıflayıp şişmanlamasını yönetir.
    *   **Süt:** Su, Stres ve Beslenmeye göre süt üretip üretmeyeceğine karar verir.

### **3. `CattleReproduction.java` (Üreme ve Genetik)**
*   **Sorumluluğu:** Kızgınlık, Gebelik ve Doğum.
*   **Neler Yapar?**
    *   4 dakikalık (4800 tick) Östrus döngüsünü çevirir.
    *   Tohumlama başarısını hesaplar (Progesteron hormonu seviyesine göre).
    *   Doğan yavrunun genetiğini (PTA) ve özelliklerini belirler.

---

## 🧠 Yapay Zeka (AI Goal) - `ai/`
Hayvanların kendi başına nasıl davranacağını belirleyen "İçgüdü" kodlarıdır.

*   **`EatFromTroughGoal.java`**: Yemlik (Trough) bloğundan yemek yeme zekası.
*   **`DrinkWaterGoal.java`**: Su kaynaklarını (Nehir, tekne) bulup su içme zekası.
*   **`NaturalBreedingGoal.java`**: Boğaların inekleri bulup doğal yolla çiftleşme zekası.
*   **`EatItemGoal.java`**: Yerdeki atılmış eşyaları (Elma vb.) yeme zekası.

---

## 👷 İşçiler - `worker/`

### **`WorkerEntity.java`**
*   **Görevi:** Çiftlikte oyuncuya yardım eden işçi NPC.
*   **İşlevi:** Maaş karşılığı çalışır, gübreleri temizler, hayvanları besler.
