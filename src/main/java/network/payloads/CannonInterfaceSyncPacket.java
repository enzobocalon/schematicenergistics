package network.payloads;

import com.schematicenergistics.SchematicEnergistics;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import screen.CannonInterfaceScreen;

public record CannonInterfaceSyncPacket(CompoundTag data, String schematicName) implements CustomPacketPayload {
    public static final Type<CannonInterfaceSyncPacket> TYPE = new Type<>(SchematicEnergistics.makeId("cannon_sync"));

    public static final StreamCodec<ByteBuf, CannonInterfaceSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, CannonInterfaceSyncPacket::data,
            ByteBufCodecs.STRING_UTF8, CannonInterfaceSyncPacket::schematicName,
            CannonInterfaceSyncPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CannonInterfaceSyncPacket payload, IPayloadContext context) {
        if (Minecraft.getInstance().screen instanceof CannonInterfaceScreen screen) {
            screen.updateScreenItem(payload.data, payload.schematicName);
        }
    }
}
