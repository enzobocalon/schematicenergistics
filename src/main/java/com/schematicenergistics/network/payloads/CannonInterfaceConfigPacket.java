package com.schematicenergistics.network.payloads;

import com.schematicenergistics.menu.CannonInterfaceMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CannonInterfaceConfigPacket {

    private final boolean value;
    private final String configType;

    public CannonInterfaceConfigPacket(boolean value, String configType) {
        this.value = value;
        this.configType = configType;
    }

    // Decoder
    public static CannonInterfaceConfigPacket fromBytes(FriendlyByteBuf buf) {
        boolean value = buf.readBoolean();
        String configType = buf.readUtf();
        return new CannonInterfaceConfigPacket(value, configType);
    }

    // Encoder
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(value);
        buf.writeUtf(configType);
    }

    // Handle
    public static void handle(CannonInterfaceConfigPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.containerMenu instanceof CannonInterfaceMenu menu) {
                menu.receiveStates(packet.configType, packet.value);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
