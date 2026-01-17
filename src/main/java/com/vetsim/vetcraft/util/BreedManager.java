package com.vetsim.vetcraft.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vetsim.vetcraft.VetCraft;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BreedManager {
    private static List<BreedData> loadedBreeds = new ArrayList<>();
    private static final Random random = new Random();

    public static void loadBreeds() {
        try {
            Gson gson = new Gson();
            InputStream stream = BreedManager.class.getResourceAsStream("/assets/vetcraft/breeds/cattle.json");

            if (stream == null) {
                VetCraft.LOGGER.error("Sığır ırk dosyası (cattle.json) bulunamadı!");
                return;
            }

            Reader reader = new InputStreamReader(stream);
            Type listType = new TypeToken<ArrayList<BreedData>>() {
            }.getType();

            loadedBreeds.clear();
            List<BreedData> breeds = gson.fromJson(reader, listType);
            loadedBreeds.addAll(breeds);

            VetCraft.LOGGER.info("Sığır Irkları Yüklendi: " + loadedBreeds.size() + " adet.");
        } catch (Exception e) {
            VetCraft.LOGGER.error("Irk yükleme hatası: " + e.getMessage());
        }
    }

    public static BreedData getBreed(String id) {
        // İsim benzerliği kontrolü (küçük harf duyarlılığı için)
        for (BreedData b : loadedBreeds) {
            if (b.id.equalsIgnoreCase(id) || b.displayName.equalsIgnoreCase(id)) {
                return b;
            }
        }
        // Bulunamazsa varsayılan
        return loadedBreeds.isEmpty() ? null : loadedBreeds.get(0);
    }

    public static BreedData getRandomBreed() {
        if (loadedBreeds.isEmpty())
            return null;
        return loadedBreeds.get(random.nextInt(loadedBreeds.size()));
    }
}
