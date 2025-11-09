package com.schematicenergistics.tab;

import com.schematicenergistics.SchematicEnergistics;
import com.schematicenergistics.core.Registration;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class CreativeTab {
    public static final DeferredRegister<CreativeModeTab> SE_CREATIVE_MODE_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SchematicEnergistics.MOD_ID);

    public static final Supplier<CreativeModeTab> CREATIVE_TAB = SE_CREATIVE_MODE_TAB.register("schematic_energistics_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(Registration.CANNON_INTERFACE.get()))
                    .title(Component.translatable("creativetab.schematicenergistics.title"))
                    .displayItems((displayParameters, output) -> {
                        output.accept(Registration.CANNON_INTERFACE.get());
                        output.accept(Registration.CANNON_INTERFACE_PART_ITEM.get());
                        output.accept(Registration.CANNON_TERMINAL.get());
                    })
                    .build()
    );

    public static void register(IEventBus eventBus) {
        SE_CREATIVE_MODE_TAB.register(eventBus);
    }
}

