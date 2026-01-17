package com.vetsim.vetcraft.util;

import java.util.List;

public class DiseaseData {
    public String id;
    public String displayName;
    public int triggerHungerBelow;
    public double triggerChance;
    public double weightLossPerTick;
    public double damagePerTick;
    public boolean contagious;
    public double abortChance;
    public double bcsLoss; // Her tickte ne kadar kondisyon düşecek
    public boolean stopMilk; // Süt vermeyi engeller mi?// <--- YENİ: Düşük yapma ihtimali (0.0 ile 1.0
                             // arası)
    public float contagionRate; // Bulaşma ihtimali (0.0 - 1.0)
    public float waterLoss; // Ekstra su kaybı (Dehidrasyon hızı)
    public boolean isFatal; // Öldürücü mü?
    public String targetAge = "ANY"; // "ANY", "BABY", "ADULT"

    // Rastgele Oluşum (Phase 16)
    public float randomOccurrenceChance; // 0.0 - 1.0 (Her 200 tickte bir şans)

    // Görsel ve Hareket
    public String visualEffect; // "SNEEZE", "SMOKE"
    public boolean slowness;

    // Tedavi
    public String cureType; // "ITEM" veya "FEED"
    public List<String> cureTarget; // Eşya ID'si
    public List<RiskyItem> risky_items;

    public static class RiskyItem {
        public String item; // Örn: "minecraft:rotten_flesh"
        public float chance; // Örn: 0.3
    }

    // Vektör Taşıyıcılar (Phase 15 Refactor)
    public List<VectorData> vectors;

    public static class VectorData {
        public String entity; // Örn: "minecraft:sheep"
        public float chance; // Örn: 0.01
        public int radius; // Örn: 5
    }

    // Tanı Bilgileri
    public Symptoms symptoms;

    public static class Symptoms {
        public String anamnesis;
        public String stethoscope;
        public double temperature;
        public String visual;
    }
}