package com.vetsim.vetcraft.network;

import com.vetsim.vetcraft.VetCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record VetDiagnosisPacket(
        int entityId,
        String earTag,
        String age,
        String breed,
        String gender,
        String owner,
        float bcs,
        float rumenFill,
        float rumenPh,
        float hydration,
        int stress,
        String disease,
        String secondaryDisease,
        boolean isPregnant,
        float progesterone,
        int breedingCooldown,
        int estrusTimer,
        boolean isLactating,

        boolean isDryPeriod,
        float weight) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(VetCraft.MOD_ID, "vet_diagnosis");

    public VetDiagnosisPacket(FriendlyByteBuf buffer) {
        this(buffer.readInt(),
                buffer.readUtf(),
                buffer.readUtf(),
                buffer.readUtf(),
                buffer.readUtf(),
                buffer.readUtf(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readInt(),
                buffer.readUtf(),
                buffer.readUtf(),
                buffer.readBoolean(),
                buffer.readFloat(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readFloat());
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(entityId);
        buffer.writeUtf(earTag);
        buffer.writeUtf(age);
        buffer.writeUtf(breed);
        buffer.writeUtf(gender);
        buffer.writeUtf(owner);
        buffer.writeFloat(bcs);
        buffer.writeFloat(rumenFill);
        buffer.writeFloat(rumenPh);
        buffer.writeFloat(hydration);
        buffer.writeInt(stress);
        buffer.writeUtf(disease);
        buffer.writeUtf(secondaryDisease);
        buffer.writeBoolean(isPregnant);
        buffer.writeFloat(progesterone);
        buffer.writeInt(breedingCooldown);
        buffer.writeInt(estrusTimer);
        buffer.writeBoolean(isLactating);
        buffer.writeBoolean(isDryPeriod);
        buffer.writeFloat(weight);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(VetDiagnosisPacket payload, PlayPayloadContext context) {
        context.workHandler().submitAsync(() -> {
            // Client-side handling
            com.vetsim.vetcraft.client.ClientPacketHandler.openVetDiagnosisScreen(payload);
        });
    }
}
