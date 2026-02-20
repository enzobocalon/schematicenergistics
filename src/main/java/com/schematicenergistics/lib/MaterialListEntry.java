package com.schematicenergistics.lib;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public record MaterialListEntry(CompoundTag item, long available, long required, int gathered, boolean craftable) {

    public static MaterialListEntry fromNetwork(FriendlyByteBuf buf) {
        CompoundTag item = buf.readNbt();
        long available = buf.readVarLong();
        long required = buf.readVarLong();
        int gathered = buf.readVarInt();
        boolean craftable = buf.readBoolean();
        return new MaterialListEntry(item, available, required, gathered, craftable);
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeNbt(item);
        buf.writeVarLong(available);
        buf.writeVarLong(required);
        buf.writeVarInt(gathered);
        buf.writeBoolean(craftable);
    }
}