package com.vetsim.vetcraft.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vetsim.vetcraft.VetCraft;

// --- YENİ EKLENEN İMPORTLAR ---
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
// ------------------------------

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DiseaseManager {
    private static List<DiseaseData> loadedDiseases = new ArrayList<>();

    // Genel kullanım için Random (Açlık kontrolü vb. için)
    private static final Random internalRandom = new Random();

    public static void loadDiseases() {
        try {
            Gson gson = new Gson();
            // Dosya yolunu güncelledik (Mod ID: vetcraft)
            InputStream stream = DiseaseManager.class.getResourceAsStream("/assets/vetcraft/diseases/cattle.json");

            if (stream == null) {
                VetCraft.LOGGER.error("Sığır hastalık dosyası (cattle.json) bulunamadı!");
                return;
            }

            Reader reader = new InputStreamReader(stream);
            Type listType = new TypeToken<ArrayList<DiseaseData>>() {
            }.getType();

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
                if (internalRandom.nextDouble() < disease.triggerChance) {
                    return disease;
                }
            }
        }
        return null;
    }

    public static DiseaseData getDiseaseById(String id) {
        for (DiseaseData d : loadedDiseases) {
            if (d.id.equals(id))
                return d;
        }
        return null;
    }

    public static String getDiseaseName(String id) {
        if ("NONE".equals(id))
            return "Temiz";
        DiseaseData data = getDiseaseById(id);
        if (data != null) {
            return data.displayName;
        }

        // Debugging
        StringBuilder sb = new StringBuilder();
        for (DiseaseData d : loadedDiseases) {
            sb.append(d.id).append(", ");
        }
        VetCraft.LOGGER.error("Hastalık bulunamadı: " + id + ". Yüklü olanlar: " + sb.toString());

        return "Bilinmiyor (" + id + ")";
    }

    // --- YENİ EKLENEN METOT: YEMEK RİSK HESAPLAMA ---
    public static String calculateRisk(ItemStack stack, RandomSource random) {
        if (stack.isEmpty())
            return null;

        // Yenen eşyanın string ID'sini al (örn: "minecraft:rotten_flesh")
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

        for (DiseaseData disease : loadedDiseases) {
            // Eğer hastalığın 'risky_items' listesi varsa kontrol et
            if (disease.risky_items != null) {
                for (DiseaseData.RiskyItem risk : disease.risky_items) {

                    // Eşya ID'si eşleşiyor mu?
                    if (risk.item.equals(itemId)) {
                        // Şans faktörü (0.3 > 0.1 vb.)
                        // Entity'nin kendi RandomSource'unu kullanıyoruz ki simülasyon tutarlı olsun
                        if (random.nextFloat() < risk.chance) {
                            return disease.id; // Hastalık ID'sini döndür (örn: "acidosis")
                        }
                    }
                }
            }
        }
        return null; // Risk yok
    }

    public static List<DiseaseData> getDiseases() {
        return loadedDiseases;
    }
}