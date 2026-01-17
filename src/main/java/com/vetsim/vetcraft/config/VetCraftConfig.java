package com.vetsim.vetcraft.config;

public class VetCraftConfig {

    // --- GENEL ---
    public static final int TICKS_PER_DAY = 24000;

    // --- REPRODÜKSİYON (ÜREME) ---
    public static final int GESTATION_PERIOD = 2400; // Gebelik süresi (Tick)
    public static final int POST_BIRTH_COOLDOWN = 1200; // Lohusalık süresi (Tick)
    public static final int ESTRUS_CYCLE_LENGTH = 4800; // Kızgınlık döngüsü toplam süresi (4 Dakika)
    public static final int ESTRUS_WINDOW_START = 1800; // Kızgınlığın başladığı tick
    public static final int ESTRUS_WINDOW_END = 3000; // Kızgınlığın bittiği tick (Süre 1200 tick = 1 Dakika)
    public static final int DRY_PERIOD_THRESHOLD = 600; // Doğuma kaç tick kala kuru dönem başlar
    public static final int ABORTION_RECOVERY_TIME = 24000; // Düşük sonrası iyileşme (1 Gün)
    public static final int HORMONE_INDUCED_ESTRUS_TIME = 1850; // İğne ile tetiklenince döngünün ayarlandığı yer

    // --- METABOLİZMA ---
    public static final int MANURE_FREQUENCY = 1200; // Dışkılama sıklığı
    public static final int MILK_PRODUCTION_FREQUENCY = 2400; // Süt üretim döngüsü (Kova dolma süresi değil, iç sayaç)
    public static final int LACTATION_DURATION_DAYS = 10; // Süt verme süresi (Oyun Günü)
    public static final int LACTATION_DURATION_TICKS = LACTATION_DURATION_DAYS * TICKS_PER_DAY;
    public static final int MILKING_WARNING_TICKS = 12000; // Yarım gün sağılmazsa bağırmaya başlar
    public static final int MASTITIS_RISK_TICKS = 24000; // 1 gün sağılmazsa mastit riski başlar

    // --- SAĞLIK ---
    public static final int DISEASE_RECOVERY_TIME = 24000; // Tedavi sonrası nekahet süresi
    public static final int WITHDRAWAL_TIME_DEFAULT = 24000; // İlaç kalıntı süresi (Varsayılan)

    // --- BÜYÜME ---
    public static final int BABY_GROWTH_DAYS = 1; // Bebeklerin büyüme süresi
}
