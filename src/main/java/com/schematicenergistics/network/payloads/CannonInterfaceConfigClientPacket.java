package com.schematicenergistics.network.payloads;

import com.schematicenergistics.lib.CannonInterfaceClientState;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import com.schematicenergistics.screen.CannonInterfaceScreen;

import java.util.function.Supplier;

public class CannonInterfaceConfigClientPacket {

    private final boolean gunpowderState;
    private final boolean craftingState;
    private final boolean gunpowderCraftingState;
    private final boolean bulkCraftState;

    public CannonInterfaceConfigClientPacket(boolean gunpowderState, boolean craftingState, boolean gunpowderCraftingState, boolean bulkCraftState) {
        this.gunpowderState = gunpowderState;
        this.craftingState = craftingState;
        this.gunpowderCraftingState = gunpowderCraftingState;
        this.bulkCraftState = bulkCraftState;
    }

    // Decoder
    public static CannonInterfaceConfigClientPacket fromBytes(FriendlyByteBuf buf) {
        return new CannonInterfaceConfigClientPacket(buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
    }

    // Encoder
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(gunpowderState);
        buf.writeBoolean(craftingState);
        buf.writeBoolean(gunpowderCraftingState);
        buf.writeBoolean(bulkCraftState);
    }

    // Handle
    public static void handle(CannonInterfaceConfigClientPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                CannonInterfaceClientState.setState(
                        packet.gunpowderState,
                        packet.craftingState,
                        packet.gunpowderCraftingState,
                        packet.bulkCraftState
                );

                Minecraft mc = Minecraft.getInstance();
                if (mc.screen instanceof CannonInterfaceScreen screen) {
                    screen.updateStates(
                            packet.gunpowderState,
                            packet.craftingState,
                            packet.gunpowderCraftingState,
                            packet.bulkCraftState
                    );
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
