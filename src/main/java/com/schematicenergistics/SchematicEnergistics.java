package com.schematicenergistics;

import appeng.api.util.AEColor;
import appeng.client.render.StaticItemColor;
import appeng.init.client.InitScreens;
import core.Registration;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import network.PacketHandler;
import screen.CannonInterfaceScreen;
import screen.CannonInterfaceTerminalScreen;
import tab.CreativeTab;

@Mod(SchematicEnergistics.MOD_ID)
public class SchematicEnergistics {
    public static final String MOD_ID = "schematicenergistics";

    public SchematicEnergistics(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
        Registration.init(modEventBus);
        CreativeTab.register(modEventBus);
        PacketHandler.init(modEventBus);
        modContainer.registerConfig(Type.COMMON, Config.SPEC);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(this::registerScreens);
            modEventBus.addListener(this::registerColorHandler);
        }
    }

    public static ResourceLocation makeId(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    private void commonSetup(FMLCommonSetupEvent event) {}

    private void addCreative(BuildCreativeModeTabContentsEvent event) {}

    private void registerScreens(RegisterMenuScreensEvent event) {
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

    // Credits to ExtendedAE
    private static ItemColor makeOpaque(ItemColor itemColor) {
        return (stack, tintIndex) -> FastColor.ARGB32.opaque(itemColor.getColor(stack, tintIndex));
    }

    public void registerColorHandler(RegisterColorHandlersEvent.Item event) {
        event.register(makeOpaque(new StaticItemColor(AEColor.TRANSPARENT)), Registration.CANNON_TERMINAL);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {}

    @EventBusSubscriber(
            modid = SchematicEnergistics.MOD_ID,
            bus = Bus.MOD,
            value = {Dist.CLIENT}
    )
    public static class ClientModEvents {
        public ClientModEvents() {
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }
    }
}
