package com.schematicenergistics.mixin;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.stacks.AEItemKey;
import com.schematicenergistics.blockentity.CannonInterfaceEntity;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity;
import com.simibubi.create.content.schematics.cannon.SchematicannonInventory;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.schematicenergistics.logic.CannonInterfaceLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.schematicenergistics.part.CannonInterfacePart;

@Mixin({SchematicannonBlockEntity.class})
public abstract class SchematicCannonMixin {
    @Shadow
    public SchematicannonInventory inventory;

    private CannonInterfaceLogic cannonInterface;

    @Shadow
    public ItemStack missingItem;

    @Shadow
    public SchematicannonBlockEntity.State state;

    @Shadow
    public String statusMsg;

    @Inject(
            method = {"initializePrinter"},
            at = {@At("HEAD")},
            cancellable = true,
            remap = false
    )
    protected void initializePrinter(CallbackInfo ci) {
        if (this.cannonInterface != null) {
            this.cannonInterface.setLinkedCannon((SchematicannonBlockEntity)(Object) this);
        }
    }

    @Inject(
            method = {"findInventories"},
            at = {@At("TAIL")},
            remap = false
    )
    public void findInventories(CallbackInfo ci) {
        Level level = ((SchematicannonBlockEntity)(Object)this).getLevel();
        BlockPos pos = ((SchematicannonBlockEntity)(Object)this).getBlockPos();

        CannonInterfaceLogic logicalHost = null;
        if (level == null) return;

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            BlockEntity be = level.getBlockEntity(neighborPos);
            if (be instanceof CannonInterfaceEntity candidateInterface) {
                var logic = candidateInterface.getLogic();
                if (logic == null) continue;
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
            this.cannonInterface = logicalHost;
        } else {
            this.cannonInterface = null;
        }
    }

    @Inject(
            method = {"grabItemsFromAttachedInventories"},
            at = {@At("TAIL")},
            cancellable = true,
            remap = false
    )
    protected void grabItemsFromAttachedInventories(ItemRequirement.StackRequirement required, boolean simulate, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            if (this.cannonInterface != null) {
                AEItemKey key = AEItemKey.of(required.stack);
                long neededCount = required.stack.getCount();
                boolean result = this.cannonInterface.request(key, neededCount, simulate);
                cir.setReturnValue(result);
            }
        }
    }

    @Inject(
            method = {"tickPrinter"},
            at = {@At("HEAD")},
            cancellable = true,
            remap = false
    )
    protected void tickPrinter(CallbackInfo ci) {
        if (this.cannonInterface == null) return;

        var blueprint = inventory.getStackInSlot(0);
        this.cannonInterface.setStatusMsg(statusMsg);
        this.cannonInterface.setState(state.toString());
        if (!blueprint.isEmpty() && blueprint.getTag() != null) {
            this.cannonInterface.setSchematicName(blueprint.getTag().getString("File"));
        } else {
            this.cannonInterface.setSchematicName(null);
            this.cannonInterface.setItem(null);
        }

        if (missingItem != null) {
            var key = AEItemKey.of(missingItem);
            if (key != this.cannonInterface.getItem()) {
                this.cannonInterface.setItem(key);
            }
        }

        int maxStackSize = this.inventory.getStackInSlot(4).getMaxStackSize();
        int currentAmountOnSlot = this.inventory.getStackInSlot(4).getCount();
        if (currentAmountOnSlot >= maxStackSize) return;
        int amountToRefill = maxStackSize - currentAmountOnSlot;
        if (amountToRefill <= 0) return;

        int insertedItems = this.cannonInterface.refill(amountToRefill);
        if (insertedItems <= 0)  return;

        ItemStack gunpowderStack = new ItemStack(Items.GUNPOWDER, insertedItems);
        this.inventory.insertItem(4, gunpowderStack, false);
    }
}
