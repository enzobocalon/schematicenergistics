package com.schematicenergistics.core;

import appeng.api.util.AEColor;
import appeng.client.render.StaticItemColor;
import appeng.init.client.InitScreens;
import com.schematicenergistics.SchematicEnergistics;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.util.FastColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import com.schematicenergistics.screen.CannonInterfaceScreen;
import com.schematicenergistics.screen.CannonInterfaceTerminalScreen;

@EventBusSubscriber(modid = SchematicEnergistics.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        InitScreens.register(
                event,
                Registration.CANNON_INTERFACE_MENU.get(),
                CannonInterfaceScreen::new,
                "/screens/cannon_interface.json"
        );

        InitScreens.register(
                event,
                Registration.CANNON_INTERFACE_TERMINAL_MENU.get(),
                CannonInterfaceTerminalScreen::new,
                "/screens/cannon_interface_terminal.json"
        );
    }

    @SubscribeEvent
    public static void registerColorHandlers(RegisterColorHandlersEvent.Item event) {
        event.register(makeOpaque(new StaticItemColor(AEColor.TRANSPARENT)), Registration.CANNON_TERMINAL.get());
    }

    private static ItemColor makeOpaque(ItemColor itemColor) {
        return (stack, tintIndex) -> FastColor.ARGB32.opaque(itemColor.getColor(stack, tintIndex));
    }
}
