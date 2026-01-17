# 💊 Drug & Medicine Configuration Guide (`drugs.json`)

Bu dosya, oyundaki ilaçların, aşıların ve hormonların etkilerini tanımlar. Bir ilacın yan etkilerini veya ne işe yaradığını buradan ayarlayabilirsiniz.

## 📝 Değişkenler ve Anlamları

### **İlaç Kimliği**
*   **`id`**: İlacın oyun içindeki eşya ID'si (Örn: `vetcraft:antibiotics`).

### **Kategori ve Aksiyon**
*   **`category`**: İlacın genel kullanım amacı.
    *   `CURE`: Hastalık tedavi edici (İyileştirici).
    *   `FUNCTIONAL`: Fonksiyonel (Hormon, Vitamin vb.).
*   **`action`**: (Sadece `FUNCTIONAL` için) Özel etki türü.
    *   `ABORT_OR_ESTRUS`: Gebeliği sonlandırır (Düşük) veya Kızgınlığa sokar (PGF2a).
    *   `VACCINE`: Koruyucu aşı (Şap aşısı).
    *   `MILK_LETDOWN`: Süt indirmeyi sağlar (Oksitosin).

### **Metabolik Etkiler (Yan Etkiler)**
*   **`stress_impact`**: İlaç kullanıldığında hayvanda oluşan stres değişimi.
    *   `+10`: Stresi 10 puan artırır (Can yakar).
    *   `-10`: Stresi 10 puan azaltır (Ağrı kesici, sakinleştirici).
*   **`bcs_impact`**: Vücut kondisyonuna etkisi.
    *   `0.0`: Etkisi yok.
    *   `-0.10`: Kondisyonu düşürür (Zayıflatır).
    *   `0.05`: Kondisyonu artırır (Vitamin).
*   **`toxicity`**: Karaciğere yüklediği toksisite miktarı (Toksik etki).
    *   Yüksek dozda ilaç kullanımı toksisiteyi artırır ve hayvanı zehirleyebilir.
*   **`withdrawal_days`**: **Yasal Arınma Süresi (Gün).**
    *   Bu süre boyunca hayvanın sütü ve eti "İlaçlı" sayılır ve satılamaz.
    *   Örn: `3` ise, ilaç verildikten sonra 3 gün (72000 tick) beklenmelidir.

---
**Önemli:** Eğer `drugs.json` dosyasında olmayan bir eşyayı sağ tıkla kullanırsanız, oyun varsayılan (etkisiz) değerleri kullanır.
