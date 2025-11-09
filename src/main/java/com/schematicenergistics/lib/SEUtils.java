package com.schematicenergistics.lib;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public class SEUtils {

    public enum InterfaceType {
        BLOCK, PART;

        public void toBuffer(FriendlyByteBuf buf) {
            buf.writeVarInt(this.ordinal());
        }

        public static InterfaceType fromBuffer(FriendlyByteBuf buf) {
            int ordinal = buf.readVarInt();
            return InterfaceType.values()[ordinal];
        }
    }

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