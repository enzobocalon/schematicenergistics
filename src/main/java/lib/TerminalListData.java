package lib;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record TerminalListData(BlockPos cannonPos, String schematicName, SEUtils.InterfaceType type, String dimension, String status, String state) {

    public static final StreamCodec<ByteBuf, TerminalListData> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, TerminalListData::cannonPos,
            ByteBufCodecs.STRING_UTF8, TerminalListData::schematicName,
            SEUtils.STREAM_CODEC, TerminalListData::type,
            ByteBufCodecs.STRING_UTF8, TerminalListData::dimension,
            ByteBufCodecs.STRING_UTF8, TerminalListData::status,
            ByteBufCodecs.STRING_UTF8, TerminalListData::state,
            TerminalListData::new
    );
}