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

public class FeedManager {
    // Hafızadaki Yem Listesi
    private static final Map<String, FeedData> FEED_MAP = new HashMap<>();

    public static void loadFeeds() {
        FEED_MAP.clear();
        try {
            // Dosya yolunu kontrol et
            String path = "/assets/vetcraft/data/feeds.json";
            InputStream stream = FeedManager.class.getResourceAsStream(path);

            if (stream == null) {
                System.out.println("VetCraft: Yem dosyası bulunamadı! (" + path + ")");
                // Dosya yoksa bile kod çökmesin diye varsayılanları elle ekle
                loadDefaults();
                return;
            }

            Gson gson = new Gson();
            JsonObject json = gson.fromJson(new InputStreamReader(stream), JsonObject.class);
            JsonArray feeds = json.getAsJsonArray("feeds");

            for (JsonElement element : feeds) {
                JsonObject obj = element.getAsJsonObject();

                String id = obj.get("id").getAsString();
                String name = obj.has("name") ? obj.get("name").getAsString() : "Bilinmeyen Yem";
                int nutrition = obj.get("nutrition").getAsInt();
                boolean isDry = obj.get("is_dry").getAsBoolean();
                float protein = obj.has("protein") ? obj.get("protein").getAsFloat() : 0.0f;

                FEED_MAP.put(id, new FeedData(id, name, nutrition, isDry, protein));
            }
            System.out.println("VetCraft: " + FEED_MAP.size() + " çeşit yem yüklendi.");

        } catch (Exception e) {
            e.printStackTrace();
            loadDefaults();
        }
    }

    // JSON okuyamazsa oyun çökmesin diye acil durum yemleri
    private static void loadDefaults() {
        FEED_MAP.put("minecraft:wheat", new FeedData("minecraft:wheat", "Buğday", 20, false, 0.12f));
        FEED_MAP.put("minecraft:hay_block", new FeedData("minecraft:hay_block", "Saman", 40, true, 0.08f));
    }

    // ItemStack (Eşya) verince Yem Verisini döndürür
    public static FeedData getFeedData(ItemStack stack) {
        if (stack.isEmpty())
            return null;

        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return FEED_MAP.get(itemId);
    }

    // String ID (Yazı) verince Yem Verisini döndürür (AI için gerekli)
    public static FeedData getFeedData(String itemId) {
        if (!itemId.contains(":"))
            itemId = "minecraft:" + itemId;
        return FEED_MAP.get(itemId);
    }
}