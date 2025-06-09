package core;

import appeng.api.AECapabilities;
import appeng.block.AEBaseBlock;
import appeng.blockentity.AEBaseBlockEntity;
import block.CannonInterface;
import blockentity.CannonInterfaceEntity;
import blockitem.CannonInterfaceBlockItem;
import com.schematicenergistics.SchematicEnergistics;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class Registration {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SchematicEnergistics.MOD_ID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(SchematicEnergistics.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES;
    public static final DeferredBlock<CannonInterface> CANNON_INTERFACE;
    public static final DeferredItem<BlockItem> CANNON_INTERFACE_ITEM;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CannonInterfaceEntity>> CANNON_INTERFACE_ENTITY;


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
        eventBus.addListener(Registration::registerCapabilities);
    }

    static {
        BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SchematicEnergistics.MOD_ID);
        CANNON_INTERFACE = BLOCKS.registerBlock("cannon_interface", CannonInterface::new, AEBaseBlock.metalProps());
        CANNON_INTERFACE_ITEM = ITEMS.register("cannon_interface", () -> new CannonInterfaceBlockItem(CANNON_INTERFACE.get(), new Item.Properties()));
        CANNON_INTERFACE_ENTITY = BLOCK_ENTITIES.register("cannon_interface", () -> {
            BlockEntityType<CannonInterfaceEntity> type = Builder.of(CannonInterfaceEntity::new, CANNON_INTERFACE.get()).build(null);
            AEBaseBlockEntity.registerBlockEntityItem(type, CANNON_INTERFACE.asItem());
            CANNON_INTERFACE.get().setBlockEntity(CannonInterfaceEntity.class, type, null, null);
            return type;
        });
    }
}
