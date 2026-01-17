package com.vetsim.vetcraft.client;

import com.vetsim.vetcraft.gui.VetDiagnosisScreen;
import com.vetsim.vetcraft.network.VetDiagnosisPacket;
import net.minecraft.client.Minecraft;

public class ClientPacketHandler {
    public static void openVetDiagnosisScreen(VetDiagnosisPacket packet) {
        Minecraft.getInstance().setScreen(new VetDiagnosisScreen(packet));
    }
}
