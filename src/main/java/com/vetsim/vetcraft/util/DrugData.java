package com.vetsim.vetcraft.util;

public class DrugData {
    public String itemId;       // Eşya ID'si
    public String category;     // "CURE" (Tedavi) veya "FUNCTIONAL" (Hormon/İşlevsel)
    public String actionTag;    // Özel etki etiketi (ABORT, ESTRUS, STRESS_DOWN)

    // Farmakolojik Etkiler (Yan Etkiler)
    public int stressImpact;    // Stres değişimi (+ artırır, - azaltır)
    public float bcsImpact;     // Kondisyon değişimi (- zayıflatır, + kilo aldırır)
    public float toxicity;      // Karaciğer/Böbrek yorgunluğu (İleride kullanılabilir)

    public DrugData(String itemId, String category, String actionTag, int stressImpact, float bcsImpact) {
        this.itemId = itemId;
        this.category = category;
        this.actionTag = actionTag;
        this.stressImpact = stressImpact;
        this.bcsImpact = bcsImpact;
        this.toxicity = 0.0f;
    }
}