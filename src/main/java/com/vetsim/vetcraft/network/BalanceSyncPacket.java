package com.vetsim.vetcraft.network;

import com.vetsim.vetcraft.VetCraft;
import com.vetsim.vetcraft.client.ClientBankData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record BalanceSyncPacket(double balance) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(VetCraft.MOD_ID, "balance_sync");

    public BalanceSyncPacket(FriendlyByteBuf buf) {
        this(buf.readDouble());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeDouble(balance);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(BalanceSyncPacket msg, PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            // Client side handling
            ClientBankData.setBalance(msg.balance);
        });
    }
}
