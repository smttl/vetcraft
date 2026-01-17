package com.vetsim.vetcraft.util;

public class BreedData {
    public String id;
    public String displayName;
    public float milkMultiplier;
    public float diseaseResistance; // 0.0 (Hassas) - 1.0 (Dirençli)
    public String heatTolerance; // "COLD", "HEAT", "NEUTRAL"
    public float minBcs; // İdeal alt sınır
    public float maxBcs; // İdeal üst sınır
    public String texturePath; // Örnek: "textures/entity/cattle/holstein.png"
    public float scale = 1.0f; // Visual Scale (Default: 1.0)
}
