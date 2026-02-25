package com.schematicenergistics.network.payloads;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record MaterialListSubscribePacket(boolean subscribed) {

    public static MaterialListSubscribePacket fromNetwork(FriendlyByteBuf buf) {
        return new MaterialListSubscribePacket(buf.readBoolean());
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeBoolean(subscribed);
    }

    public static void handle(MaterialListSubscribePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // No-op: MaterialsMenu auto-subscribes. Kept for network protocol compatibility.
        });
        context.setPacketHandled(true);
    }
}