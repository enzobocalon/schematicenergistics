package com.schematicenergistics.mixin;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.stacks.AEItemKey;
import com.schematicenergistics.blockentity.CannonInterfaceEntity;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity;
import com.simibubi.create.content.schematics.cannon.SchematicannonInventory;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.schematicenergistics.logic.CannonInterfaceLogic;
import com.schematicenergistics.util.ISchematicAccessor;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.schematicenergistics.part.CannonInterfacePart;

import java.lang.reflect.Field;
import java.util.Collections;

@Mixin({ SchematicannonBlockEntity.class })
public abstract class SchematicCannonMixin implements ISchematicAccessor {
    @Shadow
    public SchematicannonInventory inventory;

    // @Shadow
    // public List<ItemRequirement> checklist; // Replaced with reflection

    @Shadow
    public ItemStack missingItem;

    @Shadow
    public SchematicannonBlockEntity.State state;

    @Shadow
    public String statusMsg;

    @Unique
    private CannonInterfaceLogic schematicenergistics$cannonInterface;

    @Unique
    private static Field schematicenergistics$checklistField;

    @Unique
    private static boolean schematicenergistics$checklistFieldInitialized = false;

    @Override
    public Object schematicenergistics$getChecklist() {
        if (!schematicenergistics$checklistFieldInitialized) {
            try {
                // Try to find the field by name "checklist"
                // You might want to add other possible names if "checklist" is not correct
                Class<?> clazz = SchematicannonBlockEntity.class;
                Field f = null;
                try {
                    f = clazz.getDeclaredField("checklist");
                } catch (NoSuchFieldException e) {
                    // Try other names if needed, or iterate fields to find List<ItemRequirement>
                    // For now, let's assume it might be obfuscated or named differently?
                    // But usually in 1.21 NeoForge it is "checklist".
                    // If it fails, we log it.
                }

                if (f != null) {
                    f.setAccessible(true);
                    schematicenergistics$checklistField = f;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            schematicenergistics$checklistFieldInitialized = true;
        }

        if (schematicenergistics$checklistField != null) {
            try {
                return schematicenergistics$checklistField.get(this);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Inject(method = { "initializePrinter" }, at = { @At("HEAD") })
    protected void initializePrinter(CallbackInfo ci) {
        if (this.schematicenergistics$cannonInterface != null) {
            this.schematicenergistics$cannonInterface.setLinkedCannon((SchematicannonBlockEntity) (Object) this);
        }
    }

    @Inject(method = { "findInventories" }, at = { @At("TAIL") })
    public void findInventories(CallbackInfo ci) {
        Level level = ((SchematicannonBlockEntity) (Object) this).getLevel();
        BlockPos pos = ((SchematicannonBlockEntity) (Object) this).getBlockPos();

        CannonInterfaceLogic logicalHost = null;
        if (level == null)
            return;

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            BlockEntity be = level.getBlockEntity(neighborPos);
            if (be instanceof CannonInterfaceEntity candidateInterface) {
                var logic = candidateInterface.getLogic();
                if (logic == null)
                    continue;
                logicalHost = logic;
                break;
            } else if (be instanceof IPartHost partHost) {
                IPart part = partHost.getPart(dir.getOpposite());
                if (part instanceof CannonInterfacePart cannonPart) {
                    logicalHost = cannonPart.getLogic();
                }
            }
        }

        if (logicalHost != null) {
            this.schematicenergistics$cannonInterface = logicalHost;
        } else {
            this.schematicenergistics$cannonInterface = null;
        }
    }

    @Inject(method = { "grabItemsFromAttachedInventories" }, at = { @At("TAIL") }, cancellable = true)
    protected void grabItemsFromAttachedInventories(ItemRequirement.StackRequirement required, boolean simulate,
            CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            if (this.schematicenergistics$cannonInterface != null) {
                AEItemKey key = AEItemKey.of(required.stack);
                this.schematicenergistics$cannonInterface.setItem(key);
                long neededCount = required.stack.getCount();
                boolean result = this.schematicenergistics$cannonInterface.request(key, neededCount, simulate);
                cir.setReturnValue(result);
            }
        }
    }

    @Inject(method = { "tickPrinter" }, at = { @At("HEAD") })
    protected void tickPrinter(CallbackInfo ci) {
        if (this.schematicenergistics$cannonInterface == null)
            return;
        var blueprint = inventory.getStackInSlot(0);
        this.schematicenergistics$cannonInterface.setStatusMsg(statusMsg);
        this.schematicenergistics$cannonInterface.setState(state.toString());
        if (!blueprint.isEmpty()) {
            this.schematicenergistics$cannonInterface.setSchematicName(blueprint.get(AllDataComponents.SCHEMATIC_FILE));
        } else {
            this.schematicenergistics$cannonInterface.setSchematicName(null);
            this.schematicenergistics$cannonInterface.setItem(null);
        }

        if (missingItem != null) {
            var key = AEItemKey.of(missingItem);
            if (key != this.schematicenergistics$cannonInterface.getItem()) {
                this.schematicenergistics$cannonInterface.setItem(key);
            }
        }

        int maxStackSize = this.inventory.getStackInSlot(4).getMaxStackSize();
        int currentAmountOnSlot = this.inventory.getStackInSlot(4).getCount();
        if (currentAmountOnSlot >= maxStackSize)
            return;
        int amountToRefill = maxStackSize - currentAmountOnSlot;
        if (amountToRefill <= 0)
            return;

        int insertedItems = this.schematicenergistics$cannonInterface.refill(amountToRefill);
        if (insertedItems <= 0)
            return;

        ItemStack gunpowderStack = new ItemStack(Items.GUNPOWDER, insertedItems);
        this.inventory.insertItem(4, gunpowderStack, false);
    }
}
