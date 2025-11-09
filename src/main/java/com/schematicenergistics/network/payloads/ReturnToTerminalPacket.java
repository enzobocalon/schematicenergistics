package com.schematicenergistics.network.payloads;

import appeng.api.parts.IPartHost;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import com.schematicenergistics.SchematicEnergistics;
import com.schematicenergistics.core.Registration;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.schematicenergistics.part.CannonInterfaceTerminal;

public record ReturnToTerminalPacket(BlockPos terminalPos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ReturnToTerminalPacket> TYPE =
            new CustomPacketPayload.Type<>(SchematicEnergistics.makeId("return_to_terminal"));


    public static final StreamCodec<ByteBuf, ReturnToTerminalPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ReturnToTerminalPacket::terminalPos,
            ReturnToTerminalPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ReturnToTerminalPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                BlockEntity blockEntity = player.level().getBlockEntity(packet.terminalPos());

                if (blockEntity instanceof IPartHost host) {
                    for (var direction : Direction.values()) {
                        var part = host.getPart(direction);
                        if (part instanceof CannonInterfaceTerminal terminal) {
                            var menuLocators = MenuLocators.forPart(terminal);
                            MenuOpener.open(Registration.CANNON_INTERFACE_TERMINAL_MENU.get(), player, menuLocators);
                            return;
                        }
                    }
                }
            }
        });
    }
}
