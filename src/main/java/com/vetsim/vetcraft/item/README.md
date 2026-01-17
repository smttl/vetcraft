# 🧪 Item Architecture

Bu klasör, modun oyuna eklediği özel eşyaların (Items) davranış kodlarını içerir.

## 📄 Dosyalar

### **`CelepWhistleItem.java` (Celep Islığı)**
*   **İşlevi:** Tüccarı (Celep) çağırmak için kullanılan araç.
*   **Kullanımı:** Sağ tıklandığında yakına bir tüccar ışınlar veya spawn eder.

### **`FilledBloodTubeItem.java` (Kan Tüpü)**
*   **İşlevi:** Hayvandan alınan kan örneğini taşır.
*   **Özelliği:** İçinde NBT verisi olarak hayvanın **WBC (Lökosit)**, **pH** ve **Hastalık Bilgisi**ni saklar. Mikroskop veya analiz makinesinde kullanılır.

### **`HalterItem.java` (Yular)**
*   **İşlevi:** Hayvanları bağlayıp çekmek için kullanılan ip (Lead benzeri ama daha gelişmiş).

### **`MedicineItem.java` (İlaçlar)**
*   **İşlevi:** Tüm ilaçların (Antibiyotik, Vitamin vb.) temel sınıfı.
*   **Özelliği:** `drugs.json` dosyasındaki verileri okuyarak ilacın etkisini belirler.

### **`SemenStrawItem.java` (Sperma Payeti)**
*   **İşlevi:** Suni tohumlama için boğadan alınan genetik materyal.
*   **Özelliği:** İçinde babanın **Irkı**, **Verim Puanı (PTA)** ve **Kalitesi** kayıtlıdır.

### **`SmartPhoneItem.java` (VetPhone)**
*   **İşlevi:** Yönetim paneli.
*   **Kullanımı:** Banka, Market, İşçi kiralama gibi arayüzleri açar.

### **`VetSpawnEggItem.java`**
*   **İşlevi:** Yaratıcı modda hayvan çağırmak için kullanılan yumurtalar.
