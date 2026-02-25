package com.schematicenergistics.mixin;

import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import com.google.common.collect.Sets;
import com.schematicenergistics.lib.ColorHelper;
import com.schematicenergistics.logic.CannonInterfaceLogic;
import com.schematicenergistics.logic.IMaterialChecklistAccessor;
import com.simibubi.create.content.schematics.cannon.MaterialChecklist;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(MaterialChecklist.class)
public class MaterialChecklistMixin implements IMaterialChecklistAccessor {
    @Unique
    private CannonInterfaceLogic schematicenergistics$logic;

    @Override
    public CannonInterfaceLogic schematicenergistics$getLogic() {
        return this.schematicenergistics$logic;
    }

    @Override
    public void schematicenergistics$setLogic(CannonInterfaceLogic logic) {
        this.schematicenergistics$logic = logic;
    }

    @Unique
    private Map<Item, Integer> schematicenergistics$gatheredBackup;

    @Inject(
            method = "entry",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private void injectEntry(
            ItemStack item,
            int amount,
            boolean unfinished,
            boolean forBook,
            CallbackInfoReturnable<MutableComponent> cir
    ) {
        if (schematicenergistics$logic == null) return;

        AEItemKey key = AEItemKey.of(item);

        ICraftingService craftingService = schematicenergistics$logic.getCraftingService();
        boolean craftable = craftingService != null && craftingService.isCraftable(key);

        if (!unfinished || !craftable) return;

        MutableComponent original = cir.getReturnValue();
        MutableComponent extra = Component.empty();

        if (!forBook) {
            extra.append("\n");
        }

        extra.append(Component.literal(" \u2692 ").withStyle(ColorHelper.CRAFTABLE_FMT));
        extra.append(Component.translatable("gui.schematicenergistics.label.craftable")
                .withStyle(ColorHelper.CRAFTABLE_FMT));

        extra.append(forBook ? "\n\n" : "\n");

        original.append(extra);

        cir.setReturnValue(original);
    }


    @Inject(method = "createWrittenBook", at = @At("HEAD"), remap = false)
    private void beforeCreateWrittenBook(CallbackInfoReturnable<ItemStack> cir) {
        if (schematicenergistics$logic == null) return;
        schematicenergistics$enrichGathered();
    }

    @Inject(method = "createWrittenBook", at = @At("RETURN"), remap = false)
    private void afterCreateWrittenBook(CallbackInfoReturnable<ItemStack> cir) {
        schematicenergistics$restoreGathered();
    }

    @Inject(method = "createWrittenClipboard", at = @At("HEAD"), remap = false)
    private void beforeCreateWrittenClipboard(CallbackInfoReturnable<ItemStack> cir) {
        if (schematicenergistics$logic == null) return;
        schematicenergistics$enrichGathered();
    }

    @Inject(method = "createWrittenClipboard", at = @At("RETURN"), remap = false)
    private void afterCreateWrittenClipboard(CallbackInfoReturnable<ItemStack> cir) {
        schematicenergistics$restoreGathered();
    }

    @Unique
    private void schematicenergistics$enrichGathered() {
        MEStorage storage = schematicenergistics$logic.getStorage();
        if (storage == null) return;

        MaterialChecklist self = (MaterialChecklist) (Object) this;

        schematicenergistics$gatheredBackup = new HashMap<>(self.gathered);

        for (Item item : Sets.union(self.required.keySet(), self.damageRequired.keySet())) {
            AEItemKey key = AEItemKey.of(new ItemStack(item));
            long inStorage = storage.getAvailableStacks().get(key);
            if (inStorage <= 0) continue;

            int alreadyGathered = self.gathered.getOrDefault(item, 0);
            int required = self.getRequiredAmount(item);
            self.gathered.put(item, (int) Math.min(required, alreadyGathered + inStorage));
        }
    }

    @Unique
    private void schematicenergistics$restoreGathered() {
        if (schematicenergistics$gatheredBackup == null) return;

        MaterialChecklist self = (MaterialChecklist) (Object) this;
        self.gathered.clear();
        self.gathered.putAll(schematicenergistics$gatheredBackup);
        schematicenergistics$gatheredBackup = null;
    }
}
