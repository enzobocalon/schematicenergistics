package com.schematicenergistics.network.payloads;

import com.schematicenergistics.menu.CannonInterfaceMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CannonStatePacket {

    private final String state;

    public CannonStatePacket(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public static CannonStatePacket fromBytes(FriendlyByteBuf buf) {
        return new CannonStatePacket(buf.readUtf());
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(state);
    }

    public static void handle(CannonStatePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getSender() != null && context.getSender().containerMenu instanceof CannonInterfaceMenu menu) {
                menu.updateStateFromInterface(packet.getState());
            }
        });
        context.setPacketHandled(true);
    }
}
