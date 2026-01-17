# 🎨 Client Renderer Architecture

Bu klasör, Moddaki varlıkların (Entity) oyuncuya nasıl görüneceğini belirler.

## 📄 Dosyalar

### **`CattleRenderer.java`**
*   **Görevi:** İneklerin modelini ve kaplamasını (Texture) çizer.
*   **Özelliği:** `BreedManager` ile konuşarak ineğin ırkına göre (Holstein, Angus) farklı renklerde görünmesini sağlar.
*   **Dinamik:** Bebek inekleri küçük çizer (`scale` işlemi).

### **`CelepRenderer.java`**
*   **Görevi:** Celep (Tüccar) NPC'sinin görünümünü ayarlar.

### **`WorkerRenderer.java`**
*   **Görevi:** Çiftlik işçisinin görünümünü ayarlar.
