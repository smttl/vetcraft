package com.vetsim.vetcraft.init;

import com.vetsim.vetcraft.VetCraft;
import com.vetsim.vetcraft.gui.PhoneMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, VetCraft.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<PhoneMenu>> PHONE_MENU = MENUS.register("phone_menu",
            () -> IMenuTypeExtension.create(PhoneMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<com.vetsim.vetcraft.gui.BloodAnalysisMenu>> BLOOD_ANALYSIS_MENU = MENUS
            .register("blood_analysis_menu",
                    () -> IMenuTypeExtension.create(com.vetsim.vetcraft.gui.BloodAnalysisMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}