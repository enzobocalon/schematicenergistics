package lib;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class SEUtils {

    public enum InterfaceType {
        BLOCK, PART
    }

    public static final StreamCodec<ByteBuf, InterfaceType> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(
                    id -> InterfaceType.values()[id],
                    InterfaceType::ordinal
            );

    public static Component formatCannonStatus(String statusMsg) {
        return Component.literal("Cannon ").copy().append(Component.translatable("create.schematicannon.status." + statusMsg));
    }

}