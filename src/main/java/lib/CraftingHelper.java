package lib;

import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.AEItemKey;
import blockentity.CannonInterfaceEntity;
import java.util.Objects;
import java.util.concurrent.Future;
import net.minecraft.world.level.Level;

public class CraftingHelper {
    private CraftingRequest pendingCraft;
    private final CannonInterfaceEntity entity;

    public CraftingHelper(CannonInterfaceEntity entity) {
        this.entity = entity;
    }

    public void startCraft(AEItemKey key, long amount) {
        ICraftingService service = (Objects.requireNonNull(this.entity.getGridNode())).getGrid().getCraftingService();
        if (key != null && amount > 0) {
            if (service.isCraftable(key)) {
                Level level = this.entity.getLevel();
                CannonInterfaceEntity entity = this.entity;
                Future<ICraftingPlan> future = service.beginCraftingCalculation(level, entity::getActionSource, key, amount, CalculationStrategy.REPORT_MISSING_ITEMS);
                if (future != null) {
                    this.pendingCraft = new CraftingRequest(key, amount, future);
                }
            }
        }
    }

    public void clearPendingCraft() {
        this.pendingCraft = null;
    }

    public CraftingRequest getPendingCraft() {
        return this.pendingCraft;
    }
}
