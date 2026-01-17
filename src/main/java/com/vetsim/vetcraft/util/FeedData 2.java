package com.vetsim.vetcraft.util;

public class FeedData {
    // Değişkenler (Public yaptık ki getter/setter ile uğraşmayalım)
    public String id;         // Yemin kodu (minecraft:wheat)
    public String name;       // Yemin ekranda görünen adı (Buğday)
    public int nutrition;     // Doyuruculuk (Mideyi ne kadar doldurur)
    public boolean isDry;     // Kuru madde mi? (pH dengesi için)
    public float protein;     // Protein oranı (Süt verimi ve büyüme hızı için)

    // --- 1. DETAYLI CONSTRUCTOR (5 Parametreli) ---
    // AI kodunda (Ot yeme, Yemlikten yeme) bunu kullanıyoruz.
    // Örn: new FeedData("grass", "Çayır Otu", 15, true, 0.0f);
    public FeedData(String id, String name, int nutrition, boolean isDry, float protein) {
        this.id = id;
        this.name = name;
        this.nutrition = nutrition;
        this.isDry = isDry;
        this.protein = protein;
    }

    // --- 2. BASİT CONSTRUCTOR (3 Parametreli - UYUMLULUK İÇİN) ---
    // FeedManager dosyasında eski kodların hata vermemesi için bunu tutuyoruz.
    // Eğer isim ve protein verilmezse, varsayılan değerler atıyoruz.
    public FeedData(String id, int nutrition, boolean isDry) {
        this(id, "Bilinmeyen Yem", nutrition, isDry, 0.0f);
    }
}