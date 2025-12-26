package com.vetsim.vetcraft.util;

public class DiseaseData {
    public String id;
    public String displayName;
    public int triggerHungerBelow;
    public double triggerChance;
    public double weightLossPerTick;
    public double damagePerTick;
    public boolean contagious;

    // Görsel ve Hareket
    public String visualEffect; // "SNEEZE", "SMOKE"
    public boolean slowness;

    // Tedavi
    public String cureType;   // "ITEM" veya "FEED"
    public String cureTarget; // Eşya ID'si

    // Tanı Bilgileri
    public Symptoms symptoms;

    public static class Symptoms {
        public String anamnesis;
        public String stethoscope;
        public double temperature;
        public String visual;
    }
}