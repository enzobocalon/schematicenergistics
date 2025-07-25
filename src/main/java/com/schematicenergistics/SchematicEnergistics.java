package com.schematicenergistics;

import appeng.api.ids.AECreativeTabIds;
import appeng.init.client.InitScreens;
import core.Registration;
import net.minecraft.world.item.CreativeModeTabs;
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
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import screen.CannonInterfaceScreen;
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
        modContainer.registerConfig(Type.COMMON, Config.SPEC);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(this::registerScreens);
        }
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
