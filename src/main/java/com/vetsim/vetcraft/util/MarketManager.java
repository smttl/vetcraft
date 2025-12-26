package com.vetsim.vetcraft.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vetsim.vetcraft.VetCraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarketManager {
    // Alınabilecek ürünler (Market)
    public static final List<MarketItem> BUY_LIST = new ArrayList<>();
    // Satılabilecek ürünler (Bizim ürünler)
    public static final Map<String, Integer> SELL_MAP = new HashMap<>();

    public static class MarketItem {
        public String itemId;
        public int price;
        public String name; // Ekranda görünecek isim (Opsiyonel)
    }

    public static void loadMarkets() {
        BUY_LIST.clear();
        SELL_MAP.clear();
        Gson gson = new Gson();

        try {
            // 1. ALIM LİSTESİ (Tedarik)
            InputStream streamBuy = MarketManager.class.getResourceAsStream("/assets/vetsim/data/shop_buy.json");
            if (streamBuy != null) {
                Reader reader = new InputStreamReader(streamBuy);
                Type listType = new TypeToken<ArrayList<MarketItem>>(){}.getType();
                List<MarketItem> list = gson.fromJson(reader, listType);
                BUY_LIST.addAll(list);
            }

            // 2. SATIM LİSTESİ (Ürünler)
            InputStream streamSell = MarketManager.class.getResourceAsStream("/assets/vetsim/data/shop_sell.json");
            if (streamSell != null) {
                Reader reader = new InputStreamReader(streamSell);
                Type listType = new TypeToken<ArrayList<MarketItem>>(){}.getType();
                List<MarketItem> list = gson.fromJson(reader, listType);
                for (MarketItem item : list) {
                    SELL_MAP.put(item.itemId, item.price);
                }
            }

            VetCraft.LOGGER.info("Market verileri yüklendi. Alınabilir: " + BUY_LIST.size() + ", Satılabilir: " + SELL_MAP.size());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getSellPrice(ItemStack stack) {
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return SELL_MAP.getOrDefault(id, 0);
    }
}
