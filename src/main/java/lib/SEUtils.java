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
        Component translatableComponent = Component.translatable("create.schematicannon.status." + statusMsg);

        String text = translatableComponent.getString();
        if (text.endsWith(":")) {
            translatableComponent = Component.literal(text.substring(0, text.length() - 1));
        }

        return Component.literal("Cannon ").copy().append(translatableComponent);
    }

    public static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

}