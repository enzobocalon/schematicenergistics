package network.payloads;

import com.schematicenergistics.SchematicEnergistics;
import io.netty.buffer.ByteBuf;
import menu.CannonInterfaceMenu;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CannonInterfaceConfigPacket(boolean value, String configType) implements CustomPacketPayload {
    public static final Type<CannonInterfaceConfigPacket> TYPE = new Type<>(SchematicEnergistics.makeId("cannon_config"));

    public static final StreamCodec<ByteBuf, CannonInterfaceConfigPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, CannonInterfaceConfigPacket::value,
            ByteBufCodecs.STRING_UTF8, CannonInterfaceConfigPacket::configType,
            CannonInterfaceConfigPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CannonInterfaceConfigPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().containerMenu instanceof CannonInterfaceMenu menu))
                return;

            menu.receiveStates(packet.configType(), packet.value());
        });
    }
}
