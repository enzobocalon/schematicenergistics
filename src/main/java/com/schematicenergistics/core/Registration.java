package com.schematicenergistics.core;

import appeng.api.implementations.parts.IMonitorPart;
import appeng.api.parts.PartModels;
import appeng.block.AEBaseBlock;
import appeng.blockentity.AEBaseBlockEntity;
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
import com.schematicenergistics.part.CannonInterfaceTerminal;
import com.schematicenergistics.part.CannonInterfaceTerminalPartItem;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import com.schematicenergistics.network.PacketHandler;
import com.schematicenergistics.part.CannonInterfacePart;
import com.schematicenergistics.part.CannonInterfacePartItem;

public class Registration {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SchematicEnergistics.MOD_ID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, SchematicEnergistics.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, SchematicEnergistics.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, SchematicEnergistics.MOD_ID);


    public static final RegistryObject<CannonInterface> CANNON_INTERFACE = BLOCKS.register(
            "cannon_interface",
            () -> new CannonInterface(AEBaseBlock.metalProps())
    );

    public static final RegistryObject<BlockItem> CANNON_INTERFACE_ITEM = ITEMS.register("cannon_interface",
            () -> new CannonInterfaceBlockItem(CANNON_INTERFACE.get(), new Item.Properties()));

    public static final RegistryObject<BlockEntityType<CannonInterfaceEntity>> CANNON_INTERFACE_ENTITY =
            BLOCK_ENTITIES.register("cannon_interface",
                    () -> {
                        BlockEntityType<CannonInterfaceEntity> type = BlockEntityType.Builder.of(CannonInterfaceEntity::new, CANNON_INTERFACE.get()).build(null);
                        CANNON_INTERFACE.get().setBlockEntity(CannonInterfaceEntity.class, type, null, null);
                        AEBaseBlockEntity.registerBlockEntityItem(type, CANNON_INTERFACE_ITEM.get());
                        return type;
                    });



    public static RegistryObject<Item> CANNON_INTERFACE_PART_ITEM = ITEMS.register(
            "cannon_interface_part",
            () -> {
                PartModels.registerModels(PartModelsHelper.createModels(CannonInterfacePart.class));

                return new CannonInterfacePartItem(
                        new Item.Properties(),
                        CannonInterfacePart.class,
                        CannonInterfacePart::new
                );
            }
    );

    public static RegistryObject<Item> CANNON_INTERFACE_TERMINAL = ITEMS.register(
            "cannon_interface_terminal",
            () -> {
                PartModels.registerModels(PartModelsHelper.createModels(CannonInterfaceTerminal.class));

                return new CannonInterfaceTerminalPartItem(new Item.Properties(), CannonInterfaceTerminal.class, (partItem) -> new CannonInterfaceTerminal(partItem, true));
            }
    );

    public static final RegistryObject<MenuType<CannonInterfaceMenu>> CANNON_INTERFACE_MENU =
            MENUS.register("cannon_interface", () -> MenuTypeBuilder.create(CannonInterfaceMenu::new, ICannonInterfaceHost.class)
                    .build("cannon_interface"));

    public static final RegistryObject<MenuType<CannonInterfaceTerminalMenu>> CANNON_INTERFACE_TERMINAL_MENU =
            MENUS.register("cannon_interface_terminal", () -> MenuTypeBuilder.create(CannonInterfaceTerminalMenu::new, IMonitorPart.class)
                    .build("cannon_interface_terminal"));

    public static final RegistryObject<MenuType<MaterialsMenu>> MATERIALS_MENU =
            MENUS.register("materials_menu", () -> MenuTypeBuilder.create(MaterialsMenu::new, ICannonInterfaceHost.class)
                    .build("materials_menu"));

    public static void init(IEventBus eventBus) {
        ITEMS.register(eventBus);
        BLOCKS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        MENUS.register(eventBus);
        PacketHandler.init();
    }

}
