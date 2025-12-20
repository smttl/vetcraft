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

public class DiseaseManager {
    private static List<DiseaseData> loadedDiseases = new ArrayList<>();
    private static final Random random = new Random();

    public static void loadDiseases() {
        try {
            Gson gson = new Gson();
            // Dosya yolunu güncelledik (Tür spesifik)
            InputStream stream = DiseaseManager.class.getResourceAsStream("/assets/vetsim/diseases/cattle.json");

            if (stream == null) {
                VetCraft.LOGGER.error("Sığır hastalık dosyası (cattle.json) bulunamadı!");
                return;
            }

            Reader reader = new InputStreamReader(stream);
            Type listType = new TypeToken<ArrayList<DiseaseData>>(){}.getType();

            loadedDiseases.clear();
            List<DiseaseData> cattleDiseases = gson.fromJson(reader, listType);
            loadedDiseases.addAll(cattleDiseases);

            VetCraft.LOGGER.info("Sığır Hastalıkları Yüklendi: " + loadedDiseases.size() + " adet.");
        } catch (Exception e) {
            VetCraft.LOGGER.error("Hastalık yükleme hatası: " + e.getMessage());
        }
    }

    public static DiseaseData checkForDisease(int currentHunger) {
        for (DiseaseData disease : loadedDiseases) {
            if (currentHunger < disease.triggerHungerBelow) {
                if (random.nextDouble() < disease.triggerChance) {
                    return disease;
                }
            }
        }
        return null;
    }

    public static DiseaseData getDiseaseById(String id) {
        for (DiseaseData d : loadedDiseases) {
            if (d.id.equals(id)) return d;
        }
        return null;
    }
}