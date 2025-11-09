package com.schematicenergistics.lib;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class TerminalListData {
    private final BlockPos cannonPos;
    private final String schematicName;
    private final SEUtils.InterfaceType type;
    private final String dimension;
    private final String status;
    private final String state;

    public TerminalListData(BlockPos cannonPos, String schematicName, SEUtils.InterfaceType type, String dimension, String status, String state) {
        this.cannonPos = cannonPos;
        this.schematicName = schematicName;
        this.type = type;
        this.dimension = dimension;
        this.status = status;
        this.state = state;
    }

    public void toBuffer(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.cannonPos);
        buf.writeUtf(this.schematicName);
        this.type.toBuffer(buf);
        buf.writeUtf(this.dimension);
        buf.writeUtf(this.status);
        buf.writeUtf(this.state);
    }

    public static TerminalListData fromBuffer(FriendlyByteBuf buf) {
        BlockPos cannonPos = buf.readBlockPos();
        String schematicName = buf.readUtf();
        SEUtils.InterfaceType type = SEUtils.InterfaceType.fromBuffer(buf);
        String dimension = buf.readUtf();
        String status = buf.readUtf();
        String state = buf.readUtf();

        return new TerminalListData(cannonPos, schematicName, type, dimension, status, state);
    }

    // Getters
    public BlockPos cannonPos() {
        return cannonPos;
    }

    public String schematicName() {
        return schematicName;
    }

    public SEUtils.InterfaceType type() {
        return type;
    }

    public String dimension() {
        return dimension;
    }

    public String status() {
        return status;
    }

    public String state() {
        return state;
    }
}