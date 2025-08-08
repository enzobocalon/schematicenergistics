package lib;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record TerminalListData(BlockPos pos, String schematicName, SEUtils.InterfaceType type) {
    public static final StreamCodec<ByteBuf, TerminalListData> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, TerminalListData::pos,
            ByteBufCodecs.STRING_UTF8, TerminalListData::schematicName,
            SEUtils.STREAM_CODEC, TerminalListData::type,
            TerminalListData::new
    );
}