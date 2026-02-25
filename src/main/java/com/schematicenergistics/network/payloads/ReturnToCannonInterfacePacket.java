package com.schematicenergistics.network.payloads;

import appeng.api.parts.IPartHost;
import appeng.menu.locator.MenuLocators;
import com.schematicenergistics.SchematicEnergistics;
import com.schematicenergistics.logic.ICannonInterfaceHost;
import com.schematicenergistics.part.CannonInterfacePart;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// Client -> Server: returns from MaterialsScreen back to CannonInterfaceScreen
public record ReturnToCannonInterfacePacket(BlockPos cannonBlockPos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ReturnToCannonInterfacePacket> TYPE =
            new CustomPacketPayload.Type<>(SchematicEnergistics.makeId("return_to_cannon_interface"));

    public static final StreamCodec<ByteBuf, ReturnToCannonInterfacePacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ReturnToCannonInterfacePacket::cannonBlockPos,
            ReturnToCannonInterfacePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ReturnToCannonInterfacePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                BlockEntity blockEntity = player.level().getBlockEntity(packet.cannonBlockPos());

                if (blockEntity instanceof ICannonInterfaceHost host) {
                    var locator = MenuLocators.forBlockEntity(blockEntity);
                    host.openMenu(player, locator);
                    return;
                }

                if (blockEntity instanceof IPartHost host) {
                    for (var direction : Direction.values()) {
                        var part = host.getPart(direction);
                        if (part instanceof CannonInterfacePart cannonPart) {
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
