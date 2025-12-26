package com.vetsim.vetcraft.network;

import com.vetsim.vetcraft.VetCraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

@Mod.EventBusSubscriber(modid = VetCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class VetNetwork {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlerEvent event) {
        final IPayloadRegistrar registrar = event.registrar(VetCraft.MOD_ID)
                .versioned("1.0");

        registrar.play(PhonePacket.ID, PhonePacket::new, handler -> handler.server(PhonePacket::handle));
    }
}