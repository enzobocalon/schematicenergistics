package com.schematicenergistics.mixin;


import com.simibubi.create.content.schematics.cannon.MaterialChecklist;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({MaterialChecklist.class})
public class MaterialChecklistMixin {

    @Inject(method = "entry", at = @At("RETURN"), cancellable = true)
    private void entry(ItemStack item, int amount, boolean unfinished, boolean forBook, CallbackInfoReturnable<MutableComponent> cir) {

        MutableComponent originalValue = cir.getReturnValue();
    }

}
