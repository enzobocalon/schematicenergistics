package com.schematicenergistics.network.payloads;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import com.schematicenergistics.screen.CannonInterfaceScreen;

import java.util.function.Supplier;

public class CannonInterfaceSyncPacket {

    private final CompoundTag data;
    private final String schematicName;
    private final String statusMsg;
    private final String state;
    private final boolean hasTerminal;
    private final BlockPos terminalPos;

    public CannonInterfaceSyncPacket(CompoundTag data, String schematicName, String statusMsg, String state) {
        this(data, schematicName, statusMsg, state, false, BlockPos.ZERO);
    }

    public CannonInterfaceSyncPacket(CompoundTag data, String schematicName, String statusMsg, String state, BlockPos terminalPos) {
        this(data, schematicName, statusMsg, state, true, terminalPos);
    }

    public CannonInterfaceSyncPacket(CompoundTag data, String schematicName, String statusMsg, String state, boolean hasTerminal, BlockPos terminalPos) {
        this.data = data;
        this.schematicName = schematicName;
        this.statusMsg = statusMsg;
        this.state = state;
        this.hasTerminal = hasTerminal;
        this.terminalPos = terminalPos;
    }

    public static CannonInterfaceSyncPacket fromBytes(FriendlyByteBuf buf) {
        CompoundTag data = buf.readNbt();
        String schematicName = buf.readUtf();
        String statusMsg = buf.readUtf();
        String state = buf.readUtf();
        boolean hasTerminal = buf.readBoolean();
        BlockPos terminalPos = hasTerminal ? buf.readBlockPos() : BlockPos.ZERO;
        return new CannonInterfaceSyncPacket(data, schematicName, statusMsg, state, hasTerminal, terminalPos);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeNbt(data);
        buf.writeUtf(schematicName);
        buf.writeUtf(statusMsg);
        buf.writeUtf(state);
        buf.writeBoolean(hasTerminal);
        if (hasTerminal) {
            buf.writeBlockPos(terminalPos);
        }
    }

    public BlockPos getTerminalPosOrNull() {
        return hasTerminal ? terminalPos : null;
    }

    public static void handle(CannonInterfaceSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleClient(packet));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(CannonInterfaceSyncPacket packet) {
        if (Minecraft.getInstance().screen instanceof CannonInterfaceScreen screen) {
            screen.updateScreenItem(packet.data, packet.schematicName, packet.statusMsg, packet.state, packet.getTerminalPosOrNull());
        }
    }
}
