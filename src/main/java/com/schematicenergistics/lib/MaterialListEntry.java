package com.schematicenergistics.lib;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record MaterialListEntry(CompoundTag item, long available, long required, int gathered, boolean craftable) {
    public static final StreamCodec<ByteBuf, MaterialListEntry> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, MaterialListEntry::item,
            ByteBufCodecs.VAR_LONG, MaterialListEntry::available,
            ByteBufCodecs.VAR_LONG, MaterialListEntry::required,
            ByteBufCodecs.VAR_INT, MaterialListEntry::gathered,
            ByteBufCodecs.BOOL, MaterialListEntry::craftable,
            MaterialListEntry::new);
}
