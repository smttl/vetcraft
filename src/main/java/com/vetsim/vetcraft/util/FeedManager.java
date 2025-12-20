package com.vetsim.vetcraft.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedManager {

    // Eşya ID'sine göre Yem Verisini saklayan harita
    private static final Map<String, FeedData> FEED_MAP = new HashMap<>();

    // Verileri Yükle (Manuel veya otomatik çağrılabilir)
    public static void loadFeeds() {
        FEED_MAP.clear();

        // Şimdilik test için hardcoded yüklüyoruz.
        // İleride burayı gerçek dosya okuma sistemiyle değiştirebiliriz.
        // Ama simülasyon mantığı için bu yöntem en güvenlisidir.

        registerFeed("minecraft:wheat", 10, false);
        registerFeed("minecraft:hay_block", 50, true);
        registerFeed("minecraft:apple", 5, false);

        System.out.println("VetCraft: Yem veritabanı yüklendi.");
    }

    private static void registerFeed(String id, int nutrition, boolean isDry) {
        FEED_MAP.put(id, new FeedData(id, nutrition, isDry));
    }

    // Bu eşya bir yem mi?
    public static FeedData getFeedData(ItemStack stack) {
        // Eşyanın ID'sini al (minecraft:wheat gibi)
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return FEED_MAP.get(itemId);
    }
}