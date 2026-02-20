package com.schematicenergistics.network;

import com.schematicenergistics.SchematicEnergistics;
import com.schematicenergistics.network.payloads.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    private static final ResourceLocation CHANNEL_NAME = SchematicEnergistics.makeId("main");
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            CHANNEL_NAME,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int nextId() {
        return packetId++;
    }

    public static void init() {
        CHANNEL.registerMessage(
                nextId(), CannonInterfaceSyncPacket.class,
                CannonInterfaceSyncPacket::toBytes,
                CannonInterfaceSyncPacket::fromBytes,
                CannonInterfaceSyncPacket::handle
        );

        CHANNEL.registerMessage(
                nextId(), CannonInterfaceConfigPacket.class,
                CannonInterfaceConfigPacket::toBytes,
                CannonInterfaceConfigPacket::fromBytes,
                CannonInterfaceConfigPacket::handle
        );

        CHANNEL.registerMessage(
                nextId(), CannonInterfaceConfigClientPacket.class,
                CannonInterfaceConfigClientPacket::toBytes,
                CannonInterfaceConfigClientPacket::fromBytes,
                CannonInterfaceConfigClientPacket::handle
        );

        CHANNEL.registerMessage(
                nextId(), CannonStatePacket.class,
                CannonStatePacket::toBytes,
                CannonStatePacket::fromBytes,
                CannonStatePacket::handle
        );

        CHANNEL.registerMessage(
                nextId(), TerminalListClientPacket.class,
                TerminalListClientPacket::encode,
                TerminalListClientPacket::decode,
                TerminalListClientPacket::handle
        );

        CHANNEL.registerMessage(
                nextId(), OpenCannonInterfacePacket.class,
                OpenCannonInterfacePacket::encode,
                OpenCannonInterfacePacket::decode,
                OpenCannonInterfacePacket::handle
        );

        CHANNEL.registerMessage(
                nextId(), ReturnToTerminalPacket.class,
                ReturnToTerminalPacket::encode,
                ReturnToTerminalPacket::decode,
                ReturnToTerminalPacket::handle
        );

        CHANNEL.registerMessage(
                nextId(), MaterialListClientPacket.class,
                MaterialListClientPacket::toNetwork,
                MaterialListClientPacket::fromNetwork,
                MaterialListClientPacket::handle
        );

        CHANNEL.registerMessage(
                nextId(), MaterialListSubscribePacket.class,
                MaterialListSubscribePacket::toNetwork,
                MaterialListSubscribePacket::fromNetwork,
                MaterialListSubscribePacket::handle
        );

        CHANNEL.registerMessage(
                nextId(), OpenMaterialsScreenPacket.class,
                OpenMaterialsScreenPacket::toNetwork,
                OpenMaterialsScreenPacket::fromNetwork,
                OpenMaterialsScreenPacket::handle
        );

        CHANNEL.registerMessage(
                nextId(), ReturnToCannonInterfacePacket.class,
                ReturnToCannonInterfacePacket::toNetwork,
                ReturnToCannonInterfacePacket::fromNetwork,
                ReturnToCannonInterfacePacket::handle
        );
    }

    public static <MSG> void sendToServer(MSG message) {
        CHANNEL.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void sendToClient(MSG message, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}