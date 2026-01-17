package com.vetsim.vetcraft.gui;

import com.vetsim.vetcraft.init.ModMenuTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class BloodAnalysisMenu extends AbstractContainerMenu {
    public BloodAnalysisMenu(int containerId, Inventory inv, net.minecraft.network.FriendlyByteBuf extraData) {
        this(containerId, inv);
    }

    public BloodAnalysisMenu(int containerId, Inventory inv) {
        super(ModMenuTypes.BLOOD_ANALYSIS_MENU.get(), containerId);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
