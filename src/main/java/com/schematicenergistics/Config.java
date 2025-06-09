package com.schematicenergistics;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(
        modid = SchematicEnergistics.MOD_ID,
        bus = Bus.MOD
)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    static final ModConfigSpec SPEC;

    public Config() {
    }

    @SubscribeEvent
    static void onLoad(ModConfigEvent event) {
    }

    static {
        SPEC = BUILDER.build();
    }
}
