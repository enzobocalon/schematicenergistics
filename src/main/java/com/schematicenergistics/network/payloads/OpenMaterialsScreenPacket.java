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

// Client -> Server: opens MaterialsMenu for the cannon interface at the given position
public record OpenMaterialsScreenPacket(BlockPos cannonBlockPos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenMaterialsScreenPacket> TYPE =
            new CustomPacketPayload.Type<>(SchematicEnergistics.makeId("open_materials_screen"));

    public static final StreamCodec<ByteBuf, OpenMaterialsScreenPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, OpenMaterialsScreenPacket::cannonBlockPos,
            OpenMaterialsScreenPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenMaterialsScreenPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                BlockEntity blockEntity = player.level().getBlockEntity(packet.cannonBlockPos());

                if (blockEntity instanceof ICannonInterfaceHost host) {
                    var locator = MenuLocators.forBlockEntity(blockEntity);
                    host.openMaterialsMenu(player, locator);
                    return;
                }

                if (blockEntity instanceof IPartHost host) {
                    for (var direction : Direction.values()) {
                        var part = host.getPart(direction);
                        if (part instanceof CannonInterfacePart cannonPart) {
                            var locator = MenuLocators.forPart(cannonPart);
                            cannonPart.openMaterialsMenu(player, locator);
                            return;
                        }
                    }
                }
            }
        });
    }
}
