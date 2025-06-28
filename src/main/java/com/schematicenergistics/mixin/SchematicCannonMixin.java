package com.schematicenergistics.mixin;

import appeng.api.stacks.AEItemKey;
import blockentity.CannonInterfaceEntity;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity;
import com.simibubi.create.content.schematics.cannon.SchematicannonInventory;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
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

@Mixin({SchematicannonBlockEntity.class})
public abstract class SchematicCannonMixin {
    @Shadow
    public SchematicannonInventory inventory;
    private CannonInterfaceEntity cannonInterface;

    @Inject(
            method = {"findInventories"},
            at = {@At("TAIL")},
            remap = false
    )
    public void findInventories(CallbackInfo ci) {
        Level level = ((SchematicannonBlockEntity)(Object)this).getLevel();
        BlockPos pos = ((SchematicannonBlockEntity)(Object)this).getBlockPos();

        CannonInterfaceEntity foundInterface = null;
        if (level == null) return;

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            BlockEntity be = level.getBlockEntity(neighborPos);
            if (be instanceof CannonInterfaceEntity candidateInterface) {
                foundInterface = candidateInterface;
                break;
            }
        }

        if (foundInterface != null) {
            this.cannonInterface = foundInterface;
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
