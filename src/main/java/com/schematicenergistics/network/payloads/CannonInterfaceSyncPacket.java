package com.schematicenergistics.network.payloads;

import com.schematicenergistics.SchematicEnergistics;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.schematicenergistics.screen.CannonInterfaceScreen;

public record CannonInterfaceSyncPacket(CompoundTag data, String schematicName, String statusMsg, String state, boolean hasTerminal, BlockPos terminalPos) implements CustomPacketPayload {
    public static final Type<CannonInterfaceSyncPacket> TYPE = new Type<>(SchematicEnergistics.makeId("cannon_sync"));

    public static final StreamCodec<ByteBuf, CannonInterfaceSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, CannonInterfaceSyncPacket::data,
            ByteBufCodecs.STRING_UTF8, CannonInterfaceSyncPacket::schematicName,
            ByteBufCodecs.STRING_UTF8, CannonInterfaceSyncPacket::statusMsg,
            ByteBufCodecs.STRING_UTF8, CannonInterfaceSyncPacket::state,
            ByteBufCodecs.BOOL, CannonInterfaceSyncPacket::hasTerminal,
            BlockPos.STREAM_CODEC, CannonInterfaceSyncPacket::terminalPos,
            CannonInterfaceSyncPacket::new
    );

    public CannonInterfaceSyncPacket(CompoundTag data, String schematicName, String statusMsg, String state) {
        this(data, schematicName, statusMsg, state, false, BlockPos.ZERO);
    }

    public CannonInterfaceSyncPacket(CompoundTag data, String schematicName, String statusMsg, String state, BlockPos terminalPos) {
        this(data, schematicName, statusMsg, state, true, terminalPos);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public BlockPos getTerminalPosOrNull() {
        return hasTerminal ? terminalPos : null;
    }

    public static void handle(CannonInterfaceSyncPacket payload, IPayloadContext context) {
        if (Minecraft.getInstance().screen instanceof CannonInterfaceScreen screen) {
            screen.updateScreenItem(payload.data, payload.schematicName, payload.statusMsg, payload.state, payload.getTerminalPosOrNull());
        }
    }
}