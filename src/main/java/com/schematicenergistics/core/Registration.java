package com.schematicenergistics.core;

import appeng.api.AECapabilities;
import appeng.api.implementations.parts.IMonitorPart;
import appeng.api.parts.PartModels;
import appeng.block.AEBaseBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import appeng.menu.implementations.MenuTypeBuilder;
import com.schematicenergistics.block.CannonInterface;
import com.schematicenergistics.blockentity.CannonInterfaceEntity;
import com.schematicenergistics.blockitem.CannonInterfaceBlockItem;
import com.schematicenergistics.SchematicEnergistics;
import com.schematicenergistics.logic.ICannonInterfaceHost;
import com.schematicenergistics.menu.CannonInterfaceMenu;
import com.schematicenergistics.menu.CannonInterfaceTerminalMenu;
import com.schematicenergistics.menu.MaterialsMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.schematicenergistics.part.CannonInterfacePart;
import com.schematicenergistics.part.CannonInterfacePartItem;
import com.schematicenergistics.part.CannonInterfaceTerminal;
import com.schematicenergistics.part.CannonInterfaceTerminalPartItem;

import java.util.function.Supplier;

public final class Registration {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SchematicEnergistics.MOD_ID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(SchematicEnergistics.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES;
    public static final DeferredBlock<CannonInterface> CANNON_INTERFACE;
    public static final DeferredItem<BlockItem> CANNON_INTERFACE_ITEM;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CannonInterfaceEntity>> CANNON_INTERFACE_ENTITY;
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, SchematicEnergistics.MOD_ID);
    public static DeferredItem<PartItem<CannonInterfacePart>> CANNON_INTERFACE_PART_ITEM;
    public static DeferredItem<PartItem<CannonInterfaceTerminal>> CANNON_TERMINAL;
    public static final Supplier<MenuType<CannonInterfaceMenu>> CANNON_INTERFACE_MENU;
    public static final Supplier<MenuType<CannonInterfaceTerminalMenu>> CANNON_INTERFACE_TERMINAL_MENU;
    public static final Supplier<MenuType<MaterialsMenu>> MATERIALS_MENU;

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                CANNON_INTERFACE_ENTITY.get(),
                (cInterface, ctx) -> cInterface);

    }

    public static void init(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        MENUS.register(eventBus);

        eventBus.addListener(Registration::registerCapabilities);
    }

    static {
        BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SchematicEnergistics.MOD_ID);
        CANNON_INTERFACE = BLOCKS.registerBlock("cannon_interface", CannonInterface::new, AEBaseBlock.metalProps());
        CANNON_INTERFACE_ITEM = ITEMS.register("cannon_interface", () -> new CannonInterfaceBlockItem(CANNON_INTERFACE.get(), new Item.Properties()));
        CANNON_INTERFACE_PART_ITEM = ITEMS.registerItem(
                "cannon_interface_part", properties -> {
                    PartModels.registerModels(PartModelsHelper.createModels(CannonInterfacePart.class));

                    return new CannonInterfacePartItem(
                            properties,
                            CannonInterfacePart.class,
                            CannonInterfacePart::new
                    );
                }
        );

        CANNON_TERMINAL = ITEMS.registerItem(
                "cannon_interface_terminal", properties -> {
                    PartModels.registerModels(PartModelsHelper.createModels(CannonInterfaceTerminal.class));

                    return new CannonInterfaceTerminalPartItem(properties, CannonInterfaceTerminal.class, (partItem) -> new CannonInterfaceTerminal(partItem, true));
                }
        );

        CANNON_INTERFACE_ENTITY = BLOCK_ENTITIES.register("cannon_interface", () -> {
            BlockEntityType<CannonInterfaceEntity> type = Builder.of(CannonInterfaceEntity::new, CANNON_INTERFACE.get()).build(null);
            AEBaseBlockEntity.registerBlockEntityItem(type, CANNON_INTERFACE.asItem());
            CANNON_INTERFACE.get().setBlockEntity(CannonInterfaceEntity.class, type, null, null);
            return type;
        });

        CANNON_INTERFACE_MENU =
                MENUS.register("cannon_interface", () ->
                        MenuTypeBuilder.create(CannonInterfaceMenu::new, ICannonInterfaceHost.class)
                                .build(ResourceLocation.fromNamespaceAndPath(SchematicEnergistics.MOD_ID, "cannon_interface")));

        CANNON_INTERFACE_TERMINAL_MENU =
                MENUS.register("cannon_interface_terminal", () ->
                        MenuTypeBuilder.create(CannonInterfaceTerminalMenu::new, IMonitorPart.class)
                                .build(ResourceLocation.fromNamespaceAndPath(SchematicEnergistics.MOD_ID, "cannon_interface_terminal")));

        MATERIALS_MENU =
                MENUS.register("materials", () ->
                        MenuTypeBuilder.create(MaterialsMenu::new, ICannonInterfaceHost.class)
                                .build(ResourceLocation.fromNamespaceAndPath(SchematicEnergistics.MOD_ID, "materials")));
    }
}
