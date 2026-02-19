package com.schematicenergistics.network.payloads;

import com.schematicenergistics.SchematicEnergistics;
import com.schematicenergistics.lib.MaterialListEntry;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.schematicenergistics.screen.CannonInterfaceScreen;

import java.util.List;

public record MaterialListClientPacket(int page, int totalPages, List<MaterialListEntry> entries)
        implements CustomPacketPayload {
    public static final Type<MaterialListClientPacket> TYPE = new Type<>(SchematicEnergistics.makeId("material_list"));

    public static final StreamCodec<ByteBuf, MaterialListClientPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MaterialListClientPacket::page,
            ByteBufCodecs.VAR_INT, MaterialListClientPacket::totalPages,
            MaterialListEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), MaterialListClientPacket::entries,
            MaterialListClientPacket::new);

    public static void handle(MaterialListClientPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var minecraft = Minecraft.getInstance();
            if (minecraft.screen instanceof CannonInterfaceScreen screen) {
                screen.receiveMaterialsData(packet.page(), packet.totalPages(), packet.entries());
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
