package com.schematicenergistics.network.payloads;

import com.schematicenergistics.SchematicEnergistics;
import io.netty.buffer.ByteBuf;
import com.schematicenergistics.lib.CannonInterfaceClientState;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.schematicenergistics.screen.CannonInterfaceScreen;

// Server -> Client
public record CannonInterfaceConfigClientPacket(boolean gunpowderState, boolean craftingState, boolean gunpowderCraftingState) implements CustomPacketPayload {
    public static final Type<CannonInterfaceConfigClientPacket> TYPE = new Type<>(SchematicEnergistics.makeId("cannon_state"));

    public static final StreamCodec<ByteBuf, CannonInterfaceConfigClientPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, CannonInterfaceConfigClientPacket::gunpowderState,
            ByteBufCodecs.BOOL, CannonInterfaceConfigClientPacket::craftingState,
            ByteBufCodecs.BOOL, CannonInterfaceConfigClientPacket::gunpowderCraftingState,
            CannonInterfaceConfigClientPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CannonInterfaceConfigClientPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var minecraft = Minecraft.getInstance();
            CannonInterfaceClientState.setState(packet.gunpowderState(), packet.craftingState(), packet.gunpowderCraftingState());
            if (minecraft.screen instanceof CannonInterfaceScreen screen) {
                screen.updateStates(packet.gunpowderState(), packet.craftingState(), packet.gunpowderCraftingState());
            }
        });
    }

}