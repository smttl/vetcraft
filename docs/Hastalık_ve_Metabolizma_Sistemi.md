# 🐄 VetCraft Hastalık ve Metabolizma Sistemi (İç Tüzüğü)

Şu an kodlarda çalışan sistemin tam özeti şudur. 4 ana yoldan hastalık kaparlar ve metabolizmaları buna göre çöker:

## 1. Hastalık Tetikleyicileri (Neden Hasta Olurlar?)

### A) İhmal (Açlık & Susuzluk)
*   **Ketozis:** Hayvan açlıktan ölmek üzereyse (Rumen <%10) kan şekeri düşer, ağzı aseton kokar.
*   **Dehidrasyon:** Su vermezseniz (Hidrasyon <%10) doğrudan hasta olur.
*   **Pnömoni/Mastitis:** Yetersiz beslenen hayvanların bağışıklığı düşer ve bu hastalıklara yakalanma şansı artar.

### B) Yanlış Besleme (Metabolik Krizler)
*   **Asidoz:** Sadece Buğday/Mısır verirseniz mide pH'ı düşer (<5.8). Hayvan yemden kesilir.
*   **Bloat (Gazlı Şişkinlik):** Çürük et veya zehirli patates yerse veya çok şişerse gazı (Metan) atamaz. Patlama (ölüm) riski vardır.

### C) Vektör ve Çevre (Bulaşma)
*   **Şap (FMD):** Koyun veya Domuzlardan bulaşır. VEYA artık %0.1 ihtimalle havadan kapabilirler.
*   **Kirlilik:** Yerde çok gübre varsa (Manure) enfeksiyon riski artar.

### D) Genetik / Rastgele
*   Her hastalığın çok düşük de olsa "durduk yere" çıkma şansı vardır (Zayıf genetik simülasyonu).

## 2. Metabolizmanın Tepkisi (Semptomlar)
Hayvan hasta olduğunda vücudu şöyle tepki verir:

*   **Stres Artışı:** Nabız yükselir (Taşikardi). Stres > 60 olursa süt vermeyi keser ve gebe kalamaz.
*   **Kilo Kaybı (BCS):** Hasta hayvan her saniye zayıflar. (BCS 3.5 -> 2.0). Çok zayıflarsa iyileşmesi zorlaşır.
*   **Ateş:** Enfeksiyonlarda (Pnömoni, Mastitis) ateş 39.5°C üstüne çıkar. Metaboliklerde (Asidoz) genelde normaldir.
*   **Verim Kaybı:** Mastitis doğrudan sütü keser. Diğerleri azaltır.

## 3. Tedavi ve Bağışıklık (Çözüm)

*   **Buzağı Kalkanı:** Doğar doğmaz **Ağız Sütü (Colostrum)** içen buzağıların bağışıklığı %100 başlar ve hastalıklara dirençli olur. İçmezse "**Buzağı İshali**" (Scours) olur ve ölür.
*   **Protokol:** Her hastalığın ilacı farklıdır (JSON'da tanımlı).
    *   Enfeksiyon -> **Antibiyotik / Penisilin**
    *   Asidoz -> **Saman** (pH dengeler) veya **Sirke**
    *   Ketozis -> **Şeker / Dekstroz**
*   **İyileşme:** İlaç verince iyileşme (Nekahet) süreci başlar. Bu sürede hayvan tekrar hasta olmaz ama sütü "**İlaçlı**" olur (İçilmez).

> **Özetle:** "Temiz bak, doğru besle, aşısını yap." Sistemi bunun üzerine kurulu.
