package com.schematicenergistics.network.payloads;

import com.schematicenergistics.SchematicEnergistics;
import com.schematicenergistics.lib.MaterialListEntry;
import com.schematicenergistics.screen.MaterialsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record MaterialListClientPacket(int page, int totalPages, List<MaterialListEntry> entries) {

    public static MaterialListClientPacket fromNetwork(FriendlyByteBuf buf) {
        int page = buf.readVarInt();
        int totalPages = buf.readVarInt();
        int size = buf.readVarInt();
        List<MaterialListEntry> entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            entries.add(MaterialListEntry.fromNetwork(buf));
        }
        return new MaterialListClientPacket(page, totalPages, entries);
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeVarInt(page);
        buf.writeVarInt(totalPages);
        buf.writeVarInt(entries.size());
        for (MaterialListEntry entry : entries) {
            entry.toNetwork(buf);
        }
    }

    public static void handle(MaterialListClientPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen instanceof MaterialsScreen screen) {
                screen.receiveMaterialsData(packet.page(), packet.totalPages(), packet.entries());
            }
        });
        context.setPacketHandled(true);
    }
}