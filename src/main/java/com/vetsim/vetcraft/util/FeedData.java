package com.vetsim.vetcraft.util;

public class FeedData {
    public String itemId;    // Eşya ID'si (minecraft:wheat)
    public int nutrition;    // Doyuruculuk
    public boolean isDryFood; // Kuru yem mi? (Asidoz için)

    public FeedData(String itemId, int nutrition, boolean isDryFood) {
        this.itemId = itemId;
        this.nutrition = nutrition;
        this.isDryFood = isDryFood;
    }
}