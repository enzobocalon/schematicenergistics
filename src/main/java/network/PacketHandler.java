package network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import network.payloads.CannonInterfaceConfigClientPacket;
import network.payloads.CannonInterfaceConfigPacket;
import network.payloads.CannonInterfaceSyncPacket;

public class PacketHandler {

    private static final String PROTOCOL = "1";

    private PacketHandler() {}

    public static void init (IEventBus bus) {
        bus.addListener(PacketHandler::handlePacketRegistration);
    }

    private static void handlePacketRegistration(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(PROTOCOL);

        registrar.playToClient(
                CannonInterfaceSyncPacket.TYPE,
                CannonInterfaceSyncPacket.STREAM_CODEC,
                handler(CannonInterfaceSyncPacket::handle)
        );

        registrar.playToServer(
                CannonInterfaceConfigPacket.TYPE,
                CannonInterfaceConfigPacket.STREAM_CODEC,
                handler(CannonInterfaceConfigPacket::handle)
        );

        registrar.playToClient(
                CannonInterfaceConfigClientPacket.TYPE,
                CannonInterfaceConfigClientPacket.STREAM_CODEC,
                handler(CannonInterfaceConfigClientPacket::handle)
        );
    }

    private static <T extends CustomPacketPayload> IPayloadHandler<T> handler(IPayloadHandler<T> handler) {
        return (payload, context) -> context.enqueueWork(() -> handler.handle(payload, context));
    };
}
