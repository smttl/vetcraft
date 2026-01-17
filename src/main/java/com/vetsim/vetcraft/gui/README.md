# 🖥️ GUI (Interface) Architecture

Bu klasör, oyuncunun ekranda gördüğü pencereleri ve menüleri (Graphical User Interface) yönetir.

## 📄 Dosyalar

### **`PhoneScreen.java` & `PhoneMenu.java` (VetPhone)**
*   **Görevi:** Akıllı telefon arayüzü.
*   **İşlevi:** Banka bakiyesini gösterir, marketten hayvan alma ve işçi kiralama butonlarını içerir.
*   **Mekanik:** Butonlara basıldığında `PhonePacket` göndererek sunucuyla (Server) iletişime geçer.

### **`BloodAnalysisScreen.java` & `BloodAnalysisMenu.java`**
*   **Görevi:** Kan Analiz Makinesi arayüzü.
*   **İşlevi:** Kan tüpünü koyduğunuzda sonuçları (WBC, pH, Hastalık) ekrana yazdırır.
