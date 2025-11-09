package com.schematicenergistics;

import appeng.api.util.AEColor;
import appeng.client.render.StaticItemColor;
import appeng.init.client.InitScreens;
import com.schematicenergistics.core.Registration;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import com.schematicenergistics.screen.CannonInterfaceScreen;
import com.schematicenergistics.screen.CannonInterfaceTerminalScreen;
import com.schematicenergistics.tab.CreativeTab;

@Mod(SchematicEnergistics.MOD_ID)
public class SchematicEnergistics {
    public static final String MOD_ID = "schematicenergistics";

    public SchematicEnergistics() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        Registration.init(modEventBus);
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        CreativeTab.register(modEventBus);
        modEventBus.addListener(this::addCreative);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    public static ResourceLocation makeId(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {}

    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {}

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @Mod.EventBusSubscriber(
            modid = SchematicEnergistics.MOD_ID,
            bus = Mod.EventBusSubscriber.Bus.MOD,
            value = {Dist.CLIENT}
    )
    public static class ClientModEvents {
        public ClientModEvents() {
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                InitScreens.register(
                        Registration.CANNON_INTERFACE_MENU.get(),
                        CannonInterfaceScreen::new,
                        "/screens/cannon_interface.json"
                );

                InitScreens.register(
                        Registration.CANNON_INTERFACE_TERMINAL_MENU.get(),
                        CannonInterfaceTerminalScreen::new,
                        "/screens/cannon_interface_terminal.json"
                );
            });
        }

        // Credits to ExtendedAE
        private static ItemColor makeOpaque(ItemColor itemColor) {
            return (stack, tintIndex) -> {
                int color = itemColor.getColor(stack, tintIndex);
                return (color & 0x00FFFFFF) | 0xFF000000;
            };
        }

        @SubscribeEvent
        public static void registerColorHandler(RegisterColorHandlersEvent.Item event) {
            event.register(
                    makeOpaque(new StaticItemColor(AEColor.TRANSPARENT)),
                    Registration.CANNON_INTERFACE_TERMINAL.get()
            );
        }
    }
}
