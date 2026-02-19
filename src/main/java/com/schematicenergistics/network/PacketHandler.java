package com.schematicenergistics.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import com.schematicenergistics.network.payloads.*;

public class PacketHandler {

        private static final String PROTOCOL = "1";

        private PacketHandler() {
        }

        public static void init(IEventBus bus) {
                bus.addListener(PacketHandler::handlePacketRegistration);
        }

        private static void handlePacketRegistration(RegisterPayloadHandlersEvent event) {
                var registrar = event.registrar(PROTOCOL);

                registrar.playToClient(
                                CannonInterfaceSyncPacket.TYPE,
                                CannonInterfaceSyncPacket.STREAM_CODEC,
                                handler(CannonInterfaceSyncPacket::handle));

                registrar.playToServer(
                                CannonInterfaceConfigPacket.TYPE,
                                CannonInterfaceConfigPacket.STREAM_CODEC,
                                handler(CannonInterfaceConfigPacket::handle));

                registrar.playToClient(
                                CannonInterfaceConfigClientPacket.TYPE,
                                CannonInterfaceConfigClientPacket.STREAM_CODEC,
                                handler(CannonInterfaceConfigClientPacket::handle));

                registrar.playToServer(
                                CannonStatePacket.TYPE,
                                CannonStatePacket.STREAM_CODEC,
                                handler(CannonStatePacket::handle));

                registrar.playToClient(
                                TerminalListClientPacket.TYPE,
                                TerminalListClientPacket.STREAM_CODEC,
                                handler(TerminalListClientPacket::handle));

                registrar.playToServer(
                                OpenCannonInterfacePacket.TYPE,
                                OpenCannonInterfacePacket.STREAM_CODEC,
                                handler(OpenCannonInterfacePacket::handle));

                registrar.playToServer(
                                ReturnToTerminalPacket.TYPE,
                                ReturnToTerminalPacket.STREAM_CODEC,
                                handler(ReturnToTerminalPacket::handle));

                registrar.playToClient(
                                MaterialListClientPacket.TYPE,
                                MaterialListClientPacket.STREAM_CODEC,
                                handler(MaterialListClientPacket::handle));

                registrar.playToServer(
                                MaterialListSubscribePacket.TYPE,
                                MaterialListSubscribePacket.STREAM_CODEC,
                                handler(MaterialListSubscribePacket::handle));

                registrar.playToServer(
                                MaterialListPageRequestPacket.TYPE,
                                MaterialListPageRequestPacket.STREAM_CODEC,
                                handler(MaterialListPageRequestPacket::handle));
        }

        private static <T extends CustomPacketPayload> IPayloadHandler<T> handler(IPayloadHandler<T> handler) {
                return (payload, context) -> context.enqueueWork(() -> handler.handle(payload, context));
        };
}
