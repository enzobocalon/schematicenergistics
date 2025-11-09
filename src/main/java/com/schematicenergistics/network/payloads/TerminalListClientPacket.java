package com.schematicenergistics.network.payloads;

import com.schematicenergistics.lib.TerminalListData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import com.schematicenergistics.screen.CannonInterfaceTerminalScreen;

import java.util.List;
import java.util.function.Supplier;

public class TerminalListClientPacket {

    private final List<TerminalListData> data;
    private final BlockPos terminalPos;

    public TerminalListClientPacket(List<TerminalListData> data, BlockPos terminalPos) {
        this.data = data;
        this.terminalPos = terminalPos;
    }

    public static TerminalListClientPacket decode(FriendlyByteBuf buf) {
        List<TerminalListData> data = buf.readList(TerminalListData::fromBuffer); // precisa implementar
        BlockPos pos = buf.readBlockPos();
        return new TerminalListClientPacket(data, pos);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeCollection(data, (b, e) -> e.toBuffer(b)); // precisa implementar
        buf.writeBlockPos(terminalPos);
    }

    public static void handle(TerminalListClientPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleClient(packet));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(TerminalListClientPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof CannonInterfaceTerminalScreen screen) {
            screen.receiveData(packet.data, packet.terminalPos);
        }
    }
}
