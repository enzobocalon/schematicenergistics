package com.schematicenergistics.network.payloads;

import com.schematicenergistics.SchematicEnergistics;
import io.netty.buffer.ByteBuf;
import com.schematicenergistics.menu.CannonInterfaceMenu;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// Client -> Server
public record CannonStatePacket(String state) implements CustomPacketPayload {
    public static final Type<CannonStatePacket> TYPE = new Type<>(SchematicEnergistics.makeId("cannon_state_sync"));

    public static final StreamCodec<ByteBuf, CannonStatePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, CannonStatePacket::state,
            CannonStatePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CannonStatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().containerMenu instanceof CannonInterfaceMenu menu))
                return;

            menu.updateStateFromInterface(packet.state());
        });
    }
}
