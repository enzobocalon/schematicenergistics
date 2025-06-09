package lib;

import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingLink;
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
    private ICraftingLink link;

    public CraftingHelper(CannonInterfaceEntity entity) {
        this.entity = entity;
    }

    public void startCraft(AEItemKey key, long amount) {
        if (key == null || amount <= 0 || this.entity.getLevel() == null) {
            return;
        }

        var node = this.entity.getGridNode();
        if (node == null) return;

        var grid = node.getGrid();
        if (grid == null) return;

        var service = grid.getCraftingService();
        if (!service.isCraftable(key)) {
            return;
        }

        var future = service.beginCraftingCalculation(
                this.entity.getLevel(),
                entity::getActionSource,
                key,
                amount,
                CalculationStrategy.REPORT_MISSING_ITEMS
        );

        if (future != null) {
            this.pendingCraft = new CraftingRequest(key, amount, future);
        }
    }


    public void clearPendingCraft() {
        this.pendingCraft = null;
    }

    public void setLink(ICraftingLink link) {
        this.link = link;
    }

    public ICraftingLink getLink() {
        return this.link;
    }

    public CraftingRequest getPendingCraft() {
        return this.pendingCraft;
    }
}
