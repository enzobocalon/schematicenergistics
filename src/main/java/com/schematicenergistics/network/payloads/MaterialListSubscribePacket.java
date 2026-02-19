package com.schematicenergistics.network.payloads;

import com.schematicenergistics.SchematicEnergistics;
import com.schematicenergistics.menu.CannonInterfaceMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MaterialListSubscribePacket(boolean subscribed) implements CustomPacketPayload {
    public static final Type<MaterialListSubscribePacket> TYPE = new Type<>(
            SchematicEnergistics.makeId("material_list_subscribe"));

    public static final StreamCodec<ByteBuf, MaterialListSubscribePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, MaterialListSubscribePacket::subscribed,
            MaterialListSubscribePacket::new);

    public static void handle(MaterialListSubscribePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player
                    && player.containerMenu instanceof CannonInterfaceMenu menu) {
                menu.setMaterialsSubscribed(packet.subscribed());
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
