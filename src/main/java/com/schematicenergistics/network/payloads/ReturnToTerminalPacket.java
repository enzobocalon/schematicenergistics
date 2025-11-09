package com.schematicenergistics.network.payloads;

import appeng.api.parts.IPartHost;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import com.schematicenergistics.core.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import com.schematicenergistics.part.CannonInterfaceTerminal;

import java.util.function.Supplier;

public class ReturnToTerminalPacket {

    private final BlockPos terminalPos;

    public ReturnToTerminalPacket(BlockPos terminalPos) {
        this.terminalPos = terminalPos;
    }

    public static ReturnToTerminalPacket decode(FriendlyByteBuf buf) {
        BlockPos terminalPos = buf.readBlockPos();
        return new ReturnToTerminalPacket(terminalPos);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(terminalPos);
    }

    public static void handle(ReturnToTerminalPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                BlockEntity blockEntity = player.level().getBlockEntity(packet.terminalPos);
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
        ctx.get().setPacketHandled(true);
    }
}
