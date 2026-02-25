package com.schematicenergistics.network.payloads;

import appeng.api.parts.IPartHost;
import appeng.menu.locator.MenuLocators;
import com.schematicenergistics.logic.ICannonInterfaceHost;
import com.schematicenergistics.part.CannonInterfacePart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

// Client -> Server: opens MaterialsMenu for the cannon interface at the given position
public record OpenMaterialsScreenPacket(BlockPos cannonBlockPos) {

    public static OpenMaterialsScreenPacket fromNetwork(FriendlyByteBuf buf) {
        return new OpenMaterialsScreenPacket(buf.readBlockPos());
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeBlockPos(cannonBlockPos);
    }

    public static void handle(OpenMaterialsScreenPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            BlockEntity blockEntity = player.level().getBlockEntity(packet.cannonBlockPos());

            if (blockEntity instanceof ICannonInterfaceHost host) {
                var locator = MenuLocators.forBlockEntity(blockEntity);
                host.openMaterialsMenu(player, locator);
                return;
            }

            if (blockEntity instanceof IPartHost host) {
                for (Direction direction : Direction.values()) {
                    var part = host.getPart(direction);
                    if (part instanceof CannonInterfacePart cannonPart) {
                        var locator = MenuLocators.forPart(cannonPart);
                        cannonPart.openMaterialsMenu(player, locator);
                        return;
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}