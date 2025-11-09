package com.schematicenergistics.network.payloads;

import appeng.api.parts.IPartHost;
import appeng.menu.locator.MenuLocators;
import com.schematicenergistics.SchematicEnergistics;
import io.netty.buffer.ByteBuf;
import com.schematicenergistics.logic.ICannonInterfaceHost;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.schematicenergistics.part.CannonInterfacePart;

// Client -> Server
public record OpenCannonInterfacePacket(BlockPos cannonBlockPos, BlockPos terminalBlockPos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenCannonInterfacePacket> TYPE =
            new CustomPacketPayload.Type<>(SchematicEnergistics.makeId("open_cannon_interface"));

    public static final StreamCodec<ByteBuf, OpenCannonInterfacePacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, OpenCannonInterfacePacket::cannonBlockPos,
            BlockPos.STREAM_CODEC, OpenCannonInterfacePacket::terminalBlockPos,
            OpenCannonInterfacePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenCannonInterfacePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                BlockEntity blockEntity = player.level().getBlockEntity(packet.cannonBlockPos());

                if (blockEntity instanceof ICannonInterfaceHost host) {
                    host.getLogic().setTerminalPos(packet.terminalBlockPos());
                    var locator = MenuLocators.forBlockEntity(blockEntity);
                    host.openMenu(player, locator);
                    return;
                }

                if (blockEntity instanceof IPartHost host) {
                    for (var direction: Direction.values()) {
                        var part = host.getPart(direction);
                        if (part instanceof CannonInterfacePart cannonPart) {
                            cannonPart.getLogic().setTerminalPos(packet.terminalBlockPos());
                            var locator = MenuLocators.forPart(cannonPart);
                            cannonPart.openMenu(player, locator);
                            return;
                        }
                    }
                }
            }
        });
    }
}
