package com.schematicenergistics.network.payloads;

import appeng.api.parts.IPartHost;
import appeng.menu.locator.MenuLocators;
import com.schematicenergistics.logic.ICannonInterfaceHost;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import com.schematicenergistics.part.CannonInterfacePart;

import java.util.function.Supplier;

public class OpenCannonInterfacePacket {

    private final BlockPos cannonBlockPos;
    private final BlockPos terminalBlockPos;

    public OpenCannonInterfacePacket(BlockPos cannonBlockPos, BlockPos terminalBlockPos) {
        this.cannonBlockPos = cannonBlockPos;
        this.terminalBlockPos = terminalBlockPos;
    }

    public static OpenCannonInterfacePacket decode(FriendlyByteBuf buf) {
        BlockPos cannonPos = buf.readBlockPos();
        BlockPos terminalPos = buf.readBlockPos();
        return new OpenCannonInterfacePacket(cannonPos, terminalPos);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(cannonBlockPos);
        buf.writeBlockPos(terminalBlockPos);
    }

    public static void handle(OpenCannonInterfacePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player instanceof ServerPlayer) {
                BlockEntity blockEntity = player.level().getBlockEntity(packet.cannonBlockPos);

                if (blockEntity instanceof ICannonInterfaceHost host) {
                    host.getLogic().setTerminalPos(packet.terminalBlockPos);
                    var locator = MenuLocators.forBlockEntity(blockEntity);
                    host.openMenu(player, locator);
                    return;
                }

                if (blockEntity instanceof IPartHost host) {
                    for (var direction : Direction.values()) {
                        var part = host.getPart(direction);
                        if (part instanceof CannonInterfacePart cannonPart) {
                            cannonPart.getLogic().setTerminalPos(packet.terminalBlockPos);
                            var locator = MenuLocators.forPart(cannonPart);
                            cannonPart.openMenu(player, locator);
                            return;
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
