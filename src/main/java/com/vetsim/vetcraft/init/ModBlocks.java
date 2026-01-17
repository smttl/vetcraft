
package com.vetsim.vetcraft.init; // Klasörüne göre burayı düzelt

import com.vetsim.vetcraft.VetCraft; // Ana mod dosyanın yeri
import com.vetsim.vetcraft.block.FeedTroughBlock;
import com.vetsim.vetcraft.block.SaltLickBlock; // YENİ
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModBlocks {
    // BLOK KAYIT LİSTESİ
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister
            .create(net.minecraft.core.registries.Registries.BLOCK, VetCraft.MOD_ID);

    // Yemlik Bloğu Tanımı (Odun gibi sertlik, ses vb.)
    public static final DeferredHolder<Block, Block> FEED_TROUGH = BLOCKS.register("feed_trough",
            () -> new FeedTroughBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).noOcclusion()));

    // Tuz Yalama Taşı (Salt Lick)
    public static final DeferredHolder<Block, Block> SALT_LICK = BLOCKS.register("salt_lick",
            () -> new SaltLickBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).strength(1.0f).noOcclusion()));

    // Kayıt fonksiyonu
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}