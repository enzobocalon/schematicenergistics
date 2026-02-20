package com.schematicenergistics.lib;

import net.minecraft.ChatFormatting;
import java.util.Objects;

public class ColorHelper {

    public static final ChatFormatting COMPLETE_FMT = ChatFormatting.DARK_GREEN;
    public static final ChatFormatting PARTIAL_FMT = ChatFormatting.BLUE;
    public static final ChatFormatting CRAFTABLE_FMT = ChatFormatting.GOLD;
    public static final ChatFormatting MISSING_FMT = ChatFormatting.RED;

    public static final int COMPLETE = toARGB(COMPLETE_FMT);
    public static final int PARTIAL = toARGB(PARTIAL_FMT);
    public static final int CRAFTABLE = toARGB(CRAFTABLE_FMT);
    public static final int MISSING = toARGB(MISSING_FMT);

    private static int toARGB(ChatFormatting fmt) {
        return 0xFF000000 | Objects.requireNonNull(fmt.getColor(), fmt + " has no color");
    }
}