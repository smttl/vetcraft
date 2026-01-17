package com.vetsim.vetcraft.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class DrugManager {
    private static final Map<String, DrugData> DRUG_MAP = new HashMap<>();

    public static void loadDrugs() {
        DRUG_MAP.clear();
        try {
            // Tek bir dev dosya: drugs.json
            String path = "/assets/vetcraft/medicines/drugs.json";
            InputStream stream = DrugManager.class.getResourceAsStream(path);

            if (stream == null) {
                System.out.println("VetCraft: İlaç veritabanı bulunamadı!");
                return;
            }

            Gson gson = new Gson();
            JsonObject json = gson.fromJson(new InputStreamReader(stream), JsonObject.class);
            JsonArray drugs = json.getAsJsonArray("drugs");

            for (JsonElement element : drugs) {
                JsonObject obj = element.getAsJsonObject();

                String id = obj.get("id").getAsString();
                String cat = obj.get("category").getAsString();
                // Opsiyonel alanlar
                String action = obj.has("action") ? obj.get("action").getAsString() : "NONE";
                int stress = obj.has("stress_impact") ? obj.get("stress_impact").getAsInt() : 0;
                float bcs = obj.has("bcs_impact") ? obj.get("bcs_impact").getAsFloat() : 0.0f;
                // Yeni Özellikler (Phase 18)
                float toxicity = obj.has("toxicity") ? obj.get("toxicity").getAsFloat() : 0.0f;
                int withdrawal = obj.has("withdrawal_days") ? obj.get("withdrawal_days").getAsInt() : 0;

                DRUG_MAP.put(id, new DrugData(id, cat, action, stress, bcs, toxicity, withdrawal));
            }
            System.out.println("VetCraft: " + DRUG_MAP.size() + " farmakolojik ürün yüklendi.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static DrugData getDrug(ItemStack stack) {
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        // Mod ID düzeltmesi
        if (!itemId.contains(":") && !itemId.equals("minecraft:air")) {
            // Burayı kendi mod id'nize göre uyarlayın gerekirse
        }
        return DRUG_MAP.get(itemId);
    }
}