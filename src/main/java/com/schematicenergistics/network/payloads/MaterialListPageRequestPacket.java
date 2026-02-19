package com.schematicenergistics.network.payloads;

import com.schematicenergistics.SchematicEnergistics;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MaterialListPageRequestPacket(int page) implements CustomPacketPayload {
    public static final Type<MaterialListPageRequestPacket> TYPE = new Type<>(
            SchematicEnergistics.makeId("material_list_page"));

    public static final StreamCodec<ByteBuf, MaterialListPageRequestPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MaterialListPageRequestPacket::page,
            MaterialListPageRequestPacket::new);

    public static void handle(MaterialListPageRequestPacket packet, IPayloadContext context) {
        // 分页已废弃，服务端直接推全量数据，此包不再处理
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}