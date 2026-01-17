# 🚀 Initialization (Init) Architecture

Bu klasör, modun "Başlatma Düğmesi"dir. Oyuna eklenecek her şey (Blok, Eşya, Menü) burada NeoForge sistemine kaydedilir.

## 📄 Dosyalar

### **`ModBlocks.java`**
*   **Görevi:** Blokların (Yemlik vb.) kaydedildiği yer.
*   **Detay:** `DeferredRegister` kullanarak blokları oyuna tanıtır.

### **`ModItems.java`**
*   **Görevi:** Eşyaların (İlaçlar, Aletler, Yumurtalar) kaydedildiği yer.
*   **Detay:** Yüzlerce eşyayı tek tek tanımlar ve özelliklerini (Stack boyutu vb.) belirler.

### **`ModCreativeModeTabs.java`**
*   **Görevi:** Yaratıcı Mod (Creative) menüsündeki "VetCraft" sekmesini oluşturur.
*   **Detay:** Modun eşyalarını bu sekmeye dizer.

### **`ModMenuTypes.java`**
*   **Görevi:** GUI (Arayüz) menülerinin kayıt defteridir.
*   **Detay:** Akıllı telefon, analiz makinesi gibi pencereleri tanımlar.
