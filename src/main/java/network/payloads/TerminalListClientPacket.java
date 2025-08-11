package network.payloads;

import com.schematicenergistics.SchematicEnergistics;
import io.netty.buffer.ByteBuf;
import lib.CannonInterfaceClientState;
import lib.TerminalListData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import screen.CannonInterfaceScreen;
import screen.CannonInterfaceTerminalScreen;

import java.util.List;

// Server -> Client
public record TerminalListClientPacket(List<TerminalListData> data, BlockPos terminalPos) implements CustomPacketPayload {
    public static final Type<TerminalListClientPacket> TYPE = new Type<>(SchematicEnergistics.makeId("terminal_list"));

    public static final StreamCodec<ByteBuf, TerminalListClientPacket> STREAM_CODEC = StreamCodec.composite(
            TerminalListData.STREAM_CODEC.apply(ByteBufCodecs.list()), TerminalListClientPacket::data,
            BlockPos.STREAM_CODEC, TerminalListClientPacket::terminalPos,
            TerminalListClientPacket::new
    );

    public static void handle(TerminalListClientPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var minecraft = Minecraft.getInstance();
            if (minecraft.screen instanceof CannonInterfaceTerminalScreen screen) {
                screen.receiveData(packet.data(), packet.terminalPos());
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
